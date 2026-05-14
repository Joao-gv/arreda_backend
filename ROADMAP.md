# 🗺️ Roadmap Backend: Projeto Arreda (Foco em POO)

**Objetivo:** Construir a API REST do sistema de caronas universitárias aplicando conceitos de Programação Orientada a Objetos (POO) em Java com Spring Boot.

**Equipe:** 4 Desenvolvedores Backend.

**Prazo:** 6 Sprints Semanais (Até o final de Junho).

---

## 📅 Sprints e Distribuição de Tarefas

### 🏃‍♂️ Sprint 1: O Alicerce (Configuração e Entidades de Usuário)
**Foco:** Deixar o banco rodando e mapear os primeiros objetos.

- [ ] **Pessoa 1 (Infra):** Configurar o `pom.xml` (Web, JPA, Postgres, SpringDoc/Swagger, Lombok) e o `application.properties` para conectar ao banco local ou Docker.
- [ ] **Pessoa 2 (Modelagem):** Criar as classes `Usuario` e `PerfilMotorista`. Aplicar encapsulamento e anotações JPA (`@Entity`, `@Table`, `@Id`).
- [ ] **Pessoa 3 (Persistência):** Criar as interfaces `UsuarioRepository` e `PerfilMotoristaRepository`.
- [ ] **Pessoa 4 (Business):** Criar o `UsuarioService` (ex: lógica para validar se o e-mail já existe antes de salvar).

### 🏃‍♂️ Sprint 2: O Motorista e seus Veículos
**Foco:** Relações entre objetos (Composição e Associação).

- [ ] **Pessoa 1 e 2:** Implementar a entidade `Veiculo` e configurar o relacionamento `@ManyToOne` apontando para o `PerfilMotorista`.
- [ ] **Pessoa 3 e 4:** Criar o `VeiculoController` e o `VeiculoService`. *Regra de POO:* Um usuário só pode cadastrar um veículo se o seu perfil de motorista estiver ativo.
- [ ] **Todos:** Acessar o Swagger (`http://localhost:8080/swagger-ui.html`) e testar a criação de usuários e veículos.

### 🏃‍♂️ Sprint 3: O Domínio de Caronas (A parte complexa de POO)
**Foco:** Criar o objeto Carona e suas múltiplas associações.

- [ ] **Pessoa 1:** Criar a entidade `Carona` contendo referências (`@ManyToOne`) para `PerfilMotorista` e `Veiculo`.
- [ ] **Pessoa 2:** Criar o `CaronaRepository` com métodos de busca personalizados (ex: `findByDestino`).
- [ ] **Pessoa 3:** Criar o `CaronaService`. *Regra:* Garantir que a carona só seja criada com um veículo pertencente àquele motorista.
- [ ] **Pessoa 4:** Criar o `CaronaController` expondo os endpoints `POST /caronas` e `GET /caronas`.

### 🏃‍♂️ Sprint 4: Reservas e Lógica de Estado
**Foco:** Manipulação de estado de objetos e concorrência simples.

- [ ] **Pessoa 1 e 2:** Criar a entidade `Reserva` interligando um `Usuario` (passageiro) a uma `Carona`.
- [ ] **Pessoa 3 e 4:** Implementar a lógica central no `ReservaService`.
    - *Fluxo:* Verificar vagas disponíveis -> Criar a reserva -> **Diminuir 1 vaga da classe Carona** -> Salvar alterações no banco.

### 🏃‍♂️ Sprint 5: Tratamento de Erros e DTOs
**Foco:** Deixar o código limpo, seguro e tolerante a falhas.

- [ ] **Pessoa 1 e 2:** Implementar o padrão **DTO** (Data Transfer Object). Criar classes como `UsuarioResponseDTO` para não retornar a senha do usuário na API.
- [ ] **Pessoa 3 e 4:** Criar o `@RestControllerAdvice` (GlobalExceptionHandler) para capturar exceções customizadas (ex: `VagasEsgotadasException`) e retornar mensagens de erro HTTP adequadas.

### 🏃‍♂️ Sprint 6: Avaliações e Revisão Final
**Foco:** Funcionalidades de confiança, revisão POO e documentação.

- [ ] **Pessoas 1 e 2:** Implementar a classe `Avaliacao` com auto-relacionamento (Avaliador e Avaliado apontando para `Usuario`).
- [ ] **Pessoas 3 e 4:** Adicionar uma funcionalidade usando **Interface** ou **Classe Abstrata** (ex: Serviço de Notificação simulado) para ganhar pontos extras em POO (Polimorfismo).
- [ ] **Todos:** Revisão de código final, garantindo a aplicação dos princípios SOLID e finalizando testes via Swagger.