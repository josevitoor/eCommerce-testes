package ecommerce.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import ecommerce.entity.ItemCompra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ecommerce.dto.CompraDTO;
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import jakarta.transaction.Transactional;

@Service
public class CompraService {

    private final CarrinhoDeComprasService carrinhoService;
    private final ClienteService clienteService;

    private final IEstoqueExternal estoqueExternal;
    private final IPagamentoExternal pagamentoExternal;

    @Autowired
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

        BigDecimal custoTotal = calcularCustoTotal(carrinho, cliente);

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

    public BigDecimal calcularCustoTotal(CarrinhoDeCompras carrinho, Cliente cliente) {
        BigDecimal custoProdutos = BigDecimal.ZERO;
        BigDecimal pesoTotal = BigDecimal.ZERO;

        for (ItemCompra item : carrinho.getItens()) {
            BigDecimal precoUnitario = item.getProduto().getPreco();
            Long quantidade = item.getQuantidade();
            BigDecimal custo = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
            BigDecimal peso = BigDecimal.valueOf(item.getProduto().getPeso()).multiply(BigDecimal.valueOf(quantidade));
            custoProdutos = custoProdutos.add(custo);
            pesoTotal = pesoTotal.add(peso);
        }

        BigDecimal custoFrete = calcularCustoFrete(pesoTotal, cliente);
        BigDecimal custoTotal = custoProdutos.add(custoFrete);

        return aplicarDescontos(custoTotal, custoProdutos);
    }

    private BigDecimal calcularCustoFrete(BigDecimal pesoTotal, Cliente cliente) {
        BigDecimal custoFrete = calcularCustoFretePorPeso(pesoTotal);
        return aplicarDescontoCliente(custoFrete, cliente);
    }

    private BigDecimal calcularCustoFretePorPeso(BigDecimal pesoTotal) {
        if (pesoTotal.compareTo(BigDecimal.valueOf(5)) <= 0) {
            return BigDecimal.ZERO;
        } else if (pesoTotal.compareTo(BigDecimal.valueOf(10)) <= 0) {
            return pesoTotal.multiply(BigDecimal.valueOf(2));
        } else if (pesoTotal.compareTo(BigDecimal.valueOf(50)) <= 0) {
            return pesoTotal.multiply(BigDecimal.valueOf(4));
        } else {
            return pesoTotal.multiply(BigDecimal.valueOf(7));
        }
    }

    private BigDecimal aplicarDescontoCliente(BigDecimal custoFrete, Cliente cliente) {
        switch (cliente.getTipo()) {
            case OURO:
                return BigDecimal.ZERO;
            case PRATA:
                return custoFrete.multiply(BigDecimal.valueOf(0.5));
            case BRONZE:
            default:
                return custoFrete;
        }
    }

    private BigDecimal aplicarDescontos(BigDecimal custoTotal, BigDecimal custoProdutos) {
        if (custoProdutos.compareTo(BigDecimal.valueOf(1000)) > 0) {
            custoProdutos = custoProdutos.multiply(BigDecimal.valueOf(0.8));
        } else if (custoProdutos.compareTo(BigDecimal.valueOf(500)) > 0) {
            custoProdutos = custoProdutos.multiply(BigDecimal.valueOf(0.9));
        }

        return custoProdutos.add(custoTotal.subtract(custoProdutos));
    }
}
