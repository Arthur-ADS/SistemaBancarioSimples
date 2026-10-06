package banco.excecao;

public class ContaNaoEncontradaException extends BancoException {
    public ContaNaoEncontradaException(String numero) {
        super("Conta não encontrada: " + numero);
    }
}
