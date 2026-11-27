package controlador;

import modelo.Despesa;
import modelo.DespesaFixa;
import modelo.DespesaPorPessoa;
import modelo.Participante;
import modelo.Planejamento;
import modelo.RelatorioSimulacao;
import modelo.SimuladorAlternativas;
import persistencia.GerenciadorArquivo;
import persistencia.PersistenciaDados;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ControladorPlanejamento {
    private Planejamento planejamento;
    private final PersistenciaDados persistencia;
    private String avisoInicializacao;

    public ControladorPlanejamento() {
        this(new GerenciadorArquivo());
    }

    public ControladorPlanejamento(PersistenciaDados persistencia) {
        this.persistencia = persistencia;
        try {
            this.planejamento = persistencia.carregar();
            this.planejamento.atualizarProximoCodigo();
        } catch (FileNotFoundException e) {
            this.planejamento = new Planejamento();
        } catch (Exception e) {
            this.planejamento = new Planejamento();
            this.avisoInicializacao = "Falha ao ler o arquivo salvo (" + e.getMessage() + "). Um planejamento vazio foi iniciado.";
        }
    }

    public String consumirAvisoInicializacao() {
        String msg = avisoInicializacao;
        avisoInicializacao = null;
        return msg;
    }

    public void salvarPlanejamento() throws Exception {
        persistencia.salvar(planejamento);
    }

    public void carregarPlanejamento() throws Exception {
        Planejamento planejamentoEmUso = this.planejamento;
        try {
            Planejamento carregado = persistencia.carregar();
            carregado.atualizarProximoCodigo();
            this.planejamento = carregado;
        } catch (Exception e) {
            this.planejamento = planejamentoEmUso;
            throw new IllegalStateException("Não foi possível carregar o arquivo: " + e.getMessage()
                    + " O planejamento atual foi preservado.");
        }
    }

    public void iniciarPlanejamentoVazio() {
        this.planejamento = new Planejamento();
    }

    public void adicionarParticipante(String nome, double orcamento) {
        planejamento.cadastrarParticipante(nome, orcamento);
        salvarSilenciosamente();
    }

    public void editarParticipante(int codigo, String nome, double orcamento) {
        planejamento.editarParticipante(codigo, nome, orcamento);
        salvarSilenciosamente();
    }

    public void removerParticipante(Participante p) {
        planejamento.removerParticipante(p);
        salvarSilenciosamente();
    }

    public void adicionarDespesa(String descricao, double valor, Set<Participante> envolvidos,
                                 boolean essencial, int prioridade, String tipo) {
        Despesa novaDespesa = criarInstanciaDespesa(descricao, valor, envolvidos, essencial, prioridade, tipo);
        planejamento.adicionarDespesa(novaDespesa);
        salvarSilenciosamente();
    }

    public void editarDespesa(Despesa antiga, String descricao, double valor, Set<Participante> envolvidos,
                              boolean essencial, int prioridade, String tipo) {
        Despesa novaDespesa = criarInstanciaDespesa(descricao, valor, envolvidos, essencial, prioridade, tipo);
        planejamento.editarDespesa(antiga, novaDespesa);
        salvarSilenciosamente();
    }

    public void removerDespesa(Despesa d) {
        planejamento.removerDespesa(d);
        salvarSilenciosamente();
    }

    private void salvarSilenciosamente() {
        try {
            persistencia.salvar(planejamento);
        } catch (Exception e) {
            throw new IllegalStateException("Operação concluída, mas houve falha ao salvar no arquivo: " + e.getMessage());
        }
    }

    private Despesa criarInstanciaDespesa(String descricao, double valor, Set<Participante> envolvidos,
                                          boolean essencial, int prioridade, String tipo) {
        if ("Fixa".equalsIgnoreCase(tipo)) {
            return new DespesaFixa(descricao, valor, envolvidos, essencial, prioridade);
        } else {
            return new DespesaPorPessoa(descricao, valor, envolvidos, essencial, prioridade);
        }
    }

    public String gerarRelatorioTexto() {
        if (planejamento.getParticipantes().isEmpty()) {
            return "Nenhum participante cadastrado no planejamento.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== DESPESAS CADASTRADAS E RATEIO ===\n\n");

        if (planejamento.getDespesas().isEmpty()) {
            sb.append("Nenhuma despesa registrada.\n\n");
        } else {
            for (Despesa d : planejamento.getDespesas()) {
                String tag = d.isEssencial() ? "Essencial" : "Opcional (Prior. " + d.getPrioridade() + ")";
                sb.append(String.format("• %s [%s | %s | Informado: R$ %.2f]\n",
                        d.getDescricao(), d.getTipoDescricao(), tag, d.getValorInformado()));

                Map<Participante, Double> rateio = d.calcularRateio();
                for (Participante p : planejamento.getParticipantes()) {
                    double valor = rateio.getOrDefault(p, 0.0);
                    sb.append(String.format("   - %d (%s): R$ %.2f\n", p.getCodigo(), p.getNome(), valor));
                }
                sb.append("\n");
            }
        }

        sb.append("=== RESUMO POR PARTICIPANTE ===\n\n");
        for (Participante p : planejamento.getParticipantes()) {
            double totalGasto = planejamento.gerarResumoIndividual(p.getCodigo());
            double limite = p.getOrcamento();

            sb.append(String.format("Participante %d - %s | Total: R$ %.2f | Orçamento: R$ %.2f\n",
                    p.getCodigo(), p.getNome(), totalGasto, limite));

            if (totalGasto > limite) {
                sb.append(String.format(" -> SITUAÇÃO: EXCEDEU o orçamento em R$ %.2f (marcado para revisão)\n\n",
                        (totalGasto - limite)));
            } else {
                sb.append(" -> SITUAÇÃO: Dentro do orçamento.\n\n");
            }
        }

        sb.append(String.format("CUSTO TOTAL DO PLANEJAMENTO: R$ %.2f\n", planejamento.calcularTotalGeral()));
        return sb.toString();
    }

    public String rodarSimulador() {
        SimuladorAlternativas simulador = new SimuladorAlternativas();
        RelatorioSimulacao relatorio = simulador.buscarMelhorCenario(planejamento);

        StringBuilder sb = new StringBuilder();
        sb.append("=== RESULTADO DO SIMULADOR DE ALTERNATIVAS ===\n\n");
        sb.append(relatorio.getMensagem()).append("\n\n");

        if (relatorio.isViavel()) {
            sb.append("Despesas MANTIDAS:\n");
            for (Despesa d : relatorio.getDespesasMantidas()) {
                String tipo = d.isEssencial() ? "[ESSENCIAL]" : "[OPCIONAL - Prior. " + d.getPrioridade() + "]";
                sb.append(String.format(" -> %s %s (%s: R$ %.2f)\n",
                        tipo, d.getDescricao(), d.getTipoDescricao(), d.getValorInformado()));
            }

            sb.append("\nDespesas RETIRADAS:\n");
            if (relatorio.getDespesasRetiradas().isEmpty()) {
                sb.append(" -> Nenhuma despesa precisou ser retirada.\n");
            } else {
                for (Despesa d : relatorio.getDespesasRetiradas()) {
                    sb.append(String.format(" -> [OPCIONAL - Prior. %d] %s (%s: R$ %.2f)\n",
                            d.getPrioridade(), d.getDescricao(), d.getTipoDescricao(), d.getValorInformado()));
                }
            }

            Planejamento simulado = relatorio.getPlanejamentoSimulado();
            sb.append("\nNovos Totais Individuais (Planejamento original preservado):\n");
            for (Participante p : simulado.getParticipantes()) {
                double novoTotal = simulado.gerarResumoIndividual(p.getCodigo());
                sb.append(String.format(" -> %d - %s: R$ %.2f (Orçamento: R$ %.2f)\n",
                        p.getCodigo(), p.getNome(), novoTotal, p.getOrcamento()));
            }
            sb.append(String.format("\nNovo Custo Total: R$ %.2f (Original: R$ %.2f)\n",
                    simulado.calcularTotalGeral(), planejamento.calcularTotalGeral()));
        }

        return sb.toString();
    }

    public List<Participante> getParticipantes() {
        return planejamento.getParticipantes();
    }

    public List<Despesa> getDespesas() {
        return planejamento.getDespesas();
    }
}