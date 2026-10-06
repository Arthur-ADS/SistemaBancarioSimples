package banco.excecao;

import java.math.BigDecimal;

public class SaldoInsuficienteException extends BancoException {
    public SaldoInsuficienteException(BigDecimal disponivel, BigDecimal solicitado) {
        super("Saldo insuficiente. Disponível: R$ " + disponivel + " | Solicitado: R$ " + solicitado);
    }
}
