# Login Seguro

Sistema de autenticação e autorização com **Spring Boot 4.1**, **Spring Security**, **Thymeleaf** e **MongoDB Atlas**.
Foi feito de forma modular para servir de base a outros projetos: a lógica de usuários e segurança não depende do tema visual.

## O que o sistema faz

- Cadastro com validação (nome, e-mail único, senha com no mínimo 8 caracteres, letras e números, confirmação de senha)
- Login e logout com Spring Security; a senha é guardada como hash **BCrypt** (fator 12)
- Três perfis: **ADMIN**, **MODERADOR** e **USUARIO**, com rotas e menus liberados conforme o perfil
- Bloqueio da conta por 15 minutos após 5 senhas erradas seguidas
- Sessões HTTP gravadas no MongoDB Atlas (coleção `sessions`), expirando após 30 minutos sem uso
- Registro de entradas, saídas e tentativas recusadas (coleção `registros_acesso`)
- Temas visuais trocados por configuração (`documento` claro e `noturno` escuro)

| Rota | Quem acessa |
|---|---|
| `/login`, `/cadastro` | Qualquer pessoa |
| `/painel` | Qualquer usuário logado |
| `/moderacao` | ADMIN e MODERADOR |
| `/admin/usuarios` | Somente ADMIN |

## Requisitos

- Java 21
- Uma conta no MongoDB Atlas com um cluster (o plano gratuito M0 serve)
- Não é preciso instalar o Maven: o projeto usa o Maven Wrapper (`mvnw`)

## Configurar o MongoDB Atlas

1. Crie um cluster M0 em [cloud.mongodb.com](https://cloud.mongodb.com).
2. Em **Database Access**, crie um usuário com a permissão *Read and write to any database*.
3. Em **Network Access**, libere o seu IP (ou `0.0.0.0/0` para testes).
4. Em **Connect → Drivers**, copie a string de conexão `mongodb+srv://...`.

## Configurar o ambiente

A senha do banco **nunca** fica no código. O `application.properties` lê os valores de variáveis de ambiente ou de um arquivo `.env` na raiz do projeto, que está no `.gitignore`.

1. Copie o modelo:
   ```bash
   cp .env.example .env
   ```
2. Edite o `.env`:

   | Variável | Para que serve |
   |---|---|
   | `MONGODB_URI` | String de conexão do Atlas, com usuário e senha |
   | `MONGODB_DATABASE` | Nome do banco (padrão `login_seguro`) |
   | `SENHA_INICIAL` | Senha das três contas de demonstração criadas na primeira execução |
   | `APP_TEMA` | `documento` ou `noturno` |

Se preferir, defina as mesmas variáveis no sistema ou na configuração de execução do IntelliJ (**Run → Edit Configurations → Environment variables**). Variáveis do sistema têm prioridade sobre o `.env`.

## Executar

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

Acesse **http://localhost:8080**.

Na primeira execução, se `SENHA_INICIAL` estiver definida, o sistema cria três contas para testar os perfis:

| E-mail | Perfil |
|---|---|
| `admin@loginseguro.dev` | ADMIN |
| `moderador@loginseguro.dev` | MODERADOR |
| `usuario@loginseguro.dev` | USUARIO |

Contas criadas pela tela de cadastro começam como USUARIO; o administrador muda o perfil em **Usuários**.

## Integração com o MongoDB Atlas

| Coleção | Conteúdo | Quem grava |
|---|---|---|
| `usuarios` | Nome, e-mail (índice único), hash da senha, perfil, situação, tentativas de login | `UsuarioRepository` (Spring Data MongoDB) |
| `sessions` | Sessões HTTP serializadas, com o contexto de segurança do usuário logado | `mongodb-spring-session` (`@EnableMongoHttpSession`) |
| `registros_acesso` | Entradas, saídas e tentativas recusadas, com IP e navegador | `RegistroAcessoRepository` |

A conexão usa as propriedades `spring.mongodb.uri` e `spring.mongodb.database`. No Spring Boot 4 elas substituíram as antigas `spring.data.mongodb.*`.
Como as sessões ficam no banco, o usuário continua logado mesmo se a aplicação for reiniciada, e várias instâncias podem compartilhar as mesmas sessões.

## Estrutura do projeto

```
src/main/java/com/joaohen/login_seguro
├── config/       Segurança, sessão, propriedades, contas iniciais e formatação de datas
├── usuario/      Entidade Usuario, perfis, repositório, regras de negócio e formulário de cadastro
├── seguranca/    Integração com o Spring Security e registro de acessos
├── web/          Controllers MVC e atributos comuns a todas as telas
└── comum/        Exceção de regra de negócio

src/main/resources
├── templates/layout/      Layouts reaproveitáveis (acesso e sistema)
├── templates/fragmentos/  Cabeçalho HTML, selo, ícones e mensagens
├── templates/...          Uma pasta por área: auth, painel, moderacao, admin, erro
├── static/css/base.css    Estrutura e componentes; só usa variáveis de cor
├── static/temas/          Um arquivo por tema, definindo as variáveis
└── static/img/            Padrões de fundo em SVG
```

## Criar um tema novo

1. Copie `static/temas/documento.css` para `static/temas/meutema.css` e troque as cores.
2. Defina `APP_TEMA=meutema`.

Nenhum template ou classe Java precisa mudar. Para mudar o nome ou o slogan do sistema, ajuste `app.aparencia.nome-sistema` e `app.aparencia.slogan` no `application.properties`.

## Adaptar para outro projeto

- Novos perfis: acrescente valores ao enum `Perfil` e as regras em `SecurityConfig`.
- Novas áreas: crie um controller em `web/`, um template usando `layout/sistema` e a regra de acesso em `SecurityConfig`.
- Novos dados de usuário: acrescente campos em `Usuario` e `CadastroForm`.

## Gitflow

- `main`: versões entregues, marcadas com tags (`v1.0.0`)
- `develop`: integração das funcionalidades
- `feature/*`: uma branch por funcionalidade, unida à `develop` com `--no-ff`
