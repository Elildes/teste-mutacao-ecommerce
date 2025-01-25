package ecommerce.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    public void testEmptyItensList() {
        // verifica se a lista tá vazia
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        assertEquals(0, carrinho.getItens().size());
    }

    // @Test
    // void testInicializacaoItensComConstrutorPadrao() {
    //     // Testa o construtor padrão
    //     CarrinhoDeCompras carrinhoPadrao = new CarrinhoDeCompras();
    //     assertNotNull(carrinhoPadrao.getItens(), "A lista 'itens' deve ser inicializada como um ArrayList vazio.");
    //     assertTrue(carrinhoPadrao.getItens().isEmpty(), "A lista 'itens' deve estar vazia inicialmente.");
    // }

    @Test
    void testInicializacaoListaItens() {
        // Testa a inicialização padrão do atributo 'itens'
        CarrinhoDeCompras carrinhoPadrao = new CarrinhoDeCompras();
        assertNotNull(carrinhoPadrao.getItens(), "A lista 'itens' deve ser inicializada como um ArrayList.");
        assertTrue(carrinhoPadrao.getItens().isEmpty(), "A lista 'itens' deve estar vazia ao usar o construtor padrão.");
    }
 
    // @Test
    // void testAdicionarItemNaListaItens() {
    //     // Testa se itens podem ser adicionados diretamente à lista
    //     Produto novoProduto = new Produto(2L, "Outro Produto", "Outra Descrição", BigDecimal.valueOf(50.00), 3, TipoProduto.ROUPA);
    //     ItemCompra novoItem = new ItemCompra(2L, novoProduto, 1L);

    //     carrinho.getItens().add(novoItem);

    //     assertEquals(2, carrinho.getItens().size(), "O tamanho da lista 'itens' deve refletir os itens adicionados.");
    //     assertTrue(carrinho.getItens().contains(novoItem), "O item recém-adicionado deve estar presente na lista.");
    // }

    @Test
    void testAdicionarItemNaLista() {
        // Testa a adição de itens na lista 'itens'
        Produto novoProduto = new Produto(2L, "Produto Novo", "Descrição Nova", BigDecimal.valueOf(50.00), 2, TipoProduto.ROUPA);
        ItemCompra novoItem = new ItemCompra(2L, novoProduto, 1L);

        carrinho.getItens().add(novoItem);

        assertEquals(2, carrinho.getItens().size(), "A lista 'itens' deve conter 2 itens após adicionar um novo item.");
        assertTrue(carrinho.getItens().contains(novoItem), "O novo item deve estar presente na lista 'itens'.");
    }

    @Test
    void testRemoverItemDaListaItens() {
        // Testa se itens podem ser removidos diretamente da lista
        carrinho.getItens().remove(0);

        assertTrue(carrinho.getItens().isEmpty(), "A lista 'itens' deve estar vazia após remover o único item.");
    }

    @Test
    void testSubstituirListaItens() {
        // Substitui a lista 'itens' diretamente usando o setter
        Produto outroProduto = new Produto(3L, "Produto Extra", "Descrição Extra", BigDecimal.valueOf(30.00), 1, TipoProduto.ALIMENTO);
        ItemCompra outroItem = new ItemCompra(3L, outroProduto, 1L);

        List<ItemCompra> novaLista = new ArrayList<>();
        novaLista.add(outroItem);

        carrinho.setItens(novaLista);

        assertEquals(novaLista, carrinho.getItens(), "A lista 'itens' deve ser substituída corretamente.");
        assertEquals(1, carrinho.getItens().size(), "A nova lista deve conter 1 item.");
        assertTrue(carrinho.getItens().contains(outroItem), "O item da nova lista deve estar presente.");
    }

    @Test
    void testConstrutorPadraoMantemListaVazia() {
        // Testa se a lista permanece funcional após o construtor padrão
        CarrinhoDeCompras carrinhoVazio = new CarrinhoDeCompras();
        carrinhoVazio.getItens().add(new ItemCompra(10L, new Produto(10L, "Teste", "Teste", BigDecimal.ONE, 1, TipoProduto.ALIMENTO), 1L));

        assertEquals(1, carrinhoVazio.getItens().size(), "A lista 'itens' deve permitir adição mesmo após construtor padrão.");
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

    // @Test
    // void testCarrinhoConstrutorPadrao() {
    //     // Verifica a inicialização do carrinho com o construtor padrão
    //     CarrinhoDeCompras carrinhoPadrao = new CarrinhoDeCompras();
    //     assertNotNull(carrinhoPadrao);
    //     assertNotNull(carrinhoPadrao.getItens());
    //     assertEquals(0, carrinhoPadrao.getItens().size()); // Garante que a lista foi inicializada vazia
    // }
}
