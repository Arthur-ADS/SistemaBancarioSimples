package banco.repositorio;

import banco.modelo.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteRepositorio {
    void salvar(Cliente cliente);
    Optional<Cliente> buscarPorCpf(String cpf);
    List<Cliente> listar();
}
