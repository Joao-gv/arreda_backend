    # 🚗 Arreda - Sistema de Caronas 

> **Arreda** é uma plataforma moderna para gestão de caronas, desenvolvida por alunos do IFMG pensando na questão de acessibilidade da própria instituição, a plataforma é focada em segurança e praticidade. Desenvolvida com Spring Boot 3 e Java 21.

---

### 📋 Sobre o Projeto
O Arreda conecta motoristas e passageiros de forma eficiente. O foco inicial é o ambiente universitário/corporativo, onde a confiança é a base para o compartilhamento de viagens.

### 🛠️ Tecnologias Utilizadas
- **Backend:** Java 21, Spring Boot 3, Spring Data JPA
- **Banco de Dados:** PostgreSQL 15 (via Docker)
- **Documentação:** Swagger (OpenAPI)
- **Gerenciador de Dependências:** Maven

---

### 🗺️ [Roadmap de Desenvolvimento (`ROADMAP.md`)](./ROADMAP.md)

**Para saber o que estamos construindo e qual é o rumo do projeto.**

- Entenda as grandes fases lógicas: `Caronas` e `Reservas`.
- Acompanhe o que já foi finalizado e veja a lista das tarefas (_features_) que a equipe ainda fará.


---

### 🏗️ [Arquitetura do Sistema (`ARCHITECTURE.md`)](./ARCHITECTURE.md)

**Para entender a estrutura do banco de dados e as regras do código.**
- O Diagrama de Banco de Dados (Entidade-Relacionamento) mostrando visualmente como as tabelas se conectam.
- Boas práticas e padrões de código essenciais que nossa equipe usa para manter a API rápida e organizada.
---

### 🤝 [Diretrizes de Contribuição (`COMMIT_GUIDE.md`)](./COMMIT_GUIDE.md)

**Para saber a forma correta de subir o seu trabalho concluído para o GitHub.**

- Padrão das mensagens dos Commits (`feat:`, `fix:`, `docs:`, etc).
- O passo a passo de como criar _branches_ e abrir de forma segura um _Pull Request_ (PR).

---

### 🚀 Como Executar o Projeto

### Pré-requisitos
- **JDK 21** instalado.
- **Docker Desktop** rodando.
- IDE (Recomendamos **IntelliJ IDEA**).

### Passos para rodar
1.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/SEU_USUARIO/arreda-backend.git](https://github.com/SEU_USUARIO/arreda-backend.git)
    ```
2.  **Suba o banco de dados:**
    ```bash
    docker-compose up -d
    ```
3.  **Rode a aplicação:**
    Abra o projeto no IntelliJ e execute a classe `ArredaApplication.java`.
---

### Para testar as rotas da aplicação
Estamos utilizando o Swagger, portanto basta acessar a seguinte url:

    http://localhost:8080/swagger-ui.html

---

## 👥 Equipe
- **Carlos Eduardo** - *Lider da equipe*
- **João Vieira** - *Responsável técnico*
- **João Vitor** -*Responsável pela apresentação*
- **Victor Fernandes** - *Responsável pela documentação* 

## Configuração de e-mail para desenvolvimento

O backend usa SMTP por meio do `JavaMailSender`. Para testar sem enviar mensagens a pessoas reais, configure uma caixa **Email Sandbox** no Mailtrap e copie os dados SMTP exibidos pela caixa.

1. Copie `.env.example` para `.env` como referência local e preencha `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME` e `MAIL_PASSWORD` com os dados da caixa Mailtrap. Defina `MAIL_FROM` com o remetente exibido na mensagem. `MAIL_SMTP_AUTH` e `MAIL_SMTP_STARTTLS` são opcionais; por padrão, ambos estão habilitados.
2. Disponibilize essas variáveis no ambiente em que a aplicação será executada. O Spring Boot lê variáveis de ambiente do processo; criar o arquivo `.env` não faz com que ele seja carregado automaticamente. No IntelliJ, configure-as em **Run/Debug Configurations → Environment variables**; no PowerShell, defina-as na sessão antes de iniciar a aplicação. Use os mesmos nomes e valores preenchidos no arquivo local.
3. Inicie a aplicação e, quando um fluxo chamar `EmailService.enviar(destinatario, assunto, corpoHtml)`, confira a mensagem na caixa Mailtrap.

As opções `MAIL_SMTP_AUTH` e `MAIL_SMTP_STARTTLS` controlam autenticação e STARTTLS. Os arquivos `.env` locais são ignorados pelo Git; compartilhe apenas os nomes das variáveis, nunca os valores das credenciais.

Os templates HTML iniciais estão em `src/main/resources/templates/`. Eles usam os marcadores `{{nome}}`, `{{linkConfirmacao}}` e `{{linkRedefinicao}}`; os fluxos que os preencherão serão implementados nas tarefas seguintes.
