package banco.repositorio;

import banco.modelo.Conta;

import java.util.List;
import java.util.Optional;

/**
 * INTERFACE de persistência. O sistema só conhece este contrato; hoje os dados
 * ficam em memória e, futuramente, uma classe com JDBC/MySQL poderá implementá-lo.
 */
public interface ContaRepositorio {
    void salvar(Conta conta);
    Optional<Conta> buscarPorNumero(String numero);
    List<Conta> listar();
}
