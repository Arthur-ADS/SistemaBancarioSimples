package banco.repositorio;

import banco.modelo.Cliente;

import java.util.List;
import java.util.Optional;

public class ClienteRepositorioJPA implements ClienteRepositorio {

    @Override
    public void salvar(Cliente cliente) {
        JPAUtil.emTransacao(em -> em.merge(cliente));
    }

    @Override
    public Optional<Cliente> buscarPorCpf(String cpf) {
        return JPAUtil.consultar(em -> {
            Cliente cliente = em.find(Cliente.class, cpf);
            if (cliente != null) {
                cliente.getContas().size(); // carrega a lista LAZY antes de fechar o EntityManager
            }
            return Optional.ofNullable(cliente);
        });
    }

    @Override
    public List<Cliente> listar() {
        return JPAUtil.consultar(em -> {
            List<Cliente> clientes = em.createQuery("SELECT c FROM Cliente c ORDER BY c.nome", Cliente.class)
                    .getResultList();
            clientes.forEach(c -> c.getContas().size()); // navegação de volta: Cliente -> Contas
            return clientes;
        });
    }
}
