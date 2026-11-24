# GUIA COMPLETO — ATIVIDADE PRÁTICA UNINTER: API REST BAOZI STORE

## 0. O que é este projeto e o que é exigido
O trabalho da disciplina Desenvolvimento Web Back-End pede uma API REST Java com Spring Boot, Spring Data JPA, banco relacional, JSON e arquitetura MVC. São obrigatórias 3 entidades (Cliente, Produto e Pedido) e 4 operações por entidade: POST, GET geral, GET por ID e DELETE por ID. PUT é opcional. Também são obrigatórios diagrama UML de casos de uso, capturas reais do Postman, link do GitHub e um PDF seguindo as cinco seções do modelo.

O projeto foi compilado e testado com Maven 3.10.2 e Java 25.0.3. As capturas do Postman, os dados acadêmicos e o link do GitHub são preenchidos antes da exportação do PDF.

## 1. Instalações (Windows)
1. Já tendo VS Code e GitHub Desktop, confira se possui **JDK 25** (não basta Node.js): abra PowerShell e execute `java -version` e `javac -version`. Se não funcionar, instale o JDK 25 e reabra o terminal.
2. Verifique Maven: `mvn -version`. Se não existir, instale Apache Maven 3.6.3+ seguindo https://maven.apache.org/install.html : baixe o arquivo binário ZIP, extraia-o numa pasta permanente e adicione a subpasta `bin` ao PATH do Windows. Feche/reabra VS Code e confirme `mvn -version`.
3. Instale o **Postman** (aplicativo desktop recomendado) e, se desejar, extensões VS Code "Extension Pack for Java" e "Spring Boot Extension Pack".
4. Atenção: Node.js não substitui o Java. Se PowerShell bloquear `.ps1`, tente os comandos usando CMD (Prompt de Comando) dentro do VS Code.

## 2. Abrir o código já preparado
1. Descompacte `baozi-store-projeto.zip` numa pasta como `C:\Projetos\baozi-store-projeto`.
2. VS Code > Arquivo > Abrir Pasta > selecione a pasta que **contém o pom.xml**.
3. Abra Terminal > Novo Terminal e execute `java -version`, `mvn -version`.
4. Na raiz do projeto, execute os comandos abaixo no PowerShell.

   ```powershell
   $env:JAVA_HOME = 'C:\Program Files\Java\zulu25.34.17-ca-jdk25.0.3-win_x64'
   & 'C:\Ferramentas\apache-maven-3.10.2\bin\mvn.cmd' -B clean package
   & 'C:\Ferramentas\apache-maven-3.10.2\bin\mvn.cmd' spring-boot:run
   ```
5. Na primeira execução, o Maven precisará de internet para baixar bibliotecas. Espere o terminal mostrar `Started BaoziStoreApplication`. Deixe este terminal aberto durante o Postman.
6. No navegador, abra `http://localhost:8080/clientes`. Com um banco novo, o resultado esperado é `[]` (lista vazia). Não espere uma página web bonita: é uma API.
7. O banco H2 grava arquivos na pasta local `dados/` (não enviada ao Git). Os dados persistem entre reinicializações.

## 3. Entender a estrutura (explicação dos arquivos)
- `pom.xml`: dependências do Spring Web, Spring Data JPA e banco H2 (Spring Boot 3.5.16, Java 17).
- `src/main/resources/application.properties`: porta 8080, configuração do H2 e criação/atualização de tabelas via Hibernate.
- `BaoziStoreApplication.java`: inicia o servidor.
- `model/Cliente.java`: id Long, nome String, clienteDesde LocalDate.
- `model/Produto.java`: id Long, nome String, preco BigDecimal, estoque Boolean (disponibilidade, **não** quantidade numérica).
- `model/Pedido.java`: id Long, cliente e produto como relacionamentos ManyToOne com chaves estrangeiras e quantidade Integer.
- `repository/*Repository.java`: JpaRepository faz persistência sem precisar escrever SQL CRUD.
- `controller/*Controller.java`: recebe chamadas HTTP, valida os dados, retorna JSON e consulta repositórios.
- O controlador de Pedido recebe JSON `clienteId`/`produtoId`, procura as entidades existentes e devolve esses mesmos campos sem problemas de serialização.

**Nota sobre estoque:** o atributo exigido no enunciado é Boolean. `true` = disponível, `false` = indisponível; este projeto não desconta unidades do estoque, porque quantidade de estoque não é pedida. O preço também não é somado automaticamente ao pedido, pois o enunciado não exige.

## 4. Banco H2 (opcional, para conferir visualmente)
1. Com a API ligada, abra `http://localhost:8080/h2-console`.
2. JDBC URL: `jdbc:h2:file:./dados/baozistore` | User Name: `sa` | Password: em branco.
3. Clique Connect. Confira as tabelas `CLIENTE`, `PRODUTO`, `PEDIDO`.
4. Não altere tabelas manualmente; cadastre tudo pelo Postman.

## 5. Postman: usar a coleção pronta (12 requisições)
1. Abra Postman > Import > selecione `Baozi-Store-Postman.postman_collection.json`.
2. Na primeira requisição **01 Criar cliente**, altere `SEUNOME1234567` para **seu nome seguido imediatamente do seu RU**, por exemplo `AnaSilva1234567` (o RU real deve ser o seu, não o exemplo). Altere `clienteDesde` se quiser, no formato `AAAA-MM-DD`.
3. Clique Send na requisição `01 Criar cliente`. Esperado: HTTP **201** e um JSON com `id`, `nome` e `clienteDesde`. A coleção salva o ID automaticamente.
4. Execute `02 Criar produto`. Esperado: HTTP **201**, id criado, preço e estoque.
5. Execute `03 Criar pedido`. Esperado: HTTP **201**, dados `clienteId`, `produtoId`, `quantidade`.
6. Execute as 3 requisições de **listagem GET**. Esperado: HTTP **200** e arrays JSON com seus dados.
7. Execute as 3 requisições **GET por ID**. Esperado: HTTP **200** e o registro individual.
8. Faça e guarde **PRINTS REAIS** legíveis, mostrando URL, método, JSON do request, JSON da resposta e status HTTP (especialmente os seus nome+RU e produto/quantidade). Capture também as listagens e consultas por ID.
9. SOMENTE DEPOIS dos prints das operações de criação/consulta, execute as exclusões: primeiro `DELETE /pedidos/{id}`, depois `DELETE /clientes/{id}` e `DELETE /produtos/{id}`. Esperado em cada um: HTTP **204 No Content**, corpo vazio. Capture também os prints das exclusões.
10. Para documentar mais facilmente os GETs após criar, tire os prints antes de fazer qualquer DELETE.

**Importante:** na coleção, os IDs são variáveis salvas automaticamente pelos POSTs. Não rode `POST pedido` antes dos POSTs de cliente e produto. No corpo JSON, as variáveis `{{clienteId}}` e `{{produtoId}}` ficam sem aspas, para serem substituídas pelos números. 

## 6. Tabela completa dos endpoints para relatório
| Entidade | Método | Rota | Função |
|---|---|---|---|
| Cliente | POST | /clientes | Criar cliente |
| Cliente | GET | /clientes | Listar clientes |
| Cliente | GET | /clientes/{id} | Consultar cliente |
| Cliente | DELETE | /clientes/{id} | Excluir cliente |
| Produto | POST | /produtos | Criar produto |
| Produto | GET | /produtos | Listar produtos |
| Produto | GET | /produtos/{id} | Consultar produto |
| Produto | DELETE | /produtos/{id} | Excluir produto |
| Pedido | POST | /pedidos | Criar pedido |
| Pedido | GET | /pedidos | Listar pedidos |
| Pedido | GET | /pedidos/{id} | Consultar pedido |
| Pedido | DELETE | /pedidos/{id} | Excluir pedido |

## 7. Exemplos de JSON para copiar, caso não use coleção
**POST http://localhost:8080/clientes**
```json
{"nome":"SEUNOME1234567","clienteDesde":"2026-11-23"}
```
**POST http://localhost:8080/produtos**
```json
{"nome":"Baozi tradicional","preco":12.50,"estoque":true}
```
**POST http://localhost:8080/pedidos** (troque os IDs pelos IDs realmente criados)
```json
{"clienteId":1,"produtoId":1,"quantidade":3}
```
No Postman, escolha Body > raw > JSON e clique Send.

## 8. Diagrama UML: pronto em PlantUML
O arquivo diagrama-casos-de-uso.puml contém o fonte PlantUML. Insira a imagem diagrama-casos-de-uso.png já exportada na seção 2 do relatório.

## 9. GitHub com GitHub Desktop
1. Com o projeto aberto em uma pasta, abra GitHub Desktop > File > Add local repository. Se avisar que não é repositório, escolha Create a repository here.
2. Nome sugerido: `baozi-store-backend-uninter`. Confirme que a pasta contém `pom.xml` e `src/`.
3. Escreva commit `Atividade pratica API REST Baozi Store` > Commit to main.
4. Clique Publish repository. Use repositório **público** para o avaliador acessar sem pedir permissão, salvo orientação diferente da instituição.
5. Copie o link, por exemplo `https://github.com/SEU_USUARIO/baozi-store-backend-uninter`, e cole na seção 5 do relatório.
6. Confira no navegador se `pom.xml`, `src/`, README e diagrama aparecem e a pasta `dados/` não aparece.

## 10. Relatório usando o DOCX fornecido pela professora
1. Abra o arquivo `ATIVIDADE PRATICA - MODELO.docx` fornecido pela disciplina, no Word.
2. Na capa, substitua nome do curso, nome e RU, cidade/estado e ano **2026** (o modelo exibe 2025).
3. Insira o texto do arquivo `MODELO-RELATORIO.md` nas 5 seções do documento, editando os campos `[PREENCHER]` e substituindo exemplos pelos valores que efetivamente testou.
4. Na seção 2, inclua a imagem exportada do diagrama UML.
5. Na seção 4, inclua as capturas de tela REAIS obtidas no Postman, com legenda: POST Cliente, POST Produto, POST Pedido, GET listagem (3), GET por ID (3), DELETE (3). Use imagens legíveis.
6. Se desejar, na seção 3, adicione a tabela de endpoints acima e os campos de cada entidade.
7. Exporte no Word: Arquivo > Salvar como > PDF (ou Exportar > Criar PDF).
8. Abra o PDF para checar legibilidade, páginas e link. Entregue **o PDF** na plataforma acadêmica e mantenha o repositório acessível.

## 11. Erros mais comuns e soluções
- `java` ou `javac` não reconhecido: instalar JDK e corrigir JAVA_HOME/PATH, reiniciar terminal.
- `mvn` não reconhecido: instalar Maven e adicionar `bin` no PATH.
- `Address already in use: 8080`: feche outro servidor 8080 ou ajuste `server.port=8081` e a variável `baseUrl` do Postman.
- `Failed to configure a DataSource`: verifique `pom.xml` com H2 e `application.properties`.
- `404 Not Found` no GET ID: confira ID criado no POST; não use ID presumido depois de testes anteriores.
- `400 Bad Request` no POST Pedido: `quantidade` deve ser positiva; `clienteId`/`produtoId` devem ser números válidos; o produto deve ter `estoque: true`.
- `409`/erro de integridade ao excluir cliente ou produto: exclua primeiro seus pedidos associados, como no roteiro.
- `Connection refused` Postman: a API não está em execução ou a porta está incorreta.
- `401/403`: não são esperados neste projeto (não há Spring Security); confira se acessou a URL correta.
- H2 `Database may be already in use`: não rode duas instâncias usando os mesmos dados. Feche a outra antes de reiniciar.
- As dependências não baixam: internet/proxy/Maven Central; tente novamente com rede estável.

## 12. Checklist final da avaliação
- [ ] 3 entidades JPA corretas, relacionamentos de pedido e banco H2.
- [ ] 12 endpoints funcionais, com JSON.
- [ ] Prints reais Postman de POST, GET geral, GET por ID e DELETE.
- [ ] Print do cliente **com seu nome+RU**, produto e pedido refletidos na narrativa.
- [ ] UML com ator e casos de uso de todas as entidades.
- [ ] PDF com 5 seções e capa corrigida.
- [ ] Repositório GitHub acessível com código-fonte.
- [ ] Conferência final do PDF antes do envio.
