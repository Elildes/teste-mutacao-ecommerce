package ecommerce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

 
}
