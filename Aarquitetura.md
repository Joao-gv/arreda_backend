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
