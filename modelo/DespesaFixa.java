package modelo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DespesaFixa extends Despesa {

    public DespesaFixa(String descricao, double valorInformado, Set<Participante> participantesEnvolvidos, boolean essencial, int prioridade) {
        super(descricao, valorInformado, participantesEnvolvidos, essencial, prioridade);
    }

    @Override
    public Map<Participante, Double> calcularRateio() {
        Map<Participante, Double> rateio = new HashMap<>();
        int qtd = participantesEnvolvidos.size();

        if (qtd == 0) return rateio;

        long totalCentavos = Math.round(valorInformado * 100);
        long valorBaseCentavos = totalCentavos / qtd;
        long restoCentavos = totalCentavos % qtd;

        List<Participante> participantesOrdenados = new ArrayList<>(participantesEnvolvidos);
        participantesOrdenados.sort((p1, p2) -> Integer.compare(p1.getCodigo(), p2.getCodigo()));

        for (Participante p : participantesOrdenados) {
            long valorFinalCentavos = valorBaseCentavos;

            if (restoCentavos > 0) {
                valorFinalCentavos++;
                restoCentavos--;
            }

            rateio.put(p, valorFinalCentavos / 100.0);
        }

        return rateio;
    }
}