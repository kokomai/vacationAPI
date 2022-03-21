# vacationAPI
vacation api -with Spring Boot, Security, JWT

# for application-local.yml 
(src/resources/application-local.yml)
~~~
server :
  port : 8080
---
spring-
  pid-
    file- foresys-vacation.pid
---
server:
  tomcat:
    additional-tld-skip-patterns: "*.jar"
---
# DB setting
spring:
  datasource:
    driver-class-name: "oracle.jdbc.OracleDriver"
    url: "YOUR DB URL"
    username: "YOUR DB USER ID"
    password: "YOUR DB PASSWORD"
---
# XML location
mybatis:
    mapper-locations: classpath:mappers/**/*.xml
~~~
