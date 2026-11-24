package banco.modelo;

import banco.excecao.BancoException;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** HERANÇA: estende Conta. INTERFACE: implementa Rentavel (rende 0,5% ao mês). */
@Entity
@DiscriminatorValue("POUPANCA") // valor gravado na coluna "tipo"
public class ContaPoupanca extends Conta implements Rentavel {
    private static final BigDecimal TAXA = new BigDecimal("0.005");

    protected ContaPoupanca() {} // exigido pelo Hibernate

    public ContaPoupanca(String numero, Cliente titular) {
        super(numero, titular);
    }

    @Override
    public void aplicarRendimento() throws BancoException {
        BigDecimal rendimento = getSaldo().multiply(TAXA).setScale(2, RoundingMode.HALF_EVEN);
        if (rendimento.signum() > 0) {
            creditar(TipoMovimentacao.RENDIMENTO, rendimento);
        }
    }

    @Override
    public BigDecimal calcularTarifaMensal() {
        return BigDecimal.ZERO;
    }

    @Override
    public String getTipo() { return "Conta Poupança"; }
}
