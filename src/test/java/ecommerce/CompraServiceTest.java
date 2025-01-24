package ecommerce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ecommerce.dto.CompraDTO;
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.TipoCliente;
import ecommerce.entity.TipoProduto;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import ecommerce.repository.ClienteRepository;
import ecommerce.service.CarrinhoDeComprasService;
import ecommerce.service.ClienteService;
import ecommerce.service.CompraService;
import java.math.RoundingMode;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyDouble;


class CompraServiceTest {

    private CompraService compraService;

    @Mock
    private CarrinhoDeComprasService carrinhoService;

    @Mock
    private ClienteService clienteService;

    @Mock
    private IEstoqueExternal estoqueExternal;

    @Mock
    private IPagamentoExternal pagamentoExternal;

    @Mock
    private ClienteRepository clienteRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        compraService = new CompraService(carrinhoService, clienteService, estoqueExternal, pagamentoExternal);
    }

    @Test
    void calcularCustoTotal_CarrinhoVazio_DeveRetornarZero() {
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(new Cliente(1L, "João", "Rua A, 123", TipoCliente.BRONZE));

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

        assertEquals(BigDecimal.ZERO, custoTotal);
    }

    @Test
    void calcularCustoTotal_FreteLeve_DeveCalcularFrete() {
        Produto produto = new Produto(1L, "Produto A", "Descrição A", BigDecimal.valueOf(100.0), 2, TipoProduto.ELETRONICO); // 2 kg por unidade
        ItemCompra item = new ItemCompra(1L, produto, 2L); // 4 kg total

        Cliente cliente = new Cliente(1L, "Maria", "Rua B, 456", TipoCliente.BRONZE);
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);
        carrinho.setItens(List.of(item));

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

        // Usando setScale com RoundingMode
        assertEquals(BigDecimal.valueOf(200.0).setScale(2, RoundingMode.HALF_UP), custoTotal.setScale(2, RoundingMode.HALF_UP)); // 200 (produtos) + 0 (até 5 kg não é cobrado frete)
    }

    @Test
    void calcularCustoTotal_FretePesado_DeveCalcularFrete() {
        Produto produto = new Produto(1L, "Produto B", "Descrição B", BigDecimal.valueOf(150.0), 20, TipoProduto.MOVEL); // 20 kg por unidade
        ItemCompra item = new ItemCompra(1L, produto, 3L); // 60 kg total

        Cliente cliente = new Cliente(1L, "Carlos", "Rua C, 789", TipoCliente.BRONZE);
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);
        carrinho.setItens(List.of(item));

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

        // Usando setScale com RoundingMode
        assertEquals(BigDecimal.valueOf(870.0).setScale(2, RoundingMode.HALF_UP), custoTotal.setScale(2, RoundingMode.HALF_UP)); // 450 (produtos) + 420 (acima de 50 kg é cobrado R$ 7,00 por kg)
    }

    @Test
    void calcularCustoTotal_ComDescontoPorValor_DeveAplicarDesconto() {
        Produto produto = new Produto(1L, "Produto C", "Descrição C", BigDecimal.valueOf(600.0), 1, TipoProduto.ROUPA); // 1 kg por unidade
        ItemCompra item = new ItemCompra(1L, produto, 1L); // 600 total

        Cliente cliente = new Cliente(1L, "Carlos", "Rua D, 012", TipoCliente.BRONZE);
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);
        carrinho.setItens(List.of(item));

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

        // Usando setScale com RoundingMode
        assertEquals(BigDecimal.valueOf(540.0).setScale(2, RoundingMode.HALF_UP), custoTotal.setScale(2, RoundingMode.HALF_UP)); // 600 - 60 (desconto) + 0 (até 5 kg não é cobrado frete)
    }

    @Test
    void calcularCustoTotal_ClienteOuro_DeveAplicarDescontoTotalNoFrete() {
        Produto produto = new Produto(1L, "Produto D", "Descrição D", BigDecimal.valueOf(300.0), 15, TipoProduto.ELETRONICO); // 15 kg por unidade
        ItemCompra item = new ItemCompra(1L, produto, 1L); // 15 kg total

        Cliente cliente = new Cliente(1L, "Ana", "Rua E, 345", TipoCliente.OURO);
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);
        carrinho.setItens(List.of(item));

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

        // Usando setScale com RoundingMode
        assertEquals(BigDecimal.valueOf(300.0).setScale(2, RoundingMode.HALF_UP), custoTotal.setScale(2, RoundingMode.HALF_UP)); // 300 + 0 (frete gratuito para OURO)
    }

    @Test
    void calcularCustoTotal_ClientePrata_DeveAplicarDescontoParcialNoFrete() {
        Produto produto = new Produto(1L, "Produto E", "Descrição E", BigDecimal.valueOf(300.0), 15, TipoProduto.ELETRONICO); // 15 kg por unidade
        ItemCompra item = new ItemCompra(1L, produto, 1L); // 15 kg total

        Cliente cliente = new Cliente(1L, "Ana", "Rua F, 678", TipoCliente.PRATA);
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);
        carrinho.setItens(List.of(item));

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

        // Usando setScale com RoundingMode
        assertEquals(BigDecimal.valueOf(330.0).setScale(2, RoundingMode.HALF_UP), custoTotal.setScale(2, RoundingMode.HALF_UP)); // 300 + 30 (50% do frete)
    }

    @Test
    void calcularCustoTotal_ClienteBronze_DeveAplicarFreteCompleto() {
        Produto produto = new Produto(1L, "Produto F", "Descrição F", BigDecimal.valueOf(300.0), 15, TipoProduto.ROUPA); // 15 kg por unidade
        ItemCompra item = new ItemCompra(1L, produto, 1L); // 15 kg total

        Cliente cliente = new Cliente(1L, "Ana", "Rua G, 901", TipoCliente.BRONZE);
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);
        carrinho.setItens(List.of(item));

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

        // Usando setScale com RoundingMode
        assertEquals(BigDecimal.valueOf(360.0).setScale(2, RoundingMode.HALF_UP), custoTotal.setScale(2, RoundingMode.HALF_UP)); // 300 + 60 (frete completo)
    }

    // Teste adicional para verificação de desconto de 20%
    @Test
    void calcularCustoTotal_ComDescontoDe20Porcento_DeveAplicarDesconto() {
        Produto produto = new Produto(1L, "Produto G", "Descrição G", BigDecimal.valueOf(1200.0), 2, TipoProduto.ROUPA); // 1200 total

        ItemCompra item = new ItemCompra(1L, produto, 1L); // 1200 total

        Cliente cliente = new Cliente(1L, "Marcos", "Rua H, 234", TipoCliente.BRONZE);
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);
        carrinho.setItens(List.of(item));

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho);

        // Usando setScale com RoundingMode
        assertEquals(BigDecimal.valueOf(960.0).setScale(2, RoundingMode.HALF_UP), custoTotal.setScale(2, RoundingMode.HALF_UP)); // 1200 - 240 (desconto de 20%)
    }

    @Test
    void finalizarCompra_DeveFinalizarCompraComSucesso() {
        // Mockando dependências
        Long carrinhoId = 1L;
        Long clienteId = 1L;
        Cliente cliente = new Cliente(clienteId, "Maria", "Rua B", TipoCliente.OURO);
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);
        
        // Mock das interações com os serviços
        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);
        
        // Mockando o estoque (verificando disponibilidade)
        when(estoqueExternal.verificarDisponibilidade(any(), any()))
            .thenReturn(new DisponibilidadeDTO(true, List.of()));  // Disponível, sem produtos indisponíveis

        // Mockando o pagamento (autorização)
        when(pagamentoExternal.autorizarPagamento(clienteId, BigDecimal.ZERO.doubleValue()))
                .thenReturn(new PagamentoDTO(true, 12345L));  // Pagamento autorizado

        // Mockando a baixa no estoque (sucesso)
        when(estoqueExternal.darBaixa(any(), any()))
                .thenReturn(new EstoqueBaixaDTO(true));  // Garantindo que a baixa no estoque seja bem-sucedida

        // Chamando o método de finalizar a compra
        CompraDTO compraDTO = compraService.finalizarCompra(carrinhoId, clienteId);
        
        // Verificando o comportamento esperado
        assertNotNull(compraDTO);
        
        // Verificando se a compra foi finalizada corretamente
        assertTrue(compraDTO.sucesso());  // Alterado para verificar o campo 'sucesso' em CompraDTO
        assertEquals(12345L, compraDTO.transacaoPagamentoId());  // Verificando o transacaoPagamentoId
        assertEquals("Compra finalizada com sucesso.", compraDTO.mensagem());  // Verificando a mensagem
    }

    @Test
    void finalizarCompra_EstoqueIndisponivel_DeveLancarExcecao() {
        Long carrinhoId = 1L;
        Long clienteId = 1L;

        // Mock do cliente e do carrinho
        Cliente cliente = new Cliente(clienteId, "João", "Rua A, 123", TipoCliente.BRONZE);
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);

        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);

        // Mock da indisponibilidade de estoque
        when(estoqueExternal.verificarDisponibilidade(any(), any()))
            .thenReturn(new DisponibilidadeDTO(false, List.of(1L))); // Item 1 indisponível

        // Verificar que a exceção é lançada
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> compraService.finalizarCompra(carrinhoId, clienteId));

        assertEquals("Itens fora de estoque.", exception.getMessage());
        
        verify(clienteService).buscarPorId(clienteId);
        verify(carrinhoService).buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);
        verify(estoqueExternal).verificarDisponibilidade(any(), any());
    }

    @Test
    void finalizarCompra_PagamentoNaoAutorizado_DeveLancarExcecao() {
        Long carrinhoId = 1L;
        Long clienteId = 1L;

        // Mock do cliente e do carrinho
        Cliente cliente = new Cliente(clienteId, "João", "Rua A, 123", TipoCliente.BRONZE);
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);

        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);

        // Mock de disponibilidade do estoque
        when(estoqueExternal.verificarDisponibilidade(any(), any()))
            .thenReturn(new DisponibilidadeDTO(true, List.of()));

        // Mock de pagamento não autorizado
        when(pagamentoExternal.autorizarPagamento(clienteId, 0.0))
            .thenReturn(new PagamentoDTO(false, null)); // Pagamento negado

        // Verificar que a exceção é lançada
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> compraService.finalizarCompra(carrinhoId, clienteId));

        assertEquals("Pagamento não autorizado.", exception.getMessage());
        
        verify(clienteService).buscarPorId(clienteId);
        verify(carrinhoService).buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);
        verify(estoqueExternal).verificarDisponibilidade(any(), any());
        verify(pagamentoExternal).autorizarPagamento(clienteId, 0.0);
    }

    @Test
    void finalizarCompra_BaixaEstoqueFalha_DeveLancarExcecao() {
        Long carrinhoId = 1L;
        Long clienteId = 1L;

        // Mock do cliente e do carrinho
        Cliente cliente = new Cliente(clienteId, "João", "Rua A, 123", TipoCliente.BRONZE);
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);

        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);

        // Mock de disponibilidade do estoque
        when(estoqueExternal.verificarDisponibilidade(any(), any()))
            .thenReturn(new DisponibilidadeDTO(true, List.of()));

        // Mock de pagamento autorizado
        when(pagamentoExternal.autorizarPagamento(clienteId, 0.0))
            .thenReturn(new PagamentoDTO(true, 12345L));

        // Mock de falha na baixa de estoque
        when(estoqueExternal.darBaixa(any(), any()))
            .thenReturn(new EstoqueBaixaDTO(false));

        // Verificar que a exceção é lançada
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> compraService.finalizarCompra(carrinhoId, clienteId));

        assertEquals("Erro ao dar baixa no estoque.", exception.getMessage());
        
        verify(clienteService).buscarPorId(clienteId);
        verify(carrinhoService).buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);
        verify(estoqueExternal).verificarDisponibilidade(any(), any());
        verify(pagamentoExternal).autorizarPagamento(clienteId, 0.0);
        verify(estoqueExternal).darBaixa(any(), any());
    }

    @Test
    void calcularFrete_PesoExatamente50kg_DeveCobrarFrete4() {
        Produto produto = new Produto(1L, "Produto A", "Descrição A", BigDecimal.valueOf(100.0), 25, TipoProduto.MOVEL);
        ItemCompra item = new ItemCompra(1L, produto, 2L); // Total: 50kg

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(item));

        BigDecimal frete = compraService.calcularFrete(carrinho);

        assertEquals(BigDecimal.valueOf(200).setScale(2, RoundingMode.HALF_UP), frete.setScale(2, RoundingMode.HALF_UP)); // 50kg * 4
    }
    
    @Test
    void calcularFrete_PesoExatamente51kg_DeveCobrarFrete7() {
        Produto produto = new Produto(1L, "Produto A7", "Descrição A7", BigDecimal.valueOf(100.0), 51, TipoProduto.MOVEL);
        ItemCompra item = new ItemCompra(1L, produto, 1L); // Total: 51kg

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(item));

        BigDecimal frete = compraService.calcularFrete(carrinho);

        assertEquals(BigDecimal.valueOf(357).setScale(2, RoundingMode.HALF_UP), frete.setScale(2, RoundingMode.HALF_UP)); // 51kg * 7
    }
    
    @Test
    void calcularFrete_PesoExatamente10kg_DeveCobrarFrete2() {
        Produto produto = new Produto(1L, "Produto A", "Descrição A", BigDecimal.valueOf(100.0), 5, TipoProduto.MOVEL);
        ItemCompra item = new ItemCompra(1L, produto, 2L); // Total: 10kg

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(item));

        BigDecimal frete = compraService.calcularFrete(carrinho);

        assertEquals(BigDecimal.valueOf(20).setScale(2, RoundingMode.HALF_UP), frete.setScale(2, RoundingMode.HALF_UP)); // 10kg * 2
    }
    
    @Test
    void calcularFrete_PesoExatamente11kg_DeveCobrarFrete4() {
        Produto produto = new Produto(1L, "Produto A4", "Descrição A4", BigDecimal.valueOf(100.0), 11, TipoProduto.MOVEL);
        ItemCompra item = new ItemCompra(1L, produto, 1L); // Total: 11kg

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(item));

        BigDecimal frete = compraService.calcularFrete(carrinho);

        assertEquals(BigDecimal.valueOf(44).setScale(2, RoundingMode.HALF_UP), frete.setScale(2, RoundingMode.HALF_UP)); // 11kg * 4
    }
    
    @Test
    void calcularFrete_PesoExatamente5kg_DeveNaoCobrarFrete() {
        Produto produto = new Produto(1L, "Produto A", "Descrição A", BigDecimal.valueOf(50.0), 5, TipoProduto.ELETRONICO);
        ItemCompra item = new ItemCompra(1L, produto, 1L); // Total: 5 kg

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(item));

        BigDecimal frete = compraService.calcularFrete(carrinho);

        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), frete.setScale(2, RoundingMode.HALF_UP)); // Frete grátis
    }
    
    @Test
    void calcularFrete_PesoExatamente6kg_DeveCobrarFrete2() {
        Produto produto = new Produto(1L, "Produto A2", "Descrição A2", BigDecimal.valueOf(100.0), 3, TipoProduto.MOVEL);
        ItemCompra item = new ItemCompra(1L, produto, 2L); // Total: 6kg

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(item));

        BigDecimal frete = compraService.calcularFrete(carrinho);

        assertEquals(BigDecimal.valueOf(12).setScale(2, RoundingMode.HALF_UP), frete.setScale(2, RoundingMode.HALF_UP)); // 6kg * 2
    }

    @Test
    void calcularDesconto_ValorExatamente1000_DeveAplicar10Porcento() {
        BigDecimal totalProdutos = BigDecimal.valueOf(1000);

        BigDecimal desconto = compraService.calcularDesconto(totalProdutos);

        assertEquals(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP), desconto.setScale(2, RoundingMode.HALF_UP)); // 10% de 1000
    }

    @Test
    void calcularDesconto_ValorExatamente1001_DeveAplicar20Porcento() {
        BigDecimal totalProdutos = BigDecimal.valueOf(1001);

        BigDecimal desconto = compraService.calcularDesconto(totalProdutos);

        assertEquals(BigDecimal.valueOf(200.2).setScale(2, RoundingMode.HALF_UP), desconto.setScale(2, RoundingMode.HALF_UP)); // 20% de 1001
    }
    
    @Test
    void calcularDesconto_ValorExatamente500_NaoDeveAplicar10Porcento() {
        BigDecimal totalProdutos = BigDecimal.valueOf(500);

        BigDecimal desconto = compraService.calcularDesconto(totalProdutos);

        assertEquals(BigDecimal.valueOf(0).setScale(2, RoundingMode.HALF_UP), desconto.setScale(2, RoundingMode.HALF_UP)); // 0% de 500
    }
    
    @Test
    void calcularDesconto_ValorExatamente501_DeveAplicar10Porcento() {
        BigDecimal totalProdutos = BigDecimal.valueOf(501);

        BigDecimal desconto = compraService.calcularDesconto(totalProdutos);

        assertEquals(BigDecimal.valueOf(50.10).setScale(2, RoundingMode.HALF_UP), desconto.setScale(2, RoundingMode.HALF_UP)); // 10% de 501
    }

    @Test
    void calcularDesconto_Valor499_99_NaoDeveAplicarDesconto() {
        BigDecimal totalProdutos = BigDecimal.valueOf(499.99);

        BigDecimal desconto = compraService.calcularDesconto(totalProdutos);

        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), desconto.setScale(2, RoundingMode.HALF_UP)); // Sem desconto
    }

    @Test
    void finalizarCompra_DeveChamarServicosExternos() {
        Long carrinhoId = 1L;
        Long clienteId = 1L;

        Cliente cliente = new Cliente(clienteId, "Maria", "Rua B", TipoCliente.BRONZE);
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);

        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);
        when(estoqueExternal.verificarDisponibilidade(any(), any()))
            .thenReturn(new DisponibilidadeDTO(true, List.of()));
        when(pagamentoExternal.autorizarPagamento(anyLong(), anyDouble()))
            .thenReturn(new PagamentoDTO(true, 12345L));
        when(estoqueExternal.darBaixa(any(), any()))
            .thenReturn(new EstoqueBaixaDTO(true));

        compraService.finalizarCompra(carrinhoId, clienteId);

        verify(clienteService).buscarPorId(clienteId);
        verify(carrinhoService).buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);
        verify(estoqueExternal).verificarDisponibilidade(any(), any());
        verify(pagamentoExternal).autorizarPagamento(clienteId, 0.0);
        verify(estoqueExternal).darBaixa(any(), any());
    }

    
    @Test
    void finalizarCompra_DeveExtrairIdsDosProdutosDoCarrinho() {
        // Arrange
        Long carrinhoId = 1L;
        Long clienteId = 1L;

        Produto produto1 = new Produto(1L, "Produto 1", "Descrição 1", BigDecimal.valueOf(50.0), 5, TipoProduto.ROUPA);
        Produto produto2 = new Produto(2L, "Produto 2", "Descrição 2", BigDecimal.valueOf(100.0), 10, TipoProduto.ELETRONICO);

        ItemCompra item1 = new ItemCompra(1L, produto1, 2L); // Quantidade 2
        ItemCompra item2 = new ItemCompra(2L, produto2, 1L); // Quantidade 1

        Cliente cliente = new Cliente(clienteId, "João", "Rua A, 123", TipoCliente.BRONZE);
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);
        carrinho.setItens(List.of(item1, item2));

        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);
        when(estoqueExternal.verificarDisponibilidade(any(), any()))
        .thenReturn(new DisponibilidadeDTO(true, List.of()));
        when(pagamentoExternal.autorizarPagamento(anyLong(), anyDouble()))
        .thenReturn(new PagamentoDTO(true, 12345L));
        when(estoqueExternal.darBaixa(any(), any()))
            .thenReturn(new EstoqueBaixaDTO(true));

        // Act
        CompraDTO compra = compraService.finalizarCompra(carrinhoId, clienteId);

        // Assert
        assertNotNull(compra);
        assertTrue(compra.sucesso());
        verify(estoqueExternal).verificarDisponibilidade(
            List.of(1L, 2L), // Verifica os IDs dos produtos extraídos
            List.of(2L, 1L)  // Verifica as quantidades extraídas
        );
    }

    @Test
    void finalizarCompra_DeveUsarQuantidadesCorretasDosItens() {
        // Arrange
        Long carrinhoId = 1L;
        Long clienteId = 1L;

        Produto produto = new Produto(1L, "Produto Teste", "Descrição", BigDecimal.valueOf(50.0), 5, TipoProduto.ROUPA);
        ItemCompra item = new ItemCompra(1L, produto, 3L); // Quantidade 3

        Cliente cliente = new Cliente(clienteId, "João", "Rua A, 123", TipoCliente.BRONZE);
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setCliente(cliente);
        carrinho.setItens(List.of(item));

        when(clienteService.buscarPorId(clienteId)).thenReturn(cliente);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente)).thenReturn(carrinho);
        when(estoqueExternal.verificarDisponibilidade(any(), any()))
        .thenReturn(new DisponibilidadeDTO(true, List.of()));
        when(pagamentoExternal.autorizarPagamento(anyLong(), anyDouble()))
        .thenReturn(new PagamentoDTO(true, 12345L));
        when(estoqueExternal.darBaixa(any(), any()))
            .thenReturn(new EstoqueBaixaDTO(true));

        // Act
        CompraDTO compra = compraService.finalizarCompra(carrinhoId, clienteId);

        // Assert
        assertNotNull(compra);
        assertTrue(compra.sucesso());
        verify(estoqueExternal).verificarDisponibilidade(
            List.of(1L),    // ID do produto
            List.of(3L)     // Quantidade do produto
        );
    }

    @Test
    void finalizarCompra_DeveProcessarIdsEQuantidadesDosItensCorretamente() {
        // Arrange
        Produto produto1 = new Produto(1L, "Produto 1", "Descrição 1", BigDecimal.valueOf(50.0), 5, TipoProduto.ROUPA);
        Produto produto2 = new Produto(2L, "Produto 2", "Descrição 2", BigDecimal.valueOf(100.0), 10, TipoProduto.ELETRONICO);

        ItemCompra item1 = new ItemCompra(1L, produto1, 2L); // Quantidade 2
        ItemCompra item2 = new ItemCompra(2L, produto2, 1L); // Quantidade 1

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(List.of(item1, item2));

        // Act
        List<Long> ids = carrinho.getItens().stream().map(i -> i.getProduto().getId()).toList();
        List<Long> quantidades = carrinho.getItens().stream().map(i -> i.getQuantidade()).toList();

        // Assert
        assertEquals(List.of(1L, 2L), ids); // Verifica IDs extraídos
        assertEquals(List.of(2L, 1L), quantidades); // Verifica quantidades extraídas
    }
    
}
