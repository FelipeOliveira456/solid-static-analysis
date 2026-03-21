public class ContaCorrente extends ContaBancaria {

    private String plano;
    private double taxaMensal;
    private int transacoesMes;

    public ContaCorrente(String titular, double saldoInicial, String plano) {
        super(titular, saldoInicial);
        this.plano = plano;
        this.transacoesMes = 0;
        this.taxaMensal = resolverTaxa(plano);
    }

    private double resolverTaxa(String plano) {
        switch (plano) {
            case "basico":   return 15.0;
            case "premium":  return 0.0;
            case "empresarial": return 30.0;
            default:         return 20.0;
        }
    }

    public void registrarTransacoes(int[] valores) {
        for (int v : valores) {
            if (v > 0) {
                saldo += v;
                transacoesMes++;
            }
        }
    }

    public void cobrarTaxa() {
        if (ativa && saldo >= taxaMensal) {
            saldo -= taxaMensal;
        }
    }

    @Override
    public double calcularRendimento() {
        return 0.0;
    }

    public String getPlano()       { return plano; }
    public int getTransacoesMes()  { return transacoesMes; }
}
