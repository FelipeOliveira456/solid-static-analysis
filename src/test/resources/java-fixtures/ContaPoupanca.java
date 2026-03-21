public class ContaPoupanca extends ContaBancaria implements Tributavel {

    private double taxaRendimento;
    private int mesesAplicados;

    public ContaPoupanca(String titular, double saldoInicial, double taxaRendimento) {
        super(titular, saldoInicial);
        this.taxaRendimento = taxaRendimento;
        this.mesesAplicados = 0;
    }

    public void aplicarRendimentosMensais(int meses) {
        int i = 0;
        while (i < meses) {
            saldo += saldo * taxaRendimento;
            mesesAplicados++;
            i++;
        }
    }

    @Override
    public double calcularRendimento() {
        return saldo * taxaRendimento;
    }

    @Override
    public double calcularImposto() {
        if (isIsento()) {
            return 0.0;
        } else {
            return calcularRendimento() * 0.15;
        }
    }

    @Override
    public boolean isIsento() {
        return mesesAplicados >= 12;
    }

    public double getTaxaRendimento() { return taxaRendimento; }
    public int getMesesAplicados()    { return mesesAplicados; }
}
