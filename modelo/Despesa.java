package modelo;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public abstract class Despesa implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String descricao;
    protected double valorInformado;
    protected Set<Participante> participantesEnvolvidos;
    protected boolean essencial;
    protected int prioridade;

    public Despesa(String descricao, double valorInformado, Set<Participante> participantesEnvolvidos,
                   boolean essencial, int prioridade) {
        setDescricao(descricao);
        setValorInformado(valorInformado);
        setParticipantesEnvolvidos(participantesEnvolvidos);
        setClassificacao(essencial, prioridade);
    }

    public String getDescricao() {
        return descricao;
    }

    public double getValorInformado() {
        return valorInformado;
    }

    public Set<Participante> getParticipantesEnvolvidos() {
        return Collections.unmodifiableSet(participantesEnvolvidos);
    }

    public boolean isEssencial() {
        return essencial;
    }

    public int getPrioridade() {
        return prioridade;
    }

    public void setDescricao(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("A descrição da despesa não pode ser vazia.");
        }
        this.descricao = descricao.trim();
    }

    public void setValorInformado(double valorInformado) {
        if (valorInformado <= 0) {
            throw new IllegalArgumentException("O valor da despesa deve ser maior que zero.");
        }
        this.valorInformado = valorInformado;
    }

    public void setParticipantesEnvolvidos(Set<Participante> participantesEnvolvidos) {
        if (participantesEnvolvidos == null || participantesEnvolvidos.isEmpty()) {
            throw new IllegalArgumentException("A despesa deve ter pelo menos um participante.");
        }
        this.participantesEnvolvidos = new LinkedHashSet<>(participantesEnvolvidos);
    }

    public void setClassificacao(boolean essencial, int prioridade) {
        if (!essencial && prioridade <= 0) {
            throw new IllegalArgumentException("Despesas opcionais devem ter prioridade inteira positiva (maior que zero).");
        }
        this.essencial = essencial;
        this.prioridade = essencial ? 0 : prioridade;
    }

    public abstract String getTipoDescricao();

    public abstract Map<Participante, Double> calcularRateio();

    @Override
    public String toString() {
        return descricao + " (R$ " + String.format("%.2f", valorInformado) + ")";
    }
}