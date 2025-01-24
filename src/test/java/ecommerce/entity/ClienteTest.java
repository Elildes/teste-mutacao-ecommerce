package ecommerce.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ecommerce.repository.ClienteRepository;
import ecommerce.service.ClienteService;

@ExtendWith(MockitoExtension.class)
public class ClienteTest {

    @Mock
    private ClienteRepository repository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente(1L, "João", "Rua A, 123", null);
    }

    @Test
    void buscarPorId_DeveRetornarClienteQuandoEncontrado() {
        // Configurando o mock para simular o retorno de um cliente válido
        when(repository.findById(1L)).thenReturn(Optional.of(cliente));

        // Executando o método
        Cliente resultado = clienteService.buscarPorId(1L);

        // Verificações
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("João", resultado.getNome());
        assertEquals("Rua A, 123", resultado.getEndereco());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void buscarPorId_DeveLancarExcecaoQuandoClienteNaoEncontrado() {
        // Configurando o mock para simular um retorno vazio
        when(repository.findById(1L)).thenReturn(Optional.empty());

        // Executando o método e verificando se a exceção é lançada
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.buscarPorId(1L);
        });

        // Verificando a mensagem da exceção
        assertEquals("Cliente não encontrado", exception.getMessage());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    void setId_DeveAtualizarIdDoCliente() {
        // Atualizando o ID do cliente
        cliente.setId(2L);

        // Verificação
        assertEquals(2L, cliente.getId());
    }

    @Test
    void setNome_DeveAtualizarNomeDoCliente() {
        // Atualizando o nome do cliente
        cliente.setNome("Maria");

        // Verificação
        assertEquals("Maria", cliente.getNome());
    }

    @Test
    void setEndereco_DeveAtualizarEnderecoDoCliente() {
        // Atualizando o endereço do cliente
        cliente.setEndereco("Rua B, 456");

        // Verificação
        assertEquals("Rua B, 456", cliente.getEndereco());
    }

    @Test
    void setTipo_DeveAtualizarTipoDoCliente() {
        // Atualizando o tipo do cliente
        cliente.setTipo(TipoCliente.OURO);

        // Verificação
        assertEquals(TipoCliente.OURO, cliente.getTipo());
    }
    
}
