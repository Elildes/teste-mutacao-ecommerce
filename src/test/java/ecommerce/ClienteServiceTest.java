package ecommerce;

import ecommerce.entity.Cliente;
import ecommerce.repository.ClienteRepository;
import ecommerce.service.ClienteService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClienteServiceTest {

    private ClienteRepository repository;
    private ClienteService service;

    @BeforeEach
    void setUp() {
        // Configuração inicial: mock do repositório e inicialização do serviço
        repository = mock(ClienteRepository.class);
        service = new ClienteService(repository);
    }

    @Test
    void deveRetornarClienteQuandoEncontrado() {
        // Cenário: Configurar um cliente existente
        Cliente cliente = new Cliente(1L, "Maria", "Rua A, 123", null);

        // Configurar o comportamento do mock para retornar o cliente
        when(repository.findById(1L)).thenReturn(Optional.of(cliente));

        // Ação: Chamar o método do serviço para buscar o cliente
        Cliente resultado = service.buscarPorId(1L);

        // Verificação: Validar que o cliente retornado não é nulo e corresponde ao esperado
        assertNotNull(resultado);
        assertEquals(cliente, resultado);

        // Verificar que o método do repositório foi chamado uma vez
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoForEncontrado() {
        // Cenário: Garantir que o repositório não encontra o cliente
        when(repository.findById(1L)).thenReturn(Optional.empty());

        // Ação e verificação: Validar que o método lança IllegalArgumentException com a mensagem esperada
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.buscarPorId(1L)
        );

        assertEquals("Cliente não encontrado", exception.getMessage());

        // Verificar que o método do repositório foi chamado uma vez
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void deveLidarComRepositorioRetornandoNulo() {
        // Cenário: Simular comportamento inesperado no repositório (retorno nulo)
        when(repository.findById(1L)).thenReturn(null);

        // Ação e verificação: Validar que o método lança NullPointerException
        assertThrows(NullPointerException.class, () -> service.buscarPorId(1L));

        // Verificar que o método do repositório foi chamado uma vez
        verify(repository, times(1)).findById(1L);
    }
}
