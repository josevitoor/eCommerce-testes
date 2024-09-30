package ecommerce.whitebox;

import ecommerce.dto.*;
import ecommerce.entity.*;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import ecommerce.service.CarrinhoDeComprasService;
import ecommerce.service.ClienteService;
import ecommerce.service.CompraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class WhiteBoxTest {

    private CompraService compraService;
    private CarrinhoDeComprasService carrinhoService;
    private ClienteService clienteService;
    private IEstoqueExternal estoqueExternal;
    private IPagamentoExternal pagamentoExternal;

    private Cliente clienteBronze;
    private CarrinhoDeCompras carrinho;
    private Produto produto;

    @BeforeEach
    public void setup() {
        carrinhoService = mock(CarrinhoDeComprasService.class);
        clienteService = mock(ClienteService.class);
        estoqueExternal = mock(IEstoqueExternal.class);
        pagamentoExternal = mock(IPagamentoExternal.class);

        compraService = new CompraService(carrinhoService, clienteService, estoqueExternal, pagamentoExternal);

        clienteBronze = new Cliente();
        clienteBronze.setTipo(TipoCliente.BRONZE);
        
        produto = new Produto();
        produto.setPreco(BigDecimal.valueOf(400));
        produto.setPeso(10);
        produto.setId(1L);
        
        carrinho = criarCarrinho(1L, 400, 10);
    }

    // Testes para CompraService
    /*Cenário de sucesso da compra (fluxo normal).
    Falha por indisponibilidade de estoque.
    Falha por pagamento não autorizado.
    Falha ao dar baixa no estoque.
    Cancelamento de pagamento em caso de erro de estoque.
    Descontos aplicados corretamente (baseados no total do carrinho).
    Cálculos de frete para cada tipo de cliente e peso.
    Exceções lançadas corretamente.*/

    @Test
    public void testFinalizarCompraComSucesso() {
        when(clienteService.buscarPorId(1L)).thenReturn(clienteBronze);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(1L, clienteBronze)).thenReturn(carrinho);
        when(estoqueExternal.verificarDisponibilidade(anyList(), anyList())).thenReturn(new DisponibilidadeDTO(true, Collections.emptyList()));
        when(pagamentoExternal.autorizarPagamento(anyLong(), anyDouble())).thenReturn(new PagamentoDTO(true, 1234L));
        when(estoqueExternal.darBaixa(anyList(), anyList())).thenReturn(new EstoqueBaixaDTO(true));

        CompraDTO compraDTO = compraService.finalizarCompra(1L, 1L);

        assertTrue(compraDTO.sucesso());
        assertEquals(Long.valueOf(1234), compraDTO.transacaoPagamentoId());
    }

    @Test
    public void testFinalizarCompraFalhaPorEstoqueIndisponivel() {
        when(clienteService.buscarPorId(1L)).thenReturn(clienteBronze);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(1L, clienteBronze)).thenReturn(carrinho);
        when(estoqueExternal.verificarDisponibilidade(anyList(), anyList())).thenReturn(new DisponibilidadeDTO(false, Collections.emptyList()));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(1L, 1L);
        });

        assertEquals("Itens fora de estoque.", exception.getMessage());
    }

    @Test
    public void testFinalizarCompraFalhaPorPagamentoNaoAutorizado() {
        when(clienteService.buscarPorId(1L)).thenReturn(clienteBronze);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(1L, clienteBronze)).thenReturn(carrinho);
        when(estoqueExternal.verificarDisponibilidade(anyList(), anyList())).thenReturn(new DisponibilidadeDTO(true, Collections.emptyList()));
        when(pagamentoExternal.autorizarPagamento(anyLong(), anyDouble())).thenReturn(new PagamentoDTO(false, null));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(1L, 1L);
        });

        assertEquals("Pagamento não autorizado.", exception.getMessage());
    }

    @Test
    public void testFinalizarCompraFalhaBaixaEstoque() {
        when(clienteService.buscarPorId(1L)).thenReturn(clienteBronze);
        when(carrinhoService.buscarPorCarrinhoIdEClienteId(1L, clienteBronze)).thenReturn(carrinho);
        when(estoqueExternal.verificarDisponibilidade(anyList(), anyList())).thenReturn(new DisponibilidadeDTO(true, Collections.emptyList()));
        when(pagamentoExternal.autorizarPagamento(anyLong(), anyDouble())).thenReturn(new PagamentoDTO(true, 1234L));
        when(estoqueExternal.darBaixa(anyList(), anyList())).thenReturn(new EstoqueBaixaDTO(false));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            compraService.finalizarCompra(1L, 1L);
        });

        assertEquals("Erro ao dar baixa no estoque.", exception.getMessage());
        verify(pagamentoExternal, times(1)).cancelarPagamento(anyLong(), anyLong());
    }

    @Test
    public void testCalcularDescontoProdutos() {
        carrinho = criarCarrinho(1L, 1200, 5);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, clienteBronze);

        assertEquals(0, custoTotal.compareTo(BigDecimal.valueOf(960)));
    }

    @Test
    public void testCalcularFreteParaClienteBronze() {
        carrinho = criarCarrinho(1L, 500, 15);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, clienteBronze);

        assertEquals(0, custoTotal.compareTo(BigDecimal.valueOf(560)));
    }

    @Test
    public void testCalcularFreteGratisClienteOuro() {
        Cliente clienteOuro = new Cliente();
        clienteOuro.setTipo(TipoCliente.OURO);
        carrinho = criarCarrinho(1L, 600, 99999);

        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, clienteOuro);

        assertEquals(0, custoTotal.compareTo(BigDecimal.valueOf(600)));
    }

    // Testes para CarrinhoDeComprasService
    /*Adicionar um item ao carrinho com sucesso
    Remover um item do carrinho com sucesso
    Falha ao adicionar um item que não existe
    Falha ao remover um item que não está no carrinho*/

    @Test
    public void testAdicionarItemAoCarrinhoComSucesso() {
        when(carrinhoService.adicionarItem(carrinho, produto, 1)).thenReturn(carrinho);
        
        CarrinhoDeCompras carrinhoAtualizado = carrinhoService.adicionarItem(carrinho, produto, 1);
        
        assertNotNull(carrinhoAtualizado);
        assertEquals(1, carrinhoAtualizado.getItens().size());
    }

    @Test
    public void testRemoverItemDoCarrinhoComSucesso() {
        when(carrinhoService.removerItem(carrinho, produto)).thenReturn(carrinho);
        
        CarrinhoDeCompras carrinhoAtualizado = carrinhoService.removerItem(carrinho, produto);
        
        assertNotNull(carrinhoAtualizado);
        assertEquals(0, carrinhoAtualizado.getItens().size());
    }

    // Testes para ClienteService
    /*Buscar cliente por ID com sucesso
	Falha ao buscar cliente que não existe
	Atualizar informações de um cliente com sucesso*/


    @Test
    public void testBuscarClientePorIdComSucesso() {
        when(clienteService.buscarPorId(1L)).thenReturn(clienteBronze);
        
        Cliente clienteBuscado = clienteService.buscarPorId(1L);
        
        assertNotNull(clienteBuscado);
        assertEquals(TipoCliente.BRONZE, clienteBuscado.getTipo());
    }

    @Test
    public void testBuscarClientePorIdNaoExistente() {
        when(clienteService.buscarPorId(2L)).thenThrow(new IllegalArgumentException("Cliente não encontrado."));
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.buscarPorId(2L);
        });
        
        assertEquals("Cliente não encontrado.", exception.getMessage());
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
