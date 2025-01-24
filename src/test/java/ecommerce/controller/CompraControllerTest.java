package ecommerce.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ecommerce.dto.CompraDTO;
import ecommerce.service.CompraService;

@ExtendWith(MockitoExtension.class)
class CompraControllerTest {

    @Mock
    private CompraService compraService;

    @InjectMocks
    private CompraController compraController;

    private Long carrinhoId;
    private Long clienteId;

    @BeforeEach
    void setUp() {
        carrinhoId = 1L;
        clienteId = 1L;
    }

    @Test
    void finalizarCompra_DeveRetornarOkQuandoCompraBemSucedida() {
        // Arrange
        CompraDTO compraDTO = new CompraDTO(true, 123L, "Compra finalizada com sucesso.");
        when(compraService.finalizarCompra(carrinhoId, clienteId)).thenReturn(compraDTO);

        // Act
        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(carrinhoId, clienteId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(compraDTO, response.getBody());
        verify(compraService, times(1)).finalizarCompra(carrinhoId, clienteId);
    }

    @Test
    void finalizarCompra_DeveRetornarBadRequestQuandoIllegalArgumentException() {
        // Arrange
        String mensagemErro = "Carrinho não encontrado.";
        doThrow(new IllegalArgumentException(mensagemErro)).when(compraService).finalizarCompra(carrinhoId, clienteId);

        // Act
        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(carrinhoId, clienteId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(new CompraDTO(false, null, mensagemErro), response.getBody());
        verify(compraService, times(1)).finalizarCompra(carrinhoId, clienteId);
    }

    @Test
    void finalizarCompra_DeveRetornarConflictQuandoIllegalStateException() {
        // Arrange
        String mensagemErro = "Itens fora de estoque.";
        doThrow(new IllegalStateException(mensagemErro)).when(compraService).finalizarCompra(carrinhoId, clienteId);

        // Act
        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(carrinhoId, clienteId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(new CompraDTO(false, null, mensagemErro), response.getBody());
        verify(compraService, times(1)).finalizarCompra(carrinhoId, clienteId);
    }

    @Test
    void finalizarCompra_DeveRetornarInternalServerErrorQuandoErroNaoTratado() {
        // Arrange
        doThrow(new RuntimeException("Erro desconhecido.")).when(compraService).finalizarCompra(carrinhoId, clienteId);

        // Act
        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(carrinhoId, clienteId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(new CompraDTO(false, null, "Erro ao processar compra."), response.getBody());
        verify(compraService, times(1)).finalizarCompra(carrinhoId, clienteId);
    }
}
