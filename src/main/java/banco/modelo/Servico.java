package banco.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;

/** Serviço que pode ser contratado por várias contas (PIX, cartão, seguro...). */
@Entity
@Table(name = "servico")
public class Servico {
    @Id
    @Column(length = 20)
    private String codigo;

    @Column(nullable = false, length = 60)
    private String nome;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifaMensal;

    protected Servico() {} // exigido pelo Hibernate

    public Servico(String codigo, String nome, BigDecimal tarifaMensal) {
        this.codigo = codigo;
        this.nome = nome;
        this.tarifaMensal = tarifaMensal;
    }

    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public BigDecimal getTarifaMensal() { return tarifaMensal; }

    // Servico fica dentro de um Set em Conta: dois objetos com o mesmo código
    // representam o mesmo serviço (evita contratar o mesmo serviço duas vezes).
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Servico outro)) return false;
        return Objects.equals(codigo, outro.getCodigo()); // getter: funciona também com proxies do Hibernate
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(codigo);
    }

    @Override
    public String toString() {
        return codigo + " - " + nome + " (R$ " + tarifaMensal + "/mês)";
    }
}
