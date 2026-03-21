import java.util.ArrayList;
import java.util.List;

public class GerenciadorContas {

    private List<ContaBancaria> contas;
    private String agencia;

    public GerenciadorContas(String agencia) {
        this.agencia = agencia;
        this.contas = new ArrayList<>();
    }

    public ContaCorrente abrirContaCorrente(String titular, double deposito, String plano) {
        ContaCorrente conta = new ContaCorrente(titular, deposito, plano);
        contas.add(conta);
        return conta;
    }

    public ContaPoupanca abrirContaPoupanca(String titular, double deposito, double taxa) {
        ContaPoupanca conta = new ContaPoupanca(titular, deposito, taxa);
        contas.add(conta);
        return conta;
    }

    public Relatorio gerarRelatorio() {
        Relatorio rel = new Relatorio(agencia);
        for (ContaBancaria c : contas) {
            rel.adicionarConta(c);
        }
        return rel;
    }

    public int totalContas() {
        return contas.size();
    }
}
