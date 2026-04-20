# API Reference — Auth Module

Referência completa de todos os endpoints do módulo de autenticação da API SmartHAS.

> **Base URL:** `http://localhost:8080/api`
> **Formato:** todas as requisições e respostas usam `Content-Type: application/json`
> **Autenticação:** endpoints marcados com 🔒 exigem `Authorization: Bearer <accessToken>`

---

## Sumário

- [Usuários](#usuários)
    - [POST /auth/usuarios/login](#post-autusuarioslogin)
    - [POST /auth/usuarios/registrar](#post-autusuariosregistrar)
    - [POST /auth/usuarios/refresh](#post-autusuariosrefresh)
    - [POST /auth/usuarios/logout](#post-autusuarioslogout-🔒)
    - [POST /auth/usuarios/cadastrar](#post-autusuarioscadastrar-🔒)
    - [GET /auth/usuarios/listar](#get-autusuarioslistar-🔒)
    - [GET /auth/usuarios/listar-especifico/{id}](#get-autusuarioslistar-especificoid-🔒)
    - [GET /auth/usuarios/clonar/{id}](#get-autusuariosclonarid-🔒)
    - [PUT /auth/usuarios/editar](#put-autusuarioseditar-🔒)
    - [DELETE /auth/usuarios/deletar/{id}](#delete-autusuariosdeletarid-🔒)
- [Perfis](#perfis)
    - [POST /auth/perfis/cadastrar](#post-authperfiscadastrar-🔒)
    - [GET /auth/perfis/listar](#get-authperfislistar-🔒)
    - [GET /auth/perfis/listar-especifico/{id}](#get-authperfislistar-especificoid-🔒)
    - [GET /auth/perfis/listar-usuarios-vinculados/{id}](#get-authperfislistar-usuarios-vinculadosid-🔒)
    - [GET /auth/perfis/clonar/{id}](#get-authperfisclonarid-🔒)
    - [PUT /auth/perfis/editar](#put-authperfiseditar-🔒)
    - [DELETE /auth/perfis/deletar/{id}](#delete-authperfisdeletarid-🔒)
- [Permissões](#permissões)
    - [POST /auth/permissoes/cadastrar](#post-authpermissoescadastrar-🔒)
    - [GET /auth/permissoes/listar](#get-authpermissoeslistar-🔒)
    - [GET /auth/permissoes/listar-especifico/{id}](#get-authpermissoeslistar-especificoid-🔒)
    - [PUT /auth/permissoes/editar](#put-authpermissoeseditar-🔒)
    - [DELETE /auth/permissoes/deletar/{id}](#delete-authpermissoesdeletarid-🔒)
- [Schemas](#schemas)

---

## Usuários

---

### POST /auth/usuarios/login

Autentica o usuário e retorna um par de tokens (access + refresh).

**Autenticação:** pública

**Request body**

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `nomeUser` | `string` | ✅ | Nome de usuário cadastrado |
| `senha` | `string` | ✅ | Senha do usuário |

```json
{
  "nomeUser": "joao.silva",
  "senha": "Senha@123"
}
```

**Responses**

`200 OK`

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | `integer` | ID do usuário |
| `nomeAmigavel` | `string` | Nome de exibição |
| `nomeUser` | `string` | Nome de usuário |
| `accessToken` | `string` | JWT de curta duração (15 min por padrão) |
| `refreshToken` | `string` | UUID de longa duração (7 dias por padrão) |
| `expiracaoAccessTokenSegundos` | `long` | Tempo em segundos até o access token expirar |

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

`401 Unauthorized` — credenciais inválidas

---

### POST /auth/usuarios/registrar

Cadastro público do próprio cidadão. Vincula automaticamente o perfil padrão ao usuário criado.

**Autenticação:** pública

**Request body**

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `cpf` | `string` | ✅ | CPF com 11 dígitos, sem máscara |
| `nomeCompleto` | `string` | ✅ | Nome completo do usuário |
| `nomeAmigavel` | `string` | ✅ | Nome de exibição na plataforma |
| `nomeUser` | `string` | ✅ | Nome de usuário para login (único) |
| `senhaUser` | `string` | ✅ | Senha (mínimo 8 caracteres) |

```json
{
  "cpf": "12345678909",
  "nomeCompleto": "João da Silva",
  "nomeAmigavel": "João",
  "nomeUser": "joao.silva",
  "senhaUser": "Senha@123"
}
```

**Responses**

`200 OK` — retorna `UsuarioPerfilDTO` com perfil padrão vinculado

```json
{
  "usuario": {
    "id": 42,
    "cpf": "12345678909",
    "nomeCompleto": "João Da Silva",
    "nomeAmigavel": "João",
    "nomeUser": "joao.silva",
    "senhaUser": null,
    "ativo": true
  },
  "perfisUsuario": [
    {
      "id": 2,
      "nome": "Usuário Padrão",
      "descricao": "Perfil com permissões padrões do sistema",
      "ativo": true
    }
  ]
}
```

`400 Bad Request` — CPF inválido ou campos obrigatórios ausentes
`409 Conflict` — `nomeUser` já está em uso

> `senhaUser` nunca é retornado nas respostas — o campo sempre virá `null`.

---

### POST /auth/usuarios/refresh

Renova o par de tokens usando o refresh token. O token enviado é **revogado imediatamente** e um novo par é gerado (rotação).

**Autenticação:** pública

**Request body**

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `refreshToken` | `string` | ✅ | UUID do refresh token atual |

```json
{
  "refreshToken": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
}
```

**Responses**

`200 OK`

| Campo | Tipo | Descrição |
|---|---|---|
| `accessToken` | `string` | Novo JWT de acesso |
| `refreshToken` | `string` | Novo UUID (o anterior está invalidado) |
| `expiracaoAccessTokenSegundos` | `long` | Tempo em segundos até expirar |

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "expiracaoAccessTokenSegundos": 900
}
```

`401 Unauthorized` — token não encontrado, expirado ou já revogado

---

### POST /auth/usuarios/logout 🔒

Revoga todos os refresh tokens do usuário autenticado. O access token expira naturalmente.

**Autenticação:** requer `Authorization: Bearer <accessToken>`
**Permissão exigida:** nenhuma (apenas token válido)

**Request body:** nenhum

**Responses**

`204 No Content` — logout realizado com sucesso

`401 Unauthorized` — token ausente ou inválido

---

### POST /auth/usuarios/cadastrar 🔒

Cadastra um novo usuário com perfis já definidos. Uso exclusivo por administradores.

**Autenticação:** requer `Authorization: Bearer <accessToken>`
**Permissão exigida:** `cadastrarusuario`

**Request body** — `UsuarioPerfilDTO`

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `usuario` | `UsuarioDTO` | ✅ | Dados do usuário |
| `usuario.cpf` | `string` | ✅ | CPF com 11 dígitos, sem máscara |
| `usuario.nomeCompleto` | `string` | ✅ | Nome completo |
| `usuario.nomeAmigavel` | `string` | ✅ | Nome de exibição |
| `usuario.nomeUser` | `string` | ✅ | Nome de login (único) |
| `usuario.senhaUser` | `string` | ✅ | Senha (mínimo 8 caracteres) |
| `perfisUsuario` | `PerfilDTO[]` | ✅ | Lista de perfis a vincular |
| `perfisUsuario[].id` | `integer` | ✅ | ID do perfil existente |

```json
{
  "usuario": {
    "cpf": "98765432100",
    "nomeCompleto": "Maria Aparecida",
    "nomeAmigavel": "Maria",
    "nomeUser": "maria.aparecida",
    "senhaUser": "Senha@456"
  },
  "perfisUsuario": [
    {
      "id": 1,
      "nome": "Administrador de Sistemas",
      "descricao": "Perfil com acesso total ao sistema",
      "ativo": true
    }
  ]
}
```

**Responses**

`200 OK` — retorna `UsuarioPerfilDTO` com dados persistidos

```json
{
  "usuario": {
    "id": 43,
    "cpf": "98765432100",
    "nomeCompleto": "Maria Aparecida",
    "nomeAmigavel": "Maria",
    "nomeUser": "maria.aparecida",
    "senhaUser": null,
    "ativo": true
  },
  "perfisUsuario": [
    {
      "id": 1,
      "nome": "Administrador de Sistemas",
      "descricao": "Perfil com acesso total ao sistema",
      "ativo": true
    }
  ]
}
```

`400 Bad Request` — CPF inválido ou campos obrigatórios ausentes
`401 Unauthorized` — token ausente ou inválido
`403 Forbidden` — sem permissão `cadastrarusuario`
`409 Conflict` — `nomeUser` já em uso

---

### GET /auth/usuarios/listar 🔒

Lista todos os usuários ativos com paginação.

**Autenticação:** requer `Authorization: Bearer <accessToken>`
**Permissão exigida:** `listartodosusuarios`

**Query parameters** — Spring `Pageable`

| Parâmetro | Tipo | Padrão | Descrição |
|---|---|---|---|
| `page` | `integer` | `0` | Número da página (base 0) |
| `size` | `integer` | `20` | Itens por página |
| `sort` | `string` | — | Campo e direção (ex: `nomeUser,asc`) |

**Exemplo de requisição**

```http
GET /api/auth/usuarios/listar?page=0&size=10&sort=nomeUser,asc
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Responses**

`200 OK` — retorna `UsuarioDTO[]`

```json
[
  {
    "id": 42,
    "cpf": "12345678909",
    "nomeCompleto": "João Da Silva",
    "nomeAmigavel": "João",
    "nomeUser": "joao.silva",
    "senhaUser": null,
    "ativo": true
  }
]
```

`401 Unauthorized` — token ausente ou inválido
`403 Forbidden` — sem permissão `listartodosusuarios`

---

### GET /auth/usuarios/listar-especifico/{id} 🔒

Retorna os dados completos de um usuário, incluindo seus perfis vinculados.

**Autenticação:** requer `Authorization: Bearer <accessToken>`
**Permissão exigida:** `listarusuarioespecifico`

**Path parameters**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | `integer` | ID do usuário |

**Responses**

`200 OK` — retorna `UsuarioPerfilDTO`

```json
{
  "usuario": {
    "id": 42,
    "cpf": "12345678909",
    "nomeCompleto": "João Da Silva",
    "nomeAmigavel": "João",
    "nomeUser": "joao.silva",
    "senhaUser": null,
    "ativo": true
  },
  "perfisUsuario": [
    {
      "id": 2,
      "nome": "Usuário Padrão",
      "descricao": "Perfil com permissões padrões do sistema",
      "ativo": true
    }
  ]
}
```

`401 Unauthorized` — token ausente ou inválido
`403 Forbidden` — sem permissão `listarusuarioespecifico`
`404 Not Found` — usuário não encontrado

---

### GET /auth/usuarios/clonar/{id} 🔒

Retorna os dados de um usuário com perfis vinculados, sem o objeto `usuario` — útil para criar um novo usuário com os mesmos perfis.

**Autenticação:** requer `Authorization: Bearer <accessToken>`
**Permissão exigida:** `clonarusuario`

**Path parameters**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | `integer` | ID do usuário a clonar |

**Responses**

`200 OK` — retorna `UsuarioPerfilDTO` com `usuario: null`

```json
{
  "usuario": null,
  "perfisUsuario": [
    {
      "id": 2,
      "nome": "Usuário Padrão",
      "descricao": "Perfil com permissões padrões do sistema",
      "ativo": true
    }
  ]
}
```

`401 Unauthorized` — token ausente ou inválido
`403 Forbidden` — sem permissão `clonarusuario`
`404 Not Found` — usuário não encontrado

---

### PUT /auth/usuarios/editar 🔒

Atualiza os dados de um usuário existente e redefine seus perfis vinculados. A senha **não** é alterada por este endpoint.

**Autenticação:** requer `Authorization: Bearer <accessToken>`
**Permissão exigida:** `editarusuario`

**Request body** — `UsuarioPerfilDTO`

```json
{
  "usuario": {
    "id": 42,
    "cpf": "12345678909",
    "nomeCompleto": "João Da Silva Atualizado",
    "nomeAmigavel": "João",
    "nomeUser": "joao.silva",
    "senhaUser": null,
    "ativo": true
  },
  "perfisUsuario": [
    {
      "id": 3,
      "nome": "Tutor Voluntário",
      "descricao": "Perfil para tutores da plataforma",
      "ativo": true
    }
  ]
}
```

**Responses**

`200 OK` — retorna `UsuarioPerfilDTO` atualizado

`400 Bad Request` — CPF inválido ou campos obrigatórios ausentes
`401 Unauthorized` — token ausente ou inválido
`403 Forbidden` — sem permissão `editarusuario`
`404 Not Found` — usuário não encontrado
`409 Conflict` — novo `nomeUser` já em uso por outro usuário

---

### DELETE /auth/usuarios/deletar/{id} 🔒

Remove um usuário e todos os seus vínculos de perfil.

**Autenticação:** requer `Authorization: Bearer <accessToken>`
**Permissão exigida:** `deletarusuario`

**Path parameters**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | `integer` | ID do usuário a remover |

**Responses**

`200 OK` — retorna `true`

`401 Unauthorized` — token ausente ou inválido
`403 Forbidden` — sem permissão `deletarusuario`
`404 Not Found` — usuário não encontrado

---

## Perfis

---

### POST /auth/perfis/cadastrar 🔒

Cadastra um novo perfil e vincula as permissões informadas.

**Autenticação:** requer `Authorization: Bearer <accessToken>`
**Permissão exigida:** `cadastrarperfil`

**Request body** — `PerfilPermissaoDTO`

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `perfil` | `PerfilDTO` | ✅ | Dados do perfil |
| `perfil.nome` | `string` | ✅ | Nome do perfil |
| `perfil.descricao` | `string` | ✅ | Descrição do perfil |
| `permissoes` | `PermissaoDTO[]` | ✅ | Lista de permissões a vincular |
| `permissoes[].id` | `integer` | ✅ | ID da permissão existente |

```json
{
  "perfil": {
    "nome": "Tutor Voluntário",
    "descricao": "Perfil para tutores da plataforma",
    "ativo": true
  },
  "permissoes": [
    {
      "id": 3,
      "nome": "listartodosusuarios",
      "descricao": "Permissão gerada automaticamente pelo seeder",
      "ativo": true
    }
  ]
}
```

**Responses**

`200 OK` — retorna `PerfilPermissaoDTO` com dados persistidos

```json
{
  "perfil": {
    "id": 5,
    "nome": "Tutor Voluntário",
    "descricao": "Perfil para tutores da plataforma",
    "ativo": true
  },
  "permissoes": [
    {
      "id": 3,
      "nome": "listartodosusuarios",
      "descricao": "Permissão gerada automaticamente pelo seeder",
      "ativo": true
    }
  ]
}
```

`401 Unauthorized` — token ausente ou inválido
`403 Forbidden` — sem permissão `cadastrarperfil`

---

### GET /auth/perfis/listar 🔒

Lista todos os perfis com paginação.

**Autenticação:** requer `Authorization: Bearer <accessToken>`
**Permissão exigida:** `listartodosperfis`

**Query parameters** — Spring `Pageable`

| Parâmetro | Tipo | Padrão | Descrição |
|---|---|---|---|
| `page` | `integer` | `0` | Número da página (base 0) |
| `size` | `integer` | `20` | Itens por página |
| `sort` | `string` | — | Campo e direção (ex: `nome,asc`) |

**Responses**

`200 OK` — retorna `PerfilDTO[]`

```json
[
  {
    "id": 1,
    "nome": "Administrador de Sistemas",
    "descricao": "Perfil com acesso total ao sistema",
    "ativo": true
  },
  {
    "id": 2,
    "nome": "Usuário Padrão",
    "descricao": "Perfil com permissões padrões do sistema",
    "ativo": true
  }
]
```

`401 Unauthorized` — token ausente ou inválido
`403 Forbidden` — sem permissão `listartodosperfis`

---

### GET /auth/perfis/listar-especifico/{id} 🔒

Retorna os dados de um perfil com suas permissões vinculadas.

**Autenticação:** requer `Authorization: Bearer <accessToken>`
**Permissão exigida:** `listarperfilespecifico`

**Path parameters**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | `integer` | ID do perfil |

**Responses**

`200 OK` — retorna `PerfilPermissaoDTO`

```json
{
  "perfil": {
    "id": 2,
    "nome": "Usuário Padrão",
    "descricao": "Perfil com permissões padrões do sistema",
    "ativo": true
  },
  "permissoes": [
    {
      "id": 3,
      "nome": "listartodosusuarios",
      "descricao": "Permissão gerada automaticamente pelo seeder",
      "ativo": true
    }
  ]
}
```

`401 Unauthorized` — token ausente ou inválido
`403 Forbidden` — sem permissão `listarperfilespecifico`
`404 Not Found` — perfil não encontrado

---

### GET /auth/perfis/listar-usuarios-vinculados/{id} 🔒

Retorna um perfil com a lista de usuários ativos vinculados a ele.

**Autenticação:** requer `Authorization: Bearer <accessToken>`
**Permissão exigida:** `listarusuariosvinculados`

**Path parameters**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | `integer` | ID do perfil |

**Responses**

`200 OK` — retorna `PerfilUsuarioDTO`

```json
{
  "perfil": {
    "id": 2,
    "nome": "Usuário Padrão",
    "descricao": "Perfil com permissões padrões do sistema",
    "ativo": true
  },
  "usuarios": [
    {
      "id": 42,
      "cpf": "12345678909",
      "nomeCompleto": "João Da Silva",
      "nomeAmigavel": "João",
      "nomeUser": "joao.silva",
      "senhaUser": null,
      "ativo": true
    }
  ]
}
```

`401 Unauthorized` — token ausente ou inválido
`403 Forbidden` — sem permissão `listarusuariosvinculados`
`404 Not Found` — perfil não encontrado

---

### GET /auth/perfis/clonar/{id} 🔒

Retorna os dados de um perfil com suas permissões, sem o objeto `perfil` — útil para criar um novo perfil com as mesmas permissões.

**Autenticação:** requer `Authorization: Bearer <accessToken>`
**Permissão exigida:** `clonarperfil`

**Path parameters**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | `integer` | ID do perfil a clonar |

**Responses**

`200 OK` — retorna `PerfilPermissaoDTO` com `perfil: null`

```json
{
  "perfil": null,
  "permissoes": [
    {
      "id": 3,
      "nome": "listartodosusuarios",
      "descricao": "Permissão gerada automaticamente pelo seeder",
      "ativo": true
    }
  ]
}
```

`401 Unauthorized` — token ausente ou inválido
`403 Forbidden` — sem permissão `clonarperfil`
`404 Not Found` — perfil não encontrado

---

### PUT /auth/perfis/editar 🔒

Atualiza os dados de um perfil e redefine suas permissões vinculadas.

**Autenticação:** requer `Authorization: Bearer <accessToken>`
**Permissão exigida:** `editarperfil`

**Request body** — `PerfilPermissaoDTO`

```json
{
  "perfil": {
    "id": 5,
    "nome": "Tutor Voluntário Sênior",
    "descricao": "Perfil para tutores experientes",
    "ativo": true
  },
  "permissoes": [
    {
      "id": 3,
      "nome": "listartodosusuarios",
      "descricao": "Permissão gerada automaticamente pelo seeder",
      "ativo": true
    },
    {
      "id": 7,
      "nome": "listarusuarioespecifico",
      "descricao": "Permissão gerada automaticamente pelo seeder",
      "ativo": true
    }
  ]
}
```

**Responses**

`200 OK` — retorna `PerfilPermissaoDTO` atualizado

`401 Unauthorized` — token ausente ou inválido
`403 Forbidden` — sem permissão `editarperfil`
`404 Not Found` — perfil não encontrado

---

### DELETE /auth/perfis/deletar/{id} 🔒

Remove um perfil e todos os seus vínculos de permissão e de usuário.

**Autenticação:** requer `Authorization: Bearer <accessToken>`
**Permissão exigida:** `deletarperfil`

**Path parameters**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | `integer` | ID do perfil a remover |

**Responses**

`200 OK` — retorna `true`

`401 Unauthorized` — token ausente ou inválido
`403 Forbidden` — sem permissão `deletarperfil`
`404 Not Found` — perfil não encontrado

---

## Permissões

---

### POST /auth/permissoes/cadastrar 🔒

Cadastra uma nova permissão manualmente. Na maioria dos casos as permissões são criadas pelo seeder automaticamente.

**Autenticação:** requer `Authorization: Bearer <accessToken>`
**Permissão exigida:** `cadastrarpermissao`

**Request body** — `PermissaoDTO`

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `nome` | `string` | ✅ | Nome da permissão (deve ser igual ao valor de `rota` na `@Permissao` do endpoint correspondente) |
| `descricao` | `string` | ✅ | Descrição da permissão |
| `ativo` | `boolean` | — | Padrão `true` |

```json
{
  "nome": "minharotapersonalizada",
  "descricao": "Permissão para acessar minha rota personalizada",
  "ativo": true
}
```

**Responses**

`200 OK` — retorna `PermissaoDTO` com ID gerado

```json
{
  "id": 15,
  "nome": "minharotapersonalizada",
  "descricao": "Permissão para acessar minha rota personalizada",
  "ativo": true
}
```

`401 Unauthorized` — token ausente ou inválido
`403 Forbidden` — sem permissão `cadastrarpermissao`

---

### GET /auth/permissoes/listar 🔒

Lista todas as permissões cadastradas com paginação.

**Autenticação:** requer `Authorization: Bearer <accessToken>`
**Permissão exigida:** `listartodaspermissoes`

**Query parameters** — Spring `Pageable`

| Parâmetro | Tipo | Padrão | Descrição |
|---|---|---|---|
| `page` | `integer` | `0` | Número da página (base 0) |
| `size` | `integer` | `20` | Itens por página |
| `sort` | `string` | — | Campo e direção (ex: `nome,asc`) |

**Responses**

`200 OK` — retorna `PermissaoDTO[]`

```json
[
  {
    "id": 1,
    "nome": "cadastrarperfil",
    "descricao": "Permissão gerada automaticamente pelo seeder",
    "ativo": true
  },
  {
    "id": 2,
    "nome": "cadastrarpermissao",
    "descricao": "Permissão gerada automaticamente pelo seeder",
    "ativo": true
  }
]
```

`401 Unauthorized` — token ausente ou inválido
`403 Forbidden` — sem permissão `listartodaspermissoes`

---

### GET /auth/permissoes/listar-especifico/{id} 🔒

Retorna os dados de uma permissão específica.

**Autenticação:** requer `Authorization: Bearer <accessToken>`
**Permissão exigida:** `listarpermissaoespecifica`

**Path parameters**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | `integer` | ID da permissão |

**Responses**

`200 OK` — retorna `PermissaoDTO`

```json
{
  "id": 3,
  "nome": "listartodosusuarios",
  "descricao": "Permissão gerada automaticamente pelo seeder",
  "ativo": true
}
```

`401 Unauthorized` — token ausente ou inválido
`403 Forbidden` — sem permissão `listarpermissaoespecifica`
`404 Not Found` — permissão não encontrada

---

### PUT /auth/permissoes/editar 🔒

Atualiza os dados de uma permissão existente.

**Autenticação:** requer `Authorization: Bearer <accessToken>`
**Permissão exigida:** `editarpermissao`

**Request body** — `PermissaoDTO`

```json
{
  "id": 15,
  "nome": "minharotapersonalizada",
  "descricao": "Descrição atualizada da permissão",
  "ativo": true
}
```

**Responses**

`200 OK` — retorna `PermissaoDTO` atualizado

`401 Unauthorized` — token ausente ou inválido
`403 Forbidden` — sem permissão `editarpermissao`
`404 Not Found` — permissão não encontrada

---

### DELETE /auth/permissoes/deletar/{id} 🔒

Remove uma permissão e todos os seus vínculos com perfis.

**Autenticação:** requer `Authorization: Bearer <accessToken>`
**Permissão exigida:** `deletarpermissao`

**Path parameters**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `id` | `integer` | ID da permissão a remover |

**Responses**

`200 OK` — retorna `true`

`401 Unauthorized` — token ausente ou inválido
`403 Forbidden` — sem permissão `deletarpermissao`
`404 Not Found` — permissão não encontrada

---

## Schemas

### UsuarioDTO

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | `integer` | ID gerado pelo banco |
| `cpf` | `string` | CPF com 11 dígitos sem máscara |
| `nomeCompleto` | `string` | Nome completo |
| `nomeAmigavel` | `string` | Nome de exibição |
| `nomeUser` | `string` | Nome de login (único) |
| `senhaUser` | `string\|null` | Sempre `null` nas respostas |
| `ativo` | `boolean` | Status do usuário |

### UsuarioPerfilDTO

| Campo | Tipo | Descrição |
|---|---|---|
| `usuario` | `UsuarioDTO\|null` | Dados do usuário |
| `perfisUsuario` | `PerfilDTO[]` | Perfis vinculados ao usuário |

### PerfilDTO

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | `integer` | ID gerado pelo banco |
| `nome` | `string` | Nome do perfil |
| `descricao` | `string` | Descrição do perfil |
| `ativo` | `boolean` | Status do perfil |

### PerfilPermissaoDTO

| Campo | Tipo | Descrição |
|---|---|---|
| `perfil` | `PerfilDTO\|null` | Dados do perfil |
| `permissoes` | `PermissaoDTO[]` | Permissões vinculadas ao perfil |

### PerfilUsuarioDTO

| Campo | Tipo | Descrição |
|---|---|---|
| `perfil` | `PerfilDTO` | Dados do perfil |
| `usuarios` | `UsuarioDTO[]` | Usuários vinculados ao perfil |

### PermissaoDTO

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | `integer` | ID gerado pelo banco |
| `nome` | `string` | Nome da permissão — deve ser igual ao valor de `rota` na `@Permissao` |
| `descricao` | `string` | Descrição da permissão |
| `ativo` | `boolean` | Status da permissão |

### LoginRequestDTO

| Campo | Tipo | Descrição |
|---|---|---|
| `nomeUser` | `string` | Nome de usuário |
| `senha` | `string` | Senha do usuário |

### LoginResponseDTO

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | `integer` | ID do usuário autenticado |
| `nomeAmigavel` | `string` | Nome de exibição |
| `nomeUser` | `string` | Nome de usuário |
| `accessToken` | `string` | JWT de acesso |
| `refreshToken` | `string` | UUID de renovação |
| `expiracaoAccessTokenSegundos` | `long` | Tempo de vida do access token em segundos |

### RefreshTokenRequestDTO

| Campo | Tipo | Descrição |
|---|---|---|
| `refreshToken` | `string` | UUID do refresh token atual |

### RefreshTokenResponseDTO

| Campo | Tipo | Descrição |
|---|---|---|
| `accessToken` | `string` | Novo JWT de acesso |
| `refreshToken` | `string` | Novo UUID (o anterior está invalidado) |
| `expiracaoAccessTokenSegundos` | `long` | Tempo de vida do novo access token em segundos |