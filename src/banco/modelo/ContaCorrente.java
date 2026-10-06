package banco.modelo;

import java.math.BigDecimal;

/** HERANÇA: estende Conta. Possui cheque especial e tarifa mensal. */
public class ContaCorrente extends Conta {
    private static final BigDecimal LIMITE = new BigDecimal("500.00");

    public ContaCorrente(String numero, String titular) {
        super(numero, titular);
    }

    @Override
    public BigDecimal getSaldoDisponivel() {      // sobrescrito
        return getSaldo().add(LIMITE);
    }

    @Override
    public BigDecimal calcularTarifaMensal() {
        return new BigDecimal("15.00");
    }

    @Override
    public String getTipo() { return "Conta Corrente"; }
}
