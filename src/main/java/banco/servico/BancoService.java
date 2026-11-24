package banco.servico;

import banco.excecao.BancoException;
import banco.excecao.ContaNaoEncontradaException;
import banco.excecao.OperacaoNaoPermitidaException;
import banco.excecao.ValorInvalidoException;
import banco.modelo.Cliente;
import banco.modelo.Conta;
import banco.modelo.ContaCorrente;
import banco.modelo.ContaPoupanca;
import banco.modelo.Endereco;
import banco.modelo.Rentavel;
import banco.modelo.Servico;
import banco.modelo.StatusConta;
import banco.modelo.TipoMovimentacao;
import banco.repositorio.ClienteRepositorio;
import banco.repositorio.ContaRepositorio;
import banco.repositorio.ServicoRepositorio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Regras de negócio. Usa apenas as INTERFACES de repositório. */
public class BancoService {
    private final ContaRepositorio contas;
    private final ClienteRepositorio clientes;
    private final ServicoRepositorio servicos;

    public BancoService(ContaRepositorio contas, ClienteRepositorio clientes, ServicoRepositorio servicos) {
        this.contas = contas;
        this.clientes = clientes;
        this.servicos = servicos;
    }

    /** Na primeira execução, cadastra o catálogo de serviços que as contas podem contratar. */
    public void inicializarCatalogoServicos() {
        if (servicos.listar().isEmpty()) {
            servicos.salvar(new Servico("PIX", "Pix", new BigDecimal("0.00")));
            servicos.salvar(new Servico("CARTAO", "Cartão de débito", new BigDecimal("5.00")));
            servicos.salvar(new Servico("SEGURO", "Seguro residencial", new BigDecimal("12.90")));
        }
    }

    // ---------- Clientes ----------

    public Cliente cadastrarCliente(String cpf, String nome, Endereco endereco) throws BancoException {
        String cpfLimpo = cpf == null ? "" : cpf.replaceAll("\\D", "");
        if (cpfLimpo.length() != 11) {
            throw new ValorInvalidoException("O CPF deve ter 11 dígitos.");
        }
        if (nome == null || nome.isBlank()) {
            throw new ValorInvalidoException("O nome não pode ser vazio.");
        }
        if (clientes.buscarPorCpf(cpfLimpo).isPresent()) {
            throw new OperacaoNaoPermitidaException("Já existe cliente com o CPF " + cpfLimpo + ".");
        }
        Cliente cliente = new Cliente(cpfLimpo, nome.trim(), endereco);
        clientes.salvar(cliente);
        return cliente;
    }

    public List<Cliente> listarClientes() {
        return clientes.listar();
    }

    public Cliente buscarCliente(String cpf) throws OperacaoNaoPermitidaException {
        String cpfLimpo = cpf == null ? "" : cpf.replaceAll("\\D", "");
        return clientes.buscarPorCpf(cpfLimpo)
                .orElseThrow(() -> new OperacaoNaoPermitidaException("Cliente não encontrado: " + cpfLimpo));
    }

    // ---------- Contas ----------

    public Conta abrirConta(String cpfTitular, boolean poupanca) throws BancoException {
        Cliente titular = buscarCliente(cpfTitular);
        String numero = String.format("%04d", contas.contar() + 1);
        Conta conta = poupanca ? new ContaPoupanca(numero, titular)
                               : new ContaCorrente(numero, titular);
        contas.salvar(conta);
        return conta;
    }

    public void depositar(String numero, BigDecimal valor) throws BancoException {
        Conta conta = buscar(numero);
        conta.depositar(valor);
        contas.salvar(conta);
    }

    public void sacar(String numero, BigDecimal valor) throws BancoException {
        Conta conta = buscar(numero);
        conta.sacar(valor);
        contas.salvar(conta);
    }

    public void transferir(String origem, String destino, BigDecimal valor) throws BancoException {
        if (origem.equals(destino)) {
            throw new OperacaoNaoPermitidaException("A conta de origem e a de destino devem ser diferentes.");
        }
        Conta contaOrigem = buscar(origem);
        Conta contaDestino = buscar(destino);   // confere o destino antes de mexer no saldo
        contaOrigem.debitar(TipoMovimentacao.TRANSFERENCIA_ENVIADA, valor);
        contaDestino.creditar(TipoMovimentacao.TRANSFERENCIA_RECEBIDA, valor);
        contas.salvar(contaOrigem, contaDestino); // uma única transação: as duas ou nenhuma
    }

    public void contratarServico(String numero, String codigoServico) throws BancoException {
        Conta conta = buscar(numero);
        String codigo = codigoServico.trim().toUpperCase();
        Servico servico = servicos.buscarPorCodigo(codigo)
                .orElseThrow(() -> new OperacaoNaoPermitidaException("Serviço não encontrado: " + codigo));
        conta.contratar(servico);
        contas.salvar(conta);
    }

    public List<Servico> listarServicos() {
        return servicos.listar();
    }

    public void alterarStatus(String numero, StatusConta novoStatus) throws BancoException {
        Conta conta = buscar(numero);
        conta.alterarStatus(novoStatus);
        contas.salvar(conta);
    }

    public List<Conta> listar() {
        return contas.listar();
    }

    /** POLIMORFISMO: o mesmo laço trata qualquer tipo de Conta. */
    public List<String> fecharMes() {
        List<String> relatorio = new ArrayList<>();
        List<Conta> todas = contas.listar();
        for (Conta conta : todas) {
            if (conta.getStatus() != StatusConta.ATIVA) {
                relatorio.add("Conta " + conta.getNumero() + ": ignorada (" + conta.getStatus() + ").");
                continue;
            }
            try {
                if (conta instanceof Rentavel rentavel) {
                    rentavel.aplicarRendimento();
                    relatorio.add("Conta " + conta.getNumero() + ": rendimento aplicado.");
                }
                BigDecimal tarifa = conta.calcularTarifaTotal();
                if (tarifa.signum() > 0) {
                    conta.debitar(TipoMovimentacao.TARIFA, tarifa);
                    relatorio.add("Conta " + conta.getNumero() + ": tarifa de R$ " + tarifa + " cobrada.");
                }
            } catch (BancoException e) {
                relatorio.add("Conta " + conta.getNumero() + ": tarifa não cobrada (" + e.getMessage() + ")");
            }
        }
        contas.salvar(todas.toArray(new Conta[0])); // grava o fechamento inteiro numa transação
        return relatorio;
    }

    public Conta buscar(String numero) throws ContaNaoEncontradaException {
        return contas.buscarPorNumero(numero)
                .orElseThrow(() -> new ContaNaoEncontradaException(numero));
    }
}
