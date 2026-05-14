#  Guia de Padronização de Commits
Para manter a organização e a legibilidade do nosso projeto de caronas, adotaremos o padrão Conventional Commits. Isso facilita a compreensão das mudanças e permite a automação de logs de versão.

### 📝 Estrutura do Commit 
A mensagem deve seguir este formato:
tipo(escopo): descrição curta em letras minúsculas


---

### 🔍 Tipos de Commit

| Tipo | Quando usar | Exemplo |
| :--- | :--- | :--- |
| **feat** | Nova funcionalidade para o usuário | `feat(auth): adiciona login com JWT` |
| **fix** | Correção de um erro/bug | `fix(car): corrige cálculo de distância da viagem` |
| **docs** | Mudanças apenas na documentação | `docs(readme): instrução de como rodar o Maven` |
| **style** | Formatação de código (espaços, vírgulas) - Não afeta a lógica | `style(api): ajusta indentação do UserController` |
| **refactor** | Mudança no código que não corrige erro nem adiciona função | `refactor(ride): simplifica cálculo de tarifas` |
| **test** | Adição ou alteração de testes | `test(user): cria teste para validação de CPF` |
| **chore** | Mudanças em build, ferramentas ou dependências | `chore(deps): atualiza versão do Spring Boot` |

---

### 📍 Escopos Sugeridos (Java/Spring)

Para nosso contexto de aplicativo de caronas, tente usar estes escopos:

- `auth`: Segurança, Tokens, Login e Cadastro.
- `user`: Perfis de Passageiros ou Motoristas.
- `ride`: Lógica de rotas, solicitações e status da carona.
- `payment`: Integração com pagamentos ou cálculos de custo.
- `db`: Scripts SQL, Migrations ou configurações do JPA.
- `api`: Controllers e configurações de endpoints.

---

### 💡 Boas Práticas

1. **Seja Direto:** A descrição deve ser curta e clara.
2. **Use o Imperativo:** Escreva "adiciona" em vez de "adicionando" ou "foi adicionado".
3. **Commits Atômicos:** Não misture uma correção de bug com uma nova funcionalidade no mesmo commit.
4. **Descrição em Português:** Como a equipe é iniciante, manteremos a descrição em português para clareza, mas os **tipos** (feat, fix, etc.) devem ser sempre em inglês.

---

### ✅ Exemplos Certos vs. Errados

**Certo:**
- `feat(ride): implementar busca de caronas por cidade`
- `fix(auth): impedir login com senha em branco`
- `docs: atualizar diagrama do banco de dados`

**Errado:**
- `ajuste no código` (O que foi ajustado?)
- `subindo arquivos` (Genérico demais)
- `feat: fiz o botão e corrigi o erro do banco` (Misturou tipos e não usou escopo)

## 🔄 Pull Requests (PRs)

### Processo Recomendado:

1. **Antes de iniciar o código:**
    - Garanta que a sua branch base (`main`) seja a versão mais atualizada na sua máquina antes de "puxá-la":
   ```bash
   git checkout main
   git pull
   ```
    - Crie a sua branch derivada em seguida: `git checkout -b feature/sua-feature`

2. **Durante o desenvolvimento:**
    - Faça commits atômicos (frequentes e pequenos) limitados a pequenos blocos coerentes de alteração lógica.
    - Realize testes da sua funcionalidade e do projeto como um todo antes do processo final de push.

3. **Na abertura do PR:**
    - O Título deve empregar os mesmos prefixos categorizados das marcações de commit descritas acima.
    - Siga este molde simples na descrição gráfica:

   ```
   **O que foi entregue?**
   - Implementado model da transação X
   - Feita migração via CLI

   **Como Testar?**
   1. Autenticar usuário pela rota api/login e pegar o token.
   2. Realizar POST em api/transacao mandando id e payload da req.
   3. O status HTTP 201 Created é esperado.
   ```

---

## ⚠️ Prevenções no Processo de Review

Buscamos manter o projeto limpo, colaborativo e livre de conflitos obscuros. Evite as seguintes condutas durante ciclos de avaliação de PR:

- ❌ Envio de logs não informativos: "ajustes diversos", "atualizaçao".
- ❌ Commits aglomerativos (Ex: consertar um bug de autenticação e injetar um feature de logistica englobadas no mesmo commit de repositório).
- ❌ Código não validado contra o banco ou em ambiente não reprodutível.
- ❌ Aberturas de Pull Request sem formatações mínimas guiando a lógica da elaboração do código.

