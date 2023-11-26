package dao;

import com.github.britooo.looca.api.core.Looca;
import com.github.britooo.looca.api.group.discos.Disco;
import com.github.britooo.looca.api.group.memoria.Memoria;
import com.github.britooo.looca.api.group.processador.Processador;
import com.github.britooo.looca.api.group.rede.Rede;
import conexao.Conexao;
import conexao.ConexaoAws;
import conexao.ConexaoSlack;
import modelo.DadosComponentes;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DadosComponentesDao {
    Looca looca = new Looca();
    Conexao conexao = new Conexao();
    JdbcTemplate con = conexao.getConexaoDoBanco();

    ConexaoAws conexaoAWS = new ConexaoAws();
    JdbcTemplate conAWS = conexaoAWS.getConexaoDoBanco();
    // fkMaquina
    String fkMaquina = looca.getProcessador().getId();

    public void salvar() {
        // Captura do uso da CPU;
        Double usoCpu = looca.getProcessador().getUso();

        // Captura do uso de RAM;
        Long usoRam = looca.getMemoria().getEmUso();
        Double divisaoUsoRam = (usoRam / 1000000000.0);
        String usoRamFormatado = String.format("%.1f", divisaoUsoRam);
        StringBuilder s = new StringBuilder(usoRamFormatado);
        s.setCharAt(1, '.');

        Long ramDisponivel = looca.getMemoria().getDisponivel();
        Double divisaoRamDisponivel = (ramDisponivel / 1000000000.0);
        String ramDisponivelFormatado = String.format("%.1f", divisaoRamDisponivel);
        StringBuilder sb = new StringBuilder(ramDisponivelFormatado);
        sb.setCharAt(1, '.');


        // Captura do uso de DISCO;
        Long tamanhoTotalDisco = (looca.getGrupoDeDiscos().getVolumes().get(0).getTotal() / 1000 / 1000 / 1000);
        Long tamanhoDisponivel = (looca.getGrupoDeDiscos().getVolumes().get(0).getDisponivel() / 1000 / 1000 / 1000);

        if (usoCpu < 100.0) {
            con.update("INSERT INTO dadosComponente (qtdUsoCpu, fkMaquina) VALUES (?, ?);", usoCpu, fkMaquina);
        }
        con.update("INSERT INTO dadosComponente (memoriaEmUso, memoriaDisponivel, fkMaquina) VALUES (?, ?, ?);", s, sb, fkMaquina);

        con.update("INSERT INTO dadosComponente (usoAtualDisco, usoDisponivelDisco, fkMaquina) VALUES (?, ?, ?);", (tamanhoTotalDisco - tamanhoDisponivel), tamanhoDisponivel, fkMaquina);

    }
    public void salvarAws() {
        // Captura do uso da CPU;
        Double usoCpu = looca.getProcessador().getUso();

        // Captura do uso de RAM;
        Long usoRam = looca.getMemoria().getEmUso();
        Double divisaoUsoRam = (usoRam / 1000000000.0);
        String usoRamFormatado = String.format("%.1f", divisaoUsoRam);
        StringBuilder s = new StringBuilder(usoRamFormatado);
        s.setCharAt(1, '.');

        Long ramDisponivel = looca.getMemoria().getDisponivel();
        Double divisaoRamDisponivel = (ramDisponivel / 1000000000.0);
        String ramDisponivelFormatado = String.format("%.1f", divisaoRamDisponivel);
        StringBuilder sb = new StringBuilder(ramDisponivelFormatado);
        sb.setCharAt(1, '.');

        // Captura do uso de DISCO;
        Long tamanhoTotalDisco = (looca.getGrupoDeDiscos().getVolumes().get(0).getTotal() / 1000 / 1000 / 1000);
        Long tamanhoDisponivel = (looca.getGrupoDeDiscos().getVolumes().get(0).getDisponivel() / 1000 / 1000 / 1000);

        if (usoCpu < 100.0) {
            conAWS.update("INSERT INTO dadosComponente (qtdUsoCpu, fkMaquina) VALUES (?, ?);", usoCpu, fkMaquina);
        }
        conAWS.update("INSERT INTO dadosComponente (memoriaEmUso, memoriaDisponivel, fkMaquina) VALUES (?, ?, ?);", s, sb, fkMaquina);

        conAWS.update("INSERT INTO dadosComponente (usoAtualDisco, usoDisponivelDisco, fkMaquina) VALUES (?, ?, ?);", (tamanhoTotalDisco - tamanhoDisponivel), tamanhoDisponivel, fkMaquina);

    }
    public void listarAws() {
        System.out.println(conAWS.query("""
                        select dadosComponente.qtdUsoCpu,
                        dadosComponente.memoriaEmUso,
                        dadosComponente.memoriaDisponivel,
                        dadosComponente.usoAtualDisco,
                        dadosComponente.usoDisponivelDisco,
                        dadosComponente.pacoteRecebido,
                        dadosComponente.pacoteEnviado
                        from dadosComponente
                        where fkMaquina = 1;""",
                new BeanPropertyRowMapper<>(DadosComponentes.class)));
    }
    public void listar() {
        System.out.println(con.query("""
                        select dadosComponente.qtdUsoCpu,
                        dadosComponente.memoriaEmUso,
                        dadosComponente.memoriaDisponivel,
                        dadosComponente.usoAtualDisco,
                        dadosComponente.usoDisponivelDisco,
                        dadosComponente.pacoteRecebido,
                        dadosComponente.pacoteEnviado
                        from dadosComponente
                        where fkMaquina = 1;""",
                new BeanPropertyRowMapper<>(DadosComponentes.class)));
    }
}