package banco.repositorio;

import banco.modelo.Conta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Implementação provisória: guarda as contas em um Map (simula a tabela). */
public class ContaRepositorioMemoria implements ContaRepositorio {
    private final Map<String, Conta> contas = new LinkedHashMap<>();

    @Override
    public void salvar(Conta conta) {
        contas.put(conta.getNumero(), conta);
    }

    @Override
    public Optional<Conta> buscarPorNumero(String numero) {
        return Optional.ofNullable(contas.get(numero));
    }

    @Override
    public List<Conta> listar() {
        return new ArrayList<>(contas.values());
    }
}
