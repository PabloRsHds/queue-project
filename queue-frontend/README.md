# 🎫 Queue Management System - Frontend

Frontend do **Queue Management System**, um sistema completo para gerenciamento de filas de atendimento, agendamentos, usuários, serviços e departamentos, com comunicação em tempo real entre os diferentes módulos do sistema.

Desenvolvido utilizando **Angular 19** e arquitetura baseada em componentes standalone, Signals e WebSocket.

---

## 📋 Sobre o Projeto

O **Queue Management System** foi desenvolvido para facilitar o gerenciamento de filas e atendimentos em ambientes que trabalham com distribuição de senhas e atendimento por guichês.

O sistema permite organizar departamentos, serviços, usuários, agendamentos e atendimentos, além de fornecer atualizações em tempo real para os usuários através de WebSocket.

### Principais funcionalidades

- 🎫 Gerenciamento de senhas e filas
- 🧑‍💼 Gerenciamento de atendentes e guichês
- 📢 Chamada de senhas em tempo real
- 🖨️ Geração e impressão de senhas
- 👥 Gerenciamento de usuários
- 🔐 Autenticação e controle de acesso por perfil
- 🏢 Gerenciamento de departamentos
- 🛎️ Gerenciamento de serviços
- 📅 Gerenciamento de agendamentos
- 👤 Gerenciamento de clientes
- 📊 Dashboard com estatísticas
- 🔄 Atualizações em tempo real via WebSocket
- 📺 Painel para exibição das chamadas
- 🔎 Pesquisa e filtros
- 📱 Interface responsiva

---

# 🚀 Tecnologias

### Frontend

- **Angular 19**
- **TypeScript**
- **HTML5**
- **CSS3**

### Comunicação

- **HTTP / REST API**
- **WebSocket**
- **STOMP**
- **SockJS**

### Gerenciamento de Estado

- **Angular Signals**
- **RxJS**

### Componentes e visualização

- **PrimeNG**
- **ApexCharts**
- **ng-apexcharts**

---

# 📦 Pré-requisitos

Antes de executar o projeto, certifique-se de possuir:

- [Node.js](https://nodejs.org/) 18+
- npm 9+
- Angular CLI 19+

Para verificar as versões instaladas:

```bash
node --version
npm --version
ng version
🔧 Instalação

Clone o repositório:

git clone https://github.com/PabloRsHds/Queue-Management-System---FrontEnd.git

Acesse o diretório:

cd Queue-Management-System---FrontEnd

Instale as dependências:

npm install
⚙️ Configuração de Ambiente

O frontend precisa conhecer o endereço da API do backend para realizar as requisições HTTP e estabelecer a comunicação WebSocket.

💻 Desenvolvimento local

Por padrão, o ambiente de desenvolvimento utiliza:

http://localhost:8080

A configuração pode ser encontrada nos arquivos de environment do Angular.

Exemplo:

export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'
};
🌐 Utilizando outro dispositivo na rede

Caso seja necessário acessar o sistema através de outro dispositivo na mesma rede local, como um celular, tablet ou outro computador, configure o endereço IP da máquina onde o backend está sendo executado.

Exemplo:

export const environment = {
  production: false,
  apiUrl: 'http://192.168.0.100:8080'
};

Substitua 192.168.0.100 pelo endereço IP da máquina que está executando o backend.

Execute o Angular permitindo conexões externas:

ng serve --host 0.0.0.0

O frontend poderá então ser acessado através do IP da máquina:

http://192.168.0.100:4200

⚠️ O backend também precisa estar configurado para aceitar requisições provenientes do endereço utilizado pelo frontend.

▶️ Executando o Projeto

Para executar em desenvolvimento:

ng serve

Ou permitindo acesso através da rede local:

ng serve --host 0.0.0.0

Depois, acesse:

http://localhost:4200
🌎 Ambientes
Ambiente	Comando	Descrição
Desenvolvimento	ng serve	Executa utilizando a configuração de desenvolvimento
Rede local	ng serve --host 0.0.0.0	Permite acesso através de outros dispositivos
Produção	ng build --configuration=production	Gera o build otimizado para produção
🔌 Comunicação em Tempo Real

O Queue utiliza WebSocket com STOMP e SockJS para manter os diferentes módulos do sistema sincronizados em tempo real.

Isso permite que uma alteração realizada em um módulo seja refletida automaticamente em outros dispositivos conectados ao sistema.

Exemplos
📢 Chamada de uma nova senha
🎫 Atualização da fila
🧑‍💼 Atualização dos atendimentos
📺 Atualização do painel de chamadas
📜 Atualização do histórico
🔄 Sincronização entre recepção e atendentes
Principais tópicos
Tópico	Descrição
/topic/tickets	Atualizações relacionadas às senhas
/topic/queue-display	Atualizações do painel
/topic/tickets/history	Atualizações do histórico
/topic/queue-display/call	Chamadas de senhas
📊 Funcionalidades
🎫 Gerenciamento de Filas
Geração de senhas
Organização da fila de atendimento
Controle da ordem de atendimento
Associação da senha ao serviço
Associação da senha ao departamento
Monitoramento da fila
Atualização da fila em tempo real
📢 Chamada de Senhas
Chamada de clientes da fila
Identificação da senha chamada
Identificação do guichê
Atualização em tempo real
Comunicação com o painel de atendimento
🖨️ Impressão de Senhas
Geração do ticket
Visualização da senha
Modal de impressão
Informações do atendimento no ticket
Preparação para impressão em impressoras térmicas
🧑‍💼 Atendimento
Controle dos atendentes
Controle de presença
Associação do atendente ao atendimento
Controle do atendimento realizado
Atualização do atendimento em tempo real
🏢 Departamentos
Cadastro de departamentos
Edição de departamentos
Exclusão de departamentos
Listagem de departamentos
Pesquisa de departamentos
Associação de serviços
🛎️ Serviços
Cadastro de serviços
Edição de serviços
Exclusão de serviços
Listagem de serviços
Pesquisa de serviços
Associação com departamentos
👥 Usuários
Cadastro de usuários
Edição de usuários
Exclusão de usuários
Listagem de usuários
Pesquisa de usuários
Controle de permissões
Perfis

O sistema possui diferentes níveis de acesso:

Perfil	Descrição
ADMIN	Administrador do sistema
MANAGER	Gerenciamento
RECEPTION	Recepção
ATTENDANT	Atendimento
🔐 Autenticação e Segurança

O frontend utiliza autenticação baseada em JWT.

Recursos relacionados à autenticação:

Login
Access Token
Refresh Token
Renovação de sessão
Interceptor HTTP
Proteção de rotas
Controle de acesso baseado em perfil
Tratamento de sessão expirada
Redirecionamento para autenticação
📅 Agendamentos

O sistema permite gerenciar agendamentos relacionados aos serviços.

Status disponíveis
SCHEDULED
CONFIRMED
FINISHED
CANCELED
MISSED

Funcionalidades:

Criação de agendamentos
Consulta de agendamentos
Pesquisa
Filtro por data
Associação ao serviço
Associação ao departamento
Controle do status do agendamento
👤 Clientes
Cadastro de clientes
Edição de informações
Registro de foto
Preview da foto
Avatar padrão
Associação do cliente ao atendimento
📺 Painel de Atendimento

O sistema possui um painel destinado à exibição das chamadas para os clientes.

O painel pode apresentar:

┌───────────────────────────────┐
│                               │
│          SENHA A123           │
│                               │
│          GUICHÊ 03            │
│                               │
└───────────────────────────────┘

As chamadas são atualizadas em tempo real através de WebSocket.

📊 Dashboard

O sistema possui um dashboard para acompanhamento das informações da aplicação.

Entre as informações disponíveis estão:

Estatísticas de usuários
Estatísticas de departamentos
Estatísticas de serviços
Informações de atendimentos
Dados agrupados por período
Gráficos estatísticos

Os gráficos são desenvolvidos utilizando ApexCharts.

🔎 Pesquisa e Filtros

O sistema possui mecanismos de pesquisa e filtragem para facilitar o gerenciamento dos dados.

Incluindo:

Pesquisa de usuários
Pesquisa de departamentos
Pesquisa de serviços
Pesquisa de agendamentos
Filtros por data
Filtros de acordo com permissões
🧠 Gerenciamento de Estado

O frontend utiliza Angular Signals para gerenciamento de estado.

Entre os estados utilizados pelo sistema estão:

DepartmentStateService
ServiceManagementService
SchedulingStateService
UserStateService

Essa abordagem permite:

Compartilhamento de informações entre componentes
Atualização reativa da interface
Redução de requisições desnecessárias
Sincronização dos dados
Melhor organização da aplicação
📱 Responsividade

A interface foi desenvolvida para funcionar em diferentes tamanhos de tela:

🖥️ Desktop
💻 Notebook
📱 Tablet
📱 Smartphone

O layout possui adaptações para telas menores, incluindo componentes responsivos e rolagem horizontal em tabelas quando necessário.

🛠️ Comandos Úteis
Desenvolvimento
ng serve
Desenvolvimento com acesso pela rede
ng serve --host 0.0.0.0
Build de produção
ng build --configuration=production
Testes
ng test
Lint
ng lint
🏗️ Estrutura Geral

A aplicação é organizada de forma modular, separando as principais responsabilidades do sistema:

src/
├── app/
│   ├── authentication/
│   ├── dashboard/
│   ├── users/
│   ├── departments/
│   ├── services/
│   ├── scheduling/
│   ├── tickets/
│   ├── attendance/
│   ├── queue-display/
│   └── shared/
│
├── environments/
│   ├── environment.ts
│   └── environment.prod.ts
│
└── assets/
🔗 Backend

O frontend foi desenvolvido para trabalhar em conjunto com o backend do Queue Management System.

O backend é responsável pela API REST, autenticação, persistência dos dados e comunicação WebSocket.

Configure corretamente o endereço da API antes de executar o frontend.

🤝 Contribuição

Contribuições são bem-vindas!

Faça um Fork do projeto.
Crie uma branch para sua alteração:
git checkout -b feature/nova-feature
Faça suas alterações.
Commit:
git commit -m "Adiciona nova feature"
Envie para o repositório:
git push origin feature/nova-feature
Abra um Pull Request.
👤 Autor

PabloRsHds

GitHub: @PabloRsHds

❤️ Queue Management System

Desenvolvido com ❤️ por PabloRsHds.
