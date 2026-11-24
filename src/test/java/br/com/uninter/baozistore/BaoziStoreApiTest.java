package br.com.uninter.baozistore;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:baozi-api-test",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.h2.console.enabled=false",
        "spring.jpa.show-sql=false"
})
class BaoziStoreApiTest {
    @Autowired
    private TestRestTemplate http;

    @Test
    void cadastraConsultaListaEExcluiOsTresRecursos() {
        JsonNode cliente = post("/clientes", Map.of("nome", "SEUNOME1234567", "clienteDesde", "2026-11-23"));
        JsonNode produto = post("/produtos", Map.of("nome", "Baozi tradicional", "preco", 12.50, "estoque", true));
        long clienteId = cliente.path("id").asLong();
        long produtoId = produto.path("id").asLong();
        JsonNode pedido = post("/pedidos", Map.of("clienteId", clienteId, "produtoId", produtoId, "quantidade", 3));

        assertThat(cliente.path("nome").asText()).isEqualTo("SEUNOME1234567");
        assertThat(cliente.path("clienteDesde").asText()).isEqualTo("2026-11-23");
        assertThat(produto.path("nome").asText()).isEqualTo("Baozi tradicional");
        assertThat(produto.path("preco").asDouble()).isEqualTo(12.50);
        assertThat(produto.path("estoque").asBoolean()).isTrue();
        assertThat(pedido.path("clienteId").asLong()).isEqualTo(clienteId);
        assertThat(pedido.path("produtoId").asLong()).isEqualTo(produtoId);
        assertThat(pedido.path("quantidade").asInt()).isEqualTo(3);

        verificaConsultas("/clientes", cliente);
        verificaConsultas("/produtos", produto);
        verificaConsultas("/pedidos", pedido);

        assertThat(excluir("/clientes/" + clienteId).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(excluir("/produtos/" + produtoId).getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verificaExclusao("/pedidos/" + pedido.path("id").asLong());
        verificaExclusao("/clientes/" + clienteId);
        verificaExclusao("/produtos/" + produtoId);
    }

    @Test
    void rejeitaCorposInvalidosEReferenciasInexistentes() {
        assertThat(http.postForEntity("/clientes", Map.of("nome", ""), JsonNode.class).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(http.postForEntity("/produtos", Map.of("nome", "Baozi", "preco", -1, "estoque", true),
                JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        long clienteId = post("/clientes", Map.of("nome", "Cliente de teste")).path("id").asLong();
        assertThat(http.postForEntity("/pedidos", Map.of("clienteId", clienteId, "produtoId", 1, "quantidade", 0),
                JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(http.postForEntity("/pedidos", Map.of("clienteId", Long.MAX_VALUE, "produtoId", 1, "quantidade", 3),
                JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(http.postForEntity("/pedidos", Map.of("clienteId", clienteId, "produtoId", Long.MAX_VALUE, "quantidade", 3),
                JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        long produtoId = post("/produtos", Map.of("nome", "Indisponível", "preco", 12.50, "estoque", false))
                .path("id").asLong();
        assertThat(http.postForEntity("/pedidos", Map.of("clienteId", clienteId, "produtoId", produtoId, "quantidade", 3),
                JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private JsonNode post(String rota, Map<String, ?> dados) {
        var resposta = http.postForEntity(rota, dados, JsonNode.class);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resposta.getBody().path("id").asLong()).isPositive();
        return resposta.getBody();
    }

    private void verificaConsultas(String rota, JsonNode registro) {
        var lista = http.getForEntity(rota, JsonNode.class);
        assertThat(lista.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(lista.getBody().isArray()).isTrue();
        assertThat(lista.getBody()).contains(registro);
        var individual = http.getForEntity(rota + "/" + registro.path("id").asLong(), JsonNode.class);
        assertThat(individual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(individual.getBody()).isEqualTo(registro);
    }

    private ResponseEntity<Void> excluir(String rota) {
        return http.exchange(rota, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);
    }

    private void verificaExclusao(String rota) {
        var resposta = excluir(rota);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(resposta.getBody()).isNull();
        assertThat(http.getForEntity(rota, JsonNode.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(excluir(rota).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
