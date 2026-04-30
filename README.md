# 🚗 Arreda - Sistema de Caronas 

> **Arreda** é uma plataforma moderna para gestão de caronas, desenvolvida por alunos do IFMG pensando na questão de acessibilidade da própria instituição, a plataforma é focada em segurança e praticidade. Desenvolvida com Spring Boot 3 e Java 21.

---

## 📋 Sobre o Projeto
O Arreda conecta motoristas e passageiros de forma eficiente. O foco inicial é o ambiente universitário/corporativo, onde a confiança é a base para o compartilhamento de viagens.

### 🛠️ Tecnologias Utilizadas
- **Backend:** Java 21, Spring Boot 3, Spring Data JPA
- **Banco de Dados:** PostgreSQL 15 (via Docker)
- **Documentação:** Swagger (OpenAPI)
- **Gerenciador de Dependências:** Maven

---

## 🚀 Como Executar o Projeto

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

## 🏗️ Arquitetura do Sistema
O projeto segue o padrão de **Arquitetura em Camadas**:
1. **Controller:** Porta de entrada (API REST).
2. **Service:** Regras de negócio e validações.
3. **Repository:** Comunicação com o Banco de Dados.
4. **Model (Entity):** Mapeamento das tabelas.

---

## 👥 Equipe
- **Carlos Eduardo** - *Lider da equipe*
- **João Vieira** - *Responsável técnico*
- **João Vitor** -*Responsável pela apresentação*
- **Victor Fernandes** - *Responsável pela documentação* 
