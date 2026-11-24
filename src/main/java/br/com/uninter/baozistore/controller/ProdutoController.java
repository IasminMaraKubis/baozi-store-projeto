package br.com.uninter.baozistore.controller;

import br.com.uninter.baozistore.model.Produto;
import br.com.uninter.baozistore.repository.ProdutoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {
    private final ProdutoRepository repository;
    public ProdutoController(ProdutoRepository repository) { this.repository = repository; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Produto criar(@RequestBody Produto produto) {
        if (produto.getNome() == null || produto.getNome().isBlank() ||
            produto.getPreco() == null || produto.getPreco().compareTo(BigDecimal.ZERO) < 0 ||
            produto.getEstoque() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe nome, preco nao negativo e estoque");
        produto.setId(null);
        return repository.save(produto);
    }

    @GetMapping
    public List<Produto> listar() { return repository.findAll(); }

    @GetMapping("/{id}")
    public Produto buscar(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto nao encontrado"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        if (!repository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto nao encontrado");
        try {
            repository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Exclua primeiro os pedidos vinculados", ex);
        }
    }
}
