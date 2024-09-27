package ecommerce.external.fake;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;

import ecommerce.dto.PagamentoDTO;
import ecommerce.external.IPagamentoExternal;

@Service
public class PagamentoSimulado implements IPagamentoExternal {

    private Map<Long, PagamentoDTO> pagamentos;

    public PagamentoSimulado() {
        this.pagamentos = new HashMap<>();
    }

    @Override
    public PagamentoDTO autorizarPagamento(Long clienteId, Double custoTotal) {
        boolean pagamentoAutorizado = new Random().nextDouble() <= 0.9;
        Long transacaoId = new Random().nextLong(1000L);

        PagamentoDTO pagamentoDTO = new PagamentoDTO(pagamentoAutorizado, transacaoId);

        if (pagamentoAutorizado) {
            pagamentos.put(transacaoId, pagamentoDTO);
        }

        return pagamentoDTO;
    }

    @Override
    public void cancelarPagamento(Long clienteId, Long pagamentoTransacaoId) {
        if (pagamentos.containsKey(pagamentoTransacaoId)) {
            pagamentos.remove(pagamentoTransacaoId);
        }
    }
}