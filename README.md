# ArenaHub - Backend API

API RESTful para a plataforma **ArenaHub**, desenvolvida com **Java 21** e **Spring Boot 3.5**. O sistema é responsável pelo gerenciamento completo de arenas esportivas, agendamento de quadras, jogos abertos entre atletas, sorteio de times, gestão de assinaturas e integrações de pagamento com Stripe e Asaas.

---

## Tecnologias Utilizadas

- **Linguagem & Framework:** Java 21 | Spring Boot 3.5.0
- **Segurança & Autenticação:** Spring Security | JWT (JSON Web Tokens)
- **Persistência de Dados:** Spring Data JPA | Hibernate | PostgreSQL
- **Documentação de API:** Springdoc OpenAPI (Swagger UI)
- **Mapeamento & Utilitários:** Lombok | MapStruct | ZXing (Geração de QR Codes)
- **Gateways de Pagamento:** Stripe API | Asaas API (Pix / Webhooks)
- **Notificações:** Gmail SMTP (Email) | Zenvia SDK (SMS / WhatsApp)
- **Containerização & Deploy:** Docker | Docker Compose | Nginx (Proxy Reverso & SSL)

---

## Módulos e Funcionalidades

- **Autenticação e Segurança:** Login, cadastro de usuários (Arenas e Atletas), verificação via código SMS/Email e rotas protegidas por JWT.
- **Gestão de Arenas & Quadras:** Cadastro e gerenciamento de arenas, quadras associadas e modalidades esportivas suportadas.
- **Agendamentos:** Reserva de horários em quadras, cálculo de valores, controle de disponibilidade e cancelamentos.
- **Jogos Abertos (Networking de Atletas):** Criação e participação em partidas abertas, lista de presenças e confirmação de participantes.
- **Sorteador de Times:** Ferramenta para divisão automática e equilibrada de times para partidas.
- **Pagamentos e Assinaturas:** Processamento de pagamentos e assinaturas de planos para arenas via Stripe e Asaas, com tratamento de Webhooks em tempo real.
- **Notificações:** Notificação via SMS (Zenvia) e Email (Gmail SMTP) para confirmações de agendamentos e verificação de contas.

---

## Requisitos Prévios

Para executar o projeto localmente ou em uma máquina virtual (VM), certifique-se de ter instalado:

- **Docker** e **Docker Compose** (recomendado para deploy e ambiente containerizado)
- **Java JDK 21** e **Apache Maven 3.9+** (opcional, caso queira rodar diretamente na máquina sem Docker)
- **Git**

---

## Configuração do Ambiente (`.env`)

Crie o arquivo `.env` na raiz do repositório baseado no modelo [.env.example](file://./.env.example):

```bash
cp .env.example .env
```

Abra o arquivo `.env` e preencha as variáveis de acordo com o seu ambiente (JWT Secret, credenciais do banco PostgreSQL, chaves do Stripe, Asaas, Zenvia e Gmail).

---

## Executando com Docker Compose (Local ou VM)

O repositório já possui uma estrutura pronta contendo:
- **`db`**: Container PostgreSQL 16
- **`app`**: Aplicação Spring Boot
- **`pgadmin`**: Interface gráfica web para gerenciamento do banco (Porta `15432`)
- **`proxy`**: Nginx configurado para HTTPS/SSL e proxy reverso para a aplicação

### 1. Subir os Serviços

Para compilar a imagem e iniciar os containers em background:

```bash
docker compose up -d --build
```

### 2. Verificar os Containers em Execução

```bash
docker compose ps
```

### 3. Acompanhar os Logs da Aplicação

```bash
docker compose logs -f app
```

### 4. Parar os Serviços

```bash
docker compose down
```

---

## Executando Localmente via Maven (Sem Docker para a App)

Se preferir rodar apenas o banco PostgreSQL via Docker e a aplicação Spring Boot na sua IDE/Terminal:

1. Suba apenas o banco de dados:
   ```bash
   docker compose up -d db
   ```

2. Execute a aplicação usando o perfil `dev`:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

A API estará disponível em: `http://localhost:8084`

---

## Implantação em VM (Cloud / Linux)

Para implantar na sua VM (ex: Ubuntu Server em AWS, Google Cloud, DigitalOcean, Linode ou servidor próprio):

### Passo 1: Instalar Docker na VM
```bash
sudo apt update
sudo apt install -y docker.io docker-compose-v2
sudo systemctl enable --now docker
```

### Passo 2: Clonar o Repositório e Configurar `.env`
```bash
git clone https://github.com/seu-usuario/arenahub-backend.git
cd arenahub-backend
cp .env.example .env
nano .env # Edite suas credenciais e chaves
```

### Passo 3: Configurar Certificado SSL (Certbot / Let's Encrypt)
Caso queira utilizar o Nginx incluso para rotear o domínio (ex: `api.arenahub.app`) com HTTPS:
```bash
sudo apt install -y certbot
sudo certbot certonly --standalone -d api.arenahub.app
```
*Certifique-se de que os certificados fiquem salvos no diretório padrão `/etc/letsencrypt/live/arenahub.app/`.*

### Passo 4: Subir o Ambiente Docker
```bash
docker compose up -d --build
```

---

## Documentação da API (Swagger / OpenAPI)

Com a aplicação rodando, acesse a documentação interativa das rotas:

- **Local:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) (ou porta `8084` se rodando via Maven)
- **Produção:** `https://api.arenahub.app/swagger-ui/index.html`

---

## Estrutura do Projeto

```text
arenahub-backend/
├── src/
│   ├── main/
│   │   ├── java/com/engstrategy/arenahub_api/
│   │   │   ├── config/          # Configurações do Spring (Security, Swagger, Web)
│   │   │   ├── controller/      # Endpoints da API REST
│   │   │   ├── dto/             # Objetos de Transferência de Dados (DTOs)
│   │   │   ├── exceptions/      # Handler global de exceções
│   │   │   ├── jwt/             # Filtros e utilitários JWT
│   │   │   ├── mapper/          # Mapeadores MapStruct (Entity <-> DTO)
│   │   │   ├── model/           # Entidades JPA
│   │   │   ├── repository/      # Interfaces de acesso ao banco
│   │   │   ├── service/         # Regras de negócio da aplicação
│   │   │   └── util/            # Utilitários (QR Code, etc.)
│   │   └── resources/
│   │       └── application.yml  # Perfis e configurações (dev, docker, prod)
├── nginx/
│   └── nginx.conf               # Configuração do Proxy Reverso Nginx
├── Dockerfile                   # Build multi-stage da aplicação Java 21
├── docker-compose.yml           # Orquestração dos serviços (db, app, pgadmin, proxy)
├── .env.example                 # Modelo de variáveis de ambiente
└── README.md                    # Documentação principal
```

---

## Licença

Este projeto é de propriedade da **ArenaHub / EngStrategy**. Todos os direitos reservados.