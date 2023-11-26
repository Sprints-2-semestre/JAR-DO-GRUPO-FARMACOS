package modelo;

import com.github.seratch.jslack.api.model.IntegrationLog;

public class DadosComponentes{
    private Integer idDadosComponentes;
    private String fkMaquina;
    private Integer fkTipoComponente;
    private Integer fkMaquinaTipoComponente;
    private Double qtdUsoCpu;
    private Double memoriaEmUso;
    private Double memoriaDisponivel;
    private Integer usoAtualDisco;
    private Integer usoDisponivelDisco;
    private Double bytesRecebido;
    private Double bytesEnviado;
    private Double qtdMemoriaProcesso;
    private Double qtdProcessadorProcesso;

    public DadosComponentes(String fkMaquina, Integer fkTipoComponente, Integer fkMaquinaTipoComponente, Double qtdUsoCpu, Double memoriaEmUso, Double memoriaDisponivel, Integer usoAtualDisco, Integer usoDisponivelDisco, Double bytesRecebido, Double bytesEnviado, Double qtdMemoriaProcesso, Double qtdProcessadorProcesso) {
        idDadosComponentes = null;
        this.fkMaquina = fkMaquina;
        this.fkTipoComponente = fkTipoComponente;
        this.fkMaquinaTipoComponente = fkMaquinaTipoComponente;
        this.qtdUsoCpu = qtdUsoCpu;
        this.memoriaEmUso = memoriaEmUso;
        this.memoriaDisponivel = memoriaDisponivel;
        this.usoAtualDisco = usoAtualDisco;
        this.usoDisponivelDisco = usoDisponivelDisco;
        this.bytesRecebido = bytesRecebido;
        this.bytesEnviado = bytesEnviado;
        this.qtdMemoriaProcesso = qtdMemoriaProcesso;
        this.qtdProcessadorProcesso = qtdProcessadorProcesso;
    }

    public Integer getIdDadosComponentes() {
        return idDadosComponentes;
    }

    public void setIdDadosComponentes(Integer idDadosComponentes) {
        this.idDadosComponentes = idDadosComponentes;
    }

    public String getFkMaquina() {
        return fkMaquina;
    }

    public void setFkMaquina(String fkMaquina) {
        this.fkMaquina = fkMaquina;
    }

    public Integer getFkTipoComponente() {
        return fkTipoComponente;
    }

    public void setFkTipoComponente(Integer fkTipoComponente) {
        this.fkTipoComponente = fkTipoComponente;
    }

    public Integer getFkMaquinaTipoComponente() {
        return fkMaquinaTipoComponente;
    }

    public void setFkMaquinaTipoComponente(Integer fkMaquinaTipoComponente) {
        this.fkMaquinaTipoComponente = fkMaquinaTipoComponente;
    }

    public Double getQtdUsoCpu() {
        return qtdUsoCpu;
    }

    public void setQtdUsoCpu(Double qtdUsoCpu) {
        this.qtdUsoCpu = qtdUsoCpu;
    }

    public Double getMemoriaEmUso() {
        return memoriaEmUso;
    }

    public void setMemoriaEmUso(Double memoriaEmUso) {
        this.memoriaEmUso = memoriaEmUso;
    }

    public Double getMemoriaDisponivel() {
        return memoriaDisponivel;
    }

    public void setMemoriaDisponivel(Double memoriaDisponivel) {
        this.memoriaDisponivel = memoriaDisponivel;
    }

    public Integer getUsoAtualDisco() {
        return usoAtualDisco;
    }

    public void setUsoAtualDisco(Integer usoAtualDisco) {
        this.usoAtualDisco = usoAtualDisco;
    }

    public Integer getUsoDisponivelDisco() {
        return usoDisponivelDisco;
    }

    public void setUsoDisponivelDisco(Integer usoDisponivelDisco) {
        this.usoDisponivelDisco = usoDisponivelDisco;
    }

    public Double getBytesRecebido() {
        return bytesRecebido;
    }

    public void setBytesRecebido(Double bytesRecebido) {
        this.bytesRecebido = bytesRecebido;
    }

    public Double getBytesEnviado() {
        return bytesEnviado;
    }

    public void setBytesEnviado(Double bytesEnviado) {
        this.bytesEnviado = bytesEnviado;
    }

    public Double getQtdMemoriaProcesso() {
        return qtdMemoriaProcesso;
    }

    public void setQtdMemoriaProcesso(Double qtdMemoriaProcesso) {
        this.qtdMemoriaProcesso = qtdMemoriaProcesso;
    }

    public Double getQtdProcessadorProcesso() {
        return qtdProcessadorProcesso;
    }

    public void setQtdProcessadorProcesso(Double qtdProcessadorProcesso) {
        this.qtdProcessadorProcesso = qtdProcessadorProcesso;
    }

    @Override
    public String toString() {
        return """
                idDadosComponentes:         %d
                fkMaquina:                  %s
                fkTipoComponente:           %d
                fkMaquinaTipoComponente:    %d
                qtdUsoCpu:                  %.2f
                memoriaEmUso:               %.1f
                memoriaDisponivel:          %.1f
                usoAtualDisco:              %d
                usoDisponivelDisco:         %d
                bytesRecebido:              %.2f
                bytesEnviado:               %.2f
                qtdMemoriaProceso:          %.2f
                qtdProcessadorProcesso:     %.2f""".formatted(idDadosComponentes, fkMaquina, fkTipoComponente,
                fkMaquinaTipoComponente, qtdUsoCpu, memoriaEmUso, memoriaDisponivel, usoAtualDisco,
                usoDisponivelDisco, bytesRecebido, bytesEnviado, qtdMemoriaProcesso, qtdProcessadorProcesso);
    }
}


