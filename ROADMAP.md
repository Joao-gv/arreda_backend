# 🗺️ Roadmap do Arreda

O Arreda é uma plataforma de caronas para alunos e servidores. O projeto começou como trabalho de POO e agora está na fase de evolução necessária para a entrega da disciplina de Backend, com continuidade planejada após o semestre.

Este roadmap resume as etapas do produto. O backlog enviado contém as issues, dependências e critérios de aceite detalhados de cada tarefa. A divisão entre integrantes pode ser ajustada pela equipe sem alterar as etapas do produto.

## Histórico — fase inicial de POO e fundação do backend

As Sprints 1 a 6 abaixo registram a fase inicial do projeto. Conforme informado pela equipe, as Sprints 1 a 5 foram implementadas. A Sprint 6, referente ao deploy da aplicação, foi adiada para quando o projeto estiver concluído e pronto para entrega.

| Sprint | Tema | Status |
|---|---|---|
| 1 | Configuração, banco de dados e entidades de usuário | Concluída |
| 2 | Cadastro, autenticação e tokens | Concluída |
| 3 | Perfil de motorista e veículos | Concluída |
| 4 | Caronas e fluxo de solicitação/aceite | Concluída |
| 5 | Histórico e tratamento de erros | Concluída |
| 6 | Deploy e empacotamento final da aplicação | Adiada até o projeto estar pronto para entrega |

### Sprint 1 — Alicerce

- Configurar Maven, Spring, conexão com PostgreSQL e documentação OpenAPI.
- Modelar usuário e perfil de motorista, criar repositórios e iniciar as regras de cadastro.

### Sprint 2 — Autenticação e usuários

- **Tarefa 01:** endpoint de cadastro, validação de e-mail único e armazenamento de senha com hash.
- **Tarefa 02:** login e emissão de JWT com tratamento de credenciais inválidas.
- **Tarefa 03:** refresh token persistido, com validade e renovação de tokens.

### Sprint 3 — Motorista e veículos

- **Tarefa 04:** enums para status de carona e participação.
- **Tarefa 05:** cadastro do perfil de motorista e de veículos associados ao usuário autenticado.

### Sprint 4 — Caronas e fluxo de aceite

- **Tarefa 06:** entidades e relacionamentos de caronas e participações.
- **Tarefa 07:** publicação de carona e inclusão do motorista como participante confirmado.
- **Tarefa 08:** solicitação de vaga pelo passageiro, inicialmente pendente.
- **Tarefa 09:** aceite ou rejeição pelo motorista, com atualização das vagas ao aceitar.

### Sprint 5 — Histórico e refinamentos

- **Tarefa 10:** histórico de caronas do usuário como motorista e passageiro.
- **Tarefa 11:** tratamento global de erros de validação, negócio e recursos não encontrados.

### Sprint 6 — Finalização e deploy

- **Tarefa 12:** Dockerfile e Docker Compose para executar backend e banco juntos.


### Plano detalhado original (consulta histórica)

A seção expansível abaixo preserva o plano técnico completo das Sprints 1 a 6. As pessoas, datas e divisão de tarefas que aparecem nele pertencem ao planejamento antigo e não representam atribuições ou prazos atuais. A Sprint 6 continua pendente até a etapa final de entrega.

<details>
<summary>Mostrar plano original das Sprints 1 a 6</summary>

# 📅 Sprints e Distribuição de Tarefas

## 🎯 Sprint 1 — O Alicerce (Configuração e Entidades de Usuário)
**Foco:** Deixar o banco rodando e mapear os primeiros objetos.

- **Pessoa 1 (Infra):** Configurar o `pom.xml` (Web, JPA, Postgres, SpringDoc/Swagger, Lombok) e o `application.properties` para conectar ao banco local ou Docker.
- **Pessoa 2 (Modelagem):** Criar as classes `Usuario` e `PerfilMotorista`. Aplicar encapsulamento e anotações JPA (`@Entity`, `@Table`, `@Id`).
- **Pessoa 3 (Persistência):** Criar as interfaces `UsuarioRepository` e `PerfilMotoristaRepository`.
- **Pessoa 4 (Business):** Criar o `UsuarioService` (ex: lógica para validar se o e-mail já existe antes de salvar).

---

## 🎯 SPRINT 2 — Autenticação e Usuários

### 📋 Tarefa #01: Endpoint de Cadastro de Usuário
- **Nível Sugerido:** 🟢 Iniciante / Intermediário
- **Objetivo:** Permitir que novos usuários se cadastrem no sistema recebendo os dados via API.
- **Componentes:** `UsuarioController.java`, `UsuarioService.java`

#### 🛠️ Passo a Passo:
1. Criar a classe `UsuarioController` no pacote `controller`, anotando-a com `@RestController` e `@RequestMapping("/api/usuarios")`.
2. Criar um método `POST` (`@PostMapping`) que receba o `UsuarioCreateDTO` (já criado). Use as anotações `@RequestBody` e `@Valid` para forçar as validações dos campos.
3. No `UsuarioService`, injetar o `PasswordEncoder` e finalizar a lógica do método de cadastro:
  - Validar se o e-mail fornecido já existe no banco usando `usuarioRepository.existsByEmail()`. Se existir, lançar uma exceção de negócio.
  - Converter o e-mail para letras minúsculas (`.toLowerCase()`) antes de salvar para evitar duplicidades por caixa.
  - Criptografar a senha recebida usando `passwordEncoder.encode(dto.getSenha())`.
  - Atribuir o hash gerado ao campo `senhaHash` da entidade `Usuario`.
4. Salvar a entidade e retornar o status HTTP `201 Created`.

#### 🎯 Critérios de Aceite (O que entregar):
- Uma requisição `POST /api/usuarios` com um JSON válido deve registrar o usuário com sucesso no banco de dados.
- A senha **nunca** deve ser salva em texto puro; o banco deve conter apenas o hash criptográfico.
- Tentativas de cadastrar um e-mail já existente devem ser bloqueadas com retorno de erro apropriado.

---

### 📋 Tarefa #02: Implementação do JWT e Login
- **Nível Sugerido:** 🔴 Avançado
- **Objetivo:** Autenticar o usuário com suas credenciais e gerar um token de acesso JWT seguro.
- **Componentes:** `AuthController.java`, `TokenService.java`, `LoginDTO.java`

#### 🛠️ Passo a Passo:
1. Adicionar as dependências do `Spring Security` e de uma biblioteca JWT (como `java-jwt` da Auth0 ou `jjwt`) no seu arquivo de gerenciamento de dependências (`pom.xml` ou `build.gradle`).
2. Criar a classe `TokenService` no pacote `security`. Implementar o método `gerarToken(Usuario usuario)` definindo:
  - Tempo de expiração curto (ex: 2 horas).
  - Subject (geralmente o e-mail ou ID do usuário).
  - Algoritmo de assinatura HMAC256 utilizando uma chave secreta injetada a partir do `application.properties`.
3. Criar a classe `LoginDTO` no pacote `dto` contendo apenas os campos `email` e `senha` com validações básicas (`@NotBlank`, `@Email`).
4. Criar o `AuthController` anotado com `@RestController` e `@RequestMapping("/api/auth")`.
5. Criar o endpoint `POST /login` que recebe o `LoginDTO`:
  - Buscar o usuário no banco pelo e-mail. Se não encontrar, lançar erro de credenciais inválidas.
  - Utilizar `passwordEncoder.matches(senhaPura, senhaHashDoBanco)` para validar a senha. Se não bater, lançar erro.
  - Se as credenciais estiverem corretas, chamar o `TokenService` para gerar o JWT e retorná-lo no corpo da resposta.

#### 🎯 Critérios de Aceite (O que entregar):
- Endpoint `POST /api/auth/login` recebe e-mail e senha corretos e devolve uma String contendo o token JWT.
- Credenciais inválidas (e-mail inexistente ou senha errada) retornam erro de segurança com status `401 Unauthorized` ou `403 Forbidden`.

---

### 📋 Tarefa #03: Implementação do Refresh Token
- **Nível Sugerido:** 🔴 Avançado
- **Objetivo:** Permitir que o usuário renove seu JWT expirado de forma transparente sem precisar reintroduzir a senha.
- **Componentes:** `RefreshToken.java`, `RefreshTokenRepository.java`, `TokenService.java`

#### 🛠️ Passo a Passo:
1. Criar a entidade `RefreshToken` no pacote `model` contendo os atributos: `id` (Long), `token` (String, única), `dataExpiracao` (LocalDateTime) e um relacionamento `@OneToOne` mapeando para a entidade `Usuario`.
2. Criar a interface `RefreshTokenRepository` estendendo `JpaRepository`.
3. No `TokenService` (ou um service próprio para o refresh token), criar uma lógica para gerar uma String randômica longa (como um `UUID.randomUUID().toString()`) com expiração estendida (ex: 7 dias) e salvá-la no banco atrelada ao usuário.
4. Modificar o endpoint de Login para que ele retorne um objeto contendo tanto o `accessToken` (JWT) quanto o `refreshToken`.
5. Criar o endpoint `POST /api/auth/refresh` que recebe o Refresh Token:
  - Buscar o token no banco de dados e validar se ele existe e se não está expirado.
  - Se for válido, gerar um novo JWT (`accessToken`) e atualizar o `refreshToken` no banco (deletando o antigo ou rotacionando-o).
  - Devolver os novos tokens na resposta.

#### 🎯 Critérios de Aceite (O que entregar):
- O fluxo de login de sucesso agora retorna dois tokens (`accessToken` e `refreshToken`).
- O endpoint `/api/auth/refresh` consegue emitir um novo par de tokens válidos se receber um Refresh Token legítimo e dentro do prazo de validade.

---

## 🎯 SPRINT 3 — Motorista e Veículos

### 📋 Tarefa #04: Criação dos ENUMs do Sistema
- **Nível Sugerido:** 🟢 Iniciante
- **Objetivo:** Padronizar os status e tipos que controlam o fluxo de caronas e participações.
- **Componentes:** Pacote `enums/`

#### 🛠️ Passo a Passo:
1. Criar o pacote `enums` na estrutura do projeto.
2. Criar o enum `StatusCarona` com os valores: `ATIVA`, `LOTADA`, `CANCELADA`, `CONCLUIDA`.
3. Criar o enum `TipoParticipacao` com os valores: `MOTORISTA`, `PASSAGEIRO`.
4. Criar o enum `StatusParticipacao` com os valores: `PENDENTE`, `CONFIRMADO`, `REJEITADO`, `CANCELADO`.

#### 🎯 Critérios de Aceite (O que entregar):
- Os enums criados e prontos para serem importados nas entidades das próximas tarefas.
- Uso correto da anotação `@Enumerated(EnumType.STRING)` configurada nas propriedades onde esses enums serão utilizados.

---

### 📋 Tarefa #05: Cadastro de Perfil de Motorista e Veículo
- **Nível Sugerido:** 🟢 Iniciante
- **Objetivo:** Permitir que um usuário comum registre seus dados de habilitação e cadastre seu carro para oferecer caronas.
- **Componentes:** `PerfilMotorista.java`, `Veiculo.java`, seus respectivos Repositories e Controllers.

#### 🛠️ Passo a Passo:
1. Criar a entidade `PerfilMotorista` com os atributos: `id` (Long), `cnh` (String), `validadeCnh` (LocalDate) e o relacionamento `@OneToOne` com a entidade `Usuario`.
2. Criar a entidade `Veiculo` com os atributos: `id` (Long), `placa` (String), `marca` (String), `modelo` (String), `cor` (String), `capacidadePassageiros` (int) e o relacionamento `@ManyToOne` com `PerfilMotorista`.
3. Criar as interfaces de repositório `PerfilMotoristaRepository` e `VeiculoRepository`.
4. Criar os endpoints `POST` apropriados para o cadastro dessas informações (ex: `POST /api/motorista/perfil` e `POST /api/motorista/veiculos`), vinculando-os automaticamente ao ID do usuário autenticado extraído do token JWT.

#### 🎯 Critérios de Aceite (O que entregar):
- Um usuário autenticado consegue enviar seus dados de CNH e salvar seu perfil de motorista.
- O motorista consegue cadastrar um ou mais veículos associados à sua conta.

---

## 🎯 SPRINT 4 — Caronas e Fluxo de Aceite (Coração do Sistema)

### 📋 Tarefa #06: Estrutura Base de Caronas e Vínculos
- **Nível Sugerido:** 🟢 Iniciante
- **Objetivo:** Mapear no banco de dados as tabelas que gerenciam a oferta de caronas e o controle de assentos.
- **Componentes:** `Carona.java`, `ParticipacaoCarona.java`

#### 🛠️ Passo a Passo:
1. Criar a entidade `Carona` contendo: `id` (Long), `origem` (String), `destino` (String), `dataHoraPartida` (LocalDateTime), `vagasDisponiveis` (int), e a propriedade `status` anotada com `@Enumerated(EnumType.STRING)` mapeando para o enum `StatusCarona`.
2. Criar os relacionamentos em `Carona`: `@ManyToOne` apontando para `PerfilMotorista` e `@ManyToOne` apontando para `Veiculo`.
3. Criar a entidade associativa `ParticipacaoCarona` contendo: `id` (Long), relacionamento `@ManyToOne` com `Usuario`, `@ManyToOne` com `Carona`, campo `tipo` (`TipoParticipacao`) e campo `status` (`StatusParticipacao`).
4. Criar os repositórios `CaronaRepository` e `ParticipacaoCaronaRepository`.

#### 🎯 Critérios de Aceite (O que entregar):
- O banco de dados deve gerar as tabelas `carona` e `participacao_carona` com as chaves estrangeiras perfeitamente mapeadas pelo Hibernate.

---

### 📋 Tarefa #07: Oferecer uma Carona (Fluxo do Motorista)
- **Nível Sugerido:** 🟡 Intermediário
- **Objetivo:** Permitir que um motorista publique uma nova carona no sistema, vinculando-se automaticamente a ela.
- **Componentes:** `CaronaController.java`, `CaronaService.java`

#### 🛠️ Passo a Passo:
1. Criar o endpoint `POST /api/caronas` que recebe os dados da viagem (origem, destino, data/hora, vagas, veículo utilizado).
2. No service, implementar as seguintes validações de negócio essenciais:
  - Validar se a `dataHoraPartida` está no futuro (recusar caronas agendadas no passado).
  - Validar se o veículo selecionado realmente pertence ao motorista autenticado.
3. Salvar a entidade `Carona` com o status inicial igual a `StatusCarona.ATIVA`.
4. **Boa Prática Crítica (Atomicidade):** Na mesma transação (utilize a anotação `@Transactional` no método do Service), criar automaticamente um registro na tabela `ParticipacaoCarona` vinculando o criador à carona com `tipo = MOTORISTA` e `status = CONFIRMADO`.

#### 🎯 Critérios de Aceite (O que entregar):
- Uma carona é persistida com sucesso e, ao inspecionar o banco, o motorista criador já aparece inserido como participante confirmado daquela viagem.

---

### 📋 Tarefa #08: Solicitar Vaga em uma Carona (Fluxo do Passageiro)
- **Nível Sugerido:** 🟡 Intermediário
- **Objetivo:** Permitir que usuários visualizem caronas e enviem solicitações de reserva de vaga que ficam em estado de aprovação.
- **Componentes:** `CaronaController.java`, `ParticipacaoCaronaRepository.java`

#### 🛠️ Passo a Passo:
1. Criar o endpoint `POST /api/caronas/{id}/solicitar`, onde `{id}` é o ID da carona desejada.
2. No Service, implementar as validações de segurança e negócio obrigatórias:
  - Buscar a carona e verificar se ela existe.
  - Validar se o status da carona é `ATIVA` e se o número de `vagasDisponiveis` é maior que zero.
  - Validar se o usuário logado que está solicitando **não** é o próprio motorista da carona.
  - Validar se o usuário já não possui alguma participação (pendente ou confirmada) cadastrada para esta mesma carona.
3. Se passar nas validações, criar e salvar um registro em `ParticipacaoCarona` configurado com `tipo = PASSAGEIRO` e `status = PENDENTE`.
4. **Importante:** Não subtrair o número de vagas da carona neste momento.

#### 🎯 Critérios de Aceite (O que entregar):
- O passageiro consegue enviar a solicitação. O registro entra no banco como `PENDENTE`, aguardando a decisão do condutor.

---

### 📋 Tarefa #09: Endpoints de Aceite e Rejeição de Passageiros
- **Nível Sugerido:** 🔴 Avançado
- **Objetivo:** Dar autonomia ao motorista para gerenciar quem entra no seu veículo e atualizar as vagas em tempo real.
- **Componentes:** `CaronaController.java`, `CaronaService.java`

#### 🛠️ Passo a Passo:
1. Adicionar a anotação `@Transactional` nos métodos desta lógica no Service para evitar inconsistências no banco (Race Conditions).
2. Criar o endpoint `PUT /api/caronas/{idCarona}/participacoes/{idParticipacao}/aceitar`:
  - Validar se o usuário autenticado na requisição é o legítimo dono/motorista da carona indicada por `{idCarona}`.
  - Buscar o registro de `ParticipacaoCarona` correspondente a `{idParticipacao}` e alterar seu status para `CONFIRMADO`.
  - Recuperar a entidade `Carona` e decrementar uma vaga (`vagasDisponiveis--`).
  - Validar: se após a subtração as `vagasDisponiveis` chegarem a `0`, alterar automaticamente o status da carona para `LOTADA`. Salvar as alterações.
3. Criar o endpoint `PUT /api/caronas/{idCarona}/participacoes/{idParticipacao}/rejeitar`:
  - Validar a identidade do motorista.
  - Alterar o status da participação para `REJEITADO`. (Não altera a quantidade de vagas da carona).

#### 🎯 Critérios de Aceite (O que entregar):
- A quantidade de vagas disponíveis da carona só diminui efetivamente quando o motorista clica em "Aceitar".
- Quando a última vaga é preenchida, o status da carona muda automaticamente para `LOTADA`, bloqueando novos pedidos.

---

## 🎯 SPRINT 5 — Histórico e Refinamentos

### 📋 Tarefa #10: Histórico de Caronas do Usuário
- **Nível Sugerido:** 🟡 Intermediário
- **Objetivo:** Exibir para o usuário logado as caronas em que ele está envolvido, separando seus papéis.
- **Componentes:** `CaronaController.java` ou `UsuarioController.java`

#### 🛠️ Passo a Passo:
1. Criar endpoints para listagem de histórico (ex: `GET /api/usuarios/me/caronas`).
2. No repositório de `ParticipacaoCarona`, criar métodos de busca baseados no ID do usuário logado (ex: `findAllByUsuarioId(...)`).
3. Separar o retorno para o frontend em duas estruturas ou fornecer parâmetros de filtro:
  - Viagens onde o usuário participou como `MOTORISTA`.
  - Viagens onde o usuário participou (ou solicitou participação) como `PASSAGEIRO`.

#### 🎯 Critérios de Aceite (O que entregar):
- Chamadas ao endpoint retornam uma lista contendo as informações resumidas das caronas ligadas ao usuário autenticado atual.

---

### 📋 Tarefa #11: Tratamento de Erros Global (Exception Handler)
- **Nível Sugerido:** 🟡 Intermediário
- **Objetivo:** Capturar erros e exceções disparados pela aplicação e envelopá-los em mensagens JSON amigáveis e limpas.
- **Componentes:** `GlobalExceptionHandler.java`, pacote `exception/`

#### 🛠️ Passo a Passo:
1. Criar uma classe chamada `GlobalExceptionHandler` dentro do pacote `exception` e anotá-la com `@RestControllerAdvice`.
2. Criar classes de exceções personalizadas de tempo de execução que estendem `RuntimeException` (ex: `RegraDeNegocioException`, `RecursoNaoEncontradoException`).
3. Criar um método dentro do handler anotado com `@ExceptionHandler(MethodArgumentNotValidException.class)` para interceptar falhas nas validações de DTOs (`@Valid`). Formatar o retorno para listar de maneira inteligível apenas o nome do campo e a mensagem de erro correspondente.
4. Criar métodos para interceptar suas exceções customizadas, retornando status HTTP condizentes (ex: `400 Bad Request` para erros de negócio, `404 Not Found` para ids inexistentes).

#### 🎯 Critérios de Aceite (O que entregar):
- Enviar requisições inválidas ou causar erros propositais na API não deve estourar o *stacktrace* interno do Java no console do cliente. A API deve responder um JSON estruturado, por exemplo: `{"erro": "Vagas insuficientes", "status": 400}`.

---

## 🎯 SPRINT 6 — Finalização e Deploy

### 📋 Tarefa #12: Dockerfile e Docker Compose da Aplicação
- **Nível Sugerido:** 🔴 Avançado
- **Objetivo:** Empacotar o backend Java em um container Docker e criar a orquestração básica para subir o sistema e o banco juntos.
- **Componentes:** `Dockerfile`, `docker-compose.yml`

#### 🛠️ Passo a Passo:
1. Criar um arquivo chamado `Dockerfile` na raiz do projeto Java do backend.
2. Configurar o arquivo usando uma imagem base multi-estágio (multi-stage build) ou uma imagem simples do JDK (ex: `eclipse-temurin:21-jre-alpine` ou `amazoncorretto:21`). Copiar o arquivo `.jar` gerado pelo build (`target/*.jar` ou `build/libs/*.jar`) para dentro da imagem e definir o comando `ENTRYPOINT` para executar o jar.
3. Criar ou atualizar o arquivo `docker-compose.yml` na raiz:
  - Configurar o serviço do seu banco de dados (MySQL, PostgreSQL, etc.) com suas variáveis de ambiente de credenciais.
  - Configurar o serviço do backend (`app`), apontando o `build: .` para ler o Dockerfile.
  - Definir a propriedade `depends_on` no serviço do backend para garantir que ele só inicialize após o banco de dados estar pronto.
  - Ajustar as propriedades de conexão do `application.properties` da aplicação para apontar para o nome do serviço do banco definido no compose, em vez de `localhost`.

#### 🎯 Critérios de Aceite (O que entregar):
- Executar o comando `docker-compose up --build -d` no terminal deve compilar a aplicação, criar as imagens e colocar tanto o banco quanto a API Java rodando perfeitamente em segundo plano.

</details>

## Fundação existente

As primeiras etapas construíram a base da aplicação: API Java com Spring Boot, persistência PostgreSQL, autenticação, usuários e perfis, veículos, publicação e busca de caronas, solicitações de participação, histórico e tratamento de erros. A aplicação está organizada em camadas; consulte `ARCHITECTURE.md` e o código atual para detalhes.

## Sprint 7 — Fundação de Conta e Perfil

**Status:** Em andamento

**Objetivo:** Permitir que usuários confirmem o e-mail, recuperem o acesso e mantenham seus dados pessoais atualizados com segurança.

### S7-01 — Configurar o envio de e-mails no backend

**Área:** infra, backend · **Prioridade:** alta · **Estimativa:** M (6h) · **Desbloqueia:** `S7-02`, `S7-05`

**Motivação**
O envio de e-mails é necessário tanto para confirmar cadastros quanto para recuperar senhas. Um serviço compartilhado centraliza a configuração, os templates e o tratamento de falhas.

**Passo a passo**
- [x] Adicionar a dependência `spring-boot-starter-mail` no `pom.xml`
- [x] Criar conta gratuita no [Mailtrap](https://mailtrap.io/) (caixa de entrada falsa para desenvolvimento — nada é enviado para pessoas reais)
- [x] Configurar `spring.mail.host`, `port`, `username`, `password` no `application.properties` lendo de variáveis de ambiente (`${MAIL_HOST}` etc.)
- [x] Criar `.env.example` no repositório com as chaves **sem os valores**, e garantir que o `.env` real está no `.gitignore`
- [x] Criar `EmailService` com o método `enviar(String destinatario, String assunto, String corpoHtml)`
- [x] Criar os templates HTML de e-mail em `resources/templates/` (um para confirmação, um para redefinição de senha)
- [x] Anotar o método de envio com `@Async` para que a requisição HTTP não fique esperando o e-mail sair (e habilitar `@EnableAsync`)
- [x] Documentar no `README.md` como configurar o Mailtrap na máquina de cada pessoa

**Critérios de aceite**
- [x] Um teste manual dispara um e-mail e ele aparece na caixa do Mailtrap
- [ ] O histórico do Git não contém credenciais de e-mail
- [ ] Se o servidor de e-mail estiver fora do ar, a aplicação registra o erro no log e **não** derruba a requisição do usuário

**Como testar**
Suba a aplicação, chame o serviço por um teste `@SpringBootTest` ou por um endpoint temporário e confira a caixa do Mailtrap.

**Referências**
- [Sending Email — Spring Boot Reference](https://docs.spring.io/spring-boot/reference/io/email.html)
- [Guide to Spring Email — Baeldung](https://www.baeldung.com/spring-email)
- [Variáveis de ambiente no Spring Boot](https://docs.spring.io/spring-boot/reference/features/external-config.html)



### S7-02 — Backend: token de uso único para verificação de e-mail

**Área:** backend · **Prioridade:** alta · **Estimativa:** M (6h) · **Dependências:** `S7-01`

**Motivação**
O token enviado por e-mail comprova o acesso do usuário ao endereço cadastrado. Ele deve ser imprevisível, ter prazo de validade e ser aceito uma única vez.

**Passo a passo**
- [ ] Criar a entidade `TokenVerificacao` com os campos: `id`, `token` (String única), `usuario` (`@ManyToOne`), `tipo` (enum `CONFIRMACAO_EMAIL` / `RESET_SENHA`), `expiraEm` (`LocalDateTime`), `usadoEm` (nulo até ser usado)
- [ ] Criar a migração Flyway correspondente (`V7__cria_token_verificacao.sql`)
- [ ] Gerar o token com `UUID.randomUUID()` **ou** `SecureRandom` — nunca com `Math.random()` ou com o id do usuário
- [ ] Adicionar o campo `emailVerificado` (boolean, default `false`) na entidade `Usuario` + migração
- [ ] Criar `TokenVerificacaoRepository` com `findByTokenAndTipo(...)`
- [ ] Criar `TokenVerificacaoService` com `gerar(usuario, tipo)` e `validarEConsumir(token, tipo)`
- [ ] `validarEConsumir` deve lançar `RegraDeNegocioException` (a exceção que já existe no projeto) quando o token não existe, já expirou ou já foi usado
- [ ] Definir a validade: 24 horas para confirmação de e-mail, 30 minutos para redefinição de senha
- [ ] Escrever testes unitários para os 4 casos: token válido, inexistente, expirado, já usado

**Critérios de aceite**
- [ ] Tokens diferentes são gerados a cada solicitação
- [ ] Um token usado uma vez não funciona de novo
- [ ] Um token expirado retorna erro claro (HTTP 400 com mensagem amigável)
- [ ] Os 4 testes unitários passam

**Referências**
- [Spring Data JPA — Reference](https://docs.spring.io/spring-data/jpa/reference/index.html)
- [Migrações com Flyway no Spring Boot](https://docs.spring.io/spring-boot/how-to/data-initialization.html)
- [`SecureRandom` — documentação Java](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/security/SecureRandom.html)



### S7-03 — Backend: endpoints de confirmação de e-mail e bloqueio de login não verificado

**Área:** backend · **Prioridade:** alta · **Estimativa:** M (6h) · **Dependências:** `S7-02`

**Motivação**
Este fluxo conecta o cadastro à confirmação do endereço e condiciona a autenticação à verificação do e-mail.

**Passo a passo**
- [ ] No cadastro (`POST /api/auth/registrar`), gerar o token e disparar o e-mail com o link `${FRONTEND_URL}/confirmar-email?token=...`
- [ ] Criar `POST /api/auth/confirmar-email` recebendo `{ "token": "..." }`, validando e marcando `emailVerificado = true`
- [ ] Criar `POST /api/auth/reenviar-confirmacao` recebendo o e-mail (com limite de 1 reenvio a cada 2 minutos por usuário)
- [ ] Bloquear o login de quem tem `emailVerificado = false`, devolvendo **403** com um código de erro específico (ex.: `EMAIL_NAO_VERIFICADO`) para o frontend saber que tela mostrar
- [ ] **Cuidado com vazamento de dados:** `reenviar-confirmacao` deve responder a mesma mensagem tanto para e-mail existente quanto inexistente, senão vira uma forma de descobrir quem tem conta
- [ ] Atualizar a documentação da API (Swagger/OpenAPI ou o `README`)

**Critérios de aceite**
- [ ] Cadastro novo → e-mail chega no Mailtrap com link válido
- [ ] Clicar/chamar o endpoint com o token → conta verificada, login liberado
- [ ] Login antes de confirmar → 403 com o código `EMAIL_NAO_VERIFICADO`
- [ ] Token adulterado → 400 com mensagem amigável, sem stack trace vazando

**Referências**
- [Spring Security — Reference](https://docs.spring.io/spring-security/reference/index.html)
- [Bean Validation no Spring Boot](https://docs.spring.io/spring-boot/reference/io/validation.html)



### S7-04 — Frontend: telas de verificação de e-mail

**Área:** frontend · **Prioridade:** alta · **Estimativa:** M (6h) · **Dependências:** `S7-03`

**Motivação**
As telas orientam o usuário após o cadastro e oferecem uma forma de reenviar a mensagem de confirmação.

**Passo a passo**
- [ ] Tela pós-cadastro: "Enviamos um link para `email@ifmg.edu.br`. Confira sua caixa de entrada e o spam."
- [ ] Botão **Reenviar e-mail** com contador regressivo de 2 minutos (desabilitado enquanto conta)
- [ ] Rota `/confirmar-email` que lê o `token` da query string, chama a API e mostra três estados: carregando, sucesso (com botão "Ir para login") e erro (com opção de reenviar)
- [ ] No login, tratar o erro `EMAIL_NAO_VERIFICADO` mostrando um aviso com atalho para reenviar, em vez de "usuário ou senha inválidos"
- [ ] Garantir que as telas funcionam no celular (o cadastro vai acontecer muito no celular)

**Critérios de aceite**
- [ ] Fluxo completo funciona da tela de cadastro até o login liberado
- [ ] Os estados de carregamento e erro são exibidos de forma adequada
- [ ] Layout legível em tela de 360px de largura

**Referências**
- [Vue 3 — Guia oficial](https://vuejs.org/guide/introduction.html)
- [Vue Router — parâmetros de query](https://router.vuejs.org/guide/essentials/passing-props.html)
- [Interceptors do Axios](https://axios-http.com/docs/interceptors)



### S7-05 — Backend: fluxo "Esqueci minha senha"

**Área:** backend · **Prioridade:** alta · **Estimativa:** G (10h) · **Dependências:** `S7-02`

**Motivação**
O fluxo permite recuperar o acesso à conta sem suporte manual, com validação de token e substituição segura da senha.

**Passo a passo**
- [ ] `POST /api/auth/esqueci-senha` recebendo `{ "email": "..." }` — gera token do tipo `RESET_SENHA` (30 min) e envia o e-mail
- [ ] **Sempre responder 200 com a mesma mensagem**, exista ou não a conta (evita descobrir quem está cadastrado)
- [ ] `POST /api/auth/redefinir-senha` recebendo `{ "token": "...", "novaSenha": "..." }`
- [ ] Validar a força da nova senha (mínimo 8 caracteres; defina a regra com o time e documente)
- [ ] Gravar a senha com **BCrypt**, o mesmo encoder já usado no projeto
- [ ] Ao redefinir: invalidar o token e **revogar todos os `RefreshToken` do usuário** (quem redefiniu senha deve deslogar de todos os dispositivos)
- [ ] Limitar tentativas: no máximo 3 solicitações por e-mail a cada 15 minutos
- [ ] Testes: token válido, expirado, reutilizado, senha fraca

**Critérios de aceite**
- [ ] Solicitar com e-mail inexistente devolve a mesma resposta que com e-mail existente
- [ ] Após redefinir, a senha antiga não funciona mais e a nova funciona
- [ ] Sessões antigas (refresh tokens) deixam de funcionar após a redefinição
- [ ] Token de reset expira em 30 minutos

**Referências**
- [OWASP — Forgot Password Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Forgot_Password_Cheat_Sheet.html)
- [Spring Security — Password Storage / BCrypt](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html)



### S7-06 — Frontend: telas de recuperação de senha

**Área:** frontend · **Prioridade:** alta · **Estimativa:** M (6h) · **Dependências:** `S7-05`

**Passo a passo**
- [ ] Link "Esqueci minha senha" na tela de login
- [ ] Tela `/esqueci-senha`: campo de e-mail + mensagem neutra de confirmação após o envio
- [ ] Tela `/redefinir-senha?token=...`: campos "nova senha" e "confirmar nova senha", com indicador visual de força e validação de igualdade
- [ ] Tratar token inválido/expirado com mensagem clara e link para solicitar de novo
- [ ] Após sucesso, redirecionar para o login com um aviso "Senha alterada, faça login novamente"

**Critérios de aceite**
- [ ] Fluxo completo funciona ponta a ponta com o e-mail do Mailtrap
- [ ] Senhas diferentes nos dois campos bloqueiam o envio com mensagem clara
- [ ] Nenhum dado sensível fica na URL depois do redirecionamento

**Referências**
- [Vue 3 — formulários e `v-model`](https://vuejs.org/guide/essentials/forms.html)
- [Pinia — estado global](https://pinia.vuejs.org/introduction.html)



### S7-07 — Backend: atualização dos dados do perfil

**Área:** backend · **Prioridade:** media · **Estimativa:** M (6h)

**Motivação**
O usuário precisa conseguir manter seus dados de contato atualizados, especialmente o telefone usado para combinar a carona.

**Passo a passo**
- [ ] Criar `GET /api/usuarios/me` devolvendo os dados do usuário autenticado (via DTO, **nunca** a entidade)
- [ ] Criar `PUT /api/usuarios/me` aceitando apenas os campos editáveis: nome, telefone (e o que o time definir)
- [ ] **Não** permitir alterar e-mail, senha, papel ou id por este endpoint — se o DTO tiver esses campos, eles devem ser ignorados
- [ ] Validar os campos com Bean Validation (`@NotBlank`, `@Size`, `@Pattern` para telefone)
- [ ] Obter o identificador do usuário autenticado a partir do token JWT, nunca do corpo da requisição
- [ ] Teste: tentar editar o perfil de outro usuário deve retornar 403

**Critérios de aceite**
- [ ] `GET /api/usuarios/me` não devolve hash de senha nem campos internos
- [ ] Campos inválidos retornam 400 com a lista de erros por campo
- [ ] Tentativa de alterar e-mail/papel é ignorada ou rejeitada — nunca aplicada

**Referências**
- [Bean Validation no Spring Boot](https://docs.spring.io/spring-boot/reference/io/validation.html)
- [Spring Security — obter o usuário autenticado](https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html)



### S7-08 — Frontend: tela "Meu Perfil"

**Área:** frontend · **Prioridade:** media · **Estimativa:** M (6h) · **Dependências:** `S7-07`

**Passo a passo**
- [ ] Criar a rota `/perfil` protegida por guarda de navegação (só logado acessa)
- [ ] Carregar os dados de `GET /api/usuarios/me` e preencher o formulário
- [ ] Modo leitura + botão **Editar**, ou formulário sempre editável com botão **Salvar** — decidam e sigam o mesmo padrão no resto do app
- [ ] Mostrar feedback de sucesso e de erro (toast ou alerta na própria tela)
- [ ] Atualizar o nome exibido no cabeçalho após salvar (store do Pinia)
- [ ] Bloquear o botão Salvar enquanto a requisição está em andamento (evita clique duplo)

**Critérios de aceite**
- [ ] Recarregar a página mantém os dados atualizados
- [ ] Erro de validação do backend aparece no campo correspondente
- [ ] Não é possível salvar duas vezes com um clique duplo

**Referências**
- [Vue Router — guardas de navegação](https://router.vuejs.org/guide/advanced/navigation-guards.html)
- [Pinia — atualizando o estado](https://pinia.vuejs.org/core-concepts/state.html)



### S7-09 — Backend: upload da foto de perfil

**Área:** backend · **Prioridade:** media · **Estimativa:** G (10h)

**Motivação**
A foto de perfil ajuda os participantes a reconhecerem uns aos outros e contribui para a confiança durante a combinação da viagem.

**Passo a passo**
- [ ] Decidir com o time **onde** o arquivo fica: pasta local do servidor (mais simples, recomendado para o projeto) ou serviço externo. Registrar a decisão no `README`
- [ ] Criar `POST /api/usuarios/me/foto` recebendo `multipart/form-data`
- [ ] Validar **tipo real** do arquivo (JPEG/PNG — conferir os primeiros bytes, não só a extensão) e tamanho máximo (ex.: 2 MB, via `spring.servlet.multipart.max-file-size`)
- [ ] Gerar nome de arquivo aleatório (UUID) — **nunca** usar o nome enviado pelo usuário, para evitar path traversal
- [ ] Redimensionar a imagem para no máximo 512×512 antes de salvar (biblioteca [Thumbnailator](https://github.com/coobird/thumbnailator))
- [ ] Salvar apenas o caminho/URL no campo `fotoUrl` do `Usuario` + migração
- [ ] Criar `DELETE /api/usuarios/me/foto` para remover a foto
- [ ] Servir a imagem por uma rota estática ou por `GET /api/usuarios/{id}/foto`

**Critérios de aceite**
- [ ] Arquivo maior que o limite retorna 400 com mensagem clara (não 500)
- [ ] Arquivo `.exe` renomeado para `.jpg` é rejeitado
- [ ] Trocar a foto substitui a anterior e não deixa arquivo órfão no disco
- [ ] A URL retornada abre a imagem no navegador

**Referências**
- [OWASP — File Upload Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/File_Upload_Cheat_Sheet.html)
- [Spring — upload de arquivos com `MultipartFile`](https://docs.spring.io/spring-boot/reference/web/servlet.html)
- [Thumbnailator (redimensionamento)](https://github.com/coobird/thumbnailator)



### S7-10 — Frontend: avatar e upload com pré-visualização

**Área:** frontend · **Prioridade:** media · **Estimativa:** M (6h) · **Dependências:** `S7-09`

**Passo a passo**
- [ ] Componente `AvatarUsuario.vue` reutilizável: mostra a foto ou as iniciais do nome quando não houver foto
- [ ] Na tela de perfil, permitir selecionar arquivo e **pré-visualizar antes de enviar** (`URL.createObjectURL`)
- [ ] Validar tipo e tamanho no navegador antes de subir (economiza tempo do usuário; a validação do backend continua sendo a que vale)
- [ ] Barra ou spinner de progresso durante o envio
- [ ] Usar o avatar também no cabeçalho e nos cards de carona (motorista/passageiro)
- [ ] Botão para remover a foto

**Critérios de aceite**
- [ ] Pré-visualização aparece antes do envio
- [ ] Usuário sem foto vê as iniciais, nunca uma imagem quebrada
- [ ] Após o upload, a foto nova aparece em todos os lugares sem precisar recarregar a página

**Referências**
- [MDN — File API e `createObjectURL`](https://developer.mozilla.org/pt-BR/docs/Web/API/File_API/Using_files_from_web_applications)
- [Vue 3 — componentes e props](https://vuejs.org/guide/essentials/component-basics.html)



### S7-11 — Backend: exclusão de conta com anonimização (LGPD)

**Área:** backend · **Prioridade:** media · **Estimativa:** G (10h)

**Motivação**
A exclusão deve atender aos direitos do titular sem invalidar registros históricos necessários para os demais participantes. A anonimização preserva esses vínculos sem manter os dados pessoais identificáveis.

**Passo a passo**
- [ ] Discutir com o time (e registrar na issue) o que é anonimizar aqui: nome vira "Usuário removido", e-mail vira um valor único sem sentido, telefone/CNH/foto são apagados, `ativo = false`
- [ ] Adicionar os campos `ativo` (boolean) e `anonimizadoEm` (`LocalDateTime`) em `Usuario` + migração
- [ ] Criar `DELETE /api/usuarios/me` exigindo **confirmação de senha** no corpo da requisição
- [ ] Regra de negócio: não permitir excluir com carona **ativa** como motorista ou participação confirmada como passageiro — retornar erro explicando o motivo
- [ ] Ao anonimizar: revogar todos os refresh tokens, apagar o arquivo da foto, manter as caronas passadas apontando para o usuário anonimizado
- [ ] Bloquear login de conta com `ativo = false`
- [ ] Testes: exclusão feliz, senha errada, usuário com carona ativa

**Critérios de aceite**
- [ ] Após a exclusão, nenhum dado pessoal do usuário aparece em qualquer endpoint da API
- [ ] Caronas antigas continuam existindo e não quebram nenhuma tela
- [ ] O e-mail anonimizado não colide com outro usuário nem permite recadastro fantasma (defina a regra e documente)
- [ ] Senha errada impede a exclusão

**Referências**
- [LGPD — Lei nº 13.709/2018, arts. 17 e 18](https://www.planalto.gov.br/ccivil_03/_ato2015-2018/2018/lei/l13709.htm)
- [Guia da ANPD sobre direitos do titular](https://www.gov.br/anpd/pt-br)



### S7-12 — Frontend: fluxo de exclusão de conta

**Área:** frontend · **Prioridade:** media · **Estimativa:** P (3h) · **Dependências:** `S7-11`

**Passo a passo**
- [ ] Seção "Zona de perigo" no fim da tela de perfil, visualmente separada (borda/texto em vermelho)
- [ ] Modal de confirmação explicando **o que acontece**: dados anonimizados, histórico mantido, ação irreversível
- [ ] Exigir a senha para confirmar
- [ ] Após sucesso: limpar o estado local, remover os tokens e redirecionar para a home com mensagem de despedida
- [ ] Tratar o erro "existe carona ativa" com mensagem orientando o que fazer antes

**Critérios de aceite**
- [ ] É impossível excluir por acidente (precisa de dois passos + senha)
- [ ] Depois de excluir, o usuário não continua "logado" em nenhuma tela

**Referências**
- [Vue 3 — modais com `<Teleport>`](https://vuejs.org/guide/built-ins/teleport.html)



## Sprint 8 — Segurança, Moderação e Controle de Acesso

**Status:** Planejada

**Objetivo:** Restringir o uso à comunidade institucional, proteger as funções administrativas e viabilizar a moderação de motoristas.

### S8-01 — Backend: papel de administrador e proteção das rotas `/api/admin/**`

**Área:** backend · **Prioridade:** alta · **Estimativa:** M (6h) · **Desbloqueia:** `S8-02`, `S8-03`

**Motivação**
Um papel administrativo explícito é necessário para restringir operações de gestão e proteger as rotas do painel.

**Passo a passo**
- [ ] Criar o enum `PapelUsuario { USUARIO, ADMIN }` e o campo `papel` em `Usuario` (default `USUARIO`) + migração
- [ ] Incluir o papel como *claim* no JWT gerado no login
- [ ] Configurar o `SecurityFilterChain`: `requestMatchers("/api/admin/**").hasRole("ADMIN")`
- [ ] Criar o primeiro admin por migração/seed (e-mail definido em variável de ambiente) — **não** criar endpoint público de "virar admin"
- [ ] Garantir que quem não é admin recebe **403**, e não 500 nem 200 com lista vazia
- [ ] Teste de integração: usuário comum chamando `/api/admin/**` recebe 403

**Critérios de aceite**
- [ ] Token de usuário comum não acessa nenhuma rota `/api/admin/**`
- [ ] Token de admin acessa normalmente
- [ ] O papel aparece no JWT e é lido pelo backend a cada requisição (não confie no que o frontend manda)

**Referências**
- [Spring Security — autorização por requisição](https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html)
- [Spring Security — Method Security (`@PreAuthorize`)](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html)



### S8-02 — Backend: endpoints do painel do administrador

**Área:** backend · **Prioridade:** alta · **Estimativa:** G (10h) · **Dependências:** `S8-01`

**Passo a passo**
- [ ] `GET /api/admin/usuarios` com **paginação** (`Pageable`), busca por nome/e-mail e filtro por papel/status
- [ ] `GET /api/admin/motoristas/pendentes` listando quem enviou CNH e aguarda aprovação
- [ ] `GET /api/admin/metricas` com números simples: total de usuários, motoristas aprovados, caronas publicadas no mês, caronas concluídas
- [ ] Criar DTOs específicos do admin (podem mostrar mais campos que os DTOs públicos, mas **nunca** hash de senha)
- [ ] Usar `Page<T>` do Spring Data e devolver `content`, `totalElements`, `totalPages`
- [ ] Escrever as consultas de métrica com `@Query` e testar com dados de exemplo

**Critérios de aceite**
- [ ] Listagem responde em página de 20 itens por padrão
- [ ] Busca por parte do nome ou do e-mail funciona
- [ ] As métricas batem com o que existe no banco (conferir manualmente com SQL)

**Referências**
- [Spring Data JPA — paginação e ordenação](https://docs.spring.io/spring-data/jpa/reference/repositories/query-methods-details.html)
- [Spring Data JPA — `@Query`](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html)



### S8-03 — Frontend: estrutura do painel administrativo

**Área:** frontend · **Prioridade:** alta · **Estimativa:** M (6h) · **Dependências:** `S8-01`

**Passo a passo**
- [ ] Criar o grupo de rotas `/admin` com layout próprio (menu lateral + cabeçalho)
- [ ] Guarda de navegação que redireciona quem não é admin para a home (o backend continua sendo a defesa real; isso é só experiência de uso)
- [ ] Guardar o papel do usuário na store do Pinia, lido do token/`/me`
- [ ] Menu com os itens: Visão geral, Usuários, Motoristas pendentes (a área de CNH poderá ser integrada a este menu)
- [ ] Mostrar o link "Painel" no cabeçalho apenas para admins
- [ ] Tela de erro 403 amigável

**Critérios de aceite**
- [ ] Usuário comum digitando `/admin` na URL é redirecionado
- [ ] O menu não aparece para quem não é admin
- [ ] O layout do painel é visivelmente diferente do app do usuário comum

**Referências**
- [Vue Router — rotas aninhadas e layouts](https://router.vuejs.org/guide/essentials/nested-routes.html)
- [Vue Router — guardas de navegação](https://router.vuejs.org/guide/advanced/navigation-guards.html)



### S8-04 — Frontend: telas de visão geral e listagem de usuários

**Área:** frontend · **Prioridade:** alta · **Estimativa:** G (10h) · **Dependências:** `S8-02`, `S8-03`

**Passo a passo**
- [ ] Tela "Visão geral" com 4 cartões de métrica (total de usuários, motoristas aprovados, caronas no mês, caronas concluídas)
- [ ] Tela "Usuários" com tabela paginada, campo de busca (com *debounce* de ~400ms) e filtro por status
- [ ] Estados de carregando (esqueleto ou spinner), vazio ("nenhum usuário encontrado") e erro com botão "tentar de novo"
- [ ] Manter página e busca na URL (`/admin/usuarios?pagina=2&busca=maria`) para poder compartilhar o link
- [ ] Tabela com rolagem horizontal no celular

**Critérios de aceite**
- [ ] Paginação funciona e mostra a página atual
- [ ] Busca não dispara uma requisição por tecla digitada
- [ ] Os estados de carregamento, vazio e erro foram validados

**Referências**
- [Vue 3 — listas e `v-for`](https://vuejs.org/guide/essentials/list.html)
- [Debounce explicado (MDN, `setTimeout`)](https://developer.mozilla.org/pt-BR/docs/Web/API/Window/setTimeout)



### S8-05 — Backend: validação estrita de domínio institucional

**Área:** backend · **Prioridade:** alta · **Estimativa:** M (6h)

**Motivação**
Restringir o cadastro a domínios institucionais mantém a plataforma alinhada ao público previsto e reforça a confiança entre participantes.

**Passo a passo**
- [ ] Criar a anotação de validação customizada `@EmailInstitucional` com seu `ConstraintValidator`
- [ ] Ler a lista de domínios permitidos de uma propriedade de configuração (`arreda.dominios-permitidos=ifmg.edu.br,aluno.ifmg.edu.br`) — **não** deixe fixo no código, a coordenação pode pedir mais domínios
- [ ] Comparar sempre em minúsculas e validar o formato do e-mail antes do domínio
- [ ] Aplicar a anotação no DTO de cadastro
- [ ] Mensagem de erro clara: "Use seu e-mail institucional do IFMG (@ifmg.edu.br ou @aluno.ifmg.edu.br)"
- [ ] Aplicar a mesma regra em qualquer outro ponto que aceite e-mail (convites, se existirem)
- [ ] Testes: domínio válido, domínio inválido, maiúsculas/minúsculas, subdomínio parecido (`ifmg.edu.br.fake.com` deve ser **rejeitado**)

**Critérios de aceite**
- [ ] `joao@gmail.com` é rejeitado com 400 e mensagem amigável
- [ ] `Joao@ALUNO.IFMG.EDU.BR` é aceito
- [ ] `fulano@ifmg.edu.br.invasor.com` é rejeitado
- [ ] Trocar a lista de domínios no `application.properties` muda o comportamento sem recompilar

**Referências**
- [Bean Validation — criando validações customizadas (Baeldung)](https://www.baeldung.com/spring-mvc-custom-validator)
- [Spring Boot — `@ConfigurationProperties`](https://docs.spring.io/spring-boot/reference/features/external-config.html)



### S8-06 — Frontend: feedback de e-mail institucional no cadastro

**Área:** frontend · **Prioridade:** media · **Estimativa:** P (3h) · **Dependências:** `S8-05`

**Passo a passo**
- [ ] Texto de apoio abaixo do campo de e-mail: "Use seu e-mail do IFMG"
- [ ] Validação no próprio formulário (mesma regra do backend) mostrando o erro assim que o campo perde o foco
- [ ] Exibir a mensagem vinda do backend quando o cadastro for rejeitado
- [ ] Explicar na tela por que a restrição existe ("O Arreda é exclusivo da comunidade do IFMG")

**Critérios de aceite**
- [ ] Erro aparece antes de enviar o formulário
- [ ] A mensagem do backend também é exibida, caso a validação local passe por engano

**Referências**
- [Vue 3 — validação de formulários](https://vuejs.org/guide/essentials/forms.html)



### S8-07 — Backend: envio da CNH e máquina de estados de aprovação

**Área:** backend · **Prioridade:** alta · **Estimativa:** G (10h)

**Motivação**
A análise da CNH permite verificar os dados do motorista antes da publicação de caronas e exige armazenamento protegido por se tratar de documento sensível.

**Passo a passo**
- [ ] Adicionar em `PerfilMotorista`: `statusCnh` (enum `PENDENTE`, `APROVADA`, `REJEITADA`), `motivoRejeicao`, `arquivoCnh`, `validadeCnh`, `avaliadoPor`, `avaliadoEm` + migração
- [ ] `POST /api/motoristas/me/cnh` com upload do documento (reaproveitar as validações de arquivo da `S7-09`)
- [ ] Guardar o arquivo em diretório **não público** — a CNH é dado sensível e não pode ser acessível por URL adivinhável
- [ ] Ao enviar (ou reenviar após rejeição), o status volta para `PENDENTE`
- [ ] Regra de negócio: **só motorista com `statusCnh = APROVADA` pode publicar carona** — aplicar no serviço de criação de carona
- [ ] `GET /api/motoristas/me/cnh` devolvendo o status e o motivo da rejeição
- [ ] Testes: publicar carona com CNH pendente deve falhar com mensagem clara

**Critérios de aceite**
- [ ] Motorista com CNH pendente recebe erro explicativo ao tentar publicar carona
- [ ] O arquivo da CNH não é acessível sem autenticação de admin
- [ ] Reenviar documento após rejeição volta o status para pendente e limpa o motivo anterior

**Referências**
- [OWASP — File Upload Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/File_Upload_Cheat_Sheet.html)
- [LGPD — tratamento de dados sensíveis](https://www.planalto.gov.br/ccivil_03/_ato2015-2018/2018/lei/l13709.htm)



### S8-08 — Backend: endpoints de moderação (aprovar / rejeitar CNH)

**Área:** backend · **Prioridade:** alta · **Estimativa:** M (6h) · **Dependências:** `S8-01`, `S8-07`

**Passo a passo**
- [ ] `POST /api/admin/cnh/{perfilMotoristaId}/aprovar` recebendo a data de validade conferida
- [ ] `POST /api/admin/cnh/{perfilMotoristaId}/rejeitar` recebendo `{ "motivo": "..." }` (motivo obrigatório)
- [ ] `GET /api/admin/cnh/{perfilMotoristaId}/arquivo` servindo o documento **apenas para admin**
- [ ] Registrar quem avaliou e quando (auditoria — importante para o relatório do projeto)
- [ ] Disparar e-mail ao motorista informando aprovação ou rejeição (reaproveitar o `EmailService` da `S7-01`)
- [ ] Não permitir aprovar uma CNH com validade vencida

**Critérios de aceite**
- [ ] Aprovar libera imediatamente a publicação de caronas para aquele motorista
- [ ] Rejeitar sem motivo retorna 400
- [ ] O motorista recebe e-mail nos dois casos
- [ ] O banco registra `avaliadoPor` e `avaliadoEm`

**Referências**
- [Spring — retornando arquivos (`ResponseEntity<Resource>`)](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/responseentity.html)



### S8-09 — Frontend: tela de moderação de CNH no painel

**Área:** frontend · **Prioridade:** alta · **Estimativa:** G (10h) · **Dependências:** `S8-03`, `S8-08`

**Passo a passo**
- [ ] Rota `/admin/cnh` dentro do layout do painel (integrado ao layout do painel administrativo)
- [ ] Lista de solicitações pendentes com nome do motorista, data de envio e prévia do documento
- [ ] Tela de detalhe: documento em tamanho legível (com opção de zoom/abrir em nova aba), campo de validade, botões **Aprovar** e **Rejeitar**
- [ ] Modal de rejeição exigindo o motivo, com opções prontas ("documento ilegível", "CNH vencida", "dados não conferem") e campo livre
- [ ] Após a ação, remover o item da lista e mostrar confirmação
- [ ] Estado vazio: "Nenhuma CNH aguardando análise"

**Critérios de aceite**
- [ ] O documento é legível na tela do admin
- [ ] Rejeitar sem escolher motivo é impossível
- [ ] A lista se atualiza sozinha após aprovar/rejeitar

**Referências**
- [Vue 3 — componentes e eventos](https://vuejs.org/guide/components/events.html)
- [MDN — exibir arquivos protegidos com `blob` e `fetch`](https://developer.mozilla.org/pt-BR/docs/Web/API/Fetch_API/Using_Fetch)



### S8-10 — Frontend: status da CNH visível para o motorista

**Área:** frontend · **Prioridade:** media · **Estimativa:** P (3h) · **Dependências:** `S8-07`

**Passo a passo**
- [ ] Cartão de status no perfil: **Pendente** (amarelo), **Aprovada** (verde), **Rejeitada** (vermelho, com o motivo)
- [ ] Botão para enviar/reenviar o documento
- [ ] Aviso na tela de publicar carona quando a CNH ainda não está aprovada, com atalho para o envio
- [ ] Texto explicando o prazo esperado de análise (combinem com a coordenação, ex.: até 3 dias úteis)

**Critérios de aceite**
- [ ] O motorista entende, sem perguntar para ninguém, em que etapa está e o que fazer
- [ ] O motivo da rejeição aparece na íntegra

**Referências**
- [Vue 3 — renderização condicional](https://vuejs.org/guide/essentials/conditional.html)




## Sprint 9 — Localização, Mapas e Precificação

**Status:** Planejada

**Objetivo:** Estruturar endereços, calcular e exibir rotas e apresentar uma sugestão transparente de rateio de combustível.

### S9-01 — Infra: projeto no Google Cloud, chaves e restrições

**Área:** infra · **Prioridade:** alta · **Estimativa:** M (6h) · **Desbloqueia:** todas as demais issues da Sprint 9

**Passo a passo**
- [ ] Criar projeto no Google Cloud com a conta institucional e ativar o faturamento (há crédito gratuito inicial)
- [ ] Habilitar as APIs: **Maps JavaScript API**, **Places API (New)** e **Routes API**
- [ ] Criar duas chaves: uma para o **frontend** (restrita por referenciador HTTP: `localhost` e o domínio do projeto) e uma para o **backend** (restrita por IP e por API)
- [ ] Definir **orçamento com alerta** por e-mail (ex.: alerta em R$ 20) em `Billing → Budgets & alerts`
- [ ] Colocar as chaves em `.env` (frontend: `VITE_GOOGLE_MAPS_KEY`) e em variável de ambiente no backend — **nunca** no código
- [ ] Conferir que `.env` está no `.gitignore` e atualizar o `.env.example`
- [ ] Documentar no `README` as etapas para que outro integrante configure a própria chave
- [ ] Anotar no `README` os limites de cota e o custo por 1.000 requisições de cada API usada

**Critérios de aceite**
- [ ] Um "hello world" de mapa renderiza no frontend usando a chave do `.env`
- [ ] A chave do frontend não funciona a partir de outro domínio
- [ ] Existe alerta de orçamento configurado e alguém do time recebe o e-mail
- [ ] Nenhuma chave aparece no histórico do Git

**Referências**
- [Boas práticas de segurança para chaves de API do Google Maps](https://developers.google.com/maps/api-security-best-practices)
- [Variáveis de ambiente no Vite](https://vite.dev/guide/env-and-mode.html)
- [Google Maps Platform — como funciona a cobrança](https://developers.google.com/maps/billing-and-pricing/pricing)



### S9-02 — Frontend: componente reutilizável de autocomplete de endereço

**Área:** frontend · **Prioridade:** alta · **Estimativa:** G (10h) · **Dependências:** `S9-01`

**Passo a passo**
- [ ] Carregar a biblioteca do Google Maps de forma assíncrona, uma única vez na aplicação
- [ ] Criar `CampoEndereco.vue` usando o **Place Autocomplete Widget** (`PlaceAutocompleteElement`)
- [ ] Restringir as sugestões ao Brasil (`componentRestrictions: { country: 'br' }`) e, se possível, enviesar para a região do campus
- [ ] Emitir para o componente pai um objeto padronizado: `{ enderecoFormatado, placeId, latitude, longitude }`
- [ ] Tratar o caso de o usuário digitar e **não** escolher nenhuma sugestão (não deixar seguir com endereço inválido)
- [ ] Permitir valor inicial (para edição de carona já criada)
- [ ] Testar no celular: o dropdown de sugestões não pode ficar escondido atrás do teclado

**Critérios de aceite**
- [ ] Digitar "IFMG" sugere endereços reais e selecionáveis
- [ ] O componente devolve latitude e longitude corretas (conferir no Google Maps)
- [ ] Digitar sem selecionar sugestão impede o envio do formulário

**Referências**
- [Place Autocomplete Widget — Maps JavaScript API](https://developers.google.com/maps/documentation/javascript/place-autocomplete-new)
- [Vue 3 — `defineEmits` e comunicação com o pai](https://vuejs.org/guide/components/events.html)



### S9-03 — Frontend: integrar o autocomplete nos formulários de carona

**Área:** frontend · **Prioridade:** alta · **Estimativa:** M (6h) · **Dependências:** `S9-02`, `S9-04`

**Passo a passo**
- [ ] Substituir os campos de texto de origem e destino no formulário de publicar carona
- [ ] Fazer o mesmo na busca de caronas (se houver filtro por local)
- [ ] Enviar os novos campos (`placeId`, `latitude`, `longitude`) para a API
- [ ] Garantir compatibilidade: caronas antigas, sem coordenadas, continuam aparecendo normalmente nas listagens
- [ ] Atalho útil: botão "Usar o campus IFMG" que preenche o endereço da instituição com um clique

**Critérios de aceite**
- [ ] Publicar uma carona nova grava latitude/longitude no banco
- [ ] Caronas antigas não quebram nenhuma tela
- [ ] O botão de atalho do campus funciona

**Referências**
- [Vue 3 — componentes com `v-model`](https://vuejs.org/guide/components/v-model.html)



### S9-04 — Backend: persistir endereços estruturados

**Área:** backend · **Prioridade:** alta · **Estimativa:** G (10h) · **Dependências:** `S9-01`

**Passo a passo**
- [ ] Criar a classe `Endereco` como `@Embeddable` com: `enderecoFormatado`, `placeId`, `latitude`, `longitude`
- [ ] Substituir os campos texto de origem/destino em `Carona` por dois `@Embedded` (com `@AttributeOverrides` para nomear as colunas)
- [ ] Escrever a migração Flyway preservando os dados existentes (copiar o texto antigo para `enderecoFormatado`, deixando as coordenadas nulas)
- [ ] Atualizar os DTOs de criação e de resposta da carona
- [ ] Validar no backend: se vier latitude, deve vir longitude, e ambas dentro de faixas válidas (-90..90 / -180..180)
- [ ] Manter os endpoints funcionando para caronas sem coordenadas (campos opcionais)
- [ ] Atualizar os testes existentes de carona

**Critérios de aceite**
- [ ] A migração roda em um banco com dados sem perder nenhuma carona
- [ ] A API aceita e devolve o endereço estruturado
- [ ] Coordenadas fora de faixa retornam 400

**Referências**
- [JPA — `@Embeddable` e `@Embedded` (Baeldung)](https://www.baeldung.com/jpa-embedded-embeddable)
- [Migrações Flyway no Spring Boot](https://docs.spring.io/spring-boot/how-to/data-initialization.html)



### S9-05 — Backend: serviço de cálculo de rota (Routes API)

**Área:** backend · **Prioridade:** alta · **Estimativa:** G (10h) · **Dependências:** `S9-01`, `S9-04`

**Motivação**
Distância e duração são necessárias para exibir a rota e calcular o rateio. O cálculo no backend protege a chave de acesso e permite reutilizar os resultados armazenados.

**Passo a passo**
- [ ] Criar `RotaService` que chama a **Routes API** (`computeRoutes`) usando `RestClient`/`WebClient`
- [ ] Enviar origem e destino por coordenadas e pedir apenas os campos necessários (`routes.duration`, `routes.distanceMeters`, `routes.polyline`) via `X-Goog-FieldMask` — pedir menos campos custa menos
- [ ] Guardar em `Carona` os campos `distanciaMetros`, `duracaoSegundos` e `polylineCodificada` + migração
- [ ] Calcular **uma vez**, no momento de criar/editar a carona, e reaproveitar o valor salvo (cache no banco)
- [ ] Tratar falhas da API externa: se der erro ou estourar o tempo limite, a carona ainda deve ser publicada, apenas sem rota
- [ ] Configurar `timeout` de 5 segundos na chamada HTTP
- [ ] Nunca expor a chave do backend em nenhuma resposta

**Critérios de aceite**
- [ ] Publicar carona entre dois pontos conhecidos grava distância e duração coerentes com o Google Maps
- [ ] Com a API fora do ar (simule com uma chave inválida), a publicação continua funcionando
- [ ] A mesma carona não dispara uma nova chamada à API a cada consulta

**Referências**
- [Routes API — visão geral](https://developers.google.com/maps/documentation/routes)
- [Routes API — `computeRoutes` e Field Mask](https://developers.google.com/maps/documentation/routes/compute_route_directions)
- [Spring — `RestClient`](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html)



### S9-06 — Frontend: mapa com a rota desenhada

**Área:** frontend · **Prioridade:** alta · **Estimativa:** G (10h) · **Dependências:** `S9-05`

**Passo a passo**
- [ ] Componente `MapaRota.vue` que recebe origem, destino e a *polyline* vinda da API
- [ ] Renderizar o mapa com marcadores de origem e destino e a linha do trajeto
- [ ] Ajustar o zoom automaticamente para caber a rota inteira (`fitBounds`)
- [ ] Exibir distância e tempo estimado em texto legível ("18 km · cerca de 27 min")
- [ ] Estado de carregamento e fallback: sem coordenadas, mostrar só os endereços em texto, sem quebrar a tela
- [ ] Usar o componente na tela de detalhes da carona
- [ ] Conferir que o mapa não estoura a largura no celular

**Critérios de aceite**
- [ ] A rota aparece corretamente para uma carona nova
- [ ] Carona antiga (sem coordenadas) mostra o fallback textual
- [ ] O mapa é usável no celular

**Referências**
- [Maps JavaScript API — primeiros passos](https://developers.google.com/maps/documentation/javascript/overview)
- [Decodificação de polyline (`geometry.encoding`)](https://developers.google.com/maps/documentation/javascript/reference/geometry)



### S9-07 — Backend: algoritmo de sugestão de rateio de combustível

**Área:** backend · **Prioridade:** media · **Estimativa:** M (6h) · **Dependências:** `S9-05`

**Motivação**
Uma estimativa calculada e documentada fornece um ponto de partida transparente para o rateio dos custos da viagem.

**Passo a passo**
- [ ] Definir a fórmula com o time e **documentá-la na issue e no README**. Sugestão inicial:
  `custo = (distância_km / consumo_medio_km_por_litro) × preço_do_litro`
  `valor_por_passageiro = custo / (passageiros + 1)` (o motorista também paga a parte dele)
- [ ] Deixar `consumo_medio` e `preço_do_litro` configuráveis (`application.properties`), não fixos no código
- [ ] Arredondar para múltiplos de R$ 0,50 (valor "de bolso", mais fácil de pagar)
- [ ] Definir um teto de segurança e um piso (ex.: nunca sugerir menos de R$ 1,00)
- [ ] Deixar claro na resposta da API que é **sugestão**, não cobrança: o campo pode se chamar `valorSugerido`
- [ ] Escrever testes com pelo menos 4 cenários de distância e lotação

**Critérios de aceite**
- [ ] Trajeto de 20 km com 3 passageiros gera um valor plausível e explicável
- [ ] Alterar o preço do litro na configuração muda o resultado
- [ ] A fórmula está documentada em português no README (vai para o relatório final)

**Referências**
- [Spring Boot — `@ConfigurationProperties`](https://docs.spring.io/spring-boot/reference/features/external-config.html)
- [`BigDecimal` para valores monetários — por que não usar `double`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/math/BigDecimal.html)



### S9-08 — Frontend: exibir o valor sugerido na publicação da carona

**Área:** frontend · **Prioridade:** media · **Estimativa:** M (6h) · **Dependências:** `S9-07`

**Passo a passo**
- [ ] Ao preencher origem e destino, buscar e mostrar a sugestão: "Sugestão de rateio: R$ 7,50 por passageiro"
- [ ] Permitir que o motorista **edite** o valor (a sugestão não é obrigatória)
- [ ] Explicar em um "?" ou tooltip como o valor foi calculado
- [ ] Exibir o valor definido no card da carona para os passageiros
- [ ] Deixar explícito que é ajuda de custo, não transporte remunerado (importante juridicamente para o projeto)

**Critérios de aceite**
- [ ] A sugestão aparece sem o usuário precisar clicar em nada
- [ ] O motorista consegue alterar o valor e o valor alterado é o que aparece para o passageiro
- [ ] A explicação do cálculo está acessível na tela

**Referências**
- [Vue 3 — `computed` e reatividade](https://vuejs.org/guide/essentials/computed.html)
- [`Intl.NumberFormat` para formatar reais](https://developer.mozilla.org/pt-BR/docs/Web/JavaScript/Reference/Global_Objects/Intl/NumberFormat)



### S9-09 — Backend: testes automatizados dos serviços de mapa e preço

**Área:** backend · **Prioridade:** media · **Estimativa:** M (6h) · **Dependências:** `S9-05`, `S9-07`

**Motivação**
Simular as respostas da API externa torna os testes reproduzíveis, evita consumo de cota e permite validar os cenários de falha sem conexão.

**Passo a passo**
- [ ] Testes unitários do cálculo de preço cobrindo distância zero, distância longa, lotação mínima e máxima
- [ ] Teste do `RotaService` com a API externa simulada (Mockito ou `MockRestServiceServer`)
- [ ] Teste do comportamento de falha: API indisponível → carona publicada sem rota
- [ ] Rodar toda a suíte com `mvn test` e garantir que passa sem internet
- [ ] Documentar no README como rodar os testes

**Critérios de aceite**
- [ ] `mvn test` passa com o wi-fi desligado
- [ ] Nenhum teste consome cota real da API do Google
- [ ] Os cenários de falha estão cobertos

**Referências**
- [Testes no Spring Boot](https://docs.spring.io/spring-boot/reference/testing/index.html)
- [Mockito](https://site.mockito.org/) · [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)



### S9-10 — Frontend: tratamento de erros e limites de cota dos mapas

**Área:** frontend · **Prioridade:** media · **Estimativa:** M (6h) · **Dependências:** `S9-02`, `S9-06`

**Passo a passo**
- [ ] Se a biblioteca do Google não carregar (offline, cota estourada, chave inválida), mostrar aviso e liberar **digitação manual do endereço**
- [ ] Nunca deixar a tela travada em "carregando" para sempre — usar tempo limite
- [ ] Registrar o erro no console com uma mensagem que ajude o time a diagnosticar
- [ ] Validar o comportamento da aplicação com uma chave inválida
- [ ] Documentar no README os erros mais comuns do Google Maps e o que significam (`RefererNotAllowedMapError`, `ApiNotActivatedMapError`, `OverQuotaMapError`)

**Critérios de aceite**
- [ ] Com chave inválida, o usuário ainda consegue publicar uma carona digitando o endereço
- [ ] Nenhuma tela fica em carregamento infinito
- [ ] O README explica os três erros mais comuns

**Referências**
- [Erros do Maps JavaScript API](https://developers.google.com/maps/documentation/javascript/error-messages)



## Sprint 10 — Comunicação e Segurança Avançada

**Status:** Planejada

**Objetivo:** Oferecer comunicação em tempo real e recursos de emergência durante as viagens, além de preparar a entrega acadêmica.

### S10-01 — Backend: infraestrutura WebSocket com STOMP e autenticação

**Área:** backend · **Prioridade:** alta · **Estimativa:** G (10h) · **Desbloqueia:** `S10-02`, `S10-03`

**Motivação**
O chat exige comunicação bidirecional em tempo real. WebSocket mantém a conexão aberta, enquanto STOMP organiza o envio e o recebimento das mensagens.

**Passo a passo**
- [ ] Adicionar `spring-boot-starter-websocket` ao `pom.xml`
- [ ] Criar a classe de configuração com `@EnableWebSocketMessageBroker`, registrando o endpoint `/ws` e o broker simples (`/topic`, `/queue`)
- [ ] Configurar o CORS do WebSocket para o domínio do frontend (o CORS do HTTP **não** vale aqui)
- [ ] Autenticar o handshake: ler o JWT enviado pelo cliente (header STOMP `Authorization` no CONNECT) e associar o usuário à sessão
- [ ] Recusar a conexão quando o token for inválido ou ausente
- [ ] Criar um endpoint de teste (`/app/ping` → `/topic/pong`) só para validar a infraestrutura, e removê-lo depois
- [ ] Documentar no README como testar o WebSocket sem frontend (ex.: extensão de navegador ou `wscat`)

**Critérios de aceite**
- [ ] Cliente com token válido conecta; sem token, é recusado
- [ ] O `ping/pong` de teste funciona entre dois navegadores abertos
- [ ] A configuração não abre o WebSocket para qualquer origem (`*`)

**Referências**
- [Spring Framework — STOMP sobre WebSocket](https://docs.spring.io/spring-framework/reference/web/websocket/stomp.html)
- [Spring — autenticação em STOMP](https://docs.spring.io/spring-framework/reference/web/websocket/stomp/authentication.html)
- [Guia "Using WebSocket to build an interactive web application" (Spring)](https://spring.io/guides/gs/messaging-stomp-websocket)



### S10-02 — Backend: mensagens, sala por carona e histórico

**Área:** backend · **Prioridade:** alta · **Estimativa:** G (10h) · **Dependências:** `S10-01`

**Passo a passo**
- [ ] Criar a entidade `MensagemChat`: `id`, `carona` (`@ManyToOne`), `autor`, `conteudo`, `enviadaEm` + migração
- [ ] Definir o destino STOMP por carona: `/topic/caronas/{caronaId}/chat`
- [ ] Criar o `@MessageMapping` que recebe a mensagem, valida, persiste e reenvia para o tópico
- [ ] **Regra de acesso (a parte mais importante):** só participam do chat o motorista e os passageiros com `ParticipacaoCarona` **confirmada**. Validar isso no servidor a cada mensagem — nunca confiar no frontend
- [ ] Validar o conteúdo: não vazio, máximo 500 caracteres, e escapar HTML ao exibir (prevenir XSS)
- [ ] Criar `GET /api/caronas/{id}/mensagens` com histórico paginado (mais recentes primeiro)
- [ ] Bloquear o envio em caronas com status `CANCELADA` ou `CONCLUIDA` (histórico continua visível por, digamos, 7 dias)
- [ ] Teste: usuário não participante tentando enviar/ler mensagem é rejeitado

**Critérios de aceite**
- [ ] Usuário que não participa da carona não lê nem envia nada naquele tópico
- [ ] Mensagens ficam salvas e voltam ao recarregar a página
- [ ] Mensagem vazia ou gigante é rejeitada
- [ ] Chat de carona cancelada é somente leitura

**Referências**
- [Spring — `@MessageMapping` e controllers de mensagem](https://docs.spring.io/spring-framework/reference/web/websocket/stomp/handle-annotations.html)
- [OWASP — Cross Site Scripting Prevention](https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html)



### S10-03 — Frontend: tela de chat da carona

**Área:** frontend · **Prioridade:** alta · **Estimativa:** G (10h) · **Dependências:** `S10-02`

**Passo a passo**
- [ ] Instalar o cliente [`@stomp/stompjs`](https://stomp-js.github.io/) e criar um serviço de conexão único na aplicação
- [ ] Enviar o JWT no CONNECT e reconectar automaticamente quando a conexão cair (`reconnectDelay`)
- [ ] Tela de chat: lista de mensagens (as suas de um lado, as dos outros do outro), nome e avatar do autor, horário
- [ ] Carregar o histórico via REST ao abrir e depois só ouvir o WebSocket
- [ ] Rolagem automática para a última mensagem, respeitando quem está lendo mensagens antigas
- [ ] Indicador de status da conexão ("conectando…", "sem conexão — tentando reconectar")
- [ ] **Desconectar ao sair da tela** (`onUnmounted`) — senão o app acumula conexões e trava
- [ ] Campo de envio com `Enter` para enviar e bloqueio enquanto a mensagem não confirma

**Critérios de aceite**
- [ ] Dois navegadores diferentes conversam em tempo real
- [ ] Recarregar a página mantém o histórico
- [ ] Desligar e religar a internet reconecta sozinho
- [ ] Sair e voltar da tela várias vezes não degrada o desempenho

**Referências**
- [STOMP.js — documentação oficial](https://stomp-js.github.io/)
- [Vue 3 — ciclo de vida (`onMounted` / `onUnmounted`)](https://vuejs.org/guide/essentials/lifecycle.html)



### S10-04 — Frontend: entrada do chat e mensagens não lidas

**Área:** frontend · **Prioridade:** media · **Estimativa:** M (6h) · **Dependências:** `S10-03`

**Passo a passo**
- [ ] Botão "Conversar" no card e na tela de detalhes da carona, visível só para participantes confirmados
- [ ] Contador de mensagens não lidas (guardar localmente o id da última mensagem lida por carona)
- [ ] Badge com o total de não lidas no cabeçalho do app
- [ ] Limpar o contador ao abrir o chat
- [ ] Mensagem explicativa quando o usuário ainda não teve a reserva aprovada ("o chat abre quando o motorista aceitar sua solicitação")

**Critérios de aceite**
- [ ] O contador sobe ao chegar mensagem com o chat fechado e zera ao abrir
- [ ] Quem não é participante não vê o botão

**Referências**
- [Pinia — estado compartilhado entre telas](https://pinia.vuejs.org/core-concepts/state.html)



### S10-05 — Backend: contatos de emergência do usuário

**Área:** backend · **Prioridade:** alta · **Estimativa:** M (6h) · **Desbloqueia:** `S10-07`

**Passo a passo**
- [ ] Criar a entidade `ContatoEmergencia`: `id`, `usuario`, `nome`, `telefone`, `email` (opcional) + migração
- [ ] CRUD completo em `/api/usuarios/me/contatos-emergencia` (listar, criar, editar, remover)
- [ ] Limitar a 3 contatos por usuário
- [ ] Validar telefone e e-mail com Bean Validation
- [ ] Garantir que ninguém acessa contato de outro usuário (o id vem do token)
- [ ] Teste: tentar ler contatos de outro usuário retorna 403

**Critérios de aceite**
- [ ] CRUD funciona com todos os campos validados
- [ ] Quarto contato retorna 400 com mensagem clara
- [ ] Isolamento entre usuários testado

**Referências**
- [Bean Validation no Spring Boot](https://docs.spring.io/spring-boot/reference/io/validation.html)
- [Spring Data JPA — relacionamentos `@OneToMany`](https://docs.spring.io/spring-data/jpa/reference/index.html)



### S10-06 — Backend: registro do alerta de emergência

**Área:** backend · **Prioridade:** alta · **Estimativa:** G (10h) · **Dependências:** `S10-05`

**Motivação**
O registro do alerta reúne as informações disponíveis sobre o acionamento, apoiando o envio de notificações e a consulta posterior do ocorrido.

**Passo a passo**
- [ ] Criar a entidade `AlertaEmergencia`: `id`, `usuario`, `carona` (opcional), `latitude`, `longitude`, `acionadoEm`, `status` (`ABERTO`, `EM_ANALISE`, `ENCERRADO`), `observacao` + migração
- [ ] `POST /api/emergencia/alertas` recebendo coordenadas e a carona ativa
- [ ] Responder **rápido** (HTTP 201) e disparar as notificações de forma assíncrona — em emergência, a pessoa não pode esperar o e-mail sair
- [ ] Aceitar o alerta mesmo sem coordenadas (o usuário pode ter negado o GPS) — registrar como "localização indisponível"
- [ ] Impedir alertas repetidos em sequência (ex.: no máximo 1 por minuto por usuário)
- [ ] `GET /api/emergencia/alertas/me` para o usuário ver os próprios acionamentos
- [ ] Testes cobrindo alerta com e sem coordenadas

**Critérios de aceite**
- [ ] O alerta é gravado com todos os dados disponíveis no momento
- [ ] A resposta chega em menos de 1 segundo, mesmo com o envio de e-mail lento
- [ ] Alerta sem GPS ainda é registrado

**Referências**
- [Spring — processamento assíncrono com `@Async`](https://docs.spring.io/spring-framework/reference/integration/scheduling.html)
- [MDN — Geolocation API](https://developer.mozilla.org/pt-BR/docs/Web/API/Geolocation_API)



### S10-07 — Backend: notificação do alerta por e-mail

**Área:** backend · **Prioridade:** alta · **Estimativa:** M (6h) · **Dependências:** `S10-06`, `S7-01`

**Passo a passo**
- [ ] Ao criar um alerta, enviar e-mail para: contatos de emergência do usuário + endereço da segurança do campus (configurável em `application.properties`)
- [ ] Conteúdo do e-mail: nome do usuário, telefone, horário, link do Google Maps com as coordenadas (`https://www.google.com/maps?q=LAT,LNG`), dados da carona e do veículo (placa, modelo, cor) e nome do motorista
- [ ] Enviar de forma assíncrona, com registro em log de sucesso/falha de cada envio
- [ ] Se o envio falhar, tentar novamente uma vez antes de desistir e registrar o erro
- [ ] **Combinar com a coordenação do campus** qual é o endereço de destino real e registrar isso no README (ação de gestão, não de código)

**Critérios de aceite**
- [ ] Acionar o alerta gera e-mail para todos os contatos cadastrados no Mailtrap
- [ ] O link do mapa abre exatamente na coordenada registrada
- [ ] Falha de envio não impede o registro do alerta

**Referências**
- [Sending Email — Spring Boot](https://docs.spring.io/spring-boot/reference/io/email.html)
- [Spring Retry (tentativas automáticas)](https://github.com/spring-projects/spring-retry)



### S10-08 — Frontend: botão de pânico durante a viagem

**Área:** frontend · **Prioridade:** alta · **Estimativa:** G (10h) · **Dependências:** `S10-06`

**Passo a passo**
- [ ] Botão fixo e visível apenas quando o usuário tem uma carona **em andamento**
- [ ] Design inequívoco: vermelho, ícone claro, texto "Emergência"
- [ ] Evitar acionamento acidental **sem atrapalhar a urgência**: usar pressionar-e-segurar por 2 segundos com barra de progresso (melhor que um modal com dois cliques)
- [ ] Pedir a localização com a Geolocation API e enviar junto; se o usuário negar, enviar mesmo assim
- [ ] Confirmação visual forte após o envio: "Alerta enviado. Seus contatos e a segurança do campus foram avisados às 21h43."
- [ ] Mostrar na mesma tela os telefones de emergência (190 / segurança do campus) — o app não substitui a polícia
- [ ] Funcionar com a tela do celular em uso normal (testar em dispositivo real, não só no navegador)

**Critérios de aceite**
- [ ] O botão só aparece durante viagem ativa
- [ ] Um toque acidental não dispara o alerta
- [ ] Negar a permissão de GPS não impede o envio
- [ ] Testado em pelo menos um celular real

**Referências**
- [MDN — Geolocation API](https://developer.mozilla.org/pt-BR/docs/Web/API/Geolocation_API)
- [MDN — eventos de toque e `pointerdown`](https://developer.mozilla.org/pt-BR/docs/Web/API/Pointer_events)



### S10-09 — Frontend: tela de contatos de emergência

**Área:** frontend · **Prioridade:** media · **Estimativa:** M (6h) · **Dependências:** `S10-05`

**Passo a passo**
- [ ] Seção "Contatos de emergência" dentro do perfil, com listar/adicionar/editar/remover
- [ ] Máscara de telefone no campo
- [ ] Aviso quando não houver nenhum contato: "Cadastre ao menos um contato para o botão de emergência funcionar melhor"
- [ ] Confirmação antes de remover
- [ ] Explicar o que acontece com esses dados (transparência de LGPD): só são usados em caso de acionamento do alerta

**Critérios de aceite**
- [ ] CRUD completo funciona pela tela
- [ ] O limite de 3 contatos é comunicado antes do erro do backend
- [ ] O texto de transparência está visível

**Referências**
- [Vue 3 — formulários](https://vuejs.org/guide/essentials/forms.html)



### S10-10 — Painel: listagem e acompanhamento de alertas

**Área:** frontend, backend · **Prioridade:** media · **Estimativa:** M (6h) · **Dependências:** `S10-06`, `S8-03`

**Passo a passo**
- [ ] `GET /api/admin/emergencia/alertas` com filtro por status e ordenação por data (mais recente primeiro)
- [ ] `PATCH /api/admin/emergencia/alertas/{id}` para mudar o status e registrar uma observação
- [ ] Tela `/admin/emergencia` no painel, com destaque visual para alertas `ABERTO`
- [ ] Link para o mapa com a localização do alerta
- [ ] Mostrar os dados de contato do usuário e da carona
- [ ] Registrar qual admin alterou o status e quando

**Critérios de aceite**
- [ ] Alertas abertos aparecem no topo e em destaque
- [ ] Mudar o status registra autor e horário
- [ ] Quem não é admin não acessa nada disso

**Referências**
- [Spring Data JPA — consultas com filtro e ordenação](https://docs.spring.io/spring-data/jpa/reference/repositories/query-methods-details.html)



### S10-11 — Documentação final, vídeo e preparação da apresentação

**Área:** docs · **Prioridade:** alta · **Estimativa:** M (6h)

**Motivação**
A documentação e os materiais de apresentação permitem que outras pessoas executem, compreendam e avaliem o sistema ao final do projeto.

**Passo a passo**
- [ ] Atualizar o `README.md`: como rodar o projeto do zero (Docker, backend, frontend, variáveis de ambiente)
- [ ] Atualizar o diagrama de classes com as entidades criadas nas Sprints 7 a 10 (`TokenVerificacao`, `MensagemChat`, `ContatoEmergencia`, `AlertaEmergencia`, `Endereco`)
- [ ] Documentar a API (Swagger/OpenAPI ou uma tabela de endpoints no README)
- [ ] Escrever o relato de metodologia: etapas, decisões técnicas, resultados e oportunidades de melhoria (as retrospectivas viram texto aqui)
- [ ] Gravar um vídeo de 3 a 5 minutos demonstrando: cadastro → confirmação de e-mail → aprovação de CNH pelo admin → publicação de carona com mapa → reserva → chat
- [ ] Ensaiar a apresentação cronometrada pelo menos uma vez antes do dia

**Critérios de aceite**
- [ ] Uma pessoa de fora consegue rodar o projeto seguindo só o README
- [ ] O diagrama reflete o banco de dados real
- [ ] O vídeo está gravado e acessível

**Referências**
- [Como escrever um bom README](https://www.makeareadme.com/)
- [springdoc-openapi (Swagger no Spring Boot 3)](https://springdoc.org/)





## Depois da entrega — possibilidades futuras

Os itens abaixo aparecem no backlog como fora do escopo das Sprints 7 a 10 e podem ser reavaliados em uma etapa futura:

- Autenticação de dois fatores.
- Leitura automática de CNH por OCR.
- Aplicativo móvel nativo, GPS em segundo plano e notificações push.
- Sistema de conquistas e métricas de sustentabilidade, como estimativa de CO₂ evitado.

## Referência de planejamento

O backlog enviado foi escrito com o calendário original de 10/09/2026 a 19/11/2026. Como a equipe ainda está na Sprint 7 e começou com atraso, essas datas são apenas uma referência inicial, não prazos atuais. A equipe deve atualizar o calendário e o estado das tarefas no planejamento das sprints.
