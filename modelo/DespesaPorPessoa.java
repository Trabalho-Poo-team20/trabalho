package modelo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DespesaPorPessoa extends Despesa {
    private static final long serialVersionUID = 1L;

    public DespesaPorPessoa(String descricao, double valorInformado, Set<Participante> participantesEnvolvidos,
                            boolean essencial, int prioridade) {
        super(descricao, valorInformado, participantesEnvolvidos, essencial, prioridade);
    }

    @Override
    public String getTipoDescricao() {
        return "Por Pessoa";
    }

    @Override
    public Map<Participante, Double> calcularRateio() {
        Map<Participante, Double> rateio = new LinkedHashMap<>();
        long valorUnitarioCentavos = Math.round(valorInformado * 100.0);
        double valorUnitario = valorUnitarioCentavos / 100.0;

        List<Participante> participantesOrdenados = new ArrayList<>(participantesEnvolvidos);
        participantesOrdenados.sort(Comparator.comparingInt(Participante::getCodigo));

        for (Participante p : participantesOrdenados) {
            rateio.put(p, valorUnitario);
        }

        return rateio;
    }
}