import java.util.ArrayList;
import java.util.List;

public class Relatorio {

    private String agencia;
    private List<ContaBancaria> contas;
    private double totalSaldo;

    public Relatorio(String agencia) {
        this.agencia = agencia;
        this.contas = new ArrayList<>();
        this.totalSaldo = 0;
    }

    public void adicionarConta(ContaBancaria conta) {
        contas.add(conta);
        totalSaldo += conta.getSaldo();
    }

    public double calcularTotalImpostos() {
        double total = 0;
        for (ContaBancaria c : contas) {
            if (c instanceof Tributavel) {
                Tributavel t = (Tributavel) c;
                total += t.calcularImposto();
            }
        }
        return total;
    }

    public void imprimirResumo() {
        System.out.println("Agencia: " + agencia);
        System.out.println("Total contas: " + contas.size());
        System.out.println("Total saldo: " + totalSaldo);
    }

    public String getAgencia()    { return agencia; }
    public double getTotalSaldo() { return totalSaldo; }
}
