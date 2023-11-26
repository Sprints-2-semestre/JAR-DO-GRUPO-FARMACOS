package teste;

import conexao.Conexao;
import dao.DadosComponentesDao;
import dao.MaquinaDao;
import dao.MaquinaTipoComponenteDao;
import dao.ValidacaoEmail;
import modelo.Maquina;
import modelo.MaquinaTipoComponente;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Teste {
    public static void main(String[] args) throws IOException {
        ValidacaoEmail validacaoEmail = new ValidacaoEmail();

        Scanner leitor = new Scanner(System.in);
        System.out.println(
                """
                
                        *------------------------------------------------------------------------------------------------*
                        ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
                        *------------------------------------------------------------------------------------------------*
                        |                 |||                                                                            |
                        |               |||                                                                              |
                        | |||||||||  |||||||||   ||||||||   ||||||||||||  |||||||||  ||||||||  ||||||||||   |||||||||    |
                        | ||         ||     ||   ||    |||  ||   ||   ||  ||     ||  ||        ||      ||   ||           |
                        | ||         ||     ||   ||    |||  ||   ||   ||  ||     ||  ||        ||      ||   |||||||||    |
                        | ||||||     |||||||||   ||||||||   ||   ||   ||  |||||||||  ||        ||      ||          ||    |
                        | ||         ||     ||   ||    |||  ||   ||   ||  ||     ||  ||        ||      ||          ||    |
                        | ||         ||     ||   ||     ||| ||   ||   ||  ||     ||  ||||||||  ||||||||||   |||||||||    |
                        |                                                                                                |
                        *------------------------------------------------------------------------------------------------*
                        ||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
                        *------------------------------------------------------------------------------------------------*
                        """);

        System.out.println("""
                ------------------------------------------------
                |           Seja Bem Vindo a Fármacos          |
                ------------------------------------------------
                | Digite o seu email para verificarmos se você |
                | tem acesso ao nosso sistema                  |
                |                                              |
                | Digite seu email:                            |
                |                                              |
                ------------------------------------------------
                """);
        String emailUsuario = leitor.next();

        Boolean existeEmail = validacaoEmail.verificarParametro(emailUsuario);


        if (existeEmail.equals(true)) {
            MaquinaDao maquinaDao = new MaquinaDao();
            MaquinaTipoComponente maquinaTipoComponente = new MaquinaTipoComponente();
            Timer timer = new Timer();

            TimerTask inserirBanco = new TimerTask() {
                @Override
                public void run() {
                    try {
                        maquinaDao.salvar();
                        maquinaDao.salvarAWS();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            timer.scheduleAtFixedRate(inserirBanco, 0, 4000);
        } else{
            System.out.println("Não foi encontrado seu email. Contrate o nosso serviço primeiro. Obrigado");
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

                bw.write(DateTimeFormatado + " Tentativa de EMAIL incorreta...");
                bw.newLine();

                bw.close();
                fw.close();}
                else {
                    FileWriter fw = new FileWriter(log, true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    bw.write(DateTimeFormatado + " Tentativa de EMAIL incorreta...");
                    bw.newLine();

                    bw.close();
                    fw.close();
                }
        }else {
                if (Files.exists(path1)){
                    log.createNewFile();
                FileWriter fw = new FileWriter(log, true);
                BufferedWriter bw = new BufferedWriter(fw);

                bw.write(DateTimeFormatado + " Tentativa de EMAIL incorreta...");
                bw.newLine();

                bw.close();
                fw.close();}else {
                    Files.createDirectory(path1);
                    log.createNewFile();
                    FileWriter fw = new FileWriter(log, true);
                    BufferedWriter bw = new BufferedWriter(fw);

                    bw.write(DateTimeFormatado + " Tentativa de EMAIL incorreta...");
                    bw.newLine();

                    bw.close();
                    fw.close();
                }
            }
    }
}
}