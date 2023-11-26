package modelo;

import com.github.britooo.looca.api.core.Looca;
import com.github.britooo.looca.api.group.processos.Processo;
import com.github.britooo.looca.api.group.rede.RedeInterface;
import com.github.britooo.looca.api.group.rede.RedeInterfaceGroup;
import conexao.Conexao;
import dao.ValidacaoIdMaquina;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

public class MaquinaTipoComponente {
    Looca looca = new Looca();
    Conexao conexao = new Conexao();
    JdbcTemplate con = conexao.getConexaoDoBanco();
    private String fkMaquina;
    private Integer fkTipoComp;
    private Integer numProcesLogicos;
    private Integer numProcesFisicos;
    private Long tamanhoTotalRam;
    private Long tamanhoTotalDisco;
    private String enderecoMac;
    private String numSerial;
    private String ipv4;
    List<RedeInterface> redes = new ArrayList<>();
    ValidacaoIdMaquina validIdMaquina = new ValidacaoIdMaquina();

    public void salvar() {
        fkMaquina = looca.getProcessador().getId();
        Boolean existeIdMaquina = validIdMaquina.verificarParametro(fkMaquina);

        if(existeIdMaquina) {
//      Captura dados físicos do processador
        numProcesFisicos = looca.getProcessador().getNumeroCpusFisicas();
        numProcesLogicos = looca.getProcessador().getNumeroCpusLogicas();
        fkMaquina = looca.getProcessador().getId();

        // Captura dados físicos da RAM
//        tamanhoTotalRam = looca.getMemoria().getTotal() / 1000000000;
//
//        // Captura dados físicos de disco
//        numSerial = looca.getGrupoDeDiscos().getDiscos().get(0).getSerial();
//        tamanhoTotalDisco = looca.getGrupoDeDiscos().getVolumes().get(0).getTotal() / 1000000000;

        con.update("INSERT INTO maquinaTipoComponente (numProcesLogicos, numProcesFisicos, fkMaquina, fkTipoComp) VALUES (?, ?, ?, ?);", numProcesFisicos, numProcesLogicos, fkMaquina, 1);

//        con.update("INSERT INTO maquinaTipoComponente (tamanhoTotalRam, fkMaquina, fkTipoComp) VALUES (?, ?, ?);", tamanhoTotalRam, fkMaquina, 2);
//
//        con.update("INSERT INTO maquinaTipoComponente (numSerial, tamanhoTotalDisco, fkMaquina, fkTipoComp) VALUES (?, ?, ?, ?);", numSerial, tamanhoTotalDisco, fkMaquina, 3);

            // Captura dados físicos de rede
//            for (int i = 0; i < redes.size(); i++) {
//                enderecoMac = redes.get(i).getEnderecoMac();
//                ipv4 = redes.get(i).getEnderecoIpv4().toString();
//                if (redes.get(i).getBytesRecebidos() > 0 && redes.get(i).getBytesEnviados() > 0) {
//                    con.update("INSERT INTO maquinaTipoComponente (enderecoMac, ipv4, fkMaquina, fkTipoComp) VALUES (?, ?, ?, ?);", enderecoMac, ipv4, fkMaquina, 4);
//                    break;
//                }
//            }
        } else {
            System.out.println("Caiu no else");
        }
    }

    public void listar() {

    }

    public String getFkMaquina() {
        return fkMaquina;
    }

    public void setFkMaquina(String fkMaquina) {
        this.fkMaquina = fkMaquina;
    }

    public Integer getFkTipoComp() {
        return fkTipoComp;
    }

    public void setFkTipoComp(Integer fkTipoComp) {
        this.fkTipoComp = fkTipoComp;
    }

    public Integer getNumProcesLogicos() {
        return numProcesLogicos;
    }

    public void setNumProcesLogicos(Integer numProcesLogicos) {
        this.numProcesLogicos = numProcesLogicos;
    }

    public Integer getNumProcesFisicos() {
        return numProcesFisicos;
    }

    public void setNumProcesFisicos(Integer numProcesFisicos) {
        this.numProcesFisicos = numProcesFisicos;
    }

    public Long getTamanhoTotalRam() {
        return tamanhoTotalRam;
    }

    public void setTamanhoTotalRam(Long tamanhoTotalRam) {
        this.tamanhoTotalRam = tamanhoTotalRam;
    }

    public Long getTamanhoTotalDisco() {
        return tamanhoTotalDisco;
    }

    public void setTamanhoTotalDisco(Long tamanhoTotalDisco) {
        this.tamanhoTotalDisco = tamanhoTotalDisco;
    }

    public String getEnderecoMac() {
        return enderecoMac;
    }

    public void setEnderecoMac(String enderecoMac) {
        this.enderecoMac = enderecoMac;
    }

    public String getNumSerial() {
        return numSerial;
    }

    public void setNumSerial(String numSerial) {
        this.numSerial = numSerial;
    }

    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }

    @Override
    public String toString() {
        return """
                fkMaquina:          %s
                fkTipoComp:         %d
                numProcesLogicos:   %d
                numProcesFisicos:   %d
                tamanhoTotalTam:    %.2f
                tamanhoTotalDisco:  %.2f
                enderecoMac:        %s
                ipv4:               %s
                pid:                %s
                nomeProcesso:       %s""".formatted(fkMaquina, fkTipoComp, numProcesLogicos,
                numProcesFisicos, tamanhoTotalRam, tamanhoTotalDisco, enderecoMac, numSerial, ipv4);

    }
}
