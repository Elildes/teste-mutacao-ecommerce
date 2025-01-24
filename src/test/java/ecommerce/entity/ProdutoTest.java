package ecommerce.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProdutoTest {

    private Produto produto;

    @BeforeEach
    void setUp() {
        // Inicializando o objeto Produto antes de cada teste
        produto = new Produto(1L, "Produto Teste", "Descrição do Produto Teste", BigDecimal.valueOf(100.00), 10, TipoProduto.ELETRONICO);
    }

    @Test
    void testGetId() {
        // Verificando o ID inicial
        assertEquals(1L, produto.getId());
    }

    @Test
    void testSetId() {
        // Atualizando o ID e verificando
        produto.setId(2L);
        assertEquals(2L, produto.getId());
    }

    @Test
    void testGetNome() {
        // Verificando o nome inicial
        assertEquals("Produto Teste", produto.getNome());
    }

    @Test
    void testSetNome() {
        // Atualizando o nome e verificando
        produto.setNome("Novo Nome");
        assertEquals("Novo Nome", produto.getNome());
    }

    @Test
    void testGetDescricao() {
        // Verificando a descrição inicial
        assertEquals("Descrição do Produto Teste", produto.getDescricao());
    }

    @Test
    void testSetDescricao() {
        // Atualizando a descrição e verificando
        produto.setDescricao("Nova Descrição");
        assertEquals("Nova Descrição", produto.getDescricao());
    }

    @Test
    void testGetPreco() {
        // Verificando o preço inicial
        assertEquals(BigDecimal.valueOf(100.00), produto.getPreco());
    }

    @Test
    void testSetPreco() {
        // Atualizando o preço e verificando
        produto.setPreco(BigDecimal.valueOf(200.00));
        assertEquals(BigDecimal.valueOf(200.00), produto.getPreco());
    }

    @Test
    void testGetPeso() {
        // Verificando o peso inicial
        assertEquals(10, produto.getPeso());
    }

    @Test
    void testSetPeso() {
        // Atualizando o peso e verificando
        produto.setPeso(20);
        assertEquals(20, produto.getPeso());
    }

    @Test
    void testGetTipo() {
        // Verificando o tipo inicial
        assertEquals(TipoProduto.ELETRONICO, produto.getTipo());
    }

    @Test
    void testSetTipo() {
        // Atualizando o tipo e verificando
        produto.setTipo(TipoProduto.ROUPA);
        assertEquals(TipoProduto.ROUPA, produto.getTipo());
    }

    @Test
    void testProdutoConstrutorPadrao() {
        // Verificando a inicialização com o construtor padrão
        Produto produtoPadrao = new Produto();
        assertNotNull(produtoPadrao);
    }
}
