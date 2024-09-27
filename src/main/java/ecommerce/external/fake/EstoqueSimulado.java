package ecommerce.external.fake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.external.IEstoqueExternal;

@Service
public class EstoqueSimulado implements IEstoqueExternal {

    private Map<Long, Long> estoque;

    public EstoqueSimulado() {
        estoque = new HashMap<>();
        estoque.put(1L, 100L);
        estoque.put(2L, 50L);
        estoque.put(3L, 200L);
    }

    @Override
    public EstoqueBaixaDTO darBaixa(List<Long> produtosIds, List<Long> produtosQuantidades) {
        boolean sucesso = true;

        for (int i = 0; i < produtosIds.size(); i++) {
            Long produtoId = produtosIds.get(i);
            Long quantidade = produtosQuantidades.get(i);
            Long estoqueAtual = estoque.getOrDefault(produtoId, 0L);

            if (estoqueAtual >= quantidade) {
                estoque.put(produtoId, estoqueAtual - quantidade);
            } else {
                sucesso = false;
                break;
            }
        }

        return new EstoqueBaixaDTO(sucesso);
    }

    @Override
    public DisponibilidadeDTO verificarDisponibilidade(List<Long> produtosIds, List<Long> produtosQuantidades) {
        boolean disponivel = true;
        List<Long> produtosIndisponiveis = new ArrayList<>();

        for (int i = 0; i < produtosIds.size(); i++) {
            Long produtoId = produtosIds.get(i);
            Long quantidade = produtosQuantidades.get(i);
            Long estoqueAtual = estoque.getOrDefault(produtoId, 0L);

            if (estoqueAtual < quantidade) {
                disponivel = false;
                produtosIndisponiveis.add(produtoId);
            }
        }

        return new DisponibilidadeDTO(disponivel, produtosIndisponiveis);
    }
}