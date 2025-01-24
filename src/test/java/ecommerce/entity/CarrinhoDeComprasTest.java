package ecommerce.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CarrinhoDeComprasTest {

    private CarrinhoDeCompras carrinho;
    private Cliente cliente;
    private List<ItemCompra> itens;

    @BeforeEach
    void setUp() {
        // Inicializando objetos necessários para os testes
        cliente = new Cliente(1L, "João Silva", "Rua Teste, 123", TipoCliente.OURO);

        Produto produto = new Produto(1L, "Produto Teste", "Descrição Produto Teste", BigDecimal.valueOf(100.00), 5, TipoProduto.ELETRONICO);
        ItemCompra itemCompra = new ItemCompra(1L, produto, 2L);
        itens = new ArrayList<>();
        itens.add(itemCompra);

        carrinho = new CarrinhoDeCompras(1L, cliente, itens, LocalDate.now());
    }

    @Test
    void testGetId() {
        // Verifica o ID inicial do carrinho
        assertEquals(1L, carrinho.getId());
    }

    @Test
    void testSetId() {
        // Atualiza o ID do carrinho e verifica
        carrinho.setId(2L);
        assertEquals(2L, carrinho.getId());
    }

    @Test
    void testGetCliente() {
        // Verifica o cliente associado ao carrinho
        assertEquals(cliente, carrinho.getCliente());
    }

    @Test
    void testSetCliente() {
        // Atualiza o cliente e verifica
        Cliente novoCliente = new Cliente(2L, "Maria Oliveira", "Rua Nova, 456", TipoCliente.PRATA);
        carrinho.setCliente(novoCliente);
        assertEquals(novoCliente, carrinho.getCliente());
    }

    @Test
    void testGetItens() {
        // Verifica os itens associados ao carrinho
        assertEquals(itens, carrinho.getItens());
    }

    @Test
    void testSetItens() {
        // Atualiza os itens do carrinho e verifica
        Produto novoProduto = new Produto(2L, "Outro Produto", "Outra Descrição", BigDecimal.valueOf(200.00), 10, TipoProduto.ROUPA);
        ItemCompra novoItem = new ItemCompra(2L, novoProduto, 1L);

        List<ItemCompra> novosItens = new ArrayList<>();
        novosItens.add(novoItem);

        carrinho.setItens(novosItens);
        assertEquals(novosItens, carrinho.getItens());
    }

    @Test
    void testGetData() {
        // Verifica a data do carrinho
        assertNotNull(carrinho.getData());
        assertEquals(LocalDate.now(), carrinho.getData());
    }

    @Test
    void testSetData() {
        // Atualiza a data do carrinho e verifica
        LocalDate novaData = LocalDate.of(2022, 12, 25);
        carrinho.setData(novaData);
        assertEquals(novaData, carrinho.getData());
    }

    @Test
    void testCarrinhoConstrutorPadrao() {
        // Verifica a inicialização do carrinho com o construtor padrão
        CarrinhoDeCompras carrinhoPadrao = new CarrinhoDeCompras();
        assertNotNull(carrinhoPadrao);
    }
}
