# Arreda — Backend

API para conectar alunos e servidores em caronas. O backend usa Java 21, Spring Boot 4, Spring Data JPA e PostgreSQL 15.

## Executar com Docker Compose

Pré-requisito: Docker Desktop instalado e em execução.

1. Se for usar SMTP local, copie `.env.example` para `.env` e configure os valores conforme [as decisões e fluxos do projeto](docs/decisoes-de-projeto.md). Não versione o `.env`.
2. Na raiz do repositório, construa e inicie a API e o banco:

   ```bash
   docker compose up --build -d
   ```

3. Acompanhe a inicialização:

   ```bash
   docker compose logs -f api
   ```

A API fica em `http://localhost:8080` e o PostgreSQL em `localhost:5433`. As fotos de perfil são mantidas no volume Docker `arreda_uploads`. `docker compose down` preserva esse volume; `docker compose down -v` o remove.

Para parar os containers sem remover os volumes:

```bash
docker compose down
```

## Executar pela IDE

Pré-requisitos: JDK 21 e IntelliJ IDEA (ou IDE equivalente).

1. Inicie o PostgreSQL:

   ```bash
   docker compose up -d db
   ```

2. Configure o diretório de trabalho da aplicação como a raiz do repositório e execute `br.com.arreda.backend.BackendApplication`.

A API usa o PostgreSQL em `localhost:5433`. As fotos ficam em `./uploads`; essa pasta é local ao projeto e não é o volume Docker nomeado.

## Documentação

- [Decisões e fluxos do projeto](docs/decisoes-de-projeto.md): autenticação, e-mails e fotos de perfil.
- [Roadmap](docs/ROADMAP.md): funcionalidades planejadas.
- [Arquitetura](docs/ARCHITECTURE.md): organização do backend e modelo de dados.
- [Guia de commits e Pull Requests](docs/COMMIT_GUIDE.md): convenções de contribuição.

## API

Com a aplicação em execução, acesse o Swagger UI em <http://localhost:8080/swagger-ui.html>.



## 👥 Equipe
- **Carlos Eduardo** - *Lider da equipe*
- **João Vieira** - *Responsável técnico*
- **João Vitor** -*Responsável pela apresentação*
- **Victor Fernandes** - *Responsável pela documentação* 