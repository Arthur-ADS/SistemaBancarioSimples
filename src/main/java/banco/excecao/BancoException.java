package banco.excecao;

/** Exceção base de todos os erros de regra de negócio do banco. */
public class BancoException extends Exception {
    public BancoException(String mensagem) {
        super(mensagem);
    }
}
