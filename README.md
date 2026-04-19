# SmartHAS — Digital 360 🌐

> Plataforma de Inclusão Digital Comunitária — Projeto Anual FIAP | Sociedade 5.0

---

## 📋 Sobre o Projeto

O **Digital 360** é uma plataforma mobile e web voltada à inclusão digital de comunidades vulneráveis, alinhada aos princípios da Sociedade 5.0. A solução oferece:

- 📚 **Cursos de alfabetização digital** com progressão por níveis
- 🏛️ **Guia de serviços públicos** integrado a APIs gov.br, INSS e e-SUS
- 👥 **Tutoria voluntária** com matching inteligente por região e disponibilidade
- 🏅 **Gamificação** com badges e acompanhamento de progresso
- 🤖 **Nivelamento por IA** para personalização da trilha de aprendizado

---

## 👨‍💻 Equipe

| Nome | RM |
|---|---|
| Eduardo Vasques | 556970 |
| Otávio Souza | 550361 |
| Enzo Paiva | 557632 |
| Rafael Albuquerque | 559136 |
| Guilherme Tiburcio | 557500 |

---

## 🛠️ Stack Tecnológica

| Camada | Tecnologia |
|---|---|
| **Back-end** | Spring Boot 4.0.5 · Java 21 |
| **Front-end** | React Native · PWA |
| **Banco de Dados** | PostgreSQL 16 |
| **Cache** | Redis 7 |
| **Mensageria** | RabbitMQ 3 |
| **Segurança** | Spring Security · JWT (JJWT 0.12) · OAuth2 |
| **Migrations** | Flyway |
| **Documentação** | SpringDoc OpenAPI 3 (Swagger UI) |
| **Observabilidade** | Spring Actuator · Micrometer · Prometheus |
| **Infraestrutura** | Docker · Docker Compose · Kubernetes (Fase 3) |
| **Cloud** | AWS / GCP |

---

## 📁 Estrutura do Projeto

```
smart-has-fiap-api/
├── src/
│   ├── main/
│   │   ├── java/br/com/fiap/on/smarthas/
│   │   │   ├── SmartHasApplication.java
│   │   │   ├── config/                    # Configurações (Security, Redis, RabbitMQ)
│   │   │   ├── shared/                    # Exceções, DTOs e Audit globais
│   │   │   ├── auth/                      # Autenticação, JWT, RBAC
│   │   │   ├── curso/                    # Cursos, módulos e avaliações
│   │   │   ├── progresso/                  # Progresso, badges e gamificação
│   │   │   ├── tutor/                     # Cadastro, matching e sessões
│   │   │   ├── notificacao/              # Push, e-mail e SMS via RabbitMQ
│   │   │   ├── guia/                     # Guia de serviços públicos
│   │   │   └── metricas/                 # Métricas e relatórios
│   │   └── resources/
│   │       ├── application.yaml
│   │       └── db/migration/              # Scripts Flyway (V1__, V2__...)
│   └── test/
│       └── java/br/com/fiap/on/smarthas/ # Testes unitários e de integração
├── docker-compose.yml
├── .env.example
├── pom.xml
└── README.md
```

---

## ⚙️ Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- [Java 21](https://adoptium.net/) (Temurin recomendado)
- [Maven 3.9+](https://maven.apache.org/)
- [Docker](https://www.docker.com/) e [Docker Compose](https://docs.docker.com/compose/)
- [Git](https://git-scm.com/)

---

## 🚀 Como Rodar Localmente

### 1. Clone o repositório

```bash
git clone https://github.com/DevRafonalde/smart-has-fiap-api.git
cd smart-has-fiap-api
```

### 2. Configure as variáveis de ambiente

```bash
cp .env.example .env
```

> ⚠️ O arquivo `.env` já vem com valores padrão para desenvolvimento local. **Nunca suba o `.env` para o Git.**

### 3. Suba a infraestrutura (PostgreSQL, Redis e RabbitMQ)

```bash
docker compose up -d
```

Verifique se os containers estão saudáveis:

```bash
docker compose ps
```

Você deverá ver os três serviços com status `healthy`:

```
NAME                   STATUS
smarthas-postgres      Up (healthy)
smarthas-redis         Up (healthy)
smarthas-rabbitmq      Up (healthy)
```

### 4. Execute a aplicação

```bash
./mvnw spring-boot:run
```

Ou, se preferir buildar o JAR:

```bash
./mvnw clean package -DskipTests
java -jar target/smartHas-0.0.1-SNAPSHOT.jar
```

### 5. Acesse a documentação da API

| Interface | URL |
|---|---|
| **Swagger UI** | http://localhost:8080/api/swagger-ui.html |
| **OpenAPI JSON** | http://localhost:8080/api/v3/api-docs |
| **Actuator** | http://localhost:8080/api/actuator/health |
| **RabbitMQ UI** | http://localhost:15672 (usuário: `smarthas` / senha: `rabbitmq123`) |

---

## 🗄️ Banco de Dados

As migrations são gerenciadas pelo **Flyway** e executadas automaticamente ao iniciar a aplicação. Os scripts ficam em:

```
src/main/resources/db/migration/
├── V1__create_users_table.sql
├── V2__create_courses_tables.sql
├── V3__create_tutor_tables.sql
└── ...
```

Para resetar o banco em desenvolvimento:

```bash
docker compose down -v   # remove volumes
docker compose up -d     # sobe novamente (Flyway recria tudo)
```

---

## 🔐 Autenticação

> 📄 Para mais detalhes sobre a implementação do módulo de autenticação, veja: [AuthModule.md](./docs/AuthModule.md)

A API utiliza **JWT Bearer Token**. Para acessar endpoints protegidos:

**1. Registre um usuário:**
```http
POST /api/auth/usuarios/registrar
Content-Type: application/json

{
  "nome": "João Silva",
  "cpf": "123.456.789-00",
  "email": "joao@email.com",
  "senha": "Senha@123",
  "role": "CITIZEN"
}
```

**2. Faça login:**
```http
POST /api/auth/usuarios/login
Content-Type: application/json

{
  "email": "joao@email.com",
  "senha": "Senha@123"
}
```

**3. Use o token retornado no header:**
```http
Authorization: Bearer <seu_token_aqui>
```

> Os tokens expiram em **15 minutos**. Use o endpoint `/api/auth/usuarios/refresh` com o refresh token para renová-los.

---

## 🧪 Testes

```bash
# Todos os testes
./mvnw test

# Apenas testes unitários (sem Docker)
./mvnw test -Dgroups="unit"

# Apenas testes de integração (requer Docker para Testcontainers)
./mvnw test -Dgroups="integration"

# Relatório de cobertura
./mvnw verify
# Relatório gerado em: target/site/jacoco/index.html
```

---

## 🌍 Variáveis de Ambiente

| Variável | Descrição | Padrão (dev) |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Perfil ativo da aplicação | `dev` |
| `SERVER_PORT` | Porta da API | `8080` |
| `DB_HOST` | Host do PostgreSQL | `localhost` |
| `DB_PORT` | Porta do PostgreSQL | `5432` |
| `DB_NAME` | Nome do banco | `smarthas` |
| `DB_USER` | Usuário do banco | `smarthas` |
| `DB_PASSWORD` | Senha do banco | `smarthas123` |
| `REDIS_HOST` | Host do Redis | `localhost` |
| `REDIS_PASSWORD` | Senha do Redis | `redis123` |
| `RABBITMQ_HOST` | Host do RabbitMQ | `localhost` |
| `RABBITMQ_USER` | Usuário do RabbitMQ | `smarthas` |
| `RABBITMQ_PASSWORD` | Senha do RabbitMQ | `rabbitmq123` |
| `JWT_SECRET` | Chave secreta JWT (mín. 256 bits) | `(ver .env.example)` |
| `JWT_EXPIRATION` | Expiração do access token (segundos) | `900` (15 min) |
| `JWT_REFRESH` | Expiração do refresh token (segundos) | `604800` (7 dias) |

---

## 📡 Principais Endpoints

### Auth
| Método | Endpoint                      | Descrição |
|---|-------------------------------|---|
| `POST` | `/api/auth/usuarios/register` | Cadastro de usuário |
| `POST` | `/api/auth/usuarios/login`    | Login e geração de token |
| `POST` | `/api/auth/usuarios/refresh`  | Renovação do token |

### Cursos
| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/courses` | Lista todos os cursos |
| `GET` | `/api/courses/{id}` | Detalhe de um curso |
| `GET` | `/api/courses/{id}/modules` | Módulos de um curso |
| `POST` | `/api/courses/{id}/enroll` | Matrícula no curso |

### Progresso
| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/progress` | Progresso do usuário autenticado |
| `POST` | `/api/progress/modules/{id}/complete` | Marcar módulo como concluído |
| `GET` | `/api/progress/badges` | Badges conquistados |

### Tutoria
| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/tutors` | Lista tutores disponíveis |
| `POST` | `/api/tutoring/sessions` | Solicitar sessão de tutoria |
| `POST` | `/api/tutoring/sessions/{id}/rate` | Avaliar sessão |

### Guia de Serviços
| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/guide/services` | Lista serviços públicos |
| `GET` | `/api/guide/services?category=saude` | Filtra por categoria |
| `GET` | `/api/guide/services/{id}` | Detalhe de um serviço |

> A documentação completa e interativa de todos os endpoints está disponível no **Swagger UI**.

---

## 🏗️ Arquitetura

O projeto adota **Clean Architecture** organizada por domínios, seguindo os princípios de separação de responsabilidades:

```
Controller → Service → Repository → Domain
               ↓
           DTOs / Mappers
```

**Perfis de ambiente:**
- `dev` — Swagger ativo, logs detalhados, hot reload
- `prod` — Swagger desabilitado, logs reduzidos, sem devtools

---

## 📜 Regras de Negócio Principais

| ID | Regra |
|---|---|
| **RN01** | CPF válido e único; e-mail confirmado em até 24h |
| **RN02** | Nivelamento por IA: Básico / Intermediário / Avançado (quiz de 5 perguntas) |
| **RN03** | Score mínimo 70% para aprovação; até 3 tentativas antes de acionar tutoria |
| **RN04** | Badge emitido apenas após 100% do módulo concluído e aprovação |
| **RN05** | Matching de tutor prioriza mesma região; SLA de 24h |
| **RN06** | Avaliação obrigatória pós-sessão; tutores < 3 estrelas em 3 sessões consecutivas saem da rotação |
| **RN08** | Conformidade LGPD: exclusão de dados em até 72h |
| **RN10** | Máximo de 3 sessões simultâneas por tutor |

---

## 🔗 Repositórios

| Repositório | Link |
|---|---|
| **Back-end (este)** | https://github.com/DevRafonalde/smart-has-fiap-api |
| **Front-end** | https://github.com/DevRafonalde/smart-has-fiap-fe |

---

## 📄 Licença

Este projeto foi desenvolvido para fins acadêmicos no contexto do **Projeto Anual FIAP — Sociedade 5.0**.

---

<p align="center">
  Desenvolvido com ❤️ pela equipe SmartHAS · FIAP 2025
</p>