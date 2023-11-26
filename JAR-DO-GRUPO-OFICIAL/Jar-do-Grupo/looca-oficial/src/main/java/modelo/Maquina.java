package modelo;

public class Maquina {
    private String idMaquina;
    private String sistemaOperacional;
    private Integer arquitetura;
    private String fabricante;
    private String tempoAtividade;
    private Integer fkAme;

    public String getIdMaquina() {
        return idMaquina;
    }

    public void setIdMaquina(String idMaquina) {
        this.idMaquina = idMaquina;
    }

    public String getSistemaOperacional() {
        return sistemaOperacional;
    }

    public void setSistemaOperacional(String sistemaOperacional) {
        this.sistemaOperacional = sistemaOperacional;
    }

    public Integer getArquitetura() {
        return arquitetura;
    }

    public void setArquitetura(Integer arquitetura) {
        this.arquitetura = arquitetura;
    }

    public String getFabricante() {
        return fabricante;
    }

    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    public String getTempoAtividade() {
        return tempoAtividade;
    }

    public void setTempoAtividade(String tempoAtividade) {
        this.tempoAtividade = tempoAtividade;
    }

    public Integer getFkAme() {
        return fkAme;
    }

    public void setFkAme(Integer fkAme) {
        this.fkAme = fkAme;
    }

    @Override
    public String toString() {
        return """
                idMaquina:              %s
                Sistema Operacional:    %s
                Arquitetura:            %d
                Fabricante:             %s
                Tempo atividade:        %s
                fkAme:                  %d""".formatted(idMaquina, sistemaOperacional, arquitetura, fabricante,
                fabricante, tempoAtividade, fkAme);
    }
}
