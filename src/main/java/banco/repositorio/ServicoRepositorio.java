package banco.repositorio;

import banco.modelo.Servico;

import java.util.List;
import java.util.Optional;

public interface ServicoRepositorio {
    void salvar(Servico servico);
    Optional<Servico> buscarPorCodigo(String codigo);
    List<Servico> listar();
}
