    ## 🏛️ Arquitetura do Backend: Arreda
Este documento define o modelo relacional de dados e as diretrizes arquiteturais técnicas do projeto. O modelo abrange os princípios de manutenção, desempenho e a escalabilidade contínua da API do aplicativo visando gerenciar o tráfego da rede de caronas.

Nosso projeto segue a **Arquitetura em Camadas**. O objetivo desse padrão é a *Separação de Responsabilidades*: cada pasta faz apenas uma coisa específica. Se der erro no banco de dados, sabemos exatamente em qual pasta procurar.

---

## 📂 Estrutura de Pastas do Projeto

```text
src/main/java/com/arreda/backend/
├── ArredaApplication.java       # Arquivo principal que liga o servidor
├── controllers/                 # Recepcionistas da API (Endpoints)
├── dtos/                        # Filtros de entrada e saída (Segurança)
├── models/ (ou entities/)       # O coração do sistema (Classes POO)
├── repositories/                # Comunicação direta com o Banco de Dados
├── services/                    # O cérebro do sistema (Regras de Negócio)
└── exceptions/                  # Tratamento de erros e exceções customizadas

```

---

##  Diagrama de Banco de Dados (Entidade-Relacionamento)

```mermaid
erDiagram
    USUARIO {
        long id
        string nome
        string email
        string senha_hash
        string telefone
        datetime created_at
        datetime updated_at
    }

    PERFIL_MOTORISTA {
        long id
        string cnh
        date validade_cnh
        string status_validacao
        long usuario_id
    }

    VEICULO {
        long id
        string placa
        string marca
        string modelo
        string cor
        int capacidade_passageiros
        long motorista_id
    }

    CARONA {
        long id
        string origem
        string destino
        datetime data_hora_partida
        int vagas_disponiveis
        float valor_sugerido
        enum status "ATIVA, LOTADA, CANCELADA, CONCLUIDA"
        long motorista_id
        long veiculo_id
    }

    PARTICIPACAO_CARONA {
        long id
        long usuario_id
        long carona_id
        enum tipo "MOTORISTA, PASSAGEIRO"
        enum status "PENDENTE, CONFIRMADO, REJEITADO, CANCELADO"
        datetime data_participacao
    }

    REFRESH_TOKEN {
        long id
        string token
        datetime data_expiracao
        long usuario_id
    }

    USUARIO ||--o| PERFIL_MOTORISTA : possui
    USUARIO ||--o| REFRESH_TOKEN : possui
    PERFIL_MOTORISTA ||--o{ VEICULO : cadastra
    PERFIL_MOTORISTA ||--o{ CARONA : oferece
    VEICULO ||--o{ CARONA : utilizado_em

    USUARIO ||--o{ PARTICIPACAO_CARONA : participa
    CARONA ||--o{ PARTICIPACAO_CARONA : possui
```
