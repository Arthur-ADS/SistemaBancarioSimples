package banco.excecao;

public class PersistenciaException extends RuntimeException{
    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
