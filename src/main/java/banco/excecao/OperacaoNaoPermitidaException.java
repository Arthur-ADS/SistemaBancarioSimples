package banco.excecao;

/** Regra de negócio violada: conta bloqueada/encerrada, CPF duplicado, serviço já contratado etc. */
public class OperacaoNaoPermitidaException extends BancoException {
    public OperacaoNaoPermitidaException(String mensagem) {
        super(mensagem);
    }
}
