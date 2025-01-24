package ecommerce.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ItemCompraTest {
private ItemCompra itemCompra;
    private Produto produto;

    @BeforeEach
    void setUp() {
        // Inicializando os objetos necessários para os testes
        produto = new Produto();
        produto.setId(1L);
        produto.setNome("Produto Teste");
        produto.setPreco(BigDecimal.valueOf(100.00));
        produto.setPeso(10);

        itemCompra = new ItemCompra(1L, produto, 5L);
    }

    @Test
    void testGetId() {
        // Verificando o ID inicial
        assertEquals(1L, itemCompra.getId());
    }

    @Test
    void testSetId() {
        // Atualizando o ID e verificando
        itemCompra.setId(2L);
        assertEquals(2L, itemCompra.getId());
    }

    @Test
    void testGetProduto() {
        // Verificando o produto inicial
        assertNotNull(itemCompra.getProduto());
        assertEquals(produto, itemCompra.getProduto());
    }

    @Test
    void testSetProduto() {
        // Criando um novo produto e atualizando o produto do itemCompra
        Produto novoProduto = new Produto();
        novoProduto.setId(2L);
        novoProduto.setNome("Outro Produto");
        novoProduto.setPreco(BigDecimal.valueOf(200.00));
        novoProduto.setPeso(20);

        itemCompra.setProduto(novoProduto);

        // Verificando a atualização
        assertEquals(novoProduto, itemCompra.getProduto());
    }

    @Test
    void testGetQuantidade() {
        // Verificando a quantidade inicial
        assertEquals(5L, itemCompra.getQuantidade());
    }

    @Test
    void testSetQuantidade() {
        // Atualizando a quantidade e verificando
        itemCompra.setQuantidade(10L);
        assertEquals(10L, itemCompra.getQuantidade());
    }
}
