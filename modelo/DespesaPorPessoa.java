package modelo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DespesaPorPessoa extends Despesa {

    public DespesaPorPessoa(String descricao, double valorInformado, Set<Participante> participantesEnvolvidos, boolean essencial, int prioridade) {
        super(descricao, valorInformado, participantesEnvolvidos, essencial, prioridade);
    }

    @Override
    public Map<Participante, Double> calcularRateio() {
        Map<Participante, Double> rateio = new HashMap<>();

        for (Participante p : participantesEnvolvidos) {
            rateio.put(p, valorInformado);
        }

        return rateio;
    }
}