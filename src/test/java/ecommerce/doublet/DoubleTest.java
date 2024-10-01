package ecommerce.doublet;

import ecommerce.dto.*;
import ecommerce.entity.*;
import ecommerce.controller.*;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import ecommerce.service.CarrinhoDeComprasService;
import ecommerce.service.ClienteService;
import ecommerce.service.CompraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;


@SpringBootTest
public class DoubleTest {
    private CompraService compraService;
    private CompraController compraController;
    private CarrinhoDeComprasService carrinhoService;
    private ClienteService clienteService;
    private IEstoqueExternal estoqueExternal;
    private IPagamentoExternal pagamentoExternal;
    private CarrinhoDeCompras carrinho;

    @BeforeEach
    public void setup() {
        carrinhoService = mock(CarrinhoDeComprasService.class);
        clienteService = mock(ClienteService.class);
        estoqueExternal = mock(IEstoqueExternal.class);
        pagamentoExternal = mock(IPagamentoExternal.class);
        compraService = new CompraService(carrinhoService, clienteService, estoqueExternal, pagamentoExternal);
        compraController = mock(CompraController.class);

        carrinho = criarCarrinho(1L, 400, 10);
    }

    @Test
    public void testFinalizarCompraSuccess() {
        Long carrinhoId = 1L;
        Long clienteId = 1L;
        CompraDTO compraDTO = compraService.finalizarCompra(1L, 1L);
        
        when(compraService.finalizarCompra(carrinhoId, clienteId)).thenReturn(compraDTO);

        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(carrinhoId, clienteId);

        assertEquals(200, response);
        assertFalse(compraDTO.sucesso());
        assertEquals("Compra finalizada com sucesso", response.getBody());
    }

    @Test
    public void testFinalizarCompraBadRequest() {
        Long carrinhoId = 1L;
        Long clienteId = 1L;

        when(compraService.finalizarCompra(carrinhoId, clienteId)).thenThrow(new IllegalArgumentException("Argumento inválido"));

        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(carrinhoId, clienteId);
        CompraDTO compraDTO = compraService.finalizarCompra(carrinhoId, clienteId);

        assertEquals(400, response);
        assertFalse(compraDTO.sucesso());;
        assertEquals("Argumento inválido", response.getBody());
    }

    @Test
    public void testFinalizarCompraConflict() {
        Long carrinhoId = 1L;
        Long clienteId = 1L;
        
        when(compraService.finalizarCompra(carrinhoId, clienteId)).thenThrow(new IllegalStateException("Estado inválido"));

        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(carrinhoId, clienteId);
        CompraDTO compraDTO = compraService.finalizarCompra(carrinhoId, clienteId);

        assertEquals(409, response);
        assertFalse(compraDTO.sucesso());
        assertEquals("Estado inválido", response.getBody());
    }

    @Test
    public void testFinalizarCompraInternalServerError() {
        Long carrinhoId = 1L;
        Long clienteId = 1L;

        when(compraService.finalizarCompra(carrinhoId, clienteId)).thenThrow(new RuntimeException("Erro inesperado"));

        ResponseEntity<CompraDTO> response = compraController.finalizarCompra(carrinhoId, clienteId);
        CompraDTO compraDTO = compraService.finalizarCompra(carrinhoId, clienteId);
        
        assertEquals(500, response.getStatusCodeValue());
        assertFalse(compraDTO.sucesso());
        assertEquals("Erro ao processar compra.", response.getBody());
    }

    private CarrinhoDeCompras criarCarrinho(Long quantidade, double preco, int peso) {
        Produto produto = new Produto();
        produto.setPreco(BigDecimal.valueOf(preco));
        produto.setPeso(peso);
        produto.setId(1L);
        ItemCompra item = new ItemCompra();
        item.setProduto(produto);
        item.setQuantidade(quantidade);
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(Arrays.asList(item));
        return carrinho;
    }
}
