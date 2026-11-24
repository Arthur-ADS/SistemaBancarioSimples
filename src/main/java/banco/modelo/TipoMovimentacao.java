package banco.modelo;

/** Enum persistido como texto em Movimentacao. Cada tipo sabe se entra ou sai dinheiro. */
public enum TipoMovimentacao {
    DEPOSITO("Depósito", true),
    SAQUE("Saque", false),
    TRANSFERENCIA_ENVIADA("Transferência enviada", false),
    TRANSFERENCIA_RECEBIDA("Transferência recebida", true),
    TARIFA("Tarifa mensal", false),
    RENDIMENTO("Rendimento", true);

    private final String descricao;
    private final boolean credito;

    TipoMovimentacao(String descricao, boolean credito) {
        this.descricao = descricao;
        this.credito = credito;
    }

    public String getDescricao() { return descricao; }
    public boolean isCredito() { return credito; }
}
