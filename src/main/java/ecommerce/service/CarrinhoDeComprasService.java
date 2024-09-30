package ecommerce.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.repository.CarrinhoDeComprasRepository;

@Service
public class CarrinhoDeComprasService {
	private final CarrinhoDeComprasRepository repository;
	
	@Autowired
	public CarrinhoDeComprasService(CarrinhoDeComprasRepository repository) {
		this.repository = repository;
	}

	public CarrinhoDeCompras buscarPorCarrinhoIdEClienteId(Long carrinhoId, Cliente cliente) {
		return repository.findByIdAndCliente(carrinhoId, cliente).orElseThrow(() -> new IllegalArgumentException("Carrinho não encontrado."));
	}
	
    public CarrinhoDeCompras adicionarItem(CarrinhoDeCompras carrinho, Produto produto, long quantidade) {
        ItemCompra itemCompra = new ItemCompra();
        itemCompra.setProduto(produto);
        itemCompra.setQuantidade(quantidade);

        carrinho.getItens().add(itemCompra);
        return carrinho;
    }
    
    public CarrinhoDeCompras removerItem(CarrinhoDeCompras carrinho, Produto produto) {
        List<ItemCompra> itens = carrinho.getItens();
        itens.removeIf(item -> item.getProduto().getId().equals(produto.getId()));
        return carrinho;
    }
}
