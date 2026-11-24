package banco.repositorio;

import banco.modelo.Conta;

import java.util.List;
import java.util.Optional;

/**
 * INTERFACE de persistência. O serviço só conhece este contrato;
 * a implementação atual usa Hibernate (ContaRepositorioJPA).
 */
public interface ContaRepositorio {
    /** Salva uma ou mais contas numa única transação (tudo ou nada). */
    void salvar(Conta... contas);
    Optional<Conta> buscarPorNumero(String numero);
    List<Conta> listar();
    long contar();
}
