package banco.repositorio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import banco.excecao.PersistenciaException;
import banco.modelo.Conta;
import banco.modelo.ContaCorrente;
import banco.modelo.ContaPoupanca;

public class ContaRepositorioMySQL implements ContaRepositorio {
    // Implementação dos métodos do repositório de contas usando MySQL
    private static final String COLUNAS = "numero, titular, tipo, saldo";

    public void salvar(Conta conta) {
        
        String sql = "INSERT INTO conta ("+COLUNAS+") VALUES (?,?,?,?)"
        + "ON DUPLICATE KEY UPDATE saldo = ?";

        try (Connection con = ConexaoFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, conta.getNumero());
                ps.setString(2, conta.getTitular());
                ps.setString(3, conta instanceof ContaPoupanca ? "POUPANCA" : "CORRENTE");
                ps.setBigDecimal(4, conta.getSaldo());
                ps.setBigDecimal(5, conta.getSaldo());
                ps.executeUpdate();
             } catch (SQLException e) {
                throw new PersistenciaException("Erro ao salvar a conta"+conta.getNumero()+"no banco de dados.", e);
             }
    }

    public Optional<Conta> buscarPorNumero(String numero) {
        // Código para buscar a conta pelo número no banco de dados MySQL
        String sql = "SELECT * FROM conta where numero = ?";

        try (Connection con = ConexaoFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, numero);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
                }
             } catch (SQLException e) {
                throw new PersistenciaException("Erro ao buscar a conta "+numero+" .", e);
             }
    }

    public List<Conta> listar() {
        String sql = "SELECT * FROM conta ORDER BY numero";
        List<Conta> contas = new ArrayList<>();
        
        try(Connection con = ConexaoFactory.getConexao();
             PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
                    while(rs.next()) {
                        contas.add(mapear(rs));
                    }
                return contas;
             } catch (SQLException e) {
                throw new PersistenciaException("Erro ao listar as contas.", e);
             }
            }

    //converte uma linha da tabela em um objeto Conta.        
    public Conta mapear(ResultSet rs) throws SQLException {
        String numero = rs.getString("numero");
        String titular = rs.getString("titular");
        BigDecimal saldo = rs.getBigDecimal("saldo");

        if("POUPANCA".equals(rs.getString("tipo"))) {
            return new ContaPoupanca(numero, titular, saldo);
        } else {
            return new ContaCorrente(numero, titular, saldo);
        }
             
    }

}
