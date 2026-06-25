# 🗺️ Roadmap Backend: Projeto Arreda (Foco em POO)

**Objetivo:** Construir a API REST do sistema de caronas universitárias aplicando conceitos de Programação Orientada a Objetos (POO) em Java com Spring Boot.

**Equipe:** 4 Desenvolvedores Backend.

**Prazo:** 6 Sprints Semanais (Até o final de Junho).

---

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