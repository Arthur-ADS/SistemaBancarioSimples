package banco.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Entidade transacional: cada linha do extrato de uma conta. */
@Entity
@Table(name = "movimentacao")
public class Movimentacao {
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // CRITÉRIO: lado "Muitos" do relacionamento bidirecional (Conta 1:N Movimentacao).
    // É este lado que tem a chave estrangeira "conta_numero" na tabela.
    @ManyToOne(optional = false)
    @JoinColumn(name = "conta_numero", nullable = false)
    private Conta conta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private TipoMovimentacao tipo;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    protected Movimentacao() {} // exigido pelo Hibernate

    /** Só a Conta cria movimentações (ver Conta.registrar). */
    Movimentacao(Conta conta, TipoMovimentacao tipo, BigDecimal valor) {
        this.conta = conta;
        this.tipo = tipo;
        this.valor = valor;
        this.dataHora = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Conta getConta() { return conta; }
    public TipoMovimentacao getTipo() { return tipo; }
    public BigDecimal getValor() { return valor; }
    public LocalDateTime getDataHora() { return dataHora; }

    /** Valor positivo para créditos e negativo para débitos (usado no saldo do extrato). */
    public BigDecimal getValorComSinal() {
        return tipo.isCredito() ? valor : valor.negate();
    }

    @Override
    public String toString() {
        String sinal = tipo.isCredito() ? "+" : "-";
        return dataHora.format(FORMATO) + " | " + String.format("%-22s", tipo.getDescricao())
                + " | " + sinal + " R$ " + valor;
    }
}
