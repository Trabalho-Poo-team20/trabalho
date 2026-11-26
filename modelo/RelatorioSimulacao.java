package modelo;

import java.util.List;

public class RelatorioSimulacao {
    private boolean sucessoCenarioBase;
    private List<Despesa> despesasAprovadas;
    private String mensagem;

    public RelatorioSimulacao(boolean sucessoCenarioBase, List<Despesa> despesasAprovadas, String mensagem) {
        this.sucessoCenarioBase = sucessoCenarioBase;
        this.despesasAprovadas = despesasAprovadas;
        this.mensagem = mensagem;
    }

    public boolean isSucessoCenarioBase() {
        return sucessoCenarioBase;
    }

    public void setSucessoCenarioBase(boolean sucessoCenarioBase) {
        this.sucessoCenarioBase = sucessoCenarioBase;
    }

    public List<Despesa> getDespesasAprovadas() {
        return despesasAprovadas;
    }

    public void setDespesasAprovadas(List<Despesa> despesasAprovadas) {
        this.despesasAprovadas = despesasAprovadas;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}