package dao;

import com.github.britooo.looca.api.core.Looca;
import com.github.britooo.looca.api.group.processos.Processo;
import com.github.britooo.looca.api.group.rede.RedeInterface;
import conexao.Conexao;
import conexao.ConexaoAws;
import conexao.ConexaoSlack;
import modelo.EnviarSlack;
import modelo.MaquinaTipoComponente;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MaquinaTipoComponenteDao {
    Looca looca = new Looca();
    Conexao conexao = new Conexao();
    JdbcTemplate con = conexao.getConexaoDoBanco();

    ConexaoAws conexaoAWS = new ConexaoAws();
    JdbcTemplate conAWS = conexaoAWS.getConexaoDoBanco();

    ValidacaoIdMaquina validacaoIdMaquina = new ValidacaoIdMaquina();

    // Captura da fkMaquina;
    private String fkMaquina = looca.getProcessador().getId();
    private String hostName = looca.getRede().getParametros().getHostName();
    // Capturas da Máquina
    private Integer qtdCpuLogico = looca.getProcessador().getNumeroCpusLogicas();
    private Integer qtdCpuFisico = looca.getProcessador().getNumeroCpusFisicas();
    // Capturas de RAM;
    private Long totalRam = looca.getMemoria().getTotal() / 1000000000;
    // Capturas de Disco;
    private String numSerial = looca.getGrupoDeDiscos().getDiscos().get(0).getSerial();
    private Long totalDisco = looca.getGrupoDeDiscos().getVolumes().get(0).getTotal() / 1000000000;
    // Capturas de Rede;
    private String enderecoMac;
    private String ipv4;
    List<RedeInterface> redes = looca.getRede().getGrupoDeInterfaces().getInterfaces();

    public void salvar() throws IOException {
        try {
            con.update("INSERT INTO maquinaTipoComponente (numProcesLogicos, numProcesFisicos, fkMaquina, fkTipoComp) VALUES (?, ?, ?, ?);", qtdCpuLogico, qtdCpuFisico, fkMaquina, 1);

            con.update("INSERT INTO maquinaTipoComponente (tamanhoTotalRam, fkMaquina, fkTipoComp) VALUES (?, ?, ?);", totalRam, fkMaquina, 2);

            con.update("INSERT INTO maquinaTipoComponente (numSerial, tamanhoTotalDisco, fkMaquina, fkTipoComp) VALUES (?, ?, ?, ?);", numSerial, totalDisco, fkMaquina, 3);

            for (int i = 0; i < redes.size(); i++) {
                if (redes.get(i).getBytesRecebidos() > 0 && redes.get(i).getBytesEnviados() > 0) {
                    enderecoMac = redes.get(i).getEnderecoMac();
                    ipv4 = redes.get(i).getEnderecoIpv4().toString();
                    con.update("INSERT INTO maquinaTipoComponente (enderecoMac, ipv4, fkMaquina, fkTipoComp) VALUES (?, ?, ?, ?);", enderecoMac, ipv4, fkMaquina, 4);

                    Long bytesRecebido = redes.get(i).getBytesRecebidos();
                    double bytesRecebidosDouble = bytesRecebido.doubleValue() / 100000;
                    String bytesRecebidoFormatado = String.format("%.2f", bytesRecebidosDouble);
                    bytesRecebidoFormatado = bytesRecebidoFormatado.replace(",", ".");

                    Long bytesEnviado = redes.get(i).getBytesEnviados();
                    double bytesEnviadosDouble = bytesEnviado.doubleValue() / 100000;
                    String bytesEnviadoFormatado = String.format("%.2f", bytesEnviadosDouble);
                    bytesEnviadoFormatado = bytesEnviadoFormatado.replace(",", ".");

                    con.update("INSERT INTO dadosComponente (bytesRecebido, bytesEnviado, fkMaquina, fkTipoComponente) VALUES (?, ?, ?, ?);", bytesRecebidoFormatado, bytesEnviadoFormatado, fkMaquina, 4);


                    EnviarSlack slack = new EnviarSlack();

                    Double parametroMaximoRede = con.queryForObject("SELECT maximo FROM parametro where fkTipoComponente = 4 ORDER BY maximo DESC LIMIT 1;", Double.class);

                    Double parametroMedioRede = con.queryForObject("SELECT medio FROM parametro where fkTipoComponente = 4 ORDER BY maximo DESC LIMIT 1;", Double.class);

                    if(bytesRecebidosDouble >= parametroMedioRede && bytesRecebidosDouble < parametroMaximoRede){
                        slack.enviarMensagemRedeMedio(hostName, bytesRecebidosDouble);
                    }

                    if(bytesEnviadosDouble >= parametroMedioRede && bytesEnviadosDouble < parametroMaximoRede){
                        slack.enviarMensagemRedeMedio(hostName, bytesEnviadosDouble);
                    }

                    if(bytesRecebidosDouble >= parametroMaximoRede){
                        slack.enviarMensagemRedeMaximo(hostName, bytesRecebidosDouble);
                    }

                    if(bytesEnviadosDouble >= parametroMaximoRede){
                        slack.enviarMensagemRedeMaximo(hostName, bytesEnviadosDouble);
                    }

                    break;
                }
            }



            Path path = Paths.get("/home/ubuntu/logs");
            Path path1 = Paths.get("/home/ubuntu/logs/" + LocalDate.now());
            File log = new File("/home/ubuntu/logs/" + LocalDate.now() + "/" + LocalDate.now() + ".txt");
            LocalDateTime momentoAtual = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String DateTimeFormatado = momentoAtual.format(formatter);

            if (!Files.exists(path)) {
                Files.createDirectory(path);
                if (!Files.exists(path1)){
                    Files.createDirectory(path1);
                    log.createNewFile();
                    FileWriter fw = new FileWriter(log, true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    bw.write(DateTimeFormatado + " Inserindo dados não voláteis e (bytesRecebidos e bytesEnviados) na tabela maquinaTipoComponente,  (lines 57 até 61)");
                    bw.newLine();

                    bw.close();
                    fw.close();
                }else {
                    FileWriter fw = new FileWriter(log, true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    bw.write(DateTimeFormatado + " Inserindo dados não voláteis e (bytesRecebidos e bytesEnviados) na tabela maquinaTipoComponente,  (lines 57 até 61)");
                    bw.newLine();

                    bw.close();
                    fw.close();
                }
            } else {
                FileWriter fw = new FileWriter(log, true);
                BufferedWriter bw = new BufferedWriter(fw);

                bw.write(DateTimeFormatado + " Inserindo dados não voláteis e (bytesRecebidos e bytesEnviados) na tabela maquinaTipoComponente,  (lines 57 até 61)");
                bw.newLine();

                bw.close();
                fw.close();
            }

        }catch (Exception erroMaquinaTipoComponenteDao){
            Path path = Paths.get("/home/ubuntu/logs");
            Path path1 = Paths.get("/home/ubuntu/logs/" + LocalDate.now());
            File log = new File("/home/ubuntu/logs/" + LocalDate.now() + "/" + LocalDate.now() + ".txt");
            LocalDateTime momentoAtual = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String DateTimeFormatado = momentoAtual.format(formatter);

            if (!Files.exists(path)) {
                Files.createDirectory(path);
                if (!Files.exists(path1)){
                    Files.createDirectory(path1);
                    log.createNewFile();
                    FileWriter fw = new FileWriter(log, true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    bw.write(DateTimeFormatado + " Erro ao tentar capturar os dados não voláteis na tabela maquinaTipoComponente (lines 57 até 61)");
                    bw.newLine();

                    bw.close();
                    fw.close();}
                else {
                    FileWriter fw = new FileWriter(log, true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    bw.write(DateTimeFormatado + " Erro ao tentar capturar os dados não voláteis na tabela maquinaTipoComponente (lines 57 até 61)");
                    bw.newLine();

                    bw.close();
                    fw.close();
                }
            }else {
                FileWriter fw = new FileWriter(log, true);
                BufferedWriter bw = new BufferedWriter(fw);

                bw.write(DateTimeFormatado + " Erro ao tentar capturar os dados não voláteis na tabela maquinaTipoComponente (lines 57 até 61");
                bw.newLine();

                bw.close();
                fw.close();
            }

        }
    }

    public void salvarAWS() throws IOException {
        try {
            conAWS.update("INSERT INTO maquinaTipoComponente (numProcesLogicos, numProcesFisicos, fkMaquina, fkTipoComp) VALUES (?, ?, ?, ?);", qtdCpuLogico, qtdCpuFisico, fkMaquina, 1);

            conAWS.update("INSERT INTO maquinaTipoComponente (tamanhoTotalRam, fkMaquina, fkTipoComp) VALUES (?, ?, ?);", totalRam, fkMaquina, 2);

            conAWS.update("INSERT INTO maquinaTipoComponente (numSerial, tamanhoTotalDisco, fkMaquina, fkTipoComp) VALUES (?, ?, ?, ?);", numSerial, totalDisco, fkMaquina, 3);

            for (int i = 0; i < redes.size(); i++) {
                if (redes.get(i).getBytesRecebidos() > 0 && redes.get(i).getBytesEnviados() > 0) {
                    enderecoMac = redes.get(i).getEnderecoMac();
                    ipv4 = redes.get(i).getEnderecoIpv4().toString();
                    conAWS.update("INSERT INTO maquinaTipoComponente (enderecoMac, ipv4, fkMaquina, fkTipoComp) VALUES (?, ?, ?, ?);", enderecoMac, ipv4, fkMaquina, 4);

                    Long bytesRecebido = redes.get(i).getBytesRecebidos();
                    double bytesRecebidosDouble = bytesRecebido.doubleValue() / 100000;
                    String bytesRecebidoFormatado = String.format("%.2f", bytesRecebidosDouble);
                    bytesRecebidoFormatado = bytesRecebidoFormatado.replace(",", ".");

                    Long bytesEnviado = redes.get(i).getBytesEnviados();
                    double bytesEnviadosDouble = bytesEnviado.doubleValue() / 100000;
                    String bytesEnviadoFormatado = String.format("%.2f", bytesEnviadosDouble);
                    bytesEnviadoFormatado = bytesEnviadoFormatado.replace(",", ".");

                    conAWS.update("INSERT INTO dadosComponente (bytesRecebido, bytesEnviado, fkMaquina, fkTipoComponente) VALUES (?, ?, ?, ?);", bytesRecebidoFormatado, bytesEnviadoFormatado, fkMaquina, 4);


                    EnviarSlack slack = new EnviarSlack();

                    Double parametroMaximoRede = conAWS.queryForObject("SELECT maximo FROM parametro where fkTipoComponente = 4 ORDER BY maximo DESC LIMIT 1;", Double.class);

                    Double parametroMedioRede = conAWS.queryForObject("SELECT medio FROM parametro where fkTipoComponente = 4 ORDER BY maximo DESC LIMIT 1;", Double.class);

                    if(bytesRecebidosDouble >= parametroMedioRede && bytesRecebidosDouble < parametroMaximoRede){
                        slack.enviarMensagemRedeMedio(hostName, bytesRecebidosDouble);
                    }

                    if(bytesEnviadosDouble >= parametroMedioRede && bytesEnviadosDouble < parametroMaximoRede){
                        slack.enviarMensagemRedeMedio(hostName, bytesEnviadosDouble);
                    }

                    if(bytesRecebidosDouble >= parametroMaximoRede){
                        slack.enviarMensagemRedeMaximo(hostName, bytesRecebidosDouble);
                    }

                    if(bytesEnviadosDouble >= parametroMaximoRede){
                        slack.enviarMensagemRedeMaximo(hostName, bytesEnviadosDouble);
                    }

                    break;
                }
            }



            Path path = Paths.get("/home/ubuntu/logs");
            Path path1 = Paths.get("/home/ubuntu/logs/" + LocalDate.now());
            File log = new File("/home/ubuntu/logs/" + LocalDate.now() + "/" + LocalDate.now() + ".txt");
            LocalDateTime momentoAtual = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String DateTimeFormatado = momentoAtual.format(formatter);

            if (!Files.exists(path)) {
                Files.createDirectory(path);
                if (!Files.exists(path1)){
                    Files.createDirectory(path1);
                    log.createNewFile();
                    FileWriter fw = new FileWriter(log, true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    bw.write(DateTimeFormatado + " Inserindo dados não voláteis e (bytesRecebidos e bytesEnviados) na tabela maquinaTipoComponente,  (lines 57 até 61)");
                    bw.newLine();

                    bw.close();
                    fw.close();
                }else {
                    FileWriter fw = new FileWriter(log, true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    bw.write(DateTimeFormatado + " Inserindo dados não voláteis e (bytesRecebidos e bytesEnviados) na tabela maquinaTipoComponente,  (lines 57 até 61)");
                    bw.newLine();

                    bw.close();
                    fw.close();
                }
            } else {
                FileWriter fw = new FileWriter(log, true);
                BufferedWriter bw = new BufferedWriter(fw);

                bw.write(DateTimeFormatado + " Inserindo dados não voláteis e (bytesRecebidos e bytesEnviados) na tabela maquinaTipoComponente,  (lines 57 até 61)");
                bw.newLine();

                bw.close();
                fw.close();
            }

        }catch (Exception erroMaquinaTipoComponenteDao){
            Path path = Paths.get("/home/ubuntu/logs");
            Path path1 = Paths.get("/home/ubuntu/logs/" + LocalDate.now());
            File log = new File("/home/ubuntu/logs/" + LocalDate.now() + "/" + LocalDate.now() + ".txt");
            LocalDateTime momentoAtual = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String DateTimeFormatado = momentoAtual.format(formatter);

            if (!Files.exists(path)) {
                Files.createDirectory(path);
                if (!Files.exists(path1)){
                    Files.createDirectory(path1);
                    log.createNewFile();
                    FileWriter fw = new FileWriter(log, true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    bw.write(DateTimeFormatado + " Erro ao tentar capturar os dados não voláteis na tabela maquinaTipoComponente (lines 57 até 61)");
                    bw.newLine();

                    bw.close();
                    fw.close();}
                else {
                    FileWriter fw = new FileWriter(log, true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    bw.write(DateTimeFormatado + " Erro ao tentar capturar os dados não voláteis na tabela maquinaTipoComponente (lines 57 até 61)");
                    bw.newLine();

                    bw.close();
                    fw.close();
                }
            }else {
                FileWriter fw = new FileWriter(log, true);
                BufferedWriter bw = new BufferedWriter(fw);

                bw.write(DateTimeFormatado + " Erro ao tentar capturar os dados não voláteis na tabela maquinaTipoComponente (lines 57 até 61");
                bw.newLine();

                bw.close();
                fw.close();
            }

        }
    }


    public void listar() {
        System.out.println(con.query("""
                        select maquinaTipoComponente.idProcessador,
                        maquinaTipoComponente.num_ProcesLogicos,
                        maquinaTipoComponente.num_ProcesFisicos,
                        maquinaTipoComponente.tamanhoTotalRam,
                        maquinaTipoComponente.numSerial,
                        maquinaTipoComponente.enderecoMac,
                        maquinaTipoComponente.ipv4
                        from maquinaTipoComponente
                        where fkMaquina = 1;""",
                new BeanPropertyRowMapper<>(MaquinaTipoComponente.class)));
    }
    public void listarAWS() {
        System.out.println(conAWS.query("""
                        select maquinaTipoComponente.idProcessador,
                        maquinaTipoComponente.num_ProcesLogicos,
                        maquinaTipoComponente.num_ProcesFisicos,
                        maquinaTipoComponente.tamanhoTotalRam,
                        maquinaTipoComponente.numSerial,
                        maquinaTipoComponente.enderecoMac,
                        maquinaTipoComponente.ipv4
                        from maquinaTipoComponente
                        where fkMaquina = 1;""",
                new BeanPropertyRowMapper<>(MaquinaTipoComponente.class)));
    }
}

