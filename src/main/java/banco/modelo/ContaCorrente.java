package banco.modelo;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

/** HERANÇA: estende Conta. Possui cheque especial e tarifa mensal. */
@Entity
@DiscriminatorValue("CORRENTE") // valor gravado na coluna "tipo"
public class ContaCorrente extends Conta {
    private static final BigDecimal LIMITE = new BigDecimal("500.00");

    protected ContaCorrente() {} // exigido pelo Hibernate

    public ContaCorrente(String numero, Cliente titular) {
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
