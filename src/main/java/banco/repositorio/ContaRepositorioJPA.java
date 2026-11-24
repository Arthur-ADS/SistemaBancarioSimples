package banco.repositorio;

import banco.modelo.Conta;

import java.util.List;
import java.util.Optional;

/** Implementação com Hibernate: nenhum SQL escrito à mão. */
public class ContaRepositorioJPA implements ContaRepositorio {

    @Override
    public void salvar(Conta... contas) {
        // merge() faz INSERT se a conta é nova ou UPDATE se já existe.
        // Pelo cascade, as movimentações novas da conta são gravadas junto.
        JPAUtil.emTransacao(em -> {
            for (Conta conta : contas) {
                em.merge(conta);
            }
            return null;
        });
    }

    @Override
    public Optional<Conta> buscarPorNumero(String numero) {
        return JPAUtil.consultar(em -> {
            Conta conta = em.find(Conta.class, numero); // polimórfico: devolve Corrente ou Poupança
            if (conta != null) {
                carregarColecoes(conta);
            }
            return Optional.ofNullable(conta);
        });
    }

    @Override
    public List<Conta> listar() {
        return JPAUtil.consultar(em -> {
            List<Conta> contas = em.createQuery("SELECT c FROM Conta c ORDER BY c.numero", Conta.class)
                    .getResultList();
            contas.forEach(this::carregarColecoes);
            return contas;
        });
    }

    @Override
    public long contar() {
        return JPAUtil.consultar(em ->
                em.createQuery("SELECT COUNT(c) FROM Conta c", Long.class).getSingleResult());
    }

    /**
     * Coleções @OneToMany e @ManyToMany são LAZY: só são buscadas quando acessadas.
     * Como o EntityManager é fechado ao fim do método, forçamos a carga aqui;
     * senão o acesso posterior lançaria LazyInitializationException.
     */
    private void carregarColecoes(Conta conta) {
        conta.getMovimentacoes().size();
        conta.getServicos().size();
    }
}
