# Decisões e fluxos do projeto

Este documento registra regras de produto e escolhas técnicas que afetam autenticação, envio de e-mail e fotos de perfil. A referência dos endpoints HTTP continua sendo o Swagger UI, disponível em `/swagger-ui.html` quando a API está em execução.

## Confirmação de e-mail

- Contas novas começam com `email_verificado = false`; o login só é permitido após a confirmação.
- O cadastro (`POST /api/usuarios/register`) cria um token `CONFIRMACAO_EMAIL` válido por 24 horas e envia o link de confirmação.
- `POST /api/auth/confirmar-email` consome o token uma única vez. Token inválido, expirado ou já usado retorna `400`.
- `POST /api/auth/reenviar-confirmacao` sempre retorna uma mensagem genérica, para não revelar se um e-mail está cadastrado. O limite é um pedido por e-mail a cada dois minutos; esse limite vale mesmo quando a conta não existe.
- Se o envio do e-mail falhar durante o cadastro, a conta permanece criada. O usuário pode pedir o reenvio.
- Um token de confirmação usado invalida os demais tokens pendentes do mesmo tipo para aquela conta.

## Login e redefinição de senha

- Uma senha correta para uma conta ainda não confirmada retorna `403` com o código `EMAIL_NAO_VERIFICADO`. Uma senha incorreta continua retornando `401`.
- A redefinição começa em `POST /api/auth/esqueci-senha`. A resposta é genérica, exista ou não uma conta para o endereço informado.
- São permitidos até três pedidos de redefinição por e-mail a cada 15 minutos. O limite é mantido em memória: reiniciar a API o zera, e ele não é compartilhado entre instâncias.
- O token `RESET_SENHA` expira após 30 minutos, só pode ser usado uma vez e invalida outros tokens de redefinição pendentes da conta.
- A senha nova deve ter de 8 a 72 caracteres, com ao menos uma letra e um número. A aplicação armazena o hash BCrypt, nunca a senha em texto puro.
- Depois da redefinição, todos os refresh tokens da conta são revogados.

## E-mail em desenvolvimento

O envio usa SMTP. Para desenvolvimento, o projeto recomenda uma caixa de teste (por exemplo, Email Sandbox no Mailtrap), para que mensagens não sejam enviadas a destinatários reais.

1. Copie `.env.example` para `.env` e configure `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` e `MAIL_FROM` com os dados da caixa de teste.
2. `MAIL_SMTP_AUTH` e `MAIL_SMTP_STARTTLS` são opcionais; ambos ficam habilitados por padrão.
3. Para executar pela IDE, use a raiz do repositório como diretório de trabalho. O Spring importa o `.env` local. No Compose, as variáveis são passadas para o container pela configuração do serviço `api`.
4. `FRONTEND_URL` define a base dos links de confirmação e redefinição; o padrão local é `http://localhost:5173`.

Os arquivos `.env` são locais e ignorados pelo Git. Compartilhe apenas os nomes das variáveis, nunca os valores. Os templates de e-mail ficam em `src/main/resources/templates/`; os valores interpolados são escapados para HTML.

### Contas antigas em desenvolvimento

Contas criadas antes da exigência de confirmação podem continuar com `email_verificado = false`. Para liberar essas contas em um banco **local de desenvolvimento**, é possível executar:

```sql
UPDATE tb_usuarios SET email_verificado = true WHERE email_verificado = false;
```

Esse comando libera todas as contas afetadas; não o execute em um ambiente compartilhado ou de produção sem decidir como verificar cada conta.

## Fotos de perfil

- Upload e remoção usam o usuário autenticado: `POST /api/usuarios/me/foto` e `DELETE /api/usuarios/me/foto`. Um usuário não escolhe o ID de outra conta no corpo da requisição.
- `GET /api/usuarios/{id}/foto` é público para que as fotos possam aparecer em perfis e cards.
- O upload recebe a parte multipart `foto`, aceita imagens JPEG ou PNG de até 2 MB e verifica o conteúdo real do arquivo. A aplicação converte o resultado para JPEG com dimensão máxima de 512×512.
- Cada arquivo recebe uma chave aleatória em `foto/{uuid}.jpg`. O PostgreSQL guarda a chave em `foto_key`; o conteúdo da imagem fica no sistema de arquivos.
- No Compose, o diretório `/app/uploads` é montado no volume nomeado `arreda_uploads`. Na execução pela IDE, o padrão é `./uploads` na raiz do repositório. O caminho pode ser configurado com `ARREDA_UPLOAD_DIR`.
- Trocar a foto atualiza a chave do usuário e tenta remover o arquivo anterior após o commit do banco; falhas na remoção são registradas no log da API. Remover a foto apaga a chave e o arquivo associado.
- O endpoint de leitura retorna `404` quando o usuário não tem foto.

## Documentação dos endpoints

O Swagger UI apresenta os contratos HTTP e pode ser aberto em <http://localhost:8080/swagger-ui.html> depois de iniciar a API. As regras de fluxo e as escolhas resumidas neste documento complementam essa referência.
