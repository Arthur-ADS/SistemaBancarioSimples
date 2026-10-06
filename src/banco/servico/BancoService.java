package banco.servico;

import banco.excecao.BancoException;
import banco.excecao.ContaNaoEncontradaException;
import banco.excecao.ValorInvalidoException;
import banco.modelo.Conta;
import banco.modelo.ContaCorrente;
import banco.modelo.ContaPoupanca;
import banco.modelo.Rentavel;
import banco.repositorio.ContaRepositorio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Regras de negócio. Usa apenas a INTERFACE ContaRepositorio. */
public class BancoService {
    private final ContaRepositorio repositorio;

    public BancoService(ContaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public Conta abrirConta(String titular, boolean poupanca) throws ValorInvalidoException {
        if (titular == null || titular.isBlank()) {
            throw new ValorInvalidoException("O nome do titular não pode ser vazio.");
        }
        String numero = String.format("%04d", repositorio.listar().size() + 1);
        Conta conta = poupanca ? new ContaPoupanca(numero, titular.trim())
                               : new ContaCorrente(numero, titular.trim());
        repositorio.salvar(conta);
        return conta;
    }

    public void depositar(String numero, BigDecimal valor) throws BancoException {
        Conta conta = buscar(numero);
        conta.depositar(valor);
        repositorio.salvar(conta);
    }

    public void sacar(String numero, BigDecimal valor) throws BancoException {
        Conta conta = buscar(numero);
        conta.sacar(valor);
        repositorio.salvar(conta);
    }

    public void transferir(String origem, String destino, BigDecimal valor) throws BancoException {
        Conta contaOrigem = buscar(origem);
        Conta contaDestino = buscar(destino);   // confere o destino antes de mexer no saldo
        contaOrigem.sacar(valor);
        contaDestino.depositar(valor);
        repositorio.salvar(contaOrigem);
        repositorio.salvar(contaDestino);
    }

    public List<Conta> listar() {
        return repositorio.listar();
    }

    /** POLIMORFISMO: o mesmo laço trata qualquer tipo de Conta. */
    public List<String> fecharMes() {
        List<String> relatorio = new ArrayList<>();
        for (Conta conta : repositorio.listar()) {
            if (conta instanceof Rentavel rentavel) {
                rentavel.aplicarRendimento();
                relatorio.add("Conta " + conta.getNumero() + ": rendimento aplicado.");
            }
            BigDecimal tarifa = conta.calcularTarifaMensal();
            if (tarifa.signum() > 0) {
                try {
                    conta.sacar(tarifa);
                    relatorio.add("Conta " + conta.getNumero() + ": tarifa de R$ " + tarifa + " cobrada.");
                } catch (BancoException e) {
                    relatorio.add("Conta " + conta.getNumero() + ": tarifa não cobrada (" + e.getMessage() + ")");
                }
            }
            repositorio.salvar(conta);
        }
        return relatorio;
    }

    public Conta buscar(String numero) throws ContaNaoEncontradaException {
        return repositorio.buscarPorNumero(numero)
                .orElseThrow(() -> new ContaNaoEncontradaException(numero));
    }
}
