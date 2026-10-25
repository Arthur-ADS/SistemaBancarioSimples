package banco.repositorio;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoFactory {
    private static final String URL = "jdbc:mysql://localhost:3306/banco_poo";
    private static final String USUARIO = "root";
    private static final String SENHA = "********";

    public static Connection getConexao() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch(ClassNotFoundException e) {
            throw new SQLException("Driver JDBC do MYSQL não encontrado.",e);
        }
    }
}
