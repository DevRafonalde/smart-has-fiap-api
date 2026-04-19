# Auth Module — SmartHAS Digital 360

Documentação técnica do módulo de autenticação, autorização e controle de permissões da API SmartHAS.

---

## Sumário

- [Visão Geral](#visão-geral)
- [Estrutura de Pacotes](#estrutura-de-pacotes)
- [Modelo de Dados](#modelo-de-dados)
- [Como Funciona](#como-funciona)
    - [Autenticação (Login)](#autenticação-login)
    - [Access Token](#access-token)
    - [Refresh Token](#refresh-token)
    - [Logout](#logout)
    - [Controle de Permissões](#controle-de-permissões)
- [Endpoints](#endpoints)
- [Como Utilizar](#como-utilizar)
    - [Fluxo completo de autenticação](#fluxo-completo-de-autenticação)
    - [Renovando o token](#renovando-o-token)
    - [Protegendo um endpoint](#protegendo-um-endpoint)
- [Configuração](#configuração)
- [Erros e Exceções](#erros-e-exceções)

---

## Visão Geral

O módulo de auth do SmartHAS é responsável por três funções principais:

1. **Autenticação** — verificar identidade do usuário via login com `nomeUser` e senha
2. **Autorização por token** — emitir e validar tokens JWT de curta duração para proteger as rotas
3. **Controle de permissões** — verificar se o usuário autenticado possui a permissão necessária para executar uma ação específica

O modelo de segurança é baseado em **JWT + Refresh Token com rotação**, sem uso do Spring Security como filtro de cadeia — a validação é feita por um interceptor customizado (`PermissaoMiddleware`) que age antes de cada requisição protegida.

---

## Estrutura de Pacotes

```
br.com.fiap.on.smarthas/
│
├── auth/
│   ├── api/
│   │   ├── controllers/
│   │   │   ├── UsuarioController.java       # Login, refresh, logout, CRUD de usuários
│   │   │   ├── PerfilController.java        # CRUD de perfis e vínculo com permissões
│   │   │   └── PermissaoController.java     # CRUD de permissões
│   │   └── middlewares/
│   │       └── PermissaoMiddleware.java     # Interceptor: valida JWT e verifica permissão
│   │
│   └── internal/
│       ├── models/
│       │   ├── entities/
│       │   │   ├── orm/
│       │   │   │   ├── UsuarioORM.java
│       │   │   │   ├── PerfilORM.java
│       │   │   │   ├── PermissaoORM.java
│       │   │   │   ├── PerfilPermissaoORM.java
│       │   │   │   ├── UsuarioPerfilORM.java
│       │   │   │   └── RefreshTokenORM.java
│       │   │   └── dto/
│       │   │       ├── LoginRequestDTO.java
│       │   │       ├── LoginResponseDTO.java
│       │   │       ├── RefreshTokenRequestDTO.java
│       │   │       ├── RefreshTokenResponseDTO.java
│       │   │       ├── UsuarioDTO.java
│       │   │       ├── UsuarioPerfilDTO.java
│       │   │       ├── PerfilDTO.java
│       │   │       ├── PerfilPermissaoDTO.java
│       │   │       └── PermissaoDTO.java
│       │   └── repositories/
│       │       ├── UsuarioRepository.java
│       │       ├── UsuarioPerfilRepository.java
│       │       ├── PerfilRepository.java
│       │       ├── PerfilPermissaoRepository.java
│       │       ├── PermissaoRepository.java
│       │       └── RefreshTokenRepository.java
│       └── services/
│           ├── UsuarioService.java
│           ├── PerfilService.java
│           ├── PermissaoService.java
│           ├── JwtService.java
│           └── RefreshTokenService.java
│
├── config/
│   ├── InterceptorConfig.java               # Registra o PermissaoMiddleware
│   └── PasswordUtil.java                    # Hash e verificação de senha (BCrypt)
│
└── shared/
    └── annotations/
        └── Permissao.java                   # @Permissao(rota = "...") para proteger métodos
```

---

## Modelo de Dados

O módulo usa cinco tabelas no PostgreSQL, todas com prefixo `t_`:

```
t_usuarios
    id, cpf, nomeCompleto, nomeAmigavel, nomeUser,
    senhaUser (hash BCrypt), senhaAtualizada, ativo

t_perfis
    id, nome, descricao, ativo

t_permissoes
    id, nome, descricao, ativo

t_perfilPermissao          ← N:N entre perfil e permissão
    id, idPerfil, idPermissao, dataHora

t_usuarioPerfil            ← N:N entre usuário e perfil
    id, idUsuario, idPerfil, dataHora

t_refreshTokens
    id, idUsuario, token (UUID), expiracao,
    revogado, criadoEm
```

**Relacionamentos:**

```
Usuário ──< UsuarioPerfil >── Perfil ──< PerfilPermissao >── Permissao
                                              ↑
                                    Define o que o perfil pode fazer

Usuário ──< RefreshToken
                ↑
         Um por usuário (ativo)
```

> O modelo suporta múltiplos perfis por usuário, mas na prática cada usuário tem um único perfil ativo. A estrutura N:N foi mantida para flexibilidade futura.

---

## Como Funciona

### Autenticação (Login)

O cliente envia `nomeUser` e `senha`. O `UsuarioService.autenticar()` busca o usuário pelo `nomeUser` e verifica a senha usando `PasswordUtil.verificarSenha()`, que compara a senha recebida com o hash BCrypt armazenado no banco.

Se as credenciais são válidas, o sistema emite dois tokens: um **access token** de curta duração e um **refresh token** de longa duração.

```
Cliente                          API
  │                               │
  │  POST /usuarios/login         │
  │  { nomeUser, senha }          │
  │ ─────────────────────────────>│
  │                               │  1. Busca usuário por nomeUser
  │                               │  2. Verifica senha (BCrypt)
  │                               │  3. Gera access token (JWT, 15min)
  │                               │  4. Gera refresh token (UUID, 7 dias)
  │                               │  5. Salva refresh token no banco
  │  200 OK                       │
  │  { accessToken,               │
  │    refreshToken,              │
  │    expiracaoAccessTokenSegundos } │
  │ <─────────────────────────────│
```

### Access Token

O access token é um **JWT assinado com HMAC-SHA256**. Ele contém:

| Campo | Valor |
|---|---|
| `sub` | `nomeUser` do usuário |
| `idUsuario` | ID numérico do usuário |
| `iat` | Timestamp de emissão |
| `exp` | Timestamp de expiração (15 minutos por padrão) |

O token é **stateless** — a API não precisa consultar o banco para validar um access token, apenas verifica a assinatura com a chave secreta.

### Refresh Token

O refresh token é um **UUID aleatório** armazenado no banco de dados. Ele tem duração de 7 dias por padrão e é usado exclusivamente para obter um novo par de tokens sem precisar fazer login novamente.

O módulo implementa **rotação de refresh token**: a cada chamada em `/refresh`, o token usado é revogado e um novo é emitido. Isso significa que cada refresh token só pode ser usado **uma única vez**.

```
Cliente                          API
  │                               │
  │  POST /usuarios/refresh       │
  │  { refreshToken: "uuid..." }  │
  │ ─────────────────────────────>│
  │                               │  1. Busca token no banco
  │                               │  2. Verifica se não está revogado
  │                               │  3. Verifica se não expirou
  │                               │  4. Revoga o token usado
  │                               │  5. Gera novo access token
  │                               │  6. Gera novo refresh token
  │  200 OK                       │
  │  { accessToken,               │
  │    refreshToken (novo),       │
  │    expiracaoAccessTokenSegundos } │
  │ <─────────────────────────────│
```

> **Por que rotacionar?** Se um refresh token for interceptado por um agente malicioso, a rotação garante que após o usuário legítimo usá-lo uma vez, o token do atacante se torna inválido automaticamente.

### Logout

O logout revoga **todos** os refresh tokens do usuário. O access token ainda é tecnicamente válido até sua expiração natural (15 min), mas sem refresh token o usuário não consegue renová-lo.

```
Cliente                          API
  │                               │
  │  POST /usuarios/logout        │
  │  Authorization: Bearer <jwt>  │
  │ ─────────────────────────────>│
  │                               │  1. Extrai idUsuario do JWT
  │                               │  2. Revoga todos os refresh tokens
  │  204 No Content               │
  │ <─────────────────────────────│
```

### Controle de Permissões

Toda rota protegida usa a annotation `@Permissao(rota = "nomedarota")`. O `PermissaoMiddleware` intercepta cada requisição e executa a seguinte sequência:

```
Requisição chega
       │
       ▼
O método tem @Permissao?
  ├── Não → deixa passar (rota pública)
  └── Sim ↓
       │
       ▼
Header Authorization presente e começa com "Bearer "?
  ├── Não → 401 Unauthorized
  └── Sim ↓
       │
       ▼
JWT é válido (assinatura + expiração)?
  ├── Não → 401 Unauthorized
  └── Sim → extrai idUsuario
       │
       ▼
Carrega perfis do usuário
       │
       ▼
Algum perfil tem a permissão com nome == rota exigida?
  ├── Não → 403 Forbidden
  └── Sim → deixa a requisição prosseguir
```

---

## Endpoints

### Públicos (não exigem token)

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/usuarios/login` | Autenticação — retorna access + refresh token |
| `POST` | `/usuarios/refresh` | Renova os tokens com o refresh token |

### Autenticados (exigem `Authorization: Bearer <token>`)

| Método | Rota | Permissão exigida | Descrição |
|---|---|---|---|
| `POST` | `/usuarios/logout` | — (só valida o JWT) | Revoga todos os refresh tokens |
| `POST` | `/usuarios/cadastrar` | `cadastrarusuario` | Cadastra novo usuário |
| `GET` | `/usuarios/listar` | `listartodosusuarios` | Lista usuários paginado |
| `GET` | `/usuarios/listar-especifico/{id}` | `listarusuarioespecifico` | Detalhe de um usuário |
| `GET` | `/usuarios/clonar/{id}` | `clonarusuario` | Clona dados de um usuário |
| `PUT` | `/usuarios/editar` | `editarusuario` | Atualiza dados do usuário |
| `DELETE` | `/usuarios/deletar/{id}` | `deletarusuario` | Remove um usuário |
| `POST` | `/perfis/cadastrar` | `cadastrarperfil` | Cadastra novo perfil |
| `GET` | `/perfis/listar` | `listartodosperfis` | Lista perfis paginado |
| `GET` | `/perfis/listar-especifico/{id}` | `listarperfilespecifico` | Detalhe de um perfil |
| `GET` | `/perfis/listar-usuarios-vinculados/{id}` | `listarusuariosvinculados` | Usuários do perfil |
| `GET` | `/perfis/clonar/{id}` | `clonarperfil` | Clona um perfil |
| `PUT` | `/perfis/editar` | `editarperfil` | Atualiza um perfil |
| `DELETE` | `/perfis/deletar/{id}` | `deletarperfil` | Remove um perfil |
| `POST` | `/permissoes/cadastrar` | `cadastrarpermissao` | Cadastra nova permissão |
| `GET` | `/permissoes/listar` | `listartodaspermissoes` | Lista permissões paginado |
| `GET` | `/permissoes/listar-especifico/{id}` | `listarpermissaoespecifica` | Detalhe de uma permissão |
| `PUT` | `/permissoes/editar` | `editarpermissao` | Atualiza uma permissão |
| `DELETE` | `/permissoes/deletar/{id}` | `deletarpermissao` | Remove uma permissão |

---

## Como Utilizar

### Fluxo completo de autenticação

**1. Fazer login**

```http
POST /api/usuarios/login
Content-Type: application/json

{
  "nomeUser": "joao.silva",
  "senha": "Senha@123"
}
```

Resposta:

```json
{
  "id": 42,
  "nomeAmigavel": "João Silva",
  "nomeUser": "joao.silva",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "expiracaoAccessTokenSegundos": 900
}
```

**2. Usar o access token nas requisições protegidas**

```http
GET /api/usuarios/listar
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**3. Armazenar os dois tokens no cliente**

O app mobile deve armazenar `accessToken` e `refreshToken` de forma segura (ex: `SecureStore` no React Native, nunca em `AsyncStorage` sem criptografia).

---

### Renovando o token

Quando o access token expirar (após `expiracaoAccessTokenSegundos` segundos), o cliente deve chamar `/refresh` antes de fazer novas requisições:

```http
POST /api/usuarios/refresh
Content-Type: application/json

{
  "refreshToken": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
}
```

Resposta:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "expiracaoAccessTokenSegundos": 900
}
```

> ⚠️ O `refreshToken` anterior é **invalidado imediatamente**. Salve o novo token retornado — o anterior não funciona mais.

Se o refresh token também estiver expirado, a API retorna `401` e o usuário precisa fazer login novamente.

---

### Protegendo um endpoint

Para proteger qualquer método de controller, adicione `@Permissao` com o nome da rota:

```java
@GetMapping("/meu-endpoint")
@Permissao(rota = "nomeDaMinhaPermissao")
public ResponseEntity<MeuDTO> meuEndpoint() {
    // só chega aqui se o usuário tiver a permissão cadastrada
}
```

Para que o usuário tenha acesso, o seguinte precisa estar configurado no banco:

1. A `Permissao` com `nome = "nomeDaMinhaPermissao"` deve existir em `t_permissoes`
2. Essa permissão deve estar vinculada a um `Perfil` em `t_perfilPermissao`
3. O usuário deve estar vinculado a esse perfil em `t_usuarioPerfil`

Para rotas **completamente públicas** (sem token), basta não adicionar a annotation `@Permissao`.

---

## Configuração

Todas as variáveis sensíveis ficam no `.env` e são lidas pelo `application.yml`:

```yaml
# application.yml
security:
  jwt:
    secret: ${JWT_SECRET}
    expiration: ${JWT_EXPIRATION:900}         # access token — segundos (padrão: 15 min)
    refresh-expiration: ${JWT_REFRESH:604800} # refresh token — segundos (padrão: 7 dias)
```

```bash
# .env
JWT_SECRET=sua-chave-secreta-com-minimo-256-bits
JWT_EXPIRATION=900
JWT_REFRESH=604800
```

> A chave JWT deve ter no mínimo 256 bits (32 caracteres). Para gerar uma chave segura:
> ```bash
> openssl rand -hex 64
> ```

---

## Erros e Exceções

| Situação | HTTP | Mensagem |
|---|---|---|
| Credenciais inválidas no login | `401` | `"Usuário ou senha inválidos"` |
| Token ausente na requisição | `401` | `"Não foi enviado o Token de Autorização"` |
| Token JWT malformado ou expirado | `401` | `"Token inválido, Exceção: ..."` |
| Refresh token não encontrado | `401` | `"Refresh token não encontrado"` |
| Refresh token expirado | `401` | `"Refresh token expirado. Faça login novamente"` |
| Refresh token já revogado | `401` | `"Refresh token inválido"` |
| Usuário sem permissão para a rota | `403` | `"Usuário não possui a permissão necessária para esta função"` |
| Recurso não encontrado | `404` | `"[Entidade] não encontrado no banco de dados"` |
| Atributo já em uso (ex: nomeUser) | `409` | `"Nome de Usuário já está sendo utilizado"` |