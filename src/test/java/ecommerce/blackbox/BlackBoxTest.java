package ecommerce.blackbox;

import ecommerce.entity.*;
import ecommerce.service.CarrinhoDeComprasService;
import ecommerce.service.ClienteService;
import ecommerce.service.CompraService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;

@SpringBootTest
public class BlackBoxTest {

    @Autowired
    private CompraService compraService;

    @Autowired
    private CarrinhoDeComprasService carrinhoService;

    @Autowired
    private ClienteService clienteService;

    // Clintes para classe de equivalência referente ao nível
    private Cliente clienteBronze;
    private Cliente clientePrata;
    private Cliente clienteOuro;

    @BeforeEach
    public void setup() {
        // Inicializando o serviço de compra
        compraService = new CompraService(carrinhoService, clienteService, null, null);

        // Inicializando clientes com diferentes níveis
        clienteBronze = new Cliente();
        clienteBronze.setTipo(TipoCliente.BRONZE);
        clientePrata = new Cliente();
        clientePrata.setTipo(TipoCliente.PRATA);
        clienteOuro = new Cliente();
        clienteOuro.setTipo(TipoCliente.OURO);
    }

    // Testes com particionamentos em classes de equivalências e análises de valores limites
    @Test
    public void testCarrinhoFreteGratisClienteBronzeLimiteInferior() {
        // Classe de equivalência para peso (frete grátis) e cliente (bronze)
        // Análise do valor limite para peso (frete grátis - 5kg)
        CarrinhoDeCompras carrinhoFreteGratis = criarCarrinho(1L, 400, 1);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinhoFreteGratis, clienteBronze);
        // Preço dos produtos sem frete
        assertEquals(0, custoTotal.compareTo(BigDecimal.valueOf(400)));
    }

    @Test
    public void testCarrinhoFreteMedioClienteBronzeLimiteSuperior() {
        // Classe de equivalência para peso (frete médio) e cliente (bronze)
        // Análise do valor limite para peso (frete médio - 10kg)
        CarrinhoDeCompras carrinhoFreteMedio = criarCarrinho(1L, 600, 10);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinhoFreteMedio, clienteBronze);
        // Preço dos produtos + frete (10kg * R$2)
        assertEquals(0, custoTotal.compareTo(BigDecimal.valueOf(620)));
    }

    @Test
    public void testCarrinhoFreteAltoClienteBronzeLimiteInferior() {
        // Classe de equivalência para peso (frete alto) e cliente (bronze)
        // Análise do valor limite inferior para peso (frete alto - 11kg)
        CarrinhoDeCompras carrinhoFreteAlto = criarCarrinho(2L, 800, 11);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinhoFreteAlto, clienteBronze);
        // Verifica o preço + frete (2 * 11kg * R$4)
        assertEquals(0, custoTotal.compareTo(BigDecimal.valueOf(1688)));
    }

    @Test
    public void testCarrinhoFreteSuperAltoClienteBronzeLimiteSuperior() {
        // Classe de equivalência para peso (frete super alto) e cliente (bronze)
        // Análise do valor limite inferior para peso (frete super alto - 99999kg, levando em consideração ser o peso máximo e não usando MAX_INT)
        CarrinhoDeCompras carrinhoFreteSuperAlto = criarCarrinho(1L, 1000, 99999);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinhoFreteSuperAlto, clienteBronze);
        // Verifica o preço + frete (99999kg * R$7)
        assertEquals(0, custoTotal.compareTo(BigDecimal.valueOf(700993)));
    }

    @Test
    public void testCarrinhoClienteOuroLimiteSuperior() {
        // Classe de equivalência para cliente Ouro com frete grátis
        // Análise do valor limite para peso (frete grátis ouro, independente do peso)
        CarrinhoDeCompras carrinhoFreteGratis = criarCarrinho(1L, 400, 99999);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinhoFreteGratis, clienteOuro);
        // Verifica se o preço é sem frete (frete grátis para Ouro)
        assertEquals(0, custoTotal.compareTo(BigDecimal.valueOf(400)));
    }

    @Test
    public void testCarrinhoFreteMedioClientePrataLimiteInferior() {
        // Classe de equivalência para cliente Prata com frete médio
        // Análise do valor limite inferior para peso (frete médio - 6kg)
        CarrinhoDeCompras carrinhoFreteMedio = criarCarrinho(1L, 600, 6);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinhoFreteMedio, clientePrata);
        // Verifica o preço + frete com 50% de desconto (6kg * R$2 * 0.5)
        assertEquals(0, custoTotal.compareTo(BigDecimal.valueOf(606)));
    }

    @Test
    public void testCarrinhoFreteMedioClientePrataLimiteSuperior() {
        // Classe de equivalência para cliente Prata com frete médio
        // Análise do valor limite superior para peso (frete médio - 10kg)
        CarrinhoDeCompras carrinhoFreteMedio = criarCarrinho(1L, 600, 10);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinhoFreteMedio, clientePrata);
        // Verifica o preço + frete com 50% de desconto (10kg * R$2 * 0.5)
        assertEquals(0, custoTotal.compareTo(BigDecimal.valueOf(610)));
    }

    @Test
    public void testCarrinhoFreteAltoClientePrataLimiteInferior() {
        // Classe de equivalência para cliente Prata com frete alto
        // Análise do valor limite inferior para peso (frete alto - 11kg)
        CarrinhoDeCompras carrinhoFreteAlto = criarCarrinho(1L, 800, 11);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinhoFreteAlto, clientePrata);
        // Verifica o preço + frete com 50% de desconto (11kg * R$4 * 0.5)
        assertEquals(0, custoTotal.compareTo(BigDecimal.valueOf(822)));
    }

    @Test
    public void testCarrinhoFreteAltoClientePrataLimiteSuperior() {
        // Classe de equivalência para cliente Prata com frete alto
        // Análise do valor limite superior para peso (frete alto - 50kg)
        CarrinhoDeCompras carrinhoFreteAlto = criarCarrinho(1L, 2000, 50);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinhoFreteAlto, clientePrata);
        // Verifica o preço + frete com 50% de desconto (50kg * R$4 * 0.5)
        assertEquals(0, custoTotal.compareTo(BigDecimal.valueOf(2100)));
    }

    @Test
    public void testCarrinhoFreteSuperAltoClientePrataLimiteInferior() {
        // Classe de equivalência para cliente Prata com frete super alto
        // Análise do valor limite inferior para peso (frete super alto - 51kg)
        CarrinhoDeCompras carrinhoFreteSuperAlto = criarCarrinho(1L, 2500, 51);
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinhoFreteSuperAlto, clientePrata);
        // Verifica o preço + frete com 50% de desconto (51kg * R$7 * 0.5)
        assertEquals(0, custoTotal.compareTo(BigDecimal.valueOf(2678.50)));
    }


    private CarrinhoDeCompras criarCarrinho(Long quantidade, double preco, int peso) {
        Produto produto = new Produto();
        produto.setPreco(BigDecimal.valueOf(preco));
        produto.setPeso(peso);

        ItemCompra item = new ItemCompra();
        item.setProduto(produto);
        item.setQuantidade(quantidade);

        CarrinhoDeCompras carrinho = new CarrinhoDeCompras();
        carrinho.setItens(Arrays.asList(item));

        return carrinho;
    }
}
