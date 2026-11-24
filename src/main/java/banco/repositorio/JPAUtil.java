package banco.repositorio;

import banco.excecao.PersistenciaException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ponto único de acesso ao Hibernate (substitui a antiga ConexaoFactory do JDBC).
 * A EntityManagerFactory é cara de criar, por isso existe uma só para toda a aplicação.
 */
public final class JPAUtil {
    // Mantém só avisos e erros do Hibernate no console (o SQL continua aparecendo via show_sql).
    private static final Logger LOG_HIBERNATE = Logger.getLogger("org.hibernate");
    private static EntityManagerFactory factory;

    private JPAUtil() {}

    private static synchronized EntityManagerFactory getFactory() {
        if (factory == null) {
            LOG_HIBERNATE.setLevel(Level.WARNING);
            try {
                factory = Persistence.createEntityManagerFactory("banco_pu");
            } catch (RuntimeException e) {
                throw new PersistenciaException("Não foi possível conectar ao banco. Verifique se o MySQL "
                        + "está rodando e se usuário/senha do persistence.xml estão corretos.", e);
            }
        }
        return factory;
    }

    /**
     * Executa a ação dentro de UMA transação: ou tudo é gravado (commit), ou nada é (rollback).
     * É o que garante, por exemplo, que a transferência nunca fique pela metade.
     */
    public static <T> T emTransacao(Function<EntityManager, T> acao) {
        EntityManager em = getFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T resultado = acao.apply(em);
            tx.commit();
            return resultado;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw traduzir(e);
        } finally {
            em.close();
        }
    }

    /** Executa uma consulta (somente leitura, sem transação). */
    public static <T> T consultar(Function<EntityManager, T> acao) {
        EntityManager em = getFactory().createEntityManager();
        try {
            return acao.apply(em);
        } catch (RuntimeException e) {
            throw traduzir(e);
        } finally {
            em.close();
        }
    }

    public static synchronized void fechar() {
        if (factory != null && factory.isOpen()) {
            factory.close();
        }
    }

    // As camadas de cima só conhecem PersistenciaException, nunca exceções do Hibernate.
    private static RuntimeException traduzir(RuntimeException e) {
        if (e instanceof PersistenciaException) {
            return e;
        }
        return new PersistenciaException("Erro ao acessar o banco de dados: " + e.getMessage(), e);
    }
}
