package ecommerce.doublet;

import ecommerce.dto.*;
import ecommerce.controller.*;
import ecommerce.service.CompraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
public class DoubleTest {
    @Mock
    private CompraService compraService;

    @InjectMocks
    private CompraController compraController;

    private CompraDTO compraSucesso;
    private CompraDTO compraFalhaEstoque;
    private CompraDTO compraFalhaPagamento;

    @BeforeEach
    public void setup() {
        // Criação de diferentes respostas simuladas para os testes
        compraSucesso = new CompraDTO(true, 1234L, "Compra finalizada com sucesso.");
        compraFalhaEstoque = new CompraDTO(false, null, "Itens fora de estoque.");
        compraFalhaPagamento = new CompraDTO(false, null, "Pagamento não autorizado.");
    }

    @Test
    public void testFinalizarCompraComSucesso() {
        // Simula o comportamento do serviço de compra retornando sucesso
        when(compraService.finalizarCompra(anyLong(), anyLong())).thenReturn(compraSucesso);

        // Chama o endpoint da controller
        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(1L, 1L);

        // Verifica se o status da resposta é 200 OK e se a resposta é a esperada
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().sucesso());
        assertEquals(1234L, response.getBody().transacaoPagamentoId());
        assertEquals("Compra finalizada com sucesso.", response.getBody().mensagem());
    }

    @Test
    public void testFinalizarCompraFalhaPorEstoqueIndisponivel() {
        // Simula o comportamento do serviço de compra retornando falha de estoque
        when(compraService.finalizarCompra(anyLong(), anyLong())).thenThrow(new IllegalStateException("Itens fora de estoque."));

        // Chama o endpoint da controller
        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(1L, 1L);

        // Verifica se o status da resposta é 409 CONFLICT e se a resposta contém a mensagem de erro
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().sucesso());
        assertEquals("Itens fora de estoque.", response.getBody().mensagem());
    }

    @Test
    public void testFinalizarCompraFalhaPorPagamentoNaoAutorizado() {
        // Simula o comportamento do serviço de compra retornando falha de pagamento
        when(compraService.finalizarCompra(anyLong(), anyLong())).thenThrow(new IllegalStateException("Pagamento não autorizado."));

        // Chama o endpoint da controller
        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(1L, 1L);

        // Verifica se o status da resposta é 409 CONFLICT e se a resposta contém a mensagem de erro
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().sucesso());
        assertEquals("Pagamento não autorizado.", response.getBody().mensagem());
    }

    @Test
    public void testFinalizarCompraErroInterno() {
        // Simula uma exceção genérica durante o processo de compra
        when(compraService.finalizarCompra(anyLong(), anyLong())).thenThrow(new RuntimeException("Erro inesperado."));

        // Chama o endpoint da controller
        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(1L, 1L);

        // Verifica se o status da resposta é 500 INTERNAL_SERVER_ERROR e se a mensagem de erro é adequada
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().sucesso());
        assertEquals("Erro ao processar compra.", response.getBody().mensagem());
    }
}
