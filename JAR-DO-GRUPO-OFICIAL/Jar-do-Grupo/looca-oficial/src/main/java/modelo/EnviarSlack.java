package modelo;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.webhook.Payload;
import com.github.seratch.jslack.api.webhook.WebhookResponse;
import conexao.ConexaoSlack;
import org.json.JSONObject;

import java.io.IOException;

public class EnviarSlack {

    public void enviarMensagemCpuMaximo(String hostname, Double usoCpu) throws IOException, InterruptedException {
        ConexaoSlack slack = new ConexaoSlack();

        JSONObject json = new JSONObject();
        String msg = """
                ALERTA CRÍTICO CPU:
                A máquina do usuário: %s
                Atingiu o dado: %.2f
                """.formatted(hostname, usoCpu);
        json.put("text", msg);
        slack.sendMessage(json);
    }

    public void enviarMensagemCpuMedio(String hostname, Double usoCpu) throws IOException, InterruptedException {
        ConexaoSlack slack = new ConexaoSlack();

        JSONObject json = new JSONObject();
        String msg = """
                ALERTA PREOCUPANTE CPU:
                A máquina do usuário: %s
                Atingiu o dado: %.2f
                """.formatted(hostname, usoCpu);
        json.put("text", msg);
        slack.sendMessage(json);
    }

    public void enviarMensagemRamMaximo(String hostname, Double usoRam) throws IOException, InterruptedException{
        ConexaoSlack slack = new ConexaoSlack();

        JSONObject json = new JSONObject();
        String msg = """
                ALERTA CRÍTICO RAM:
                A máquina do usuário: %s
                Atingiu o dado: %.2f
                """.formatted(hostname, usoRam);
        json.put("text", msg);
        slack.sendMessage(json);
    }

    public void enviarMensagemRamMedio(String hostname, Double usoRam) throws IOException, InterruptedException{
        ConexaoSlack slack = new ConexaoSlack();

        JSONObject json = new JSONObject();
        String msg = """
                ALERTA PREOCUPANTE RAM:
                A máquina do usuário: %s
                Atingiu o dado: %.2f
                """.formatted(hostname, usoRam);
        json.put("text", msg);
        slack.sendMessage(json);
    }

    public void enviarMensagemDiscoMaximo(String hostname, Integer atualDisco) throws IOException, InterruptedException{
        ConexaoSlack slack = new ConexaoSlack();

        JSONObject json = new JSONObject();
        String msg = """
                ALERTA CRÍTICO DISCO:
                A máquina do usuário: %s
                Atingiu o dado: %d
                """.formatted(hostname, atualDisco);
        json.put("text", msg);
        slack.sendMessage(json);
    }

    public void enviarMensagemDiscoMedio(String hostname, Integer atualDisco) throws IOException, InterruptedException{
        ConexaoSlack slack = new ConexaoSlack();

        JSONObject json = new JSONObject();
        String msg = """
                ALERTA PREOCUPANTE DISCO:
                A máquina do usuário: %s
                Atingiu o dado: %d
                """.formatted(hostname, atualDisco);
        json.put("text", msg);
        slack.sendMessage(json);
    }

    public void enviarMensagemRedeMaximo(String hostname, Double rede) throws IOException, InterruptedException{
        ConexaoSlack slack = new ConexaoSlack();

        JSONObject json = new JSONObject();
        String msg = """
                ALERTA CRÍTICO REDE:
                A máquina do usuário: %s
                Atingiu o dado: %.2f
                """.formatted(hostname, rede);
        json.put("text", msg);
        slack.sendMessage(json);
    }

    public void enviarMensagemRedeMedio(String hostname, Double rede) throws IOException, InterruptedException{
        ConexaoSlack slack = new ConexaoSlack();

        JSONObject json = new JSONObject();
        String msg = """
                ALERTA PREOCUPANTE REDE:
                A máquina do usuário: %s
                Atingiu o dado: %.2f
                """.formatted(hostname, rede);
        json.put("text", msg);
        slack.sendMessage(json);
    }

}
