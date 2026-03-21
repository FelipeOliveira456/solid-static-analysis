public abstract class ContaBancaria {

    protected String titular;
    protected double saldo;
    protected boolean ativa;

    public ContaBancaria(String titular, double saldoInicial) {
        this.titular = titular;
        this.saldo = saldoInicial;
        this.ativa = true;
    }

    public void depositar(double valor) {
        if (valor > 0) {
            saldo += valor;
        }
    }

    public boolean sacar(double valor) {
        if (!ativa) {
            System.out.println("Conta inativa.");
            return false;
        } else if (valor > saldo) {
            System.out.println("Saldo insuficiente.");
            return false;
        } else {
            saldo -= valor;
            return true;
        }
    }

    public void encerrar() {
        this.ativa = false;
        this.saldo = 0;
    }

    public abstract double calcularRendimento();

    public String getTitular() { return titular; }
    public double getSaldo()   { return saldo; }
    public boolean isAtiva()   { return ativa; }
}
