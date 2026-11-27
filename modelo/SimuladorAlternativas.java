package modelo;

import java.util.ArrayList;
import java.util.List;

public class SimuladorAlternativas {

    public RelatorioSimulacao buscarMelhorCenario(Planejamento planejamentoOriginal) {
        if (planejamentoOriginal.getParticipantes().isEmpty()) {
            throw new IllegalArgumentException("Cadastre participantes antes de executar o simulador.");
        }

        List<Despesa> essenciais = new ArrayList<>();
        List<Despesa> opcionais = new ArrayList<>();

        for (Despesa despesa : planejamentoOriginal.getDespesas()) {
            if (despesa.isEssencial()) {
                essenciais.add(despesa);
            } else {
                opcionais.add(despesa);
            }
        }

        if (opcionais.size() > 10) {
            throw new IllegalArgumentException("O simulador suporta no máximo 10 despesas opcionais.");
        }

        Planejamento simulacaoBase = criarCopiaPlanejamento(planejamentoOriginal.getParticipantes(), essenciais);

        StringBuilder excessos = new StringBuilder();
        for (Participante p : simulacaoBase.getParticipantes()) {
            double gastoEssencial = simulacaoBase.gerarResumoIndividual(p.getCodigo());
            if (gastoEssencial > p.getOrcamento()) {
                double excesso = Math.round((gastoEssencial - p.getOrcamento()) * 100.0) / 100.0;
                excessos.append(String.format(" -> %s: essenciais custam R$ %.2f | Orçamento: R$ %.2f | Excesso: R$ %.2f\n",
                        p.getNome(), gastoEssencial, p.getOrcamento(), excesso));
            }
        }

        if (!excessos.isEmpty()) {
            return new RelatorioSimulacao(
                    false,
                    essenciais,
                    opcionais,
                    simulacaoBase,
                    0,
                    "Sem solução viável: apenas as despesas essenciais já ultrapassam o orçamento:\n" + excessos
            );
        }

        int n = opcionais.size();
        int melhorSomaPrioridades = -1;
        int melhorMascara = 0;

        for (int mask = 0; mask < (1 << n); mask++) {
            int somaPrioridades = 0;
            List<Despesa> candidatas = new ArrayList<>(essenciais);

            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    Despesa opc = opcionais.get(i);
                    candidatas.add(opc);
                    somaPrioridades += opc.getPrioridade();
                }
            }

            if (somaPrioridades <= melhorSomaPrioridades) {
                continue;
            }

            Planejamento teste = criarCopiaPlanejamento(planejamentoOriginal.getParticipantes(), candidatas);
            if (dentroDosLimites(teste)) {
                melhorSomaPrioridades = somaPrioridades;
                melhorMascara = mask;
            }
        }

        List<Despesa> mantidas = new ArrayList<>(essenciais);
        List<Despesa> retiradas = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if ((melhorMascara & (1 << i)) != 0) {
                mantidas.add(opcionais.get(i));
            } else {
                retiradas.add(opcionais.get(i));
            }
        }

        Planejamento cenarioFinal = criarCopiaPlanejamento(planejamentoOriginal.getParticipantes(), mantidas);
        return new RelatorioSimulacao(
                true,
                mantidas,
                retiradas,
                cenarioFinal,
                melhorSomaPrioridades,
                "Alternativa viável encontrada com prioridade máxima (" + melhorSomaPrioridades + " pontos)."
        );
    }

    private Planejamento criarCopiaPlanejamento(List<Participante> participantes, List<Despesa> despesas) {
        Planejamento copia = new Planejamento();
        for (Participante p : participantes) {
            copia.cadastrarParticipante(p);
        }
        for (Despesa d : despesas) {
            copia.adicionarDespesa(d);
        }
        return copia;
    }

    private boolean dentroDosLimites(Planejamento plano) {
        for (Participante p : plano.getParticipantes()) {
            if (plano.gerarResumoIndividual(p.getCodigo()) > p.getOrcamento()) {
                return false;
            }
        }
        return true;
    }
}