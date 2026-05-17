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
        string senha
        string telefone
    }
    PERFIL-MOTORISTA {
        long id
        string cnh
        date validadeCnh
        string statusValidacao
    }
    VEICULO {
        long id
        string placa
        string marca
        string modelo
        string cor
    }
    CARONA {
        long id
        string origem
        string destino
        datetime dataHoraPartida
        int vagasDisponiveis
        float valorSugerido
    }
    RESERVA {
        long id
        datetime dataSolicitacao
        string status
    }
    AVALIACAO {
        long id
        int nota
        string comentario
        datetime dataAvaliacao
    }

    USUARIO |o--o| PERFIL-MOTORISTA : possui
    PERFIL-MOTORISTA ||--o{ VEICULO : cadastra
    PERFIL-MOTORISTA ||--o{ CARONA : oferece
    VEICULO ||--o{ CARONA : utilizado-em
    CARONA ||--o{ RESERVA : possui
    USUARIO ||--o{ RESERVA : solicita
    CARONA ||--o{ AVALIACAO : gera
    USUARIO ||--o{ AVALIACAO : faz
    USUARIO ||--o{ AVALIACAO : recebe
```
