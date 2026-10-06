package banco.modelo;

import banco.excecao.BancoException;
import banco.excecao.SaldoInsuficienteException;
import banco.excecao.ValorInvalidoException;

import java.math.BigDecimal;

/**
 * ABSTRAÇÃO: modelo genérico de conta (não existe "conta" pura, apenas seus tipos).
 * ENCAPSULAMENTO: o saldo é privado e só muda por depositar() e sacar().
 */
public abstract class Conta {
    private final String numero;
    private final String titular;
    private BigDecimal saldo = BigDecimal.ZERO;

    protected Conta(String numero, String titular) {
        this.numero = numero;
        this.titular = titular;
    }

    // Cada tipo de conta define o seu (POLIMORFISMO)
    public abstract String getTipo();
    public abstract BigDecimal calcularTarifaMensal();

    public void depositar(BigDecimal valor) throws ValorInvalidoException {
        validar(valor);
        saldo = saldo.add(valor);
    }

    public void sacar(BigDecimal valor) throws BancoException {
        validar(valor);
        if (getSaldoDisponivel().compareTo(valor) < 0) {
            throw new SaldoInsuficienteException(getSaldoDisponivel(), valor);
        }
        saldo = saldo.subtract(valor);
    }

    /** Pode ser sobrescrito: a conta corrente soma o cheque especial. */
    public BigDecimal getSaldoDisponivel() {
        return saldo;
    }

    private void validar(BigDecimal valor) throws ValorInvalidoException {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValorInvalidoException("O valor deve ser maior que zero.");
        }
    }

    public String getNumero() { return numero; }
    public String getTitular() { return titular; }
    public BigDecimal getSaldo() { return saldo; }

    @Override
    public String toString() {
        return getTipo() + " | Nº " + numero + " | Titular: " + titular + " | Saldo: R$ " + saldo;
    }
}
