# Baozi Store

API REST da atividade de Desenvolvimento Web Back-End da Uninter. O projeto usa Spring Boot 3.5.16, Spring MVC, Spring Data JPA, H2 e JSON. A compilação tem como destino Java 17 e a execução foi validada com Java 25.0.3 e Maven 3.10.2.

## Executar no PowerShell

Na pasta que contém o `pom.xml`, execute:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\zulu25.34.17-ca-jdk25.0.3-win_x64'
& 'C:\Ferramentas\apache-maven-3.10.2\bin\mvn.cmd' -B clean package
& 'C:\Ferramentas\apache-maven-3.10.2\bin\mvn.cmd' spring-boot:run
```

A API atende em `http://localhost:8080`. O H2 armazena os dados em `dados/baozistore.mv.db`. Os testes de integração usam um banco separado em memória.

## Entrega

- [Guia de execução e capturas](GUIA-PASSO-A-PASSO.md)
- [Coleção para importar no Postman](Baozi-Store-Postman.postman_collection.json)
- [Diagrama UML](diagrama-casos-de-uso.png) e [fonte PlantUML](diagrama-casos-de-uso.puml)
- [Modelo de relatório](MODELO-RELATORIO.md)

