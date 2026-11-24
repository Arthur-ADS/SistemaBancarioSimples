package banco.modelo;

import banco.excecao.BancoException;
import banco.excecao.OperacaoNaoPermitidaException;
import banco.excecao.SaldoInsuficienteException;
import banco.excecao.ValorInvalidoException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ABSTRAÇÃO: modelo genérico de conta (não existe "conta" pura, apenas seus tipos).
 * ENCAPSULAMENTO: o saldo é privado e só muda por creditar() e debitar().
 *
 * HERANÇA NO BANCO: SINGLE_TABLE guarda Corrente e Poupança na mesma tabela "conta";
 * a coluna "tipo" (discriminador) diz qual subclasse o Hibernate deve instanciar.
 */
@Entity
@Table(name = "conta")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING, length = 10)
public abstract class Conta {
    @Id
    @Column(length = 4)
    private String numero;

    // CRITÉRIO: lado "Muitos" do 1:N bidirecional com Cliente (dono da FK cliente_cpf).
    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_cpf", nullable = false)
    private Cliente titular;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    // CRITÉRIO: Enum gravado como texto ("ATIVA"), e não como número (0, 1, 2).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusConta status = StatusConta.ATIVA;

    // CRITÉRIOS: lado "Um" do 1:N bidirecional (mappedBy) + CASCADE.
    // Ao salvar a conta, o Hibernate salva junto as movimentações novas.
    @OneToMany(mappedBy = "conta", cascade = CascadeType.ALL)
    @OrderBy("id")
    private List<Movimentacao> movimentacoes = new ArrayList<>();

    // CRITÉRIO: N:M com @JoinTable. A tabela intermediária "conta_servico"
    // é criada e mantida pelo Hibernate, sem precisar de uma classe Java para ela.
    @ManyToMany
    @JoinTable(name = "conta_servico",
            joinColumns = @JoinColumn(name = "conta_numero"),
            inverseJoinColumns = @JoinColumn(name = "servico_codigo"))
    private Set<Servico> servicos = new HashSet<>();

    protected Conta() {} // exigido pelo Hibernate

    protected Conta(String numero, Cliente titular) {
        this.numero = numero;
        this.titular = titular;
        titular.adicionarConta(this); // mantém os dois lados da relação coerentes
    }

    // Cada tipo de conta define o seu (POLIMORFISMO)
    public abstract String getTipo();
    public abstract BigDecimal calcularTarifaMensal();

    public void depositar(BigDecimal valor) throws BancoException {
        creditar(TipoMovimentacao.DEPOSITO, valor);
    }

    public void sacar(BigDecimal valor) throws BancoException {
        debitar(TipoMovimentacao.SAQUE, valor);
    }

    /** Entrada de dinheiro: depósito, transferência recebida, rendimento. */
    public void creditar(TipoMovimentacao tipo, BigDecimal valor) throws BancoException {
        validarAtiva();
        validar(valor);
        saldo = saldo.add(valor);
        registrar(tipo, valor);
    }

    /** Saída de dinheiro: saque, transferência enviada, tarifa. */
    public void debitar(TipoMovimentacao tipo, BigDecimal valor) throws BancoException {
        validarAtiva();
        validar(valor);
        if (getSaldoDisponivel().compareTo(valor) < 0) {
            throw new SaldoInsuficienteException(getSaldoDisponivel(), valor);
        }
        saldo = saldo.subtract(valor);
        registrar(tipo, valor);
    }

    public void contratar(Servico servico) throws BancoException {
        validarAtiva();
        if (!servicos.add(servico)) {
            throw new OperacaoNaoPermitidaException("A conta " + numero + " já possui o serviço " + servico.getNome() + ".");
        }
    }

    public void alterarStatus(StatusConta novoStatus) throws BancoException {
        if (status == StatusConta.ENCERRADA) {
            throw new OperacaoNaoPermitidaException("A conta " + numero + " está encerrada e não pode ser alterada.");
        }
        if (novoStatus == StatusConta.ENCERRADA && saldo.signum() != 0) {
            throw new OperacaoNaoPermitidaException("Para encerrar a conta, o saldo precisa estar zerado (atual: R$ " + saldo + ").");
        }
        status = novoStatus;
    }

    /** Tarifa do tipo de conta (polimórfica) + soma das tarifas dos serviços contratados. */
    public BigDecimal calcularTarifaTotal() {
        BigDecimal total = calcularTarifaMensal();
        for (Servico servico : servicos) {
            total = total.add(servico.getTarifaMensal());
        }
        return total;
    }

    /** Extrato agrupado por mês, em ordem cronológica (usado na opção "Extrato" do menu). */
    public Map<YearMonth, List<Movimentacao>> getMovimentacoesPorMes() {
        Map<YearMonth, List<Movimentacao>> porMes = new LinkedHashMap<>();
        for (Movimentacao m : movimentacoes) {
            porMes.computeIfAbsent(YearMonth.from(m.getDataHora()), mes -> new ArrayList<>()).add(m);
        }
        return porMes;
    }

    /** Pode ser sobrescrito: a conta corrente soma o cheque especial. */
    public BigDecimal getSaldoDisponivel() {
        return saldo;
    }

    private void registrar(TipoMovimentacao tipo, BigDecimal valor) {
        movimentacoes.add(new Movimentacao(this, tipo, valor));
    }

    private void validarAtiva() throws OperacaoNaoPermitidaException {
        if (status != StatusConta.ATIVA) {
            throw new OperacaoNaoPermitidaException("A conta " + numero + " está " + status + ".");
        }
    }

    private void validar(BigDecimal valor) throws ValorInvalidoException {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValorInvalidoException("O valor deve ser maior que zero.");
        }
    }

    public String getNumero() { return numero; }
    public Cliente getTitular() { return titular; }
    public BigDecimal getSaldo() { return saldo; }
    public StatusConta getStatus() { return status; }
    public List<Movimentacao> getMovimentacoes() { return Collections.unmodifiableList(movimentacoes); }
    public Set<Servico> getServicos() { return Collections.unmodifiableSet(servicos); }

    @Override
    public String toString() {
        return getTipo() + " | Nº " + numero + " | Titular: " + titular.getNome()
                + " | " + status + " | Saldo: R$ " + saldo;
    }
}
