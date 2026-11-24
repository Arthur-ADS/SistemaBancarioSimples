package banco.repositorio;

import banco.modelo.Servico;

import java.util.List;
import java.util.Optional;

public class ServicoRepositorioJPA implements ServicoRepositorio {

    @Override
    public void salvar(Servico servico) {
        JPAUtil.emTransacao(em -> em.merge(servico));
    }

    @Override
    public Optional<Servico> buscarPorCodigo(String codigo) {
        return JPAUtil.consultar(em -> Optional.ofNullable(em.find(Servico.class, codigo)));
    }

    @Override
    public List<Servico> listar() {
        return JPAUtil.consultar(em ->
                em.createQuery("SELECT s FROM Servico s ORDER BY s.codigo", Servico.class).getResultList());
    }
}
