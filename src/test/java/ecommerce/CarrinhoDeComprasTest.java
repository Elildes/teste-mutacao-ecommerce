package ecommerce;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.repository.CarrinhoDeComprasRepository;
import ecommerce.service.CarrinhoDeComprasService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CarrinhoDeComprasServiceTest {

    private CarrinhoDeComprasRepository repository;
    private CarrinhoDeComprasService service;

    @BeforeEach
    void setUp() {
        // Configuração inicial: mock do repositório e inicialização do serviço
        repository = mock(CarrinhoDeComprasRepository.class);
        service = new CarrinhoDeComprasService(repository);
    }

    @Test
    void deveRetornarCarrinhoQuandoEncontrado() {
        // Cenário: Configurar um cliente e um carrinho existente
        Cliente cliente = new Cliente(1L, "Maria", "Rua A, 123", null);
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras(1L, cliente, null, null);

        // Configurar o comportamento do mock para retornar o carrinho
        when(repository.findByIdAndCliente(1L, cliente)).thenReturn(Optional.of(carrinho));

        // Ação: Chamar o método do serviço para buscar o carrinho
        CarrinhoDeCompras resultado = service.buscarPorCarrinhoIdEClienteId(1L, cliente);

        // Verificação: Validar que o carrinho retornado não é nulo e corresponde ao esperado
        assertNotNull(resultado);
        assertEquals(carrinho, resultado);
        
        // Verificar que o método do repositório foi chamado uma vez
        verify(repository, times(1)).findByIdAndCliente(1L, cliente);
    }

    @Test
    void deveLancarExcecaoQuandoCarrinhoNaoForEncontrado() {
        // Cenário: Configurar um cliente e garantir que o repositório não encontra o carrinho
        Cliente cliente = new Cliente(1L, "Maria", "Rua A, 123", null);

        // Configurar o comportamento do mock para retornar um Optional vazio
        when(repository.findByIdAndCliente(1L, cliente)).thenReturn(Optional.empty());

        // Ação e verificação: Validar que o método lança IllegalArgumentException com a mensagem esperada
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.buscarPorCarrinhoIdEClienteId(1L, cliente)
        );

        // Verificar a mensagem da exceção
        assertEquals("Carrinho não encontrado.", exception.getMessage());
        
        // Verificar que o método do repositório foi chamado uma vez
        verify(repository, times(1)).findByIdAndCliente(1L, cliente);
    }

    @Test
    void deveLidarComRepositorioRetornandoNulo() {
        // Cenário: Configurar um cliente e simular comportamento inesperado no repositório (retorno nulo)
        Cliente cliente = new Cliente(1L, "Maria", "Rua A, 123", null);

        // Configurar o comportamento do mock para retornar null
        when(repository.findByIdAndCliente(1L, cliente)).thenReturn(null);

        // Ação e verificação: Validar que o método lança NullPointerException
        assertThrows(NullPointerException.class, () -> service.buscarPorCarrinhoIdEClienteId(1L, cliente));

        // Verificar que o método do repositório foi chamado uma vez
        verify(repository, times(1)).findByIdAndCliente(1L, cliente);
    }
}
