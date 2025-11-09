# API de Pagamentos (FADESP) — Desafio Técnico Nível 1

Este projeto é um microsserviço Java Spring Boot responsável pelo gerenciamento e processamento de pagamentos.
Permite criar, consultar, atualizar e realizar exclusão lógica de pagamentos, com controle de status e integração completa via API REST.
A documentação está disponível via Swagger UI, e o sistema utiliza H2 Database em memória para persistência e testes locais.

## Tecnologias Utilizadas

- Java 17

- Spring Boot 3.5.7

- Spring Data JPA

- Springdoc OpenAPI / Swagger UI

- H2 Database (testes)

- JUnit 5 / Mockito (testes unitários)

- Maven 3.9+

### Perfis Disponíveis
Ambiente	Descrição	Arquivo de configuração
h2	Banco de dados em memória (padrão para testes)	application.properties
### Variáveis de Ambiente

Essas variáveis podem ser configuradas no application.properties (ou sobrescritas via ambiente/Docker):

Variável	Descrição	Valor Padrão
spring.datasource.url	URL do banco H2	jdbc:h2:mem:db_pagamentos
spring.datasource.username	Usuário do banco	sa
spring.datasource.password	Senha do banco	(vazio)
spring.h2.console.path	Caminho do console H2	/h2-console
spring.jpa.hibernate.ddl-auto	Estratégia de geração do schema	create
springdoc.swagger-ui.path	Caminho do Swagger UI	/swagger-ui.html
springdoc.api-docs.path	Caminho JSON da documentação	/v3/api-docs
server.port	Porta padrão da aplicação	8080


## Como Executar
 Modo Desenvolvimento (H2)
mvn spring-boot:run


Acesse:
 - Swagger UI: http://localhost:8080/swagger-ui/index.html
- H2 Console: http://localhost:8080/h2-console

No console H2 use:

- JDBC URL: jdbc:h2:mem:db_pagamentos

- User: sa

- Password: (vazio)

#### Scripts e Coleções

 Em src/main/resources/collections há uma coleção pronta para Postman/Insomnia.

#### Caso deseje inserir dados iniciais, crie um arquivo data.sql no mesmo diretório.

## Autor

Valberton Cabral |
Desenvolvedor Java

 Email: valbertonviana@gmail.com

 GitHub: github.com/CabralV8

 LinkedIn: linkedin.com/in/valbertoncabral
