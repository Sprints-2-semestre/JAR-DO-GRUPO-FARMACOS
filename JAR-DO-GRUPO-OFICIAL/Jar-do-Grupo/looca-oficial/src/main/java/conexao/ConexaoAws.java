package conexao;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class ConexaoAws {

    private JdbcTemplate conexaoDoBanco;
    public ConexaoAws() {
        BasicDataSource dataSource = new BasicDataSource();

        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        dataSource.setUrl("jdbc:sqlserver://54.197.240.46:1433;database=farmacos;"
                + "user=sa;password=farmacos;encrypt=true;"
                + "trustServerCertificate=true;hostNameInCertificate=*.database.windows.net;loginTimeout=30;");
        dataSource.setUsername("sa");
        dataSource.setPassword("farmacos");
        this.conexaoDoBanco = new JdbcTemplate(dataSource);
    }
    public JdbcTemplate getConexaoDoBanco() {
        return conexaoDoBanco;
    }
}
