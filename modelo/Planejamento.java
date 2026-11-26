package modelo;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class Planejamento implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Participante> participantes;
    private List<Despesa> despesas;
    private int proximoCodigoParticipante = 1;

    public Planejamento() {
        this.participantes = new ArrayList<>();
        this.despesas = new ArrayList<>();
    }

    public void atualizarProximoCodigo() {
        int max = 0;
        for(Participante p : participantes) {
            if(p.getCodigo() > max) max = p.getCodigo();
        }
        proximoCodigoParticipante = max + 1;
    }

    public void cadastrarParticipante(String nome, double orcamento) {
        Participante novo = new Participante(proximoCodigoParticipante, nome, orcamento);
        participantes.add(novo);
        proximoCodigoParticipante++;
    }

    public void cadastrarParticipante(Participante novoParticipante) {
        for (Participante p : participantes) {
            if (p.getCodigo() == novoParticipante.getCodigo()) {
                throw new IllegalArgumentException("Erro: Já existe um participante com este código.");
            }
        }
        participantes.add(novoParticipante);
        atualizarProximoCodigo();
    }

    public void editarParticipante(int codigo, String novoNome, double novoOrcamento) {
        for (Participante p : participantes) {
            if (p.getCodigo() == codigo) {
                p.setNome(novoNome);
                p.setOrcamento(novoOrcamento);
                return;
            }
        }
        throw new IllegalArgumentException("Participante não encontrado.");
    }

    public void removerParticipante(Participante p) {
        for (Despesa despesa : despesas) {
            if (despesa.getParticipantesEnvolvidos().contains(p)) {
                throw new IllegalArgumentException("Erro: Não é possível excluir '" + p.getNome() + "' pois há despesas vinculadas.");
            }
        }
        participantes.remove(p);
    }

    public void adicionarDespesa(Despesa novaDespesa) {
        despesas.add(novaDespesa);
    }

    // NOVO: Edição de despesa substituindo o objeto para permitir troca de tipo[cite: 1]
    public void editarDespesa(Despesa antiga, Despesa nova) {
        int index = despesas.indexOf(antiga);
        if (index != -1) {
            despesas.set(index, nova);
        } else {
            throw new IllegalArgumentException("Despesa não encontrada.");
        }
    }

    public void removerDespesa(Despesa d) {
        despesas.remove(d);
    }

    public double calcularTotalGeral() {
        double total = 0.0;
        for (Despesa despesa : despesas) {
            Map<Participante, Double> rateio = despesa.calcularRateio();
            for (Double valorCobrado : rateio.values()) {
                total += valorCobrado;
            }
        }
        return total;
    }

    public double gerarResumoIndividual(int codigoParticipante) {
        double totalIndividual = 0.0;
        Participante participanteAlvo = null;
        for (Participante p : participantes) {
            if (p.getCodigo() == codigoParticipante) {
                participanteAlvo = p;
                break;
            }
        }
        if (participanteAlvo == null) return 0.0;

        for (Despesa despesa : despesas) {
            Map<Participante, Double> rateio = despesa.calcularRateio();
            if (rateio.containsKey(participanteAlvo)) {
                totalIndividual += rateio.get(participanteAlvo);
            }
        }
        return totalIndividual;
    }

    public List<Participante> getParticipantes() { return participantes; }
    public List<Despesa> getDespesas() { return despesas; }
}