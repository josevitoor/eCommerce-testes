# E-commerce Test Suite

### Classes de Equivalência

| **Tipo de Cliente** | **Peso (kg)**              | **Frete por kg (R$)** | **Desconto/Isenção**            |
|---------------------|----------------------------|-----------------------|----------------------------------|
| **Bronze**          | `0 < peso ≤ 5`             | 0                     | Frete grátis                     |
|                     | `5 < peso ≤ 10`            | 2                     | Sem desconto                     |
|                     | `10 < peso ≤ 50`           | 4                     | Sem desconto                     |
|                     | `50 < peso`                | 7                     | Sem desconto                     |
| **Prata**           | `0 < peso ≤ 5`             | 0                     | Frete grátis                     |
|                     | `5 < peso ≤ 10`            | 2                     | 50% de desconto                  |
|                     | `10 < peso ≤ 50`           | 4                     | 50% de desconto                  |
|                     | `50 < peso`                | 7                     | 50% de desconto                  |
| **Ouro**            | `Qualquer peso`            | 0                     | Frete grátis (independente do peso) |

### Limites

Os testes também cobrem os limites inferiores e superiores de cada classe de equivalência:

| **Faixa de Peso (kg)** | **Valor Limite Inferior** | **Valor Limite Superior** |
|------------------------|---------------------------|---------------------------|
| `0 < peso ≤ 5`         | 5 kg                      | 5 kg                      |
| `5 < peso ≤ 10`        | 6 kg                      | 10 kg                     |
| `10 < peso ≤ 50`       | 11 kg                     | 50 kg                     |
| `50 < peso`            | 51 kg                     | ∞                         |