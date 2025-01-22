package ecommerce.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ecommerce.dto.CompraDTO;
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.TipoCliente;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import jakarta.transaction.Transactional;

@Service
public class CompraService {

	private final CarrinhoDeComprasService carrinhoService;
	private final ClienteService clienteService;

	private final IEstoqueExternal estoqueExternal;
	private final IPagamentoExternal pagamentoExternal;

	public CompraService(CarrinhoDeComprasService carrinhoService, ClienteService clienteService,
			IEstoqueExternal estoqueExternal, IPagamentoExternal pagamentoExternal) {
		this.carrinhoService = carrinhoService;
		this.clienteService = clienteService;

		this.estoqueExternal = estoqueExternal;
		this.pagamentoExternal = pagamentoExternal;
	}

	@Transactional
	public CompraDTO finalizarCompra(Long carrinhoId, Long clienteId) {
		Cliente cliente = clienteService.buscarPorId(clienteId);
		CarrinhoDeCompras carrinho = carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);

		List<Long> produtosIds = carrinho.getItens().stream().map(i -> i.getProduto().getId())
				.collect(Collectors.toList());
		List<Long> produtosQtds = carrinho.getItens().stream().map(i -> i.getQuantidade()).collect(Collectors.toList());

		DisponibilidadeDTO disponibilidade = estoqueExternal.verificarDisponibilidade(produtosIds, produtosQtds);

		if (!disponibilidade.disponivel()) {
			throw new IllegalStateException("Itens fora de estoque.");
		}

		BigDecimal custoTotal = calcularCustoTotal(carrinho);

		PagamentoDTO pagamento = pagamentoExternal.autorizarPagamento(cliente.getId(), custoTotal.doubleValue());

		if (!pagamento.autorizado()) {
			throw new IllegalStateException("Pagamento não autorizado.");
		}

		EstoqueBaixaDTO baixaDTO = estoqueExternal.darBaixa(produtosIds, produtosQtds);

		if (!baixaDTO.sucesso()) {
			pagamentoExternal.cancelarPagamento(cliente.getId(), pagamento.transacaoId());
			throw new IllegalStateException("Erro ao dar baixa no estoque.");
		}

		CompraDTO compraDTO = new CompraDTO(true, pagamento.transacaoId(), "Compra finalizada com sucesso.");

		return compraDTO;
	}


	public BigDecimal calcularFrete(CarrinhoDeCompras carrinho){
		int pesoTotal = carrinho.getItens().stream()
		.mapToInt(item -> item.getProduto().getPeso() * item.getQuantidade().intValue()).sum();

		BigDecimal frete = BigDecimal.ZERO;
		if(pesoTotal > 50){
			frete = BigDecimal.valueOf(pesoTotal * 7);
		}else if( pesoTotal > 10){
			frete = BigDecimal.valueOf(pesoTotal * 4);
		}else if(pesoTotal > 5){
			frete = BigDecimal.valueOf(pesoTotal * 2);
		}

		return frete;
	}

	public BigDecimal calcularDesconto(BigDecimal totalProdutos){
		BigDecimal desconto = BigDecimal.ZERO;

		if(totalProdutos.compareTo(BigDecimal.valueOf(1000)) > 0){
			desconto = totalProdutos.multiply(BigDecimal.valueOf(0.20));
		} else if (totalProdutos.compareTo(BigDecimal.valueOf(500)) > 0){
			desconto = totalProdutos.multiply(BigDecimal.valueOf(0.10));
		}

		return desconto;
	}

	public BigDecimal calcularDescontoTipoCliente(Cliente cliente, BigDecimal frete){
		switch (cliente.getTipo()) {
			case OURO:
				return frete;
			case PRATA:
				return frete.multiply(BigDecimal.valueOf(0.50));
			case BRONZE:
			default:
				return BigDecimal.ZERO;
		}
	}



	public BigDecimal calcularCustoTotal(CarrinhoDeCompras carrinho) {
		BigDecimal totalProdutos = carrinho.getItens().stream()
		.map(item -> item.getProduto().getPreco().multiply(BigDecimal.valueOf(item.getQuantidade())))
		.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal frete = calcularFrete(carrinho);
		BigDecimal desconto = calcularDesconto(totalProdutos);
		BigDecimal descontoTipoCliente = calcularDescontoTipoCliente(carrinho.getCliente(), frete);

		BigDecimal custoTotal = totalProdutos.subtract(desconto).add(frete).subtract(descontoTipoCliente);

		return custoTotal;
	}
}
