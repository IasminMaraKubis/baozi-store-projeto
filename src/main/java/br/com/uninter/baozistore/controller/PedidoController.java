package br.com.uninter.baozistore.controller;

import br.com.uninter.baozistore.model.*;
import br.com.uninter.baozistore.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {
    private final PedidoRepository pedidos;
    private final ClienteRepository clientes;
    private final ProdutoRepository produtos;

    public PedidoController(PedidoRepository pedidos, ClienteRepository clientes, ProdutoRepository produtos) {
        this.pedidos = pedidos;
        this.clientes = clientes;
        this.produtos = produtos;
    }

    // O JSON do pedido usa IDs; o banco guarda relacionamentos reais (chaves estrangeiras).
    public record PedidoEntrada(Long clienteId, Long produtoId, Integer quantidade) {}
    public record PedidoSaida(Long id, Long clienteId, Long produtoId, Integer quantidade) {}

    private PedidoSaida converter(Pedido pedido) {
        return new PedidoSaida(pedido.getId(), pedido.getCliente().getId(),
                pedido.getProduto().getId(), pedido.getQuantidade());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoSaida criar(@RequestBody PedidoEntrada entrada) {
        if (entrada == null || entrada.clienteId() == null || entrada.produtoId() == null ||
                entrada.quantidade() == null || entrada.quantidade() <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe clienteId, produtoId e quantidade positiva");
        Cliente cliente = clientes.findById(entrada.clienteId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado"));
        Produto produto = produtos.findById(entrada.produtoId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto nao encontrado"));
        if (!Boolean.TRUE.equals(produto.getEstoque()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto indisponivel");
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setProduto(produto);
        pedido.setQuantidade(entrada.quantidade());
        return converter(pedidos.save(pedido));
    }

    @GetMapping
    public List<PedidoSaida> listar() { return pedidos.findAll().stream().map(this::converter).toList(); }

    @GetMapping("/{id}")
    public PedidoSaida buscar(@PathVariable Long id) {
        return converter(pedidos.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido nao encontrado")));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        if (!pedidos.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido nao encontrado");
        pedidos.deleteById(id);
    }
}
