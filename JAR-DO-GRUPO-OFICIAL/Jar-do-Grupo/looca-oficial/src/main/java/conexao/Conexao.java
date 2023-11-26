package conexao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Statement;

public class Conexao {

    private JdbcTemplate conexaoDoBanco;

    public Conexao() {
        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");

        dataSource.setUrl("jdbc:mysql://localhost:3306/farmacos");

        dataSource.setUsername("farmacos");

        dataSource.setPassword("urubu100");

        this.conexaoDoBanco = new JdbcTemplate(dataSource);
    }


    public JdbcTemplate getConexaoDoBanco() {
        return conexaoDoBanco;
    }


}
