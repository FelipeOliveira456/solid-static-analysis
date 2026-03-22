# Comparação scoring: java-fixtures — k=5 vs k=100

Gerado com o código atual: em `FIXED_THRESHOLD_RELAXED`, a banda de cada indicador contínuo relaxado compara **`value = rawMetric × f(n)`** com os limiares nominais de `analysis.properties` (`rawMetric` permanece no JSON só como referência).

- **f (k=5, n=6)**: 0.5454545454545454
- **f (k=100, n=6)**: 0.05660377358490566

## Tabela

| Classe | Overall | S | O | L | I | D |
|--------|---------|---|---|---|---|---|
| ContaBancaria | MEDIO → BAIXO | MEDIO → BAIXO | BAIXO | BAIXO | BAIXO | BAIXO |
| ContaCorrente | ALTO → BAIXO | BAIXO | BAIXO | BAIXO | BAIXO | ALTO → BAIXO |
| ContaPoupanca | ALTO → BAIXO | BAIXO | BAIXO | BAIXO | BAIXO | ALTO → BAIXO |
| GerenciadorContas | ALTO | MEDIO → BAIXO | BAIXO | BAIXO | BAIXO | ALTO |
| Relatorio | MEDIO | BAIXO | BAIXO | BAIXO | BAIXO | MEDIO |
| Tributavel | MEDIO → BAIXO | MEDIO → BAIXO | BAIXO | BAIXO | BAIXO | BAIXO |

## Mudanças entre k=5 e k=100

- **ContaBancaria**: overall MEDIO→BAIXO; S MEDIO→BAIXO
- **ContaCorrente**: overall ALTO→BAIXO; D ALTO→BAIXO
- **ContaPoupanca**: overall ALTO→BAIXO; D ALTO→BAIXO
- **GerenciadorContas**: S MEDIO→BAIXO
- **Tributavel**: overall MEDIO→BAIXO; S MEDIO→BAIXO
