package banco.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * CRITÉRIO: Objeto de Valor (@Embeddable).
 * Não tem tabela nem ID próprios: suas colunas ficam dentro da tabela "cliente".
 */
@Embeddable
public class Endereco {
    @Column(length = 100)
    private String logradouro;

    @Column(length = 60)
    private String cidade;

    @Column(length = 2)
    private String uf;

    @Column(length = 8)
    private String cep;

    protected Endereco() {} // exigido pelo Hibernate

    public Endereco(String logradouro, String cidade, String uf, String cep) {
        this.logradouro = logradouro;
        this.cidade = cidade;
        this.uf = uf;
        this.cep = cep;
    }

    public String getLogradouro() { return logradouro; }
    public String getCidade() { return cidade; }
    public String getUf() { return uf; }
    public String getCep() { return cep; }

    @Override
    public String toString() {
        return logradouro + " - " + cidade + "/" + uf + " - CEP " + cep;
    }
}
