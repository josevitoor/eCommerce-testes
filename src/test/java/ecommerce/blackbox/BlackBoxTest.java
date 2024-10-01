package ecommerce.blackbox;

import ecommerce.entity.*;
import ecommerce.service.CarrinhoDeComprasService;
import ecommerce.service.ClienteService;
import ecommerce.service.CompraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class BlackBoxTest {

    @Autowired
    private CompraService compraService;

    @Autowired
    private CarrinhoDeComprasService carrinhoService;

    @Autowired
    private ClienteService clienteService;

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

    @ParameterizedTest
    @CsvSource({
            // Cliente, Peso, Quantidade, Valor, Valor Esperado
            // Casos para cliente Bronze
            "'BRONZE', 1, 1, 500, 500",
            "'BRONZE', 3, 1, 501, 450.9",
            "'BRONZE', 5, 1, 1001, 800.8",
            "'BRONZE', 6, 1, 1, 13",
            "'BRONZE', 8, 1, 1000, 916",
            "'BRONZE', 10, 1, 9999, 8019.20",
            "'BRONZE', 11, 1, 500, 544",
            "'BRONZE', 25, 1, 501, 550.9",
            "'BRONZE', 50, 1, 1001, 1000.8",
            "'BRONZE', 51, 1, 1, 358",
            "'BRONZE', 1000, 1, 1000, 7900",
            "'BRONZE', 9999, 1, 9999, 77992.2",
            // Casos para cliente Prata
            "'PRATA', 1, 1, 500, 500",
            "'PRATA', 3, 1, 501, 450.9",
            "'PRATA', 5, 1, 1001, 800.8",
            "'PRATA', 6, 1, 1, 7",
            "'PRATA', 8, 1, 1000, 908",
            "'PRATA', 10, 1, 9999, 8009.2",
            "'PRATA', 11, 1, 500, 522",
            "'PRATA', 25, 1, 501, 500.9",
            "'PRATA', 50, 1, 1001, 900.8",
            "'PRATA', 51, 1, 1, 179.5",
            "'PRATA', 1000, 1, 1000, 4400",
            "'PRATA', 9999, 1, 9999, 42995.7",
            // Casos para cliente Ouro
            "'OURO', 1, 1, 500, 500",
            "'OURO', 3, 1, 501, 450.9",
            "'OURO', 5, 1, 1001, 800.8",
            "'OURO', 6, 1, 1, 1",
            "'OURO', 8, 1, 1000, 900",
            "'OURO', 10, 1, 9999, 7999.2",
            "'OURO', 11, 1, 500, 500",
            "'OURO', 25, 1, 501, 450.9",
            "'OURO', 50, 1, 1001, 800.8",
            "'OURO', 51, 1, 1, 1",
            "'OURO', 1000, 1, 1000, 900",
            "'OURO', 9999, 1, 9999, 7999.2"
    })
    public void testCalculoCustoTotal(String clienteTipo, int peso, Long quantidade, double preco, double valorEsperado) {
        // Configurando o tipo de cliente com base nos parâmetros do CSV
        Cliente cliente = getClienteByTipo(clienteTipo);

        // Criando o carrinho de compras
        CarrinhoDeCompras carrinho = criarCarrinho(quantidade, preco, peso);

        // Calculando o custo total com base no cliente e no carrinho
        BigDecimal custoTotal = compraService.calcularCustoTotal(carrinho, cliente);

        // Comparando o valor esperado com o valor calculado
        assertEquals(0, custoTotal.compareTo(BigDecimal.valueOf(valorEsperado)));
    }

    private Cliente getClienteByTipo(String tipo) {
        switch (tipo) {
            case "OURO":
                return clienteOuro;
            case "PRATA":
                return clientePrata;
            case "BRONZE":
            default:
                return clienteBronze;
        }
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