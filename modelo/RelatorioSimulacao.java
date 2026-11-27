package modelo;

import java.util.Collections;
import java.util.List;

public class RelatorioSimulacao {
    private final boolean viavel;
    private final List<Despesa> despesasMantidas;
    private final List<Despesa> despesasRetiradas;
    private final Planejamento planejamentoSimulado;
    private final int somaPrioridades;
    private final String mensagem;

    public RelatorioSimulacao(boolean viavel, List<Despesa> despesasMantidas, List<Despesa> despesasRetiradas,
                              Planejamento planejamentoSimulado, int somaPrioridades, String mensagem) {
        this.viavel = viavel;
        this.despesasMantidas = Collections.unmodifiableList(despesasMantidas);
        this.despesasRetiradas = Collections.unmodifiableList(despesasRetiradas);
        this.planejamentoSimulado = planejamentoSimulado;
        this.somaPrioridades = somaPrioridades;
        this.mensagem = mensagem;
    }

    public boolean isViavel() {
        return viavel;
    }

    public List<Despesa> getDespesasMantidas() {
        return despesasMantidas;
    }

    public List<Despesa> getDespesasRetiradas() {
        return despesasRetiradas;
    }

    public Planejamento getPlanejamentoSimulado() {
        return planejamentoSimulado;
    }

    public int getSomaPrioridades() {
        return somaPrioridades;
    }

    public String getMensagem() {
        return mensagem;
    }
}