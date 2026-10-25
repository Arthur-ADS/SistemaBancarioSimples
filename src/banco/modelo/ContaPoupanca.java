package banco.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** HERANÇA: estende Conta. INTERFACE: implementa Rentavel (rende 0,5% ao mês). */
public class ContaPoupanca extends Conta implements Rentavel {
    private static final BigDecimal TAXA = new BigDecimal("0.005");

    public ContaPoupanca(String numero, String titular) {
        super(numero, titular);
    }

    public ContaPoupanca(String numero, String titular, BigDecimal saldo) {
        super(numero, titular, saldo);    }

    @Override
    public void aplicarRendimento() {
        BigDecimal rendimento = getSaldo().multiply(TAXA).setScale(2, RoundingMode.HALF_EVEN);
        if (rendimento.signum() > 0) {
            try {
                depositar(rendimento);
            } catch (Exception e) {
                // não ocorre: o rendimento é sempre positivo
            }
        }
    }

    @Override
    public BigDecimal calcularTarifaMensal() {
        return BigDecimal.ZERO;
    }

    @Override
    public String getTipo() { return "Conta Poupança"; }
}
