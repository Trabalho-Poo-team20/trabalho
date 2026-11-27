package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
        for (Participante p : participantes) {
            if (p.getCodigo() > max) {
                max = p.getCodigo();
            }
        }
        proximoCodigoParticipante = max + 1;
    }

    public void cadastrarParticipante(String nome, double orcamento) {
        Participante novo = new Participante(proximoCodigoParticipante, nome, orcamento);
        participantes.add(novo);
        participantes.sort(Comparator.comparingInt(Participante::getCodigo));
        proximoCodigoParticipante++;
    }

    public void cadastrarParticipante(Participante novoParticipante) {
        if (novoParticipante == null) {
            throw new IllegalArgumentException("Participante inválido.");
        }
        for (Participante p : participantes) {
            if (p.getCodigo() == novoParticipante.getCodigo()) {
                throw new IllegalArgumentException("Erro: Já existe um participante com o código " + novoParticipante.getCodigo() + ".");
            }
        }
        participantes.add(novoParticipante);
        participantes.sort(Comparator.comparingInt(Participante::getCodigo));
        atualizarProximoCodigo();
    }

    public void editarParticipante(int codigo, String novoNome, double novoOrcamento) {
        if (novoNome == null || novoNome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do participante não pode estar vazio.");
        }
        if (novoOrcamento < 0) {
            throw new IllegalArgumentException("O orçamento deve ser maior ou igual a zero.");
        }

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
                throw new IllegalArgumentException("Não é possível excluir '" + p.getNome() + "' pois há despesas vinculadas.");
            }
        }
        participantes.remove(p);
    }

    public void adicionarDespesa(Despesa novaDespesa) {
        if (novaDespesa == null) {
            throw new IllegalArgumentException("Despesa inválida.");
        }
        despesas.add(novaDespesa);
    }

    public void editarDespesa(Despesa antiga, Despesa nova) {
        if (nova == null) {
            throw new IllegalArgumentException("Despesa inválida.");
        }
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
        long totalCentavos = 0L;
        for (Despesa despesa : despesas) {
            Map<Participante, Double> rateio = despesa.calcularRateio();
            for (Double valorCobrado : rateio.values()) {
                totalCentavos += Math.round(valorCobrado * 100.0);
            }
        }
        return totalCentavos / 100.0;
    }

    public double gerarResumoIndividual(int codigoParticipante) {
        Participante participanteAlvo = null;
        for (Participante p : participantes) {
            if (p.getCodigo() == codigoParticipante) {
                participanteAlvo = p;
                break;
            }
        }
        if (participanteAlvo == null) return 0.0;

        long totalCentavos = 0L;
        for (Despesa despesa : despesas) {
            Map<Participante, Double> rateio = despesa.calcularRateio();
            Double valor = rateio.get(participanteAlvo);
            if (valor != null) {
                totalCentavos += Math.round(valor * 100.0);
            }
        }
        return totalCentavos / 100.0;
    }

    public List<Participante> getParticipantes() {
        return Collections.unmodifiableList(participantes);
    }

    public List<Despesa> getDespesas() {
        return Collections.unmodifiableList(despesas);
    }
}