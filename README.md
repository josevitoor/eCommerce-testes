# E-commerce Test Suite

## Classes de Equivalência

### Tipo de Cliente
- **Bronze**
    - Paga o frete integral.
- **Prata**
    - Tem desconto de 50% no frete.
- **Ouro**
    - Não paga frete.

### Faixas de Peso
- **Até 5 kg**
    - Frete grátis para todos.
- **Entre 5 kg e 10 kg**
    - R$ 2,00/kg.
- **Entre 10 kg e 50 kg**
    - R$ 4,00/kg.
- **Acima de 50 kg**
    - R$ 7,00/kg.

### Faixas de Valor
- **Abaixo de R$500**
    - Sem desconto.
- **Entre R$500 e R$1000**
    - 10% de desconto nos itens.
- **Acima de R$1000**
    - 20% de desconto nos itens.

### Limites

Os testes cobrem os limites inferiores e superiores de cada classe de equivalência, além dos casos de testes específicos:

| **Faixa de Peso (kg)** | **Faixa de Valor (R$)**  | **Valor Limite Inferior Peso (kg)** | **Valor Limite Superior Peso (kg)** | **Valor Limite Inferior (R$)** | **Valor Limite Superior (R$)** |
|------------------------|--------------------------|--------------------------------------|-------------------------------------|--------------------------------|-------------------------------|
| `0 < peso ≤ 5`         | `0 ≤ valor < 500`        | 1 kg                                 | 5 kg                                | 0                              | 500                           |
| `0 < peso ≤ 5`         | `500 ≤ valor < 1000`     | 1 kg                                 | 5 kg                                | 501                            | 1000                          |
| `0 < peso ≤ 5`         | `valor ≥ 1000`           | 1 kg                                 | 5 kg                                | 1001                           | ∞                             |
| `5 < peso ≤ 10`        | `0 ≤ valor < 500`        | 6 kg                                 | 10 kg                               | 0                              | 500                           |
| `5 < peso ≤ 10`        | `500 ≤ valor < 1000`     | 6 kg                                 | 10 kg                               | 501                            | 1000                          |
| `5 < peso ≤ 10`        | `valor ≥ 1000`           | 6 kg                                 | 10 kg                               | 1001                           | ∞                             |
| `10 < peso ≤ 50`       | `0 ≤ valor < 500`        | 11 kg                                | 50 kg                               | 0                              | 500                           |
| `10 < peso ≤ 50`       | `500 ≤ valor < 1000`     | 11 kg                                | 50 kg                               | 501                            | 1000                          |
| `10 < peso ≤ 50`       | `valor ≥ 1000`           | 11 kg                                | 50 kg                               | 1001                           | ∞                             |
| `50 < peso`            | `0 ≤ valor < 500`        | 51 kg                                | ∞                                   | 0                              | 500                           |
| `50 < peso`            | `500 ≤ valor < 1000`     | 51 kg                                | ∞                                   | 501                            | 1000                          |
| `50 < peso`            | `valor ≥ 1000`           | 51 kg                                | ∞                                   | 1001                           | ∞                             |

### Cobertura de Testes

A verificação de cobertura foi realizada para o CompraService, atingindo 100%. Eesse percentual foi obtido executando em conjunto os testes "WhiteBoxTest" e "BlackBoxTest", pois cada um tem seu foco específico. O relatório HTML pode ser encontrado em `src/test/java/ecommerce/CoverageTestReport/htmlReport`.