# Instruções para agentes — Backend Arreda

Este arquivo orienta agentes que trabalham no backend do Arreda. O projeto é uma API para conectar alunos e servidores em caronas, iniciada nas disciplinas de POO e Backend e planejada para continuar evoluindo depois da entrega acadêmica.

## Antes de alterar o projeto

- Leia este guia e consulte `README.md`, `ROADMAP.md`, `ARCHITECTURE.md` e `COMMIT_GUIDE.md` conforme a tarefa.
- Confira o estado atual do código e do Git antes de editar. Preserve alterações locais e não reverta mudanças que não fazem parte da tarefa.
- Trate o código e `pom.xml` como referência para a implementação atual. A documentação pode estar desatualizada: por exemplo, o pacote real é `br.com.arreda.backend`, e as versões atuais devem ser conferidas no `pom.xml`.
- Implemente apenas o escopo solicitado. Não assuma que a divisão de tarefas por pessoas ou duplas descrita no backlog continua valendo.
- Quando requisitos estiverem ambíguos, examine primeiro o código e a documentação disponível; pergunte ao usuário apenas se a decisão afetar o comportamento ou o escopo da entrega.

## Tecnologias e execução

- Java 21 e Maven, com Spring Boot. As versões e dependências efetivas estão em `pom.xml`.
- Spring Web MVC, Spring Data JPA, PostgreSQL, Spring Security, Bean Validation, JWT (`java-jwt`) e Springdoc OpenAPI.
- A configuração da aplicação fica em `src/main/resources/application.properties`.
- `docker-compose.yml` atualmente inicia o PostgreSQL; confira o arquivo antes de presumir que ele também inicia a API.
- A aplicação inicia em `br.com.arreda.backend.BackendApplication`. O Swagger/OpenAPI é configurado pelo Springdoc.

## Organização do código

Código principal em `src/main/java/br/com/arreda/backend/`:

- `controllers/`: endpoints HTTP; recebem DTOs e delegam o trabalho aos serviços.
- `dto/`: objetos de entrada e saída da API. Evite expor entidades JPA diretamente nas respostas.
- `services/`: regras de negócio e coordenação das operações; use transações para operações que alteram múltiplas entidades.
- `repositories/`: interfaces de acesso a dados com Spring Data JPA.
- `models/`: entidades persistidas e relacionamentos JPA.
- `enums/`: estados e tipos usados pelo domínio.
- `security/`: filtro de autenticação e serviços de tokens.
- `config/`: configuração do Spring, segurança e documentação da API.
- `exception/`: exceções de negócio, recursos não encontrados e tratamento global de erros.

O pacote de teste está em `src/test/java`. Mantenha a estrutura de pacotes correspondente à estrutura principal.

## Diretrizes de implementação

- Preserve a separação de responsabilidades: controller para HTTP, service para regras de negócio e repository para persistência.
- Valide dados de entrada nos DTOs com Jakarta Bean Validation quando fizer sentido e aplique `@Valid` nos endpoints.
- Use as exceções existentes (`RegraDeNegocioException` e `RecursoNaoEncontradoException`) quando forem adequadas; o tratamento global está em `GlobalExceptionHandler`.
- Não retorne senha, hash de senha, refresh token ou outros dados internos em DTOs de resposta.
- Nunca armazene senha em texto puro. Use o `PasswordEncoder` configurado na aplicação.
- Respeite a autenticação stateless com JWT e não contorne as regras de acesso definidas em `SecurityConfig`.
- Considere os relacionamentos e estados existentes antes de alterar regras de carona, participação, motorista ou veículo. Evite mudanças incompatíveis em endpoints ou no esquema sem necessidade para a tarefa.
- Não coloque senhas, chaves JWT, tokens ou outras credenciais em código, documentação ou commits. Não copie valores locais de configuração para arquivos novos.
- Se uma mudança de API afetar o frontend, mantenha nomes, tipos e formato dos campos consistentes com os DTOs e com os consumidores existentes; confirme o frontend no checkout antes de assumir sua localização.

## Convenções de colaboração

- Para qualquer tarefa de implementação, trabalhe em uma branch nova derivada da `main` atualizada. Nunca faça commit diretamente na `main`; todos os commits devem ficar na branch da tarefa.
- Ao concluir uma tarefa de implementação, envie a branch e solicite a integração por Pull Request para a `main`, seguindo o fluxo de `COMMIT_GUIDE.md`. Não integre mudanças diretamente na `main`.
- Siga o padrão Conventional Commits em português documentado em `COMMIT_GUIDE.md`.
- Não crie issues ou faça alterações fora do escopo solicitado.
- Não execute testes ou builds, nem adicione testes, a menos que o usuário peça validação ou isso faça parte explícita da tarefa.
- Ao concluir, resuma os arquivos e comportamentos alterados e informe qualquer validação solicitada que não tenha sido possível executar.
