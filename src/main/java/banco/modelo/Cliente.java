package banco.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Entidade forte: o titular das contas. */
@Entity
@Table(name = "cliente")
public class Cliente {
    @Id
    @Column(length = 11)
    private String cpf;

    @Column(nullable = false, length = 100)
    private String nome;

    // CRITÉRIO: @Embedded. Os campos de Endereco viram colunas desta mesma tabela.
    @Embedded
    private Endereco endereco;

    // CRITÉRIO: lado "Um" do relacionamento bidirecional 1:N.
    // mappedBy = "titular" diz que quem manda na chave estrangeira é o atributo
    // "titular" da classe Conta; esta lista é só o caminho de volta (Cliente -> Contas).
    @OneToMany(mappedBy = "titular")
    private List<Conta> contas = new ArrayList<>();

    protected Cliente() {}

    public Cliente(String cpf, String nome, Endereco endereco) {
        this.cpf = cpf;
        this.nome = nome;
        this.endereco = endereco;
    }

    /** Chamado pelo construtor de Conta, para manter os dois lados da relação coerentes. */
    void adicionarConta(Conta conta) {
        contas.add(conta);
    }

    public String getCpf() { return cpf; }
    public String getNome() { return nome; }
    public Endereco getEndereco() { return endereco; }
    public List<Conta> getContas() { return Collections.unmodifiableList(contas); }

    @Override
    public String toString() {
        return nome + " | CPF " + cpf + " | " + endereco;
    }
}
