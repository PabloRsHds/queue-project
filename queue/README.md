# Queue Backend

Backend da aplicação **Queue**, um sistema de gerenciamento de filas, atendimentos, guichês, serviços, agendamentos e usuários.

O projeto foi desenvolvido utilizando Java e Spring Boot, com PostgreSQL como banco de dados.

---

## 🚀 Tecnologias

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- PostgreSQL
- JWT
- WebSocket
- STOMP
- SockJS
- Maven

---

## 📋 Requisitos

Antes de executar o projeto, certifique-se de possuir:

- Java 21 ou superior
- Maven
- PostgreSQL
- Git

Verifique as versões instaladas:

```bash
java -version
mvn -version
git --version
📥 Instalação

Clone o repositório:

git clone https://github.com/PabloRsHds/Queue-Management-System---BackendEnd.git

Entre na pasta do projeto:

cd queue

Instale as dependências:

mvn clean install
🗄️ Banco de dados

O projeto utiliza PostgreSQL.

Crie um banco de dados:

CREATE DATABASE queue;

Por padrão, a aplicação utiliza:

Host: localhost
Port: 5432
Database: queue

As credenciais podem ser configuradas através das variáveis de ambiente.

⚙️ Configuração

As configurações do banco de dados podem ser definidas através de variáveis de ambiente.

O projeto possui valores padrão para desenvolvimento:

spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/queue}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root}

Caso nenhuma variável de ambiente seja definida, serão utilizados:

Database: queue
Username: root
Password: root

Para utilizar outras credenciais, configure:

DB_URL=jdbc:postgresql://localhost:5432/queue
DB_USERNAME=queue
DB_PASSWORD=sua-senha
🔐 Chaves JWT

A aplicação utiliza JWT para autenticação.

As chaves de exemplo ficam dentro de:

src/main/resources/
├── public.example-key
└── private.example-key

A configuração padrão é:

public:
  key: classpath:public.example-key

private:
  key: classpath:private.example-key
⚠️ Produção

As chaves public.example-key e private.example-key são destinadas apenas ao ambiente de desenvolvimento/exemplo.

Para produção, gere um novo par de chaves e não utilize as chaves disponibilizadas no projeto.

A chave privada nunca deve ser compartilhada ou publicada em um repositório público.

🌐 CORS

As origens permitidas pela API são configuradas diretamente no backend.

Exemplo:

configuration.setAllowedOrigins(List.of(
    "http://localhost:4200"
));

Para executar o frontend em outro endereço, adicione a origem correspondente à configuração do backend.

Exemplo:

http://192.168.100.10:4200

Não inclua / no final da origem.

Correto:

http://localhost:4200

Incorreto:

http://localhost:4200/
🔌 WebSocket

O sistema utiliza WebSocket com STOMP e SockJS para comunicação em tempo real.

O endpoint utilizado é:

/ws

A configuração das origens permitidas também é realizada diretamente no backend.

Exemplo:

registry
    .addEndpoint("/ws")
    .setAllowedOrigins(
        "http://localhost:4200"
    )
    .withSockJS();

O WebSocket é utilizado para atualizações em tempo real, como:

Atualização da fila
Chamadas de senhas
Atualização de atendimentos
Atualização de agendamentos
Comunicação entre clientes conectados
🗃️ JPA / Hibernate

O projeto utiliza Spring Data JPA e Hibernate.

A configuração padrão é:

spring:
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

    hibernate:
      ddl-auto: update

O modo:

ddl-auto: update

permite que o Hibernate atualize automaticamente a estrutura do banco de dados de acordo com as entidades da aplicação.

Para ambientes de produção, recomenda-se utilizar uma estratégia de migração de banco de dados apropriada, como Flyway ou Liquibase.

▶️ Executando o projeto

Depois de configurar o PostgreSQL, execute:

mvn spring-boot:run

Ou execute a classe principal:

QueueApplication

Por padrão, o backend estará disponível em:

http://localhost:8080
🏗️ Build

Para gerar o arquivo .jar:

mvn clean package

O arquivo será gerado dentro de:

target/

Para executar:

java -jar target/queue-*.jar
🔑 Autenticação

A aplicação utiliza autenticação baseada em JWT.

Fluxo básico:

┌──────────────┐
│   Frontend   │
└──────┬───────┘
       │
       │ Login
       ▼
┌──────────────┐
│    Backend   │
└──────┬───────┘
       │
       │ JWT
       ▼
┌──────────────┐
│   Frontend   │
└──────┬───────┘
       │
       │ Authorization: Bearer <token>
       ▼
┌──────────────┐
│    Backend   │
└──────────────┘

Endpoints protegidos exigem um token JWT válido.

Exemplo:

Authorization: Bearer eyJhbGciOi...
👥 Perfis de acesso

O sistema possui diferentes perfis de usuário:

ADMIN
MANAGER
RECEPTION
ATTENDANT

Cada perfil possui diferentes permissões dentro da aplicação.

📡 API

A API disponibiliza endpoints para gerenciamento dos principais recursos do sistema.

Entre eles:

Autenticação
Usuários
Unidades
Departamentos
Serviços
Agendamentos
Senhas
Atendimentos

Exemplo:

GET /units

Com paginação:

GET /units?page=0&size=20

Com pesquisa:

GET /units?page=0&size=20&search=nome
🔄 Comunicação em tempo real

O WebSocket permite que diferentes clientes recebam alterações sem precisar atualizar a página.

Exemplo:

                    ┌──────────────┐
                    │    Backend   │
                    └──────┬───────┘
                           │
                    WebSocket / STOMP
                           │
             ┌─────────────┼─────────────┐
             ▼             ▼             ▼
        ┌─────────┐   ┌─────────┐   ┌─────────┐
        │ Cliente │   │ Cliente │   │ Cliente │
        │    1    │   │    2    │   │    3    │
        └─────────┘   └─────────┘   └─────────┘
🌎 Executando em uma rede local

Para permitir que outro computador ou dispositivo da mesma rede utilize o backend, o frontend precisa estar configurado com o endereço da máquina onde o backend está executando.

Por exemplo:

Backend:
http://192.168.123.5:8080

Frontend:
http://192.168.123.10:4200

Nesse caso, a origem do frontend deve ser adicionada ao CORS:

configuration.setAllowedOrigins(List.of(
    "http://localhost:4200"
));

O mesmo endereço deve ser permitido na configuração do WebSocket.

🐳 Docker

Caso utilize Docker para executar o PostgreSQL:

docker compose up -d

Verifique os containers:

docker ps

Para parar:

docker compose down
🔒 Segurança

Não envie informações sensíveis para o repositório.

Exemplo de arquivos que não devem conter credenciais reais:

.env

Também não publique uma chave privada JWT real.

O .gitignore deve conter pelo menos:

.env

target/

.idea/
*.iml
.vscode/
📁 Estrutura do projeto

Estrutura simplificada:

queue/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/com/queue/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── entity/
│   │   │       ├── repository/
│   │   │       ├── service/
│   │   │       └── ...
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── public.example-key
│   │       └── private.example-key
│   │
│   └── test/
│
├── pom.xml
├── .gitignore
└── README.md
🧪 Desenvolvimento

Para um ambiente local, uma configuração básica pode utilizar:

Database:
queue

Host:
localhost

Port:
5432

Username:
root

Password:
root

Ou configurar variáveis de ambiente:

DB_URL=jdbc:postgresql://localhost:5432/queue
DB_USERNAME=queue
DB_PASSWORD=sua-senha
🤝 Contribuição

Contribuições são bem-vindas.

Para contribuir:

Faça um fork do projeto.
Crie uma branch:
git checkout -b feature/minha-feature
Faça suas alterações.
Faça o commit:
git commit -m "feat: adiciona minha feature"
Envie a branch:
git push origin feature/minha-feature
Abra um Pull Request.

📄 Licença

Este projeto está sob a licença definida no arquivo:
LICENSE
