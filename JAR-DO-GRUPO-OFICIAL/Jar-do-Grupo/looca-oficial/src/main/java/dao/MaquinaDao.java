package dao;

import com.github.britooo.looca.api.core.Looca;
import com.github.britooo.looca.api.group.processador.Processador;
import com.github.britooo.looca.api.group.processos.Processo;
import com.github.britooo.looca.api.group.rede.RedeInterface;
import com.github.britooo.looca.api.group.sistema.Sistema;
import conexao.Conexao;
import conexao.ConexaoAws;
import conexao.ConexaoSlack;
import modelo.EnviarSlack;
import modelo.Maquina;
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
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Class.forName;

public class MaquinaDao {
    Looca looca = new Looca();
    Conexao conexao = new Conexao();
    JdbcTemplate con = conexao.getConexaoDoBanco();
    // conexão aws
    ConexaoAws conexaoAWS = new ConexaoAws();
    JdbcTemplate conAWS = conexaoAWS.getConexaoDoBanco();
    private String idMaquina = looca.getProcessador().getId();
    private String hostName = looca.getRede().getParametros().getHostName();
    private String sistemaOperacional = looca.getSistema().getSistemaOperacional();
    private Integer arquitetura = looca.getSistema().getArquitetura();
    private String fabricante = looca.getProcessador().getFabricante();
    private Long tempoAtividade = looca.getSistema().getTempoDeAtividade();

    List<RedeInterface> redes = new ArrayList<>();
    ConexaoSlack conexaoSlack = new ConexaoSlack();
    ValidacaoIdMaquina validIdMaquina = new ValidacaoIdMaquina();
    MaquinaTipoComponenteDao maquinaTipoComponenteDao = new MaquinaTipoComponenteDao();

    public void salvar() throws IOException {
        Boolean existeIdMaquina = validIdMaquina.verificarParametro(idMaquina);

        try{
        if(existeIdMaquina){
            maquinaTipoComponenteDao.salvar();

            Double usoCpu = looca.getProcessador().getUso();
//            if (usoCpu >= parametroPreocupante && usoCpu < parametroCritico) {
//                conexaoSlack.enviarMensagemCpu(usoCpu);
//            }

            // Captura do uso de RAM;
            Long usoRam = looca.getMemoria().getEmUso();
            Double divisaoUsoRam = (usoRam / 1000000000.0);
            String usoRamFormatado = String.format("%.1f", divisaoUsoRam);
            StringBuilder s = new StringBuilder(usoRamFormatado);
            s.setCharAt(1, '.');


//            if (s >= parametroRamPreocupante && s < parametroRamCritico) {
//                conexaoSlack.enviarMensagemRam(s);
//            }

            Long ramDisponivel = looca.getMemoria().getDisponivel();
            Double divisaoRamDisponivel = (ramDisponivel / 1000000000.0);
            String ramDisponivelFormatado = String.format("%.1f", divisaoRamDisponivel);
            StringBuilder sb = new StringBuilder(ramDisponivelFormatado);
            sb.setCharAt(1, '.');


            // Captura do uso de DISCO;
            Long tamanhoTotalDisco = (looca.getGrupoDeDiscos().getVolumes().get(0).getTotal() / 1000 / 1000 / 1000);
            Long tamanhoDisponivel = (looca.getGrupoDeDiscos().getVolumes().get(0).getDisponivel() / 1000 / 1000 / 1000);

            Integer atualDisco = Math.toIntExact(tamanhoTotalDisco - tamanhoDisponivel);

            con.update("INSERT INTO dadosComponente (qtdUsoCpu, fkMaquina, fkTipoComponente) VALUES (?, ?, ?);", usoCpu, idMaquina, 1);

            con.update("INSERT INTO dadosComponente (memoriaEmUso, memoriaDisponivel, fkMaquina, fkTipoComponente) VALUES (?, ?, ?, ?);", s, sb, idMaquina, 2);

            con.update("INSERT INTO dadosComponente (usoAtualDisco, usoDisponivelDisco, fkMaquina, fkTipoComponente) VALUES (?, ?, ?, ?);", (tamanhoTotalDisco - tamanhoDisponivel), tamanhoDisponivel, idMaquina, 3);

            // Conexão Slack

            EnviarSlack slack = new EnviarSlack();

            Double parametroMaximoCpu = con.queryForObject("SELECT maximo FROM parametro where fkTipoComponente = 1 ORDER BY maximo DESC LIMIT 1;", Double.class);

            Double parametroMedioCpu = con.queryForObject("SELECT medio FROM parametro where fkTipoComponente = 1 ORDER BY maximo DESC LIMIT 1;", Double.class);

            if(usoCpu >= parametroMedioCpu && usoCpu < parametroMaximoCpu){
                slack.enviarMensagemCpuMedio(hostName, usoCpu);
            }

            if (usoCpu >= parametroMaximoCpu){
                slack.enviarMensagemCpuMaximo(hostName, usoCpu);
            }

            Double parametroMaximoram = con.queryForObject("SELECT maximo FROM parametro where fkTipoComponente = 2 ORDER BY maximo DESC LIMIT 1;", Double.class);

            Double parametroMedioRam = con.queryForObject("SELECT medio FROM parametro where fkTipoComponente = 2 ORDER BY maximo DESC LIMIT 1;", Double.class);

            if(divisaoUsoRam >= parametroMedioRam && divisaoUsoRam < parametroMaximoram){
                slack.enviarMensagemRamMedio(hostName, divisaoUsoRam);
            }

            if(divisaoUsoRam >= parametroMaximoram){
                slack.enviarMensagemRamMaximo(hostName, divisaoUsoRam);
            }

            Double parametroMaximoDisco = con.queryForObject("SELECT maximo FROM parametro where fkTipoComponente = 3 ORDER BY maximo DESC LIMIT 1;", Double.class);

            Double parametroMedioDisco = con.queryForObject("SELECT medio FROM parametro where fkTipoComponente = 3 ORDER BY maximo DESC LIMIT 1;", Double.class);


            if(atualDisco >= parametroMedioDisco && atualDisco < parametroMaximoDisco){
                slack.enviarMensagemDiscoMedio(hostName, atualDisco);
            }

            if(atualDisco >= parametroMaximoDisco){
                slack.enviarMensagemDiscoMaximo(hostName, atualDisco);
            }

            // Fim conexão slack

            Path path = Paths.get("/home/ubuntu/logs");
            Path path1 = Paths.get("/home/ubuntu/logs/" + LocalDate.now());
            File log = new File("/home/ubuntu/logs/" + LocalDate.now() + "/" + LocalDate.now() + ".txt");
            LocalDateTime momentoAtual = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String DateTimeFormatado = momentoAtual.format(formatter);

            if (!Files.exists(path)) {
                Files.createDirectory(path);
                if(!Files.exists(path1)) {
                    Files.createDirectory(path1);
                    log.createNewFile();
                    FileWriter fw = new FileWriter(log, true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    bw.write(DateTimeFormatado + " Inserindo dados na tabela dadosComponente (lines 84 até 88)");
                    bw.newLine();

                    bw.close();
                    fw.close();
                } else {
                FileWriter fw = new FileWriter(log, true);
                BufferedWriter bw = new BufferedWriter(fw);

                bw.write(DateTimeFormatado + " Inserindo dados na tabela dadosComponente (lines 84 até 88)");
                bw.newLine();

                bw.close();
                fw.close();
                }
            }else {
                FileWriter fw = new FileWriter(log, true);
                BufferedWriter bw = new BufferedWriter(fw);

                bw.write(DateTimeFormatado + " Inserindo dados na tabela dadosComponente (lines 84 até 88)");
                bw.newLine();

                bw.close();
                fw.close();
            }

        }
        else{
            con.update("INSERT INTO maquina (idMaquina, hostName, sistemaOperacional, arquitetura, fabricante, tempoAtividade) VALUES (?, ?, ?, ?, ?, ?);",
                    idMaquina, hostName, sistemaOperacional, arquitetura,
                    fabricante, tempoAtividade);
            maquinaTipoComponenteDao.salvar();

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

            Integer atualDisco = Math.toIntExact(tamanhoTotalDisco - tamanhoDisponivel);

            con.update("INSERT INTO dadosComponente (qtdUsoCpu, fkMaquina, fkTipoComponente) VALUES (?, ?, ?);", usoCpu, idMaquina, 1);

            con.update("INSERT INTO dadosComponente (memoriaEmUso, memoriaDisponivel, fkMaquina, fkTipoComponente) VALUES (?, ?, ?, ?);", s, sb, idMaquina, 2);

            con.update("INSERT INTO dadosComponente (usoAtualDisco, usoDisponivelDisco, fkMaquina, fkTipoComponente) VALUES (?, ?, ?, ?);", (tamanhoTotalDisco - tamanhoDisponivel), tamanhoDisponivel, idMaquina, 3);

            // Conexão Slack

            EnviarSlack slack = new EnviarSlack();

            Double parametroMaximoCpu = con.queryForObject("SELECT maximo FROM parametro where fkTipoComponente = 1 ORDER BY maximo DESC LIMIT 1;", Double.class);

            Double parametroMedioCpu = con.queryForObject("SELECT medio FROM parametro where fkTipoComponente = 1 ORDER BY maximo DESC LIMIT 1;", Double.class);

            if(usoCpu >= parametroMedioCpu && usoCpu < parametroMaximoCpu){
                slack.enviarMensagemCpuMedio(hostName, usoCpu);
            }

            if (usoCpu >= parametroMaximoCpu){
                slack.enviarMensagemCpuMaximo(hostName, usoCpu);
            }

            Double parametroMaximoram = con.queryForObject("SELECT maximo FROM parametro where fkTipoComponente = 2 ORDER BY maximo DESC LIMIT 1;", Double.class);

            Double parametroMedioRam = con.queryForObject("SELECT medio FROM parametro where fkTipoComponente = 2 ORDER BY maximo DESC LIMIT 1;", Double.class);

            if(divisaoUsoRam >= parametroMedioRam && divisaoUsoRam < parametroMaximoram){
                slack.enviarMensagemRamMedio(hostName, divisaoUsoRam);
            }

            if(divisaoUsoRam >= parametroMaximoram){
                slack.enviarMensagemRamMaximo(hostName, divisaoUsoRam);
            }

            Double parametroMaximoDisco = con.queryForObject("SELECT maximo FROM parametro where fkTipoComponente = 3 ORDER BY maximo DESC LIMIT 1;", Double.class);

            Double parametroMedioDisco = con.queryForObject("SELECT medio FROM parametro where fkTipoComponente = 3 ORDER BY maximo DESC LIMIT 1;", Double.class);



            if(atualDisco >= parametroMedioDisco && atualDisco < parametroMaximoDisco){
                slack.enviarMensagemDiscoMedio(hostName, atualDisco);
            }

            if(atualDisco >= parametroMaximoDisco){
                slack.enviarMensagemDiscoMaximo(hostName, atualDisco);
            }
            // Fim conexão slackAws

            Path path = Paths.get("/home/ubuntu/logs");
            Path path1 = Paths.get("/home/ubuntu/logs/" + LocalDate.now());
            File log = new File("/home/ubuntu/logs/" + LocalDate.now() + "/" + LocalDate.now() + ".txt");
            LocalDateTime momentoAtual = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String DateTimeFormatado = momentoAtual.format(formatter);

            if (!Files.exists(path)) {
                Files.createDirectory(path);
                if (!Files.exists(path1)) {
                    Files.createDirectory(path1);
                    log.createNewFile();
                    FileWriter fw = new FileWriter(log, true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    bw.write(DateTimeFormatado + " Não tinha máquina, mas o java criou a máquina e capturou os dados da tabela dadosComponente (lines 136 até 140)");
                    bw.newLine();

                    bw.close();
                    fw.close();
                }else {
                    FileWriter fw = new FileWriter(log, true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    bw.write(DateTimeFormatado + " Não tinha máquina, mas o java criou a máquina e capturou os dados da tabela dadosComponente (lines 136 até 140)");
                    bw.newLine();

                    bw.close();
                    fw.close();
                }
            }else {
                FileWriter fw = new FileWriter(log, true);
                BufferedWriter bw = new BufferedWriter(fw);

                bw.write(DateTimeFormatado + " Não tinha máquina, mas o java criou a máquina e capturou os dados da tabela dadosComponente (lines 136 até 140)");
                bw.newLine();

                bw.close();
                fw.close();
            }
        }
    } catch (Exception erroCapturaDadosMaquinaDao){
        erroCapturaDadosMaquinaDao.getMessage();
            Path path = Paths.get("/home/ubuntu/logs");
            Path path1 = Paths.get("/home/ubuntu/logs/" + LocalDate.now());
            File log = new File("/home/ubuntu/logs/" + LocalDate.now() + "/" + LocalDate.now() + ".txt");
            LocalDateTime momentoAtual = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String DateTimeFormatado = momentoAtual.format(formatter);

            if (!Files.exists(path)) {
                Files.createDirectory(path);
                if (!Files.exists(path1)) {
                Files.createDirectory(path1);
                log.createNewFile();
                FileWriter fw = new FileWriter(log, true);
                BufferedWriter bw = new BufferedWriter(fw);

                bw.write(DateTimeFormatado + erroCapturaDadosMaquinaDao.getMessage());
                bw.newLine();

                bw.close();
                fw.close();
                }else {
                    FileWriter fw = new FileWriter(log, true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    bw.write(DateTimeFormatado + erroCapturaDadosMaquinaDao.getMessage());
                    bw.newLine();

                    bw.close();
                    fw.close();}
            }else {
                FileWriter fw = new FileWriter(log, true);
                BufferedWriter bw = new BufferedWriter(fw);

                bw.write(DateTimeFormatado + erroCapturaDadosMaquinaDao.getMessage());
                bw.newLine();

                bw.close();
                fw.close();
            }
        }
    }
    public void salvarAWS() throws IOException {
        Boolean existeIdMaquina = validIdMaquina.verificarParametro(idMaquina);

        try{
            if(existeIdMaquina){
                maquinaTipoComponenteDao.salvarAWS();

                Double usoCpu = looca.getProcessador().getUso();
//            if (usoCpu >= parametroPreocupante && usoCpu < parametroCritico) {
//                conexaoSlack.enviarMensagemCpu(usoCpu);
//            }

                // Captura do uso de RAM;
                Long usoRam = looca.getMemoria().getEmUso();
                Double divisaoUsoRam = (usoRam / 1000000000.0);
                String usoRamFormatado = String.format("%.1f", divisaoUsoRam);
                StringBuilder s = new StringBuilder(usoRamFormatado);
                s.setCharAt(1, '.');


//            if (s >= parametroRamPreocupante && s < parametroRamCritico) {
//                conexaoSlack.enviarMensagemRam(s);
//            }

                Long ramDisponivel = looca.getMemoria().getDisponivel();
                Double divisaoRamDisponivel = (ramDisponivel / 1000000000.0);
                String ramDisponivelFormatado = String.format("%.1f", divisaoRamDisponivel);
                StringBuilder sb = new StringBuilder(ramDisponivelFormatado);
                sb.setCharAt(1, '.');


                // Captura do uso de DISCO;
                Long tamanhoTotalDisco = (looca.getGrupoDeDiscos().getVolumes().get(0).getTotal() / 1000 / 1000 / 1000);
                Long tamanhoDisponivel = (looca.getGrupoDeDiscos().getVolumes().get(0).getDisponivel() / 1000 / 1000 / 1000);

                Integer atualDisco = Math.toIntExact(tamanhoTotalDisco - tamanhoDisponivel);

                conAWS.update("INSERT INTO dadosComponente (qtdUsoCpu, fkMaquina, fkTipoComponente) VALUES (?, ?, ?);", usoCpu, idMaquina, 1);

                conAWS.update("INSERT INTO dadosComponente (memoriaEmUso, memoriaDisponivel, fkMaquina, fkTipoComponente) VALUES (?, ?, ?, ?);", s, sb, idMaquina, 2);

                conAWS.update("INSERT INTO dadosComponente (usoAtualDisco, usoDisponivelDisco, fkMaquina, fkTipoComponente) VALUES (?, ?, ?, ?);", (tamanhoTotalDisco - tamanhoDisponivel), tamanhoDisponivel, idMaquina, 3);

                // Conexão Slack

                EnviarSlack slack = new EnviarSlack();

                Double parametroMaximoCpu = conAWS.queryForObject("SELECT maximo FROM parametro where fkTipoComponente = 1 ORDER BY maximo DESC LIMIT 1;", Double.class);

                Double parametroMedioCpu = conAWS.queryForObject("SELECT medio FROM parametro where fkTipoComponente = 1 ORDER BY maximo DESC LIMIT 1;", Double.class);

                if(usoCpu >= parametroMedioCpu && usoCpu < parametroMaximoCpu){
                    slack.enviarMensagemCpuMedio(hostName, usoCpu);
                }

                if (usoCpu >= parametroMaximoCpu){
                    slack.enviarMensagemCpuMaximo(hostName, usoCpu);
                }

                Double parametroMaximoram = conAWS.queryForObject("SELECT maximo FROM parametro where fkTipoComponente = 2 ORDER BY maximo DESC LIMIT 1;", Double.class);

                Double parametroMedioRam = conAWS.queryForObject("SELECT medio FROM parametro where fkTipoComponente = 2 ORDER BY maximo DESC LIMIT 1;", Double.class);

                if(divisaoUsoRam >= parametroMedioRam && divisaoUsoRam < parametroMaximoram){
                    slack.enviarMensagemRamMedio(hostName, divisaoUsoRam);
                }

                if(divisaoUsoRam >= parametroMaximoram){
                    slack.enviarMensagemRamMaximo(hostName, divisaoUsoRam);
                }

                Double parametroMaximoDisco = conAWS.queryForObject("SELECT maximo FROM parametro where fkTipoComponente = 3 ORDER BY maximo DESC LIMIT 1;", Double.class);

                Double parametroMedioDisco = conAWS.queryForObject("SELECT medio FROM parametro where fkTipoComponente = 3 ORDER BY maximo DESC LIMIT 1;", Double.class);


                if(atualDisco >= parametroMedioDisco && atualDisco < parametroMaximoDisco){
                    slack.enviarMensagemDiscoMedio(hostName, atualDisco);
                }

                if(atualDisco >= parametroMaximoDisco){
                    slack.enviarMensagemDiscoMaximo(hostName, atualDisco);
                }

                // Fim conAWSexão slack

                Path path = Paths.get("/home/ubuntu/logs");
                Path path1 = Paths.get("/home/ubuntu/logs/" + LocalDate.now());
                File log = new File("/home/ubuntu/logs/" + LocalDate.now() + "/" + LocalDate.now() + ".txt");
                LocalDateTime momentoAtual = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                String DateTimeFormatado = momentoAtual.format(formatter);

                if (!Files.exists(path)) {
                    Files.createDirectory(path);
                    if(!Files.exists(path1)) {
                        Files.createDirectory(path1);
                        log.createNewFile();
                        FileWriter fw = new FileWriter(log, true);
                        BufferedWriter bw = new BufferedWriter(fw);

                        bw.write(DateTimeFormatado + " Inserindo dados na tabela dadosComponente (lines 84 até 88)");
                        bw.newLine();

                        bw.close();
                        fw.close();
                    } else {
                        FileWriter fw = new FileWriter(log, true);
                        BufferedWriter bw = new BufferedWriter(fw);

                        bw.write(DateTimeFormatado + " Inserindo dados na tabela dadosComponente (lines 84 até 88)");
                        bw.newLine();

                        bw.close();
                        fw.close();
                    }
                }else {
                    FileWriter fw = new FileWriter(log, true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    bw.write(DateTimeFormatado + " Inserindo dados na tabela dadosComponente (lines 84 até 88)");
                    bw.newLine();

                    bw.close();
                    fw.close();
                }

            }
            else{
                conAWS.update("INSERT INTO maquina (idMaquina, hostName, sistemaOperacional, arquitetura, fabricante, tempoAtividade) VALUES (?, ?, ?, ?, ?, ?);",
                        idMaquina, hostName, sistemaOperacional, arquitetura,
                        fabricante, tempoAtividade);
                maquinaTipoComponenteDao.salvar();

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

                Integer atualDisco = Math.toIntExact(tamanhoTotalDisco - tamanhoDisponivel);

                conAWS.update("INSERT INTO dadosComponente (qtdUsoCpu, fkMaquina, fkTipoComponente) VALUES (?, ?, ?);", usoCpu, idMaquina, 1);

                conAWS.update("INSERT INTO dadosComponente (memoriaEmUso, memoriaDisponivel, fkMaquina, fkTipoComponente) VALUES (?, ?, ?, ?);", s, sb, idMaquina, 2);

                conAWS.update("INSERT INTO dadosComponente (usoAtualDisco, usoDisponivelDisco, fkMaquina, fkTipoComponente) VALUES (?, ?, ?, ?);", (tamanhoTotalDisco - tamanhoDisponivel), tamanhoDisponivel, idMaquina, 3);

                // Conexão Slack

                EnviarSlack slack = new EnviarSlack();

                Double parametroMaximoCpu = conAWS.queryForObject("SELECT maximo FROM parametro where fkTipoComponente = 1 ORDER BY maximo DESC LIMIT 1;", Double.class);

                Double parametroMedioCpu = conAWS.queryForObject("SELECT medio FROM parametro where fkTipoComponente = 1 ORDER BY maximo DESC LIMIT 1;", Double.class);

                if(usoCpu >= parametroMedioCpu && usoCpu < parametroMaximoCpu){
                    slack.enviarMensagemCpuMedio(hostName, usoCpu);
                }

                if (usoCpu >= parametroMaximoCpu){
                    slack.enviarMensagemCpuMaximo(hostName, usoCpu);
                }

                Double parametroMaximoram = conAWS.queryForObject("SELECT maximo FROM parametro where fkTipoComponente = 2 ORDER BY maximo DESC LIMIT 1;", Double.class);

                Double parametroMedioRam = conAWS.queryForObject("SELECT medio FROM parametro where fkTipoComponente = 2 ORDER BY maximo DESC LIMIT 1;", Double.class);

                if(divisaoUsoRam >= parametroMedioRam && divisaoUsoRam < parametroMaximoram){
                    slack.enviarMensagemRamMedio(hostName, divisaoUsoRam);
                }

                if(divisaoUsoRam >= parametroMaximoram){
                    slack.enviarMensagemRamMaximo(hostName, divisaoUsoRam);
                }

                Double parametroMaximoDisco = conAWS.queryForObject("SELECT maximo FROM parametro where fkTipoComponente = 3 ORDER BY maximo DESC LIMIT 1;", Double.class);

                Double parametroMedioDisco = conAWS.queryForObject("SELECT medio FROM parametro where fkTipoComponente = 3 ORDER BY maximo DESC LIMIT 1;", Double.class);



                if(atualDisco >= parametroMedioDisco && atualDisco < parametroMaximoDisco){
                    slack.enviarMensagemDiscoMedio(hostName, atualDisco);
                }

                if(atualDisco >= parametroMaximoDisco){
                    slack.enviarMensagemDiscoMaximo(hostName, atualDisco);
                }
                // Fim conAWSexão slackAws

                Path path = Paths.get("/home/ubuntu/logs");
                Path path1 = Paths.get("/home/ubuntu/logs/" + LocalDate.now());
                File log = new File("/home/ubuntu/logs/" + LocalDate.now() + "/" + LocalDate.now() + ".txt");
                LocalDateTime momentoAtual = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                String DateTimeFormatado = momentoAtual.format(formatter);

                if (!Files.exists(path)) {
                    Files.createDirectory(path);
                    if (!Files.exists(path1)) {
                        Files.createDirectory(path1);
                        log.createNewFile();
                        FileWriter fw = new FileWriter(log, true);
                        BufferedWriter bw = new BufferedWriter(fw);

                        bw.write(DateTimeFormatado + " Não tinha máquina, mas o java criou a máquina e capturou os dados da tabela dadosComponente (lines 136 até 140)");
                        bw.newLine();

                        bw.close();
                        fw.close();
                    }else {
                        FileWriter fw = new FileWriter(log, true);
                        BufferedWriter bw = new BufferedWriter(fw);

                        bw.write(DateTimeFormatado + " Não tinha máquina, mas o java criou a máquina e capturou os dados da tabela dadosComponente (lines 136 até 140)");
                        bw.newLine();

                        bw.close();
                        fw.close();
                    }
                }else {
                    FileWriter fw = new FileWriter(log, true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    bw.write(DateTimeFormatado + " Não tinha máquina, mas o java criou a máquina e capturou os dados da tabela dadosComponente (lines 136 até 140)");
                    bw.newLine();

                    bw.close();
                    fw.close();
                }
            }
        } catch (Exception erroCapturaDadosMaquinaDao){
            erroCapturaDadosMaquinaDao.getMessage();
            Path path = Paths.get("/home/ubuntu/logs");
            Path path1 = Paths.get("/home/ubuntu/logs/" + LocalDate.now());
            File log = new File("/home/ubuntu/logs/" + LocalDate.now() + "/" + LocalDate.now() + ".txt");
            LocalDateTime momentoAtual = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String DateTimeFormatado = momentoAtual.format(formatter);

            if (!Files.exists(path)) {
                Files.createDirectory(path);
                if (!Files.exists(path1)) {
                    Files.createDirectory(path1);
                    log.createNewFile();
                    FileWriter fw = new FileWriter(log, true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    bw.write(DateTimeFormatado + erroCapturaDadosMaquinaDao.getMessage());
                    bw.newLine();

                    bw.close();
                    fw.close();
                }else {
                    FileWriter fw = new FileWriter(log, true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    bw.write(DateTimeFormatado + erroCapturaDadosMaquinaDao.getMessage());
                    bw.newLine();

                    bw.close();
                    fw.close();}
            }else {
                FileWriter fw = new FileWriter(log, true);
                BufferedWriter bw = new BufferedWriter(fw);

                bw.write(DateTimeFormatado + erroCapturaDadosMaquinaDao.getMessage());
                bw.newLine();

                bw.close();
                fw.close();
            }
        }
    }

}