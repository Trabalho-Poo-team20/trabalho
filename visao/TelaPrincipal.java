package visao;

import controlador.ControladorPlanejamento;
import modelo.Despesa;
import modelo.Participante;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public class TelaPrincipal extends JFrame {

    static {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
    }

    // =====================================================================
    //  TEMA
    // =====================================================================
    private static final class Tema {
        static final Color TINTA = new Color(0x0E2A2F);
        static final Color TINTA_CLARA = new Color(0x17393F);
        static final Color TINTA_TEXTO = new Color(0x9DB7B4);
        static final Color MENTA = new Color(0x4FD1A5);

        static final Color FUNDO = new Color(0xF2F5F4);
        static final Color PAINEL = Color.WHITE;
        static final Color CAMPO_FUNDO = new Color(0xFAFCFB);
        static final Color BORDA = new Color(0xDDE5E3);
        static final Color TEXTO = new Color(0x12262A);
        static final Color TEXTO_SUAVE = new Color(0x5F7378);

        static final Color PRIMARIA = new Color(0x0B8A6B);
        static final Color PRIMARIA_HOVER = new Color(0x08735A);
        static final Color PRIMARIA_SUAVE = new Color(0xE3F4EE);
        static final Color VERMELHO = new Color(0xC8372D);
        static final Color VERMELHO_SUAVE = new Color(0xFCEBEA);
        static final Color AMBAR = new Color(0xF5B73B);

        static final Color CONSOLE_FUNDO = new Color(0x0B1F23);
        static final Color CONSOLE_BORDA = new Color(0x1B3A40);
        static final Color CONSOLE_TEXTO = new Color(0xD7E6E3);
        static final Color CONSOLE_SUAVE = new Color(0x6F908C);

        static final String FAMILIA = familia("Segoe UI", "Inter", "SF Pro Text", "Helvetica Neue",
                "Noto Sans", "Ubuntu", Font.SANS_SERIF);
        static final String MONO = familia("Cascadia Mono", "JetBrains Mono", "Consolas", "Menlo",
                "DejaVu Sans Mono", Font.MONOSPACED);

        static final Font TITULO_PAGINA = new Font(FAMILIA, Font.BOLD, 26);
        static final Font SECAO = new Font(FAMILIA, Font.BOLD, 17);
        static final Font DESTAQUE = new Font(FAMILIA, Font.BOLD, 21);
        static final Font CORPO = new Font(FAMILIA, Font.PLAIN, 14);
        static final Font CORPO_NEGRITO = new Font(FAMILIA, Font.BOLD, 14);
        static final Font LABEL = new Font(FAMILIA, Font.BOLD, 13);
        static final Font LEGENDA = new Font(FAMILIA, Font.PLAIN, 12);
        static final Font CONSOLE = new Font(MONO, Font.PLAIN, 13);

        private static String familia(String... preferidas) {
            Set<String> disponiveis = new HashSet<>(Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
            for (String f : preferidas) if (disponiveis.contains(f)) return f;
            return preferidas[preferidas.length - 1];
        }
    }

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(PT_BR);

    private static final String[] NOMES_PAGINAS = {"participantes", "despesas", "relatorios"};
    private static final String[] ROTULOS_NAV = {"Participantes", "Despesas", "Relatórios"};
    private static final String[] TITULOS = {"Participantes", "Registrar despesa", "Relatórios e simulador"};
    private static final String[] SUBTITULOS = {
            "Cadastre quem divide a conta e quanto cada pessoa pode gastar.",
            "Informe o gasto, escolha quem divide com você e gerencie os registros.",
            "Veja como a conta fecha, teste alternativas e gerencie o arquivo salvo."
    };

    // =====================================================================
    //  ESTADO
    // =====================================================================
    private final ControladorPlanejamento controlador;
    private final DefaultListModel<Participante> modeloParticipantes = new DefaultListModel<>();
    private final DefaultListModel<Despesa> modeloDespesas = new DefaultListModel<>();

    private ListaParticipantes listaCadastrados, listaEnvolvidos;
    private ListaDespesas listaDespesasCadastradas;
    private Campo txtNome, txtOrcamento, txtDescricao, txtValor, txtPrioridade;
    private JLabel lblValorDespesa;
    private Interruptor swEssencial;
    private Segmentado segTipo;
    private JTextArea txtAreaRelatorio;
    private JLabel lblSaida, lblContagemEnvolvidos, lblResumoValor, lblResumoQtd;
    private Selo seloTotalPart, seloTotalDesp;

    private final ItemNav[] itensNav = new ItemNav[3];
    private CardLayout cardLayout;
    private JPanel painelPaginas;
    private JLabel lblTituloPagina, lblSubtituloPagina;
    private final Avisos avisos = new Avisos();

    // =====================================================================
    //  CONSTRUÇÃO
    // =====================================================================
    public TelaPrincipal(ControladorPlanejamento controlador) {
        this.controlador = controlador;

        setTitle("Cabe no Bolso - Planejador Inteligente");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 740);
        setMinimumSize(new Dimension(1060, 680));
        setLocationRelativeTo(null);

        setGlassPane(avisos);
        avisos.setVisible(false);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Tema.FUNDO);
        raiz.add(criarSidebar(), BorderLayout.WEST);
        raiz.add(criarAreaConteudo(), BorderLayout.CENTER);
        setContentPane(raiz);

        irPara(0);

        String avisoInicial = controlador.consumirAvisoInicializacao();
        if (avisoInicial != null) {
            SwingUtilities.invokeLater(() -> avisos.mostrar(avisoInicial, Avisos.Tipo.AVISO));
        }
    }

    private JComponent criarAreaConteudo() {
        JPanel area = new JPanel(new BorderLayout(0, 22));
        area.setBackground(Tema.FUNDO);
        area.setBorder(new EmptyBorder(30, 36, 30, 36));

        lblTituloPagina = new JLabel();
        lblTituloPagina.setFont(Tema.TITULO_PAGINA);
        lblTituloPagina.setForeground(Tema.TEXTO);
        lblSubtituloPagina = new JLabel();
        lblSubtituloPagina.setFont(Tema.CORPO);
        lblSubtituloPagina.setForeground(Tema.TEXTO_SUAVE);

        JPanel cabecalho = new JPanel(new GridLayout(2, 1, 0, 2));
        cabecalho.setOpaque(false);
        cabecalho.add(lblTituloPagina);
        cabecalho.add(lblSubtituloPagina);

        cardLayout = new CardLayout();
        painelPaginas = new JPanel(cardLayout);
        painelPaginas.setOpaque(false);
        painelPaginas.add(criarPaginaParticipantes(), NOMES_PAGINAS[0]);
        painelPaginas.add(criarPaginaDespesas(), NOMES_PAGINAS[1]);
        painelPaginas.add(criarPaginaRelatorios(), NOMES_PAGINAS[2]);

        area.add(cabecalho, BorderLayout.NORTH);
        area.add(painelPaginas, BorderLayout.CENTER);
        return area;
    }

    private JComponent criarSidebar() {
        JPanel lateral = new JPanel(new BorderLayout());
        lateral.setBackground(Tema.TINTA);
        lateral.setPreferredSize(new Dimension(250, 0));
        lateral.setBorder(new EmptyBorder(30, 18, 22, 18));

        JPanel topo = coluna();

        JPanel marca = new JPanel(new BorderLayout(12, 0));
        marca.setOpaque(false);
        marca.setAlignmentX(Component.LEFT_ALIGNMENT);
        marca.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        marca.add(new Marca(), BorderLayout.WEST);
        JPanel nomes = new JPanel(new GridLayout(2, 1));
        nomes.setOpaque(false);
        JLabel n1 = new JLabel("Cabe no Bolso");
        n1.setFont(Tema.SECAO);
        n1.setForeground(Color.WHITE);
        JLabel n2 = new JLabel("Planejador inteligente");
        n2.setFont(Tema.LEGENDA);
        n2.setForeground(Tema.TINTA_TEXTO);
        nomes.add(n1);
        nomes.add(n2);
        marca.add(nomes, BorderLayout.CENTER);
        topo.add(marca);
        topo.add(espaco(34));

        for (int i = 0; i < itensNav.length; i++) {
            final int destino = i;
            itensNav[i] = new ItemNav(ROTULOS_NAV[i], i, () -> irPara(destino));
            topo.add(itensNav[i]);
            topo.add(espaco(6));
        }
        lateral.add(topo, BorderLayout.NORTH);

        Cartao resumo = new Cartao(null, 16, 16, Tema.TINTA_CLARA, new Color(0x244A50), false);
        resumo.setLayout(new BoxLayout(resumo, BoxLayout.Y_AXIS));
        JLabel l1 = new JLabel("Orçamento somado");
        l1.setFont(Tema.LEGENDA);
        l1.setForeground(Tema.TINTA_TEXTO);
        lblResumoValor = new JLabel(MOEDA.format(0));
        lblResumoValor.setFont(Tema.DESTAQUE);
        lblResumoValor.setForeground(Color.WHITE);
        lblResumoQtd = new JLabel("Nenhum participante");
        lblResumoQtd.setFont(Tema.LEGENDA);
        lblResumoQtd.setForeground(Tema.TINTA_TEXTO);
        for (JLabel l : new JLabel[]{l1, lblResumoValor, lblResumoQtd}) {
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            resumo.add(l);
        }
        resumo.add(espaco(0));
        lateral.add(resumo, BorderLayout.SOUTH);
        return lateral;
    }

    // ----------------------------------------------------------------- Participantes
    private JComponent criarPaginaParticipantes() {
        JPanel pagina = new JPanel(new BorderLayout(24, 0));
        pagina.setOpaque(false);

        Cartao form = new Cartao(new BorderLayout(), 18, 24);
        form.setPreferredSize(new Dimension(340, 0));
        JPanel col = coluna();
        col.add(titulo("Novo participante"));
        col.add(espaco(4));
        col.add(suave("Informe o nome e o orçamento."));
        col.add(espaco(22));

        txtNome = new Campo("Ex: Ana");
        col.add(bloco("Nome", txtNome));
        col.add(espaco(14));
        txtOrcamento = new Campo("Ex: 70,00");
        col.add(bloco("Orçamento disponível (R$)", txtOrcamento));
        col.add(espaco(22));

        Botao btnCadastrar = new Botao("Cadastrar participante", Botao.Estilo.PRIMARIO);
        btnCadastrar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCadastrar.addActionListener(e -> cadastrarParticipanteAction());
        txtOrcamento.addActionListener(e -> cadastrarParticipanteAction());
        col.add(btnCadastrar);
        col.add(Box.createVerticalGlue());
        form.add(col, BorderLayout.CENTER);

        Cartao cartaoLista = new Cartao(new BorderLayout(0, 14), 18, 24);
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setOpaque(false);
        cabecalho.add(titulo("Participantes cadastrados"), BorderLayout.WEST);
        seloTotalPart = new Selo("0");
        JPanel ladoSelo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        ladoSelo.setOpaque(false);
        ladoSelo.add(seloTotalPart);
        cabecalho.add(ladoSelo, BorderLayout.EAST);
        cartaoLista.add(cabecalho, BorderLayout.NORTH);

        listaCadastrados = new ListaParticipantes(modeloParticipantes, false, ListSelectionModel.SINGLE_SELECTION,
                "Nenhum participante ainda.\nCadastre o primeiro ao lado.");
        listaCadastrados.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && listaCadastrados.indiceNoPonto(e.getPoint()) >= 0) {
                    editarParticipanteAction();
                }
            }
        });
        cartaoLista.add(rolagem(listaCadastrados, new Color(0xC5D1CE)), BorderLayout.CENTER);

        JPanel acoes = new JPanel(new GridLayout(1, 2, 10, 0));
        acoes.setOpaque(false);
        Botao btnEditar = new Botao("Editar", Botao.Estilo.SECUNDARIO);
        btnEditar.addActionListener(e -> editarParticipanteAction());
        Botao btnExcluir = new Botao("Excluir", Botao.Estilo.PERIGO);
        btnExcluir.addActionListener(e -> excluirParticipanteAction());
        acoes.add(btnEditar);
        acoes.add(btnExcluir);
        cartaoLista.add(acoes, BorderLayout.SOUTH);

        pagina.add(form, BorderLayout.WEST);
        pagina.add(cartaoLista, BorderLayout.CENTER);
        return pagina;
    }

    // ----------------------------------------------------------------- Despesas
    private JComponent criarPaginaDespesas() {
        JPanel pagina = new JPanel(new BorderLayout(24, 0));
        pagina.setOpaque(false);

        Cartao form = new Cartao(new BorderLayout(), 18, 24);
        form.setPreferredSize(new Dimension(340, 0));
        JPanel col = coluna();
        col.add(titulo("Detalhes da despesa"));
        col.add(espaco(20));

        txtDescricao = new Campo("Ex: Transporte");
        col.add(bloco("Descrição", txtDescricao));
        col.add(espaco(16));

        segTipo = new Segmentado("Fixa", "Por Pessoa");
        col.add(bloco("Tipo de rateio", segTipo));
        col.add(espaco(16));

        txtValor = new Campo("Ex: 90,00");
        JPanel blocoValor = bloco("Valor total (R$)", txtValor);
        lblValorDespesa = (JLabel) blocoValor.getComponent(0);
        segTipo.aoMudar(() -> lblValorDespesa.setText(
                "Fixa".equals(segTipo.getSelecionado()) ? "Valor total (R$)" : "Valor por pessoa (R$)"
        ));
        col.add(blocoValor);
        col.add(espaco(16));

        txtPrioridade = new Campo("1");
        txtPrioridade.setText("1");
        swEssencial = new Interruptor();
        swEssencial.aoMudar(() -> {
            boolean essencial = swEssencial.isSelected();
            txtPrioridade.setEnabled(!essencial);
            txtPrioridade.setText(essencial ? "0" : "1");
        });
        JPanel envolveSwitch = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        envolveSwitch.setOpaque(false);
        envolveSwitch.add(swEssencial);
        col.add(linha(bloco("Prioridade", txtPrioridade), bloco("Despesa essencial", envolveSwitch)));
        col.add(espaco(4));
        col.add(suave("Opcionais exigem prioridade > 0."));

        col.add(Box.createVerticalGlue());
        col.add(espaco(16));
        Botao btnCadastrar = new Botao("Cadastrar despesa", Botao.Estilo.PRIMARIO);
        btnCadastrar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCadastrar.addActionListener(e -> cadastrarDespesaAction());
        col.add(btnCadastrar);
        form.add(col, BorderLayout.CENTER);

        Cartao cartaoEnvolvidos = new Cartao(new BorderLayout(0, 14), 18, 24);
        JPanel cabEnvolvidos = coluna();
        cabEnvolvidos.add(titulo("Quem divide"));
        cabEnvolvidos.add(espaco(4));
        cabEnvolvidos.add(suave("Selecione os envolvidos."));
        cartaoEnvolvidos.add(cabEnvolvidos, BorderLayout.NORTH);

        listaEnvolvidos = new ListaParticipantes(modeloParticipantes, true, ListSelectionModel.MULTIPLE_INTERVAL_SELECTION,
                "Nenhum participante ainda.\nCadastre alguém na aba Participantes.");
        listaEnvolvidos.ativarMarcacaoPorClique();
        listaEnvolvidos.addListSelectionListener(e -> atualizarContagemEnvolvidos());
        cartaoEnvolvidos.add(rolagem(listaEnvolvidos, new Color(0xC5D1CE)), BorderLayout.CENTER);

        JPanel rodapeEnvolvidos = new JPanel(new BorderLayout());
        rodapeEnvolvidos.setOpaque(false);
        lblContagemEnvolvidos = new JLabel(" ");
        lblContagemEnvolvidos.setFont(Tema.LEGENDA);
        lblContagemEnvolvidos.setForeground(Tema.TEXTO_SUAVE);
        rodapeEnvolvidos.add(lblContagemEnvolvidos, BorderLayout.WEST);
        JPanel links = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        links.setOpaque(false);
        Botao todos = new Botao("Todos", Botao.Estilo.LINK);
        todos.addActionListener(e -> {
            if (modeloParticipantes.size() > 0) listaEnvolvidos.setSelectionInterval(0, modeloParticipantes.size() - 1);
        });
        Botao limpar = new Botao("Limpar", Botao.Estilo.LINK);
        limpar.addActionListener(e -> listaEnvolvidos.clearSelection());
        links.add(todos);
        links.add(Box.createHorizontalStrut(2));
        links.add(limpar);
        rodapeEnvolvidos.add(links, BorderLayout.EAST);
        cartaoEnvolvidos.add(rodapeEnvolvidos, BorderLayout.SOUTH);

        Cartao cartaoDespesas = new Cartao(new BorderLayout(0, 14), 18, 24);
        JPanel cabDespesas = new JPanel(new BorderLayout());
        cabDespesas.setOpaque(false);
        cabDespesas.add(titulo("Cadastradas"), BorderLayout.WEST);
        seloTotalDesp = new Selo("0");
        JPanel ladoSeloDesp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        ladoSeloDesp.setOpaque(false);
        ladoSeloDesp.add(seloTotalDesp);
        cabDespesas.add(ladoSeloDesp, BorderLayout.EAST);
        cartaoDespesas.add(cabDespesas, BorderLayout.NORTH);

        listaDespesasCadastradas = new ListaDespesas(modeloDespesas, "Nenhuma despesa ainda.\nCadastre ao lado.");
        listaDespesasCadastradas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && listaDespesasCadastradas.indiceNoPonto(e.getPoint()) >= 0) {
                    editarDespesaAction();
                }
            }
        });
        cartaoDespesas.add(rolagem(listaDespesasCadastradas, new Color(0xC5D1CE)), BorderLayout.CENTER);

        JPanel acoesDesp = new JPanel(new GridLayout(1, 2, 10, 0));
        acoesDesp.setOpaque(false);
        Botao btnEditarDesp = new Botao("Editar", Botao.Estilo.SECUNDARIO);
        btnEditarDesp.addActionListener(e -> editarDespesaAction());
        Botao btnExcluirDesp = new Botao("Excluir", Botao.Estilo.PERIGO);
        btnExcluirDesp.addActionListener(e -> excluirDespesaAction());
        acoesDesp.add(btnEditarDesp);
        acoesDesp.add(btnExcluirDesp);
        cartaoDespesas.add(acoesDesp, BorderLayout.SOUTH);

        JPanel divisorListas = new JPanel(new GridLayout(1, 2, 24, 0));
        divisorListas.setOpaque(false);
        divisorListas.add(cartaoEnvolvidos);
        divisorListas.add(cartaoDespesas);

        pagina.add(form, BorderLayout.WEST);
        pagina.add(divisorListas, BorderLayout.CENTER);
        return pagina;
    }

    // ----------------------------------------------------------------- Relatórios e Persistência
    private JComponent criarPaginaRelatorios() {
        JPanel pagina = new JPanel(new BorderLayout(0, 16));
        pagina.setOpaque(false);

        JPanel barra = new JPanel(new BorderLayout(10, 0));
        barra.setOpaque(false);

        JPanel principais = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        principais.setOpaque(false);
        Botao btnResumo = new Botao("Gerar relatório", Botao.Estilo.PRIMARIO);
        btnResumo.addActionListener(e -> executarSaida("Relatório atual", controlador::gerarRelatorioTexto));
        Botao btnSimulador = new Botao("Simular alternativas", Botao.Estilo.ESCURO);
        btnSimulador.addActionListener(e -> executarSaida("Simulação de alternativas", controlador::rodarSimulador));
        principais.add(btnResumo);
        principais.add(Box.createHorizontalStrut(8));
        principais.add(btnSimulador);
        barra.add(principais, BorderLayout.WEST);

        JPanel lado = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        lado.setOpaque(false);
        Botao btnNovo = new Botao("Novo vazio", Botao.Estilo.SECUNDARIO);
        btnNovo.addActionListener(e -> novoPlanejamentoAction());
        Botao btnSalvar = new Botao("Salvar", Botao.Estilo.SECUNDARIO);
        btnSalvar.addActionListener(e -> salvarArquivoAction());
        Botao btnCarregar = new Botao("Carregar", Botao.Estilo.SECUNDARIO);
        btnCarregar.addActionListener(e -> carregarArquivoAction());
        Botao btnCopiar = new Botao("Copiar", Botao.Estilo.SECUNDARIO);
        btnCopiar.addActionListener(e -> copiarResultado());

        lado.add(btnNovo);
        lado.add(Box.createHorizontalStrut(8));
        lado.add(btnSalvar);
        lado.add(Box.createHorizontalStrut(8));
        lado.add(btnCarregar);
        lado.add(Box.createHorizontalStrut(8));
        lado.add(btnCopiar);
        barra.add(lado, BorderLayout.EAST);
        pagina.add(barra, BorderLayout.NORTH);

        Cartao console = new Cartao(new BorderLayout(0, 10), 18, 20, Tema.CONSOLE_FUNDO, Tema.CONSOLE_BORDA, false);
        lblSaida = new JLabel("Resultado");
        lblSaida.setFont(Tema.LABEL);
        lblSaida.setForeground(Tema.CONSOLE_SUAVE);
        console.add(lblSaida, BorderLayout.NORTH);

        txtAreaRelatorio = new JTextArea() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setFont(Tema.CORPO);
                    g2.setColor(Tema.CONSOLE_SUAVE);
                    FontMetrics fm = g2.getFontMetrics();
                    String[] linhas = {"Nada por aqui ainda.", "Gere o relatório ou rode o simulador para ver o resultado."};
                    int y = getInsets().top + fm.getAscent();
                    for (String s : linhas) {
                        g2.drawString(s, getInsets().left, y);
                        y += fm.getHeight() + 2;
                    }
                    g2.dispose();
                }
            }
        };
        txtAreaRelatorio.setEditable(false);
        txtAreaRelatorio.setOpaque(false);
        txtAreaRelatorio.setFont(Tema.CONSOLE);
        txtAreaRelatorio.setForeground(Tema.CONSOLE_TEXTO);
        txtAreaRelatorio.setCaretColor(Tema.CONSOLE_TEXTO);
        txtAreaRelatorio.setSelectionColor(new Color(0x24585A));
        txtAreaRelatorio.setSelectedTextColor(Color.WHITE);
        txtAreaRelatorio.setBorder(new EmptyBorder(4, 2, 4, 2));
        console.add(rolagem(txtAreaRelatorio, new Color(0x35545A)), BorderLayout.CENTER);

        pagina.add(console, BorderLayout.CENTER);
        return pagina;
    }

    // =====================================================================
    //  NAVEGAÇÃO E ATUALIZAÇÃO
    // =====================================================================
    private void irPara(int indice) {
        atualizarListas();
        if (indice == 2 && !controlador.getParticipantes().isEmpty()) {
            executarSaida("Relatório atual", controlador::gerarRelatorioTexto);
        }
        cardLayout.show(painelPaginas, NOMES_PAGINAS[indice]);
        lblTituloPagina.setText(TITULOS[indice]);
        lblSubtituloPagina.setText(SUBTITULOS[indice]);
        for (int i = 0; i < itensNav.length; i++) itensNav[i].setAtivo(i == indice);
    }

    private void atualizarListas() {
        Set<Object> selecionadosEnvolvidos = new HashSet<>();
        if (listaEnvolvidos != null) {
            for (Participante p : listaEnvolvidos.getSelectedValuesList()) selecionadosEnvolvidos.add(p.getCodigo());
        }
        Object selecionadoCadastro = null;
        if (listaCadastrados != null && listaCadastrados.getSelectedValue() != null) {
            selecionadoCadastro = listaCadastrados.getSelectedValue().getCodigo();
        }

        modeloParticipantes.clear();
        double totalOrcamento = 0;
        for (Participante p : controlador.getParticipantes()) {
            modeloParticipantes.addElement(p);
            totalOrcamento += p.getOrcamento();
        }

        List<Integer> indicesPart = new ArrayList<>();
        int indiceCadastroPart = -1;
        for (int i = 0; i < modeloParticipantes.size(); i++) {
            Object codigo = modeloParticipantes.get(i).getCodigo();
            if (selecionadosEnvolvidos.contains(codigo)) indicesPart.add(i);
            if (selecionadoCadastro != null && Objects.equals(selecionadoCadastro, codigo)) indiceCadastroPart = i;
        }
        if (listaEnvolvidos != null) {
            listaEnvolvidos.setSelectedIndices(indicesPart.stream().mapToInt(Integer::intValue).toArray());
        }
        if (listaCadastrados != null && indiceCadastroPart >= 0) listaCadastrados.setSelectedIndex(indiceCadastroPart);

        int qtdPart = modeloParticipantes.size();
        lblResumoValor.setText(MOEDA.format(totalOrcamento));
        lblResumoQtd.setText(qtdPart == 0 ? "Nenhum participante" : qtdPart == 1 ? "1 participante" : qtdPart + " participantes");
        if (seloTotalPart != null) seloTotalPart.setText(String.valueOf(qtdPart));
        atualizarContagemEnvolvidos();

        Object despesaSelecionada = null;
        if (listaDespesasCadastradas != null && listaDespesasCadastradas.getSelectedValue() != null) {
            despesaSelecionada = listaDespesasCadastradas.getSelectedValue();
        }

        modeloDespesas.clear();
        int indiceCadastroDesp = -1;
        List<Despesa> todasDespesas = controlador.getDespesas();
        for (int i = 0; i < todasDespesas.size(); i++) {
            Despesa d = todasDespesas.get(i);
            modeloDespesas.addElement(d);
            if (despesaSelecionada != null && Objects.equals(despesaSelecionada, d)) indiceCadastroDesp = i;
        }
        if (listaDespesasCadastradas != null && indiceCadastroDesp >= 0) listaDespesasCadastradas.setSelectedIndex(indiceCadastroDesp);
        if (seloTotalDesp != null) seloTotalDesp.setText(String.valueOf(modeloDespesas.size()));

        repaint();
    }

    private void atualizarContagemEnvolvidos() {
        if (lblContagemEnvolvidos == null || listaEnvolvidos == null) return;
        int total = modeloParticipantes.size();
        int sel = listaEnvolvidos.getSelectedIndices().length;
        lblContagemEnvolvidos.setText(total == 0 ? " " : sel + " de " + total + (sel == 1 ? " selecionado" : " selecionados"));
    }

    // =====================================================================
    //  AÇÕES DE PARTICIPANTES
    // =====================================================================
    private void cadastrarParticipanteAction() {
        try {
            String nome = txtNome.getText().trim();
            double orcamento = parseValor(txtOrcamento.getText());

            controlador.adicionarParticipante(nome, orcamento);
            txtNome.setText("");
            txtOrcamento.setText("");
            atualizarListas();
            avisos.mostrar("Participante cadastrado", Avisos.Tipo.SUCESSO);
            txtNome.requestFocusInWindow();
        } catch (NumberFormatException ex) {
            avisos.mostrar("Digite um número válido para o orçamento.", Avisos.Tipo.ERRO);
        } catch (RuntimeException ex) {
            avisos.mostrar(mensagem(ex), Avisos.Tipo.AVISO);
        }
    }

    private void editarParticipanteAction() {
        Participante p = listaCadastrados.getSelectedValue();
        if (p == null) {
            avisos.mostrar("Selecione um participante na lista para editar.", Avisos.Tipo.AVISO);
            return;
        }

        Campo cNome = new Campo("Nome do participante");
        cNome.setText(p.getNome());
        Campo cOrcamento = new Campo("Ex: 150,00");
        cOrcamento.setText(String.format(PT_BR, "%.2f", p.getOrcamento()));

        JPanel corpo = coluna();
        corpo.add(bloco("Nome", cNome));
        corpo.add(espaco(12));
        corpo.add(bloco("Orçamento disponível (R$)", cOrcamento));

        SwingUtilities.invokeLater(() -> {
            cNome.requestFocusInWindow();
            cNome.selectAll();
        });

        boolean salvou = abrirDialogo("Editar participante", "Atualize o nome ou o orçamento.", corpo,
                "Salvar alterações", Botao.Estilo.PRIMARIO, true, () -> {
                    try {
                        double orcamento = parseValor(cOrcamento.getText());
                        controlador.editarParticipante(p.getCodigo(), cNome.getText().trim(), orcamento);
                        return null;
                    } catch (NumberFormatException ex) {
                        return "Digite um número válido para o orçamento.";
                    } catch (RuntimeException ex) {
                        return mensagem(ex);
                    }
                });

        if (salvou) {
            atualizarListas();
            avisos.mostrar("Participante atualizado", Avisos.Tipo.SUCESSO);
        }
    }

    private void excluirParticipanteAction() {
        Participante p = listaCadastrados.getSelectedValue();
        if (p == null) {
            avisos.mostrar("Selecione um participante na lista para excluir.", Avisos.Tipo.AVISO);
            return;
        }

        boolean confirmou = abrirDialogo("Excluir participante",
                "Tem certeza que deseja excluir \"" + p.getNome() + "\"? Essa ação não pode ser desfeita.",
                null, "Excluir", Botao.Estilo.PERIGO, true, null);
        if (!confirmou) return;

        try {
            controlador.removerParticipante(p);
            atualizarListas();
            avisos.mostrar("Participante excluído", Avisos.Tipo.SUCESSO);
        } catch (RuntimeException ex) {
            abrirDialogo("Exclusão bloqueada", mensagem(ex), null, "Entendi", Botao.Estilo.PRIMARIO, false, null);
        }
    }

    // =====================================================================
    //  AÇÕES DE DESPESAS
    // =====================================================================
    private void cadastrarDespesaAction() {
        try {
            String descricao = txtDescricao.getText().trim();
            boolean essencial = swEssencial.isSelected();
            String tipo = segTipo.getSelecionado();

            double valor;
            try {
                valor = parseValor(txtValor.getText());
            } catch (NumberFormatException ex) {
                avisos.mostrar("Digite um valor numérico válido para a despesa.", Avisos.Tipo.ERRO);
                txtValor.requestFocusInWindow();
                return;
            }

            int prioridade = 0;
            if (!essencial) {
                try {
                    prioridade = Integer.parseInt(txtPrioridade.getText().trim());
                } catch (NumberFormatException ex) {
                    avisos.mostrar("A prioridade deve ser um número inteiro positivo.", Avisos.Tipo.ERRO);
                    txtPrioridade.requestFocusInWindow();
                    return;
                }
            }

            List<Participante> selecionados = listaEnvolvidos.getSelectedValuesList();
            if (selecionados.isEmpty()) {
                throw new IllegalArgumentException("Selecione ao menos um participante.");
            }
            Set<Participante> envolvidos = new HashSet<>(selecionados);

            controlador.adicionarDespesa(descricao, valor, envolvidos, essencial, prioridade, tipo);

            txtDescricao.setText("");
            txtValor.setText("");
            swEssencial.setSelected(false);
            txtPrioridade.setText("1");
            listaEnvolvidos.clearSelection();
            atualizarListas();
            avisos.mostrar("Despesa cadastrada", Avisos.Tipo.SUCESSO);
            txtDescricao.requestFocusInWindow();
        } catch (RuntimeException ex) {
            avisos.mostrar(mensagem(ex), Avisos.Tipo.AVISO);
        }
    }

    private void editarDespesaAction() {
        Despesa d = listaDespesasCadastradas.getSelectedValue();
        if (d == null) {
            avisos.mostrar("Selecione uma despesa na lista para editar.", Avisos.Tipo.AVISO);
            return;
        }

        Campo cDesc = new Campo("Ex: Transporte");
        cDesc.setText(d.getDescricao());

        Campo cValor = new Campo("Ex: 50,00");
        cValor.setText(String.format(PT_BR, "%.2f", d.getValorInformado()));

        Segmentado cTipo = new Segmentado("Fixa", "Por Pessoa");
        if (d instanceof modelo.DespesaFixa) {
            cTipo.definir(0);
        } else {
            cTipo.definir(1);
        }

        Interruptor cEssencial = new Interruptor();
        cEssencial.setSelected(d.isEssencial());

        Campo cPrioridade = new Campo("1");
        cPrioridade.setText(d.isEssencial() ? "0" : String.valueOf(d.getPrioridade()));
        cPrioridade.setEnabled(!d.isEssencial());

        cEssencial.aoMudar(() -> {
            boolean ess = cEssencial.isSelected();
            cPrioridade.setEnabled(!ess);
            cPrioridade.setText(ess ? "0" : "1");
        });

        ListaParticipantes listPartDialog = new ListaParticipantes(modeloParticipantes, true,
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, "Nenhum participante.");
        listPartDialog.ativarMarcacaoPorClique();

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < modeloParticipantes.size(); i++) {
            if (d.getParticipantesEnvolvidos().contains(modeloParticipantes.get(i))) {
                indices.add(i);
            }
        }
        listPartDialog.setSelectedIndices(indices.stream().mapToInt(Integer::intValue).toArray());

        JPanel corpo = coluna();
        corpo.add(bloco("Descrição", cDesc));
        corpo.add(espaco(12));
        corpo.add(linha(bloco("Valor (R$)", cValor), bloco("Tipo", cTipo)));
        corpo.add(espaco(12));
        corpo.add(linha(bloco("Prioridade", cPrioridade), bloco("Essencial", cEssencial)));
        corpo.add(espaco(12));
        JScrollPane scrollPart = rolagem(listPartDialog, new Color(0xC5D1CE));
        scrollPart.setPreferredSize(new Dimension(300, 160));
        corpo.add(bloco("Envolvidos", scrollPart));

        SwingUtilities.invokeLater(() -> {
            cDesc.requestFocusInWindow();
            cDesc.selectAll();
        });

        boolean salvou = abrirDialogo("Editar despesa", "Atualize os dados e os envolvidos.", corpo,
                "Salvar alterações", Botao.Estilo.PRIMARIO, true, () -> {
                    try {
                        String desc = cDesc.getText().trim();
                        double valor = parseValor(cValor.getText());
                        boolean ess = cEssencial.isSelected();
                        int prio = ess ? 0 : Integer.parseInt(cPrioridade.getText().trim());
                        String tipo = cTipo.getSelecionado();
                        List<Participante> sel = listPartDialog.getSelectedValuesList();

                        if (sel.isEmpty()) return "Selecione ao menos um participante.";

                        controlador.editarDespesa(d, desc, valor, new HashSet<>(sel), ess, prio, tipo);
                        return null;
                    } catch (NumberFormatException ex) {
                        return "Digite números válidos em Valor e Prioridade.";
                    } catch (RuntimeException ex) {
                        return mensagem(ex);
                    }
                });

        if (salvou) {
            atualizarListas();
            avisos.mostrar("Despesa atualizada", Avisos.Tipo.SUCESSO);
        }
    }

    private void excluirDespesaAction() {
        Despesa d = listaDespesasCadastradas.getSelectedValue();
        if (d == null) {
            avisos.mostrar("Selecione uma despesa na lista para excluir.", Avisos.Tipo.AVISO);
            return;
        }

        boolean confirmou = abrirDialogo("Excluir despesa",
                "Tem certeza que deseja excluir a despesa \"" + d.getDescricao() + "\"? Essa ação não pode ser desfeita.",
                null, "Excluir", Botao.Estilo.PERIGO, true, null);

        if (!confirmou) return;

        try {
            controlador.removerDespesa(d);
            atualizarListas();
            avisos.mostrar("Despesa excluída", Avisos.Tipo.SUCESSO);
        } catch (RuntimeException ex) {
            abrirDialogo("Falha", mensagem(ex), null, "Entendi", Botao.Estilo.PRIMARIO, false, null);
        }
    }

    // =====================================================================
    //  AÇÕES DE RELATÓRIOS E PERSISTÊNCIA
    // =====================================================================
    private void executarSaida(String titulo, Supplier<String> geracao) {
        try {
            lblSaida.setText(titulo);
            txtAreaRelatorio.setText(geracao.get());
            txtAreaRelatorio.setCaretPosition(0);
        } catch (RuntimeException ex) {
            avisos.mostrar(mensagem(ex), Avisos.Tipo.ERRO);
        }
    }

    private void salvarArquivoAction() {
        try {
            controlador.salvarPlanejamento();
            avisos.mostrar("Planejamento salvo em planejamento.ser", Avisos.Tipo.SUCESSO);
        } catch (Exception ex) {
            abrirDialogo("Erro ao salvar", mensagem(ex), null, "Entendi", Botao.Estilo.PRIMARIO, false, null);
        }
    }

    private void carregarArquivoAction() {
        try {
            controlador.carregarPlanejamento();
            atualizarListas();
            executarSaida("Relatório atual", controlador::gerarRelatorioTexto);
            avisos.mostrar("Planejamento carregado com sucesso", Avisos.Tipo.SUCESSO);
        } catch (Exception ex) {
            boolean iniciarVazio = abrirDialogo(
                    "Falha ao carregar arquivo",
                    mensagem(ex) + "\n\nDeseja descartar o planejamento atual e iniciar um planejamento vazio?",
                    null,
                    "Iniciar vazio",
                    Botao.Estilo.PERIGO,
                    true,
                    null
            );
            if (iniciarVazio) {
                controlador.iniciarPlanejamentoVazio();
                atualizarListas();
                txtAreaRelatorio.setText("");
                lblSaida.setText("Resultado");
                avisos.mostrar("Novo planejamento vazio iniciado", Avisos.Tipo.SUCESSO);
            }
        }
    }

    private void novoPlanejamentoAction() {
        boolean confirmou = abrirDialogo(
                "Novo planejamento vazio",
                "Deseja iniciar um planejamento vazio em memória? (Use 'Salvar' caso queira sobrescrever o arquivo salvo.)",
                null,
                "Iniciar vazio",
                Botao.Estilo.PRIMARIO,
                true,
                null
        );
        if (confirmou) {
            controlador.iniciarPlanejamentoVazio();
            atualizarListas();
            txtAreaRelatorio.setText("");
            lblSaida.setText("Resultado");
            avisos.mostrar("Planejamento vazio iniciado", Avisos.Tipo.SUCESSO);
        }
    }

    private void copiarResultado() {
        String texto = txtAreaRelatorio.getText();
        if (texto == null || texto.isBlank()) {
            avisos.mostrar("Ainda não há resultado para copiar.", Avisos.Tipo.AVISO);
            return;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(texto), null);
        avisos.mostrar("Resultado copiado", Avisos.Tipo.SUCESSO);
    }

    // =====================================================================
    //  DIÁLOGO PADRÃO
    // =====================================================================
    private boolean abrirDialogo(String titulo, String descricao, JComponent corpo, String rotuloOk,
                                 Botao.Estilo estiloOk, boolean comCancelar, Supplier<String> aoConfirmar) {
        JDialog dlg = new JDialog(this, titulo, true);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        boolean[] confirmado = {false};

        JPanel raiz = coluna();
        raiz.setOpaque(true);
        raiz.setBackground(Tema.PAINEL);
        raiz.setBorder(new EmptyBorder(26, 28, 22, 28));

        raiz.add(titulo(titulo));
        raiz.add(espaco(6));
        JLabel lblDesc = new JLabel(html(descricao, 360));
        lblDesc.setFont(Tema.CORPO);
        lblDesc.setForeground(Tema.TEXTO_SUAVE);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        raiz.add(lblDesc);

        if (corpo != null) {
            raiz.add(espaco(18));
            corpo.setAlignmentX(Component.LEFT_ALIGNMENT);
            raiz.add(corpo);
        }

        JLabel lblErro = new JLabel();
        lblErro.setFont(Tema.CORPO);
        lblErro.setForeground(Tema.VERMELHO);
        lblErro.setBorder(new EmptyBorder(12, 0, 0, 0));
        lblErro.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblErro.setVisible(false);
        raiz.add(lblErro);

        raiz.add(espaco(22));
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rodape.setOpaque(false);
        rodape.setAlignmentX(Component.LEFT_ALIGNMENT);
        rodape.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        Botao ok = new Botao(rotuloOk, estiloOk);
        if (comCancelar) {
            Botao cancelar = new Botao("Cancelar", Botao.Estilo.SECUNDARIO);
            cancelar.addActionListener(e -> dlg.dispose());
            rodape.add(cancelar);
            rodape.add(Box.createHorizontalStrut(10));
        }
        rodape.add(ok);
        raiz.add(rodape);

        Runnable reajustar = () -> {
            dlg.pack();
            dlg.setSize(Math.max(dlg.getWidth(), 440), dlg.getHeight());
        };

        ok.addActionListener(e -> {
            if (aoConfirmar != null) {
                String erro = aoConfirmar.get();
                if (erro != null) {
                    lblErro.setText(html(erro, 360));
                    lblErro.setVisible(true);
                    reajustar.run();
                    return;
                }
            }
            confirmado[0] = true;
            dlg.dispose();
        });

        dlg.getRootPane().registerKeyboardAction(e -> dlg.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dlg.getRootPane().setDefaultButton(ok);
        dlg.setContentPane(raiz);
        dlg.setResizable(false);
        reajustar.run();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        return confirmado[0];
    }

    // =====================================================================
    //  UTILITÁRIOS
    // =====================================================================
    private static double parseValor(String bruto) {
        String s = bruto == null ? "" : bruto.trim().replace("R$", "").replace(" ", "").replace("\u00A0", "");
        if (s.isEmpty()) throw new NumberFormatException("vazio");
        if (s.contains(",")) s = s.replace(".", "").replace(",", ".");
        double v = Double.parseDouble(s);
        if (Double.isNaN(v) || Double.isInfinite(v)) throw new NumberFormatException("inválido");
        return v;
    }

    private static String mensagem(Exception ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank() ? "Algo deu errado. Tente novamente." : ex.getMessage();
    }

    private static String html(String s, int largura) {
        String t = s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
        return "<html><body style='width:" + largura + "px'>" + t + "</body></html>";
    }

    private static Color mix(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        return new Color(
                Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t),
                Math.round(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
    }

    private static void suavizar(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private static RoundRectangle2D.Float retangulo(float x, float y, float w, float h, float raio) {
        return new RoundRectangle2D.Float(x, y, w, h, raio, raio);
    }

    private static JPanel coluna() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        return p;
    }

    private static Component espaco(int altura) {
        JComponent s = (JComponent) Box.createVerticalStrut(altura);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        return s;
    }

    private static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.SECAO);
        l.setForeground(Tema.TEXTO);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JLabel suave(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.CORPO);
        l.setForeground(Tema.TEXTO_SUAVE);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JPanel bloco(String rotulo, JComponent campo) {
        JPanel b = new JPanel(new BorderLayout(0, 6)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        b.setOpaque(false);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel(rotulo);
        l.setFont(Tema.LABEL);
        l.setForeground(Tema.TEXTO);
        b.add(l, BorderLayout.NORTH);
        b.add(campo, BorderLayout.CENTER);
        return b;
    }

    private static JPanel linha(JComponent a, JComponent b) {
        JPanel l = new JPanel(new GridLayout(1, 2, 12, 0)) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        l.setOpaque(false);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.add(a);
        l.add(b);
        return l;
    }

    private static JScrollPane rolagem(Component conteudo, Color corThumb) {
        JScrollPane sp = new JScrollPane(conteudo);
        sp.setBorder(null);
        sp.setViewportBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUI(new BarraRolagem(corThumb));
        sp.getHorizontalScrollBar().setUI(new BarraRolagem(corThumb));
        sp.getVerticalScrollBar().setUnitIncrement(18);
        sp.getVerticalScrollBar().setOpaque(false);
        sp.getHorizontalScrollBar().setOpaque(false);
        return sp;
    }

    private static String iniciais(String nome) {
        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 0 || partes[0].isEmpty()) return "?";
        String a = partes[0].substring(0, 1);
        String b = partes.length > 1 ? partes[partes.length - 1].substring(0, 1) : "";
        return (a + b).toUpperCase(PT_BR);
    }

    private static final Color[] PALETA_AVATAR = {
            new Color(0x0B8A6B), new Color(0x2F6FDE), new Color(0x8A4FD3),
            new Color(0xD9692B), new Color(0xC2447A), new Color(0x4A7F8C)
    };

    private static void desenharAvatar(Graphics2D g2, int x, int y, int d, String nome) {
        g2.setColor(PALETA_AVATAR[Math.floorMod(nome.hashCode(), PALETA_AVATAR.length)]);
        g2.fill(new Ellipse2D.Float(x, y, d, d));
        g2.setColor(Color.WHITE);
        g2.setFont(new Font(Tema.FAMILIA, Font.BOLD, Math.round(d * 0.36f)));
        FontMetrics fm = g2.getFontMetrics();
        String ini = iniciais(nome);
        g2.drawString(ini, x + (d - fm.stringWidth(ini)) / 2f, y + (d - fm.getHeight()) / 2f + fm.getAscent());
    }

    private static void desenharIconeRecibo(Graphics2D g2, int x, int y, int size, Color bg, Color lines) {
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(x, y, size, size, 12, 12));
        g2.setColor(lines);
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        float p = size * 0.28f;
        float w = size - p * 2;
        g2.draw(new RoundRectangle2D.Float(x + p * 0.6f, y + p * 0.4f, w * 1.1f, w * 1.4f, 6, 6));
        g2.drawLine(Math.round(x + p), Math.round(y + p * 1.1f), Math.round(x + size - p), Math.round(y + p * 1.1f));
        g2.drawLine(Math.round(x + p), Math.round(y + p * 1.8f), Math.round(x + size - p), Math.round(y + p * 1.8f));
    }

    private static String abreviar(String texto, FontMetrics fm, int largura) {
        if (fm.stringWidth(texto) <= largura) return texto;
        String s = texto;
        while (s.length() > 1 && fm.stringWidth(s + "…") > largura) s = s.substring(0, s.length() - 1);
        return s + "…";
    }

    // =====================================================================
    //  COMPONENTES VISUAIS CUSTOMIZADOS
    // =====================================================================

    private static class Cartao extends JPanel {
        private final int raio;
        private final Color fundo, borda;
        private final boolean sombra;

        Cartao(LayoutManager layout, int raio, int padding) {
            this(layout, raio, padding, Tema.PAINEL, Tema.BORDA, true);
        }

        Cartao(LayoutManager layout, int raio, int padding, Color fundo, Color borda, boolean sombra) {
            super(layout);
            this.raio = raio;
            this.fundo = fundo;
            this.borda = borda;
            this.sombra = sombra;
            setOpaque(false);
            setBorder(new EmptyBorder(padding, padding, padding, padding));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            int w = getWidth(), h = getHeight();
            int baixo = sombra ? 2 : 0;
            if (sombra) {
                g2.setColor(new Color(14, 42, 47, 14));
                g2.fill(retangulo(0, 2, w, h - 2, raio));
            }
            g2.setColor(fundo);
            g2.fill(retangulo(0, 0, w, h - baixo, raio));
            g2.setColor(borda);
            g2.draw(retangulo(0.5f, 0.5f, w - 1, h - baixo - 1, raio));
            g2.dispose();
        }
    }

    private static class Botao extends JButton {
        enum Estilo {PRIMARIO, SECUNDARIO, PERIGO, ESCURO, LINK}

        private final Estilo estilo;
        private float hover, alvo;
        private boolean pressionado;
        private Timer anim;

        Botao(String texto, Estilo estilo) {
            super(texto);
            this.estilo = estilo;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            if (estilo == Estilo.LINK) {
                setFont(Tema.LABEL);
                setBorder(new EmptyBorder(6, 10, 6, 10));
            } else {
                setFont(Tema.CORPO_NEGRITO);
                setBorder(new EmptyBorder(11, 16, 11, 16));
            }
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { animar(1f); }
                @Override
                public void mouseExited(MouseEvent e) { animar(0f); }
                @Override
                public void mousePressed(MouseEvent e) { pressionado = true; repaint(); }
                @Override
                public void mouseReleased(MouseEvent e) { pressionado = false; repaint(); }
            });
        }

        private void animar(float novoAlvo) {
            alvo = novoAlvo;
            if (anim != null && anim.isRunning()) return;
            anim = new Timer(15, e -> {
                hover += (alvo - hover) * 0.35f;
                if (Math.abs(alvo - hover) < 0.02f) {
                    hover = alvo;
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            });
            anim.start();
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            int w = getWidth(), h = getHeight();
            float t = pressionado ? 1f : hover;

            Color fundo, texto, borda = null;
            if (!isEnabled()) {
                fundo = new Color(0xE6ECEA);
                texto = new Color(0x9AABA8);
            } else {
                switch (estilo) {
                    case PRIMARIO:
                        fundo = mix(Tema.PRIMARIA, Tema.PRIMARIA_HOVER, t);
                        texto = Color.WHITE;
                        break;
                    case ESCURO:
                        fundo = mix(Tema.TINTA, Tema.TINTA_CLARA, t);
                        texto = Color.WHITE;
                        break;
                    case PERIGO:
                        fundo = mix(Tema.VERMELHO_SUAVE, Tema.VERMELHO, t);
                        texto = mix(Tema.VERMELHO, Color.WHITE, t);
                        break;
                    case LINK:
                        fundo = mix(new Color(0xE3, 0xF4, 0xEE, 0), Tema.PRIMARIA_SUAVE, t);
                        texto = Tema.PRIMARIA;
                        break;
                    default:
                        fundo = mix(Tema.PAINEL, new Color(0xEEF3F1), t);
                        texto = Tema.TEXTO;
                        borda = Tema.BORDA;
                        break;
                }
            }
            float raio = estilo == Estilo.LINK ? 9 : 12;
            g2.setColor(fundo);
            g2.fill(retangulo(0, 0, w, h, raio));
            if (borda != null) {
                g2.setColor(borda);
                g2.draw(retangulo(0.5f, 0.5f, w - 1, h - 1, raio));
            }
            if (isFocusOwner()) {
                g2.setColor(new Color(11, 138, 107, 120));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(retangulo(1, 1, w - 2, h - 2, raio));
            }
            g2.setFont(getFont());
            g2.setColor(texto);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2f, (h - fm.getHeight()) / 2f + fm.getAscent());
            g2.dispose();
        }
    }

    private static class Campo extends JTextField {
        private final String dica;

        Campo(String dica) {
            this.dica = dica;
            setOpaque(false);
            setFont(Tema.CORPO);
            setForeground(Tema.TEXTO);
            setCaretColor(Tema.PRIMARIA);
            setSelectionColor(new Color(0xBFE8DA));
            setSelectedTextColor(Tema.TEXTO);
            setDisabledTextColor(new Color(0x9AABA8));
            setBorder(new EmptyBorder(10, 14, 10, 14));
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) { repaint(); }
                @Override
                public void focusLost(FocusEvent e) { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            int w = getWidth(), h = getHeight();
            g2.setColor(isEnabled() ? Tema.CAMPO_FUNDO : new Color(0xEEF2F1));
            g2.fill(retangulo(0, 0, w, h, 12));
            if (isFocusOwner()) {
                g2.setColor(new Color(11, 138, 107, 45));
                g2.setStroke(new BasicStroke(3f));
                g2.draw(retangulo(1.5f, 1.5f, w - 3, h - 3, 12));
                g2.setColor(Tema.PRIMARIA);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(retangulo(0.75f, 0.75f, w - 1.5f, h - 1.5f, 12));
            } else {
                g2.setColor(Tema.BORDA);
                g2.draw(retangulo(0.5f, 0.5f, w - 1, h - 1, 12));
            }
            g2.dispose();
            super.paintComponent(g);

            if (getText().isEmpty() && isEnabled()) {
                Graphics2D p = (Graphics2D) g.create();
                suavizar(p);
                p.setFont(getFont());
                p.setColor(new Color(0x9AABA8));
                FontMetrics fm = p.getFontMetrics();
                p.drawString(dica, getInsets().left, (h - fm.getHeight()) / 2f + fm.getAscent());
                p.dispose();
            }
        }
    }

    private static class Interruptor extends JComponent {
        private boolean ligado;
        private float pos;
        private Timer anim;
        private Runnable aoMudar;

        Interruptor() {
            Dimension d = new Dimension(46, 26);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFocusable(true);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) { requestFocusInWindow(); setSelected(!ligado); }
            });
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) { repaint(); }
                @Override
                public void focusLost(FocusEvent e) { repaint(); }
            });
            getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "alternar");
            getActionMap().put("alternar", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) { setSelected(!ligado); }
            });
        }

        void aoMudar(Runnable r) { this.aoMudar = r; }
        boolean isSelected() { return ligado; }

        void setSelected(boolean valor) {
            if (valor == ligado) return;
            ligado = valor;
            if (anim != null && anim.isRunning()) anim.stop();
            anim = new Timer(15, e -> {
                float alvo = ligado ? 1f : 0f;
                pos += (alvo - pos) * 0.4f;
                if (Math.abs(alvo - pos) < 0.02f) {
                    pos = alvo;
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            });
            anim.start();
            if (aoMudar != null) aoMudar.run();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            g2.setColor(mix(new Color(0xC9D5D2), Tema.PRIMARIA, pos));
            g2.fill(retangulo(0, 1, 46, 24, 24));
            if (isFocusOwner()) {
                g2.setColor(new Color(11, 138, 107, 120));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(retangulo(-1, 0, 48, 26, 26));
            }
            g2.setColor(new Color(0, 0, 0, 30));
            g2.fill(new Ellipse2D.Float(3 + pos * 22, 5, 18, 18));
            g2.setColor(Color.WHITE);
            g2.fill(new Ellipse2D.Float(3 + pos * 22, 4, 18, 18));
            g2.dispose();
        }
    }

    private static class Segmentado extends JComponent {
        private final String[] opcoes;
        private int sel;
        private float pos;
        private Timer anim;
        private Runnable aoMudar;

        Segmentado(String... opcoes) {
            this.opcoes = opcoes;
            setPreferredSize(new Dimension(200, 42));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int i = (int) (e.getX() / (getWidth() / (float) Segmentado.this.opcoes.length));
                    definir(Math.max(0, Math.min(Segmentado.this.opcoes.length - 1, i)));
                }
            });
        }

        void aoMudar(Runnable r) { this.aoMudar = r; }

        String getSelecionado() { return opcoes[sel]; }

        void definir(int i) {
            if (i == sel) return;
            sel = i;
            if (anim != null && anim.isRunning()) anim.stop();
            anim = new Timer(15, e -> {
                pos += (sel - pos) * 0.35f;
                if (Math.abs(sel - pos) < 0.01f) {
                    pos = sel;
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            });
            anim.start();
            if (aoMudar != null) aoMudar.run();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            int w = getWidth(), h = getHeight(), n = opcoes.length, pad = 4;
            g2.setColor(new Color(0xE8EEEC));
            g2.fill(retangulo(0, 0, w, h, 12));
            float largura = (w - 2f * pad) / n;
            g2.setColor(new Color(0, 0, 0, 18));
            g2.fill(retangulo(pad + pos * largura, pad + 1, largura, h - 2f * pad, 9));
            g2.setColor(Color.WHITE);
            g2.fill(retangulo(pad + pos * largura, pad, largura, h - 2f * pad, 9));
            g2.setFont(Tema.CORPO_NEGRITO);
            FontMetrics fm = g2.getFontMetrics();
            for (int i = 0; i < n; i++) {
                float distancia = Math.min(1f, Math.abs(pos - i));
                g2.setColor(mix(Tema.TEXTO, Tema.TEXTO_SUAVE, distancia));
                float x = pad + i * largura + (largura - fm.stringWidth(opcoes[i])) / 2f;
                g2.drawString(opcoes[i], x, (h - fm.getHeight()) / 2f + fm.getAscent());
            }
            g2.dispose();
        }
    }

    private static class Selo extends JLabel {
        Selo(String texto) {
            super(texto);
            setFont(Tema.LABEL);
            setForeground(Tema.PRIMARIA);
            setBorder(new EmptyBorder(3, 11, 3, 11));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            g2.setColor(Tema.PRIMARIA_SUAVE);
            g2.fill(retangulo(0, 0, getWidth(), getHeight(), getHeight()));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class ListaParticipantes extends JList<Participante> {
        private final String mensagemVazia;

        ListaParticipantes(DefaultListModel<Participante> modelo, boolean comMarcacao, int modoSelecao, String mensagemVazia) {
            super(modelo);
            this.mensagemVazia = mensagemVazia;
            setOpaque(false);
            setFixedCellHeight(60);
            setSelectionMode(modoSelecao);
            setCellRenderer(new Celula(comMarcacao));
        }

        int indiceNoPonto(Point p) {
            int i = locationToIndex(p);
            if (i < 0) return -1;
            Rectangle r = getCellBounds(i, i);
            return r != null && r.contains(p) ? i : -1;
        }

        void ativarMarcacaoPorClique() {
            for (MouseListener l : getMouseListeners()) {
                if (l.getClass().getName().startsWith("javax.swing.plaf")) removeMouseListener(l);
            }
            for (MouseMotionListener l : getMouseMotionListeners()) {
                if (l.getClass().getName().startsWith("javax.swing.plaf")) removeMouseMotionListener(l);
            }
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int i = indiceNoPonto(e.getPoint());
                    if (i < 0) return;
                    requestFocusInWindow();
                    if (isSelectedIndex(i)) removeSelectionInterval(i, i);
                    else addSelectionInterval(i, i);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getModel().getSize() == 0) {
                Graphics2D g2 = (Graphics2D) g.create();
                suavizar(g2);
                g2.setFont(Tema.CORPO);
                g2.setColor(Tema.TEXTO_SUAVE);
                FontMetrics fm = g2.getFontMetrics();
                String[] linhas = mensagemVazia.split("\n");
                int altura = linhas.length * fm.getHeight();
                int y = Math.max(0, (getHeight() - altura) / 2) + fm.getAscent();
                for (String s : linhas) {
                    g2.drawString(s, (getWidth() - fm.stringWidth(s)) / 2f, y);
                    y += fm.getHeight();
                }
                g2.dispose();
            }
        }

        private static class Celula extends JComponent implements ListCellRenderer<Participante> {
            private final boolean comMarcacao;
            private int codigo;
            private String nome = "";
            private double orcamento;
            private boolean selecionado;

            Celula(boolean comMarcacao) {
                this.comMarcacao = comMarcacao;
                setPreferredSize(new Dimension(10, 60));
            }

            @Override
            public Component getListCellRendererComponent(JList<? extends Participante> list, Participante value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                codigo = value.getCodigo();
                nome = value.getNome() == null ? "" : value.getNome();
                orcamento = value.getOrcamento();
                selecionado = isSelected;
                return this;
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                suavizar(g2);
                int w = getWidth(), h = getHeight();
                if (selecionado) {
                    g2.setColor(Tema.PRIMARIA_SUAVE);
                    g2.fill(retangulo(0, 3, w, h - 6, 14));
                }
                int d = 38;
                desenharAvatar(g2, 12, (h - d) / 2, d, nome);

                int xTexto = 12 + d + 14;
                int reservado = comMarcacao ? 12 + 22 + 12 : 12;
                int larguraTexto = w - xTexto - reservado;

                g2.setFont(Tema.CORPO_NEGRITO);
                FontMetrics fmN = g2.getFontMetrics();
                Font legenda = Tema.LEGENDA;
                FontMetrics fmC = g2.getFontMetrics(legenda);
                int topo = (h - (fmN.getHeight() + fmC.getHeight())) / 2;

                g2.setColor(Tema.TEXTO);
                g2.drawString(abreviar(nome, fmN, larguraTexto), xTexto, topo + fmN.getAscent());
                g2.setFont(legenda);
                g2.setColor(Tema.TEXTO_SUAVE);
                g2.drawString(abreviar("#" + codigo + " • Orçamento " + MOEDA.format(orcamento), fmC, larguraTexto),
                        xTexto, topo + fmN.getHeight() + fmC.getAscent());

                if (comMarcacao) {
                    float x = w - 12 - 22, y = (h - 22) / 2f;
                    if (selecionado) {
                        g2.setColor(Tema.PRIMARIA);
                        g2.fill(retangulo(x, y, 22, 22, 7));
                        Path2D.Float check = new Path2D.Float();
                        check.moveTo(x + 6, y + 11.5);
                        check.lineTo(x + 9.6, y + 15);
                        check.lineTo(x + 16, y + 7.5);
                        g2.setColor(Color.WHITE);
                        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2.draw(check);
                    } else {
                        g2.setColor(new Color(0xB9C7C4));
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.draw(retangulo(x + 0.75f, y + 0.75f, 20.5f, 20.5f, 7));
                    }
                }
                g2.dispose();
            }
        }
    }

    private static class ListaDespesas extends JList<Despesa> {
        private final String mensagemVazia;

        ListaDespesas(DefaultListModel<Despesa> modelo, String mensagemVazia) {
            super(modelo);
            this.mensagemVazia = mensagemVazia;
            setOpaque(false);
            setFixedCellHeight(60);
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setCellRenderer(new CelulaDespesa());
        }

        int indiceNoPonto(Point p) {
            int i = locationToIndex(p);
            if (i < 0) return -1;
            Rectangle r = getCellBounds(i, i);
            return r != null && r.contains(p) ? i : -1;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getModel().getSize() == 0) {
                Graphics2D g2 = (Graphics2D) g.create();
                suavizar(g2);
                g2.setFont(Tema.CORPO);
                g2.setColor(Tema.TEXTO_SUAVE);
                FontMetrics fm = g2.getFontMetrics();
                String[] linhas = mensagemVazia.split("\n");
                int altura = linhas.length * fm.getHeight();
                int y = Math.max(0, (getHeight() - altura) / 2) + fm.getAscent();
                for (String s : linhas) {
                    g2.drawString(s, (getWidth() - fm.stringWidth(s)) / 2f, y);
                    y += fm.getHeight();
                }
                g2.dispose();
            }
        }

        private static class CelulaDespesa extends JComponent implements ListCellRenderer<Despesa> {
            private String desc = "";
            private double valor;
            private boolean essencial;
            private int prioridade;
            private boolean selecionado;
            private String tipoRateio = "";

            CelulaDespesa() { setPreferredSize(new Dimension(10, 60)); }

            @Override
            public Component getListCellRendererComponent(JList<? extends Despesa> list, Despesa value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                desc = value.getDescricao() == null ? "" : value.getDescricao();
                valor = value.getValorInformado();
                essencial = value.isEssencial();
                prioridade = value.getPrioridade();
                selecionado = isSelected;
                tipoRateio = value.getTipoDescricao();
                return this;
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                suavizar(g2);
                int w = getWidth(), h = getHeight();
                if (selecionado) {
                    g2.setColor(Tema.PRIMARIA_SUAVE);
                    g2.fill(retangulo(0, 3, w, h - 6, 14));
                }

                int d = 38;
                Color bgIcone = essencial ? new Color(0xFCEBEA) : new Color(0xE8F2F8);
                Color linIcone = essencial ? Tema.VERMELHO : new Color(0x3B82F6);
                desenharIconeRecibo(g2, 12, (h - d) / 2, d, bgIcone, linIcone);

                int xTexto = 12 + d + 14;
                int larguraTexto = w - xTexto - 12;

                g2.setFont(Tema.CORPO_NEGRITO);
                FontMetrics fmN = g2.getFontMetrics();
                Font legenda = Tema.LEGENDA;
                FontMetrics fmC = g2.getFontMetrics(legenda);
                int topo = (h - (fmN.getHeight() + fmC.getHeight())) / 2;

                g2.setColor(Tema.TEXTO);
                g2.drawString(abreviar(desc, fmN, larguraTexto), xTexto, topo + fmN.getAscent());
                g2.setFont(legenda);
                g2.setColor(Tema.TEXTO_SUAVE);
                String tag = essencial ? "Essencial" : "P" + prioridade;
                g2.drawString(abreviar(tipoRateio + " • " + MOEDA.format(valor) + " • " + tag, fmC, larguraTexto),
                        xTexto, topo + fmN.getHeight() + fmC.getAscent());

                g2.dispose();
            }
        }
    }

    private static class BarraRolagem extends BasicScrollBarUI {
        private final Color cor;
        BarraRolagem(Color cor) { this.cor = cor; }

        @Override
        protected JButton createDecreaseButton(int orientation) { return botaoZero(); }
        @Override
        protected JButton createIncreaseButton(int orientation) { return botaoZero(); }

        private JButton botaoZero() {
            JButton b = new JButton();
            Dimension d = new Dimension(0, 0);
            b.setPreferredSize(d);
            b.setMinimumSize(d);
            b.setMaximumSize(d);
            return b;
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            return scrollbar.getOrientation() == JScrollBar.VERTICAL ? new Dimension(10, 48) : new Dimension(48, 10);
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) { }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            g2.setColor(isThumbRollover() ? mix(cor, Color.BLACK, 0.12f) : cor);
            g2.fill(retangulo(r.x + 2, r.y + 2, r.width - 4, r.height - 4, Math.min(r.width, r.height) - 4));
            g2.dispose();
        }
    }

    // ----------------------------------------------------------------- Sidebar
    private static class Marca extends JComponent {
        Marca() { setPreferredSize(new Dimension(42, 42)); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            g2.setColor(new Color(0x14A37E));
            g2.fill(retangulo(0, 0, 42, 42, 13));
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(retangulo(9, 15, 24, 17, 6));
            g2.drawLine(14, 10, 26, 10);
            g2.fill(new Ellipse2D.Float(25.6f, 21.6f, 4.8f, 4.8f));
            g2.dispose();
        }
    }

    private static class ItemNav extends JComponent {
        private final String texto;
        private final int icone;
        private boolean ativo, hover;

        ItemNav(String texto, int icone, Runnable acao) {
            this.texto = texto;
            this.icone = icone;
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFocusable(true);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override
                public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                @Override
                public void mousePressed(MouseEvent e) {
                    requestFocusInWindow();
                    acao.run();
                }
            });
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) { repaint(); }
                @Override
                public void focusLost(FocusEvent e) { repaint(); }
            });
            AbstractAction executar = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) { acao.run(); }
            };
            getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ir");
            getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "ir");
            getActionMap().put("ir", executar);
        }

        void setAtivo(boolean ativo) { this.ativo = ativo; repaint(); }

        @Override
        public Dimension getPreferredSize() { return new Dimension(200, 46); }

        @Override
        public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 46); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            int w = getWidth(), h = getHeight();
            if (ativo) {
                g2.setColor(Tema.TINTA_CLARA);
                g2.fill(retangulo(0, 0, w, h, 12));
                g2.setColor(Tema.MENTA);
                g2.fill(retangulo(0, 11, 3.5f, h - 22, 3.5f));
            } else if (hover) {
                g2.setColor(new Color(255, 255, 255, 14));
                g2.fill(retangulo(0, 0, w, h, 12));
            }

            if (isFocusOwner()) {
                g2.setColor(new Color(0x4F, 0xD1, 0xA5, 120));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(retangulo(1, 1, w - 2, h - 2, 12));
            }

            Color cor = ativo ? Tema.MENTA : Tema.TINTA_TEXTO;
            desenharIcone(g2, icone, 18, (h - 20) / 2f, 20, cor);
            g2.setFont(Tema.CORPO);
            g2.setColor(ativo ? Color.WHITE : new Color(0xC3D5D3));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(texto, 50, (h - fm.getHeight()) / 2f + fm.getAscent());
            g2.dispose();
        }

        private static void desenharIcone(Graphics2D g2, int tipo, float x, float y, float s, Color cor) {
            g2.setColor(cor);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (tipo) {
                case 0:
                    g2.draw(new Ellipse2D.Float(x + s * 0.30f, y + s * 0.06f, s * 0.40f, s * 0.40f));
                    g2.draw(new Arc2D.Float(x + s * 0.12f, y + s * 0.56f, s * 0.76f, s * 0.70f, 0, 180, Arc2D.OPEN));
                    break;
                case 1:
                    g2.draw(retangulo(x + s * 0.18f, y + s * 0.04f, s * 0.64f, s * 0.92f, s * 0.16f));
                    g2.drawLine(Math.round(x + s * 0.34f), Math.round(y + s * 0.32f), Math.round(x + s * 0.66f), Math.round(y + s * 0.32f));
                    g2.drawLine(Math.round(x + s * 0.34f), Math.round(y + s * 0.52f), Math.round(x + s * 0.66f), Math.round(y + s * 0.52f));
                    g2.drawLine(Math.round(x + s * 0.34f), Math.round(y + s * 0.72f), Math.round(x + s * 0.54f), Math.round(y + s * 0.72f));
                    break;
                default:
                    g2.setStroke(new BasicStroke(3.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(Math.round(x + s * 0.2f), Math.round(y + s * 0.92f), Math.round(x + s * 0.2f), Math.round(y + s * 0.55f));
                    g2.drawLine(Math.round(x + s * 0.5f), Math.round(y + s * 0.92f), Math.round(x + s * 0.5f), Math.round(y + s * 0.12f));
                    g2.drawLine(Math.round(x + s * 0.8f), Math.round(y + s * 0.92f), Math.round(x + s * 0.8f), Math.round(y + s * 0.38f));
                    break;
            }
        }
    }

    // ----------------------------------------------------------------- Avisos (toasts)
    private static class Avisos extends JComponent {
        enum Tipo {SUCESSO, ERRO, AVISO}

        private List<String> linhas = new ArrayList<>();
        private Tipo tipo = Tipo.SUCESSO;
        private float alfa;
        private long inicio;
        private int duracao;
        private final Timer timer;

        Avisos() {
            setOpaque(false);
            timer = new Timer(16, e -> avancar());
        }

        @Override
        public boolean contains(int x, int y) { return false; }

        void mostrar(String mensagem, Tipo tipo) {
            this.tipo = tipo;
            this.duracao = tipo == Tipo.SUCESSO ? 2200 : 4200;
            this.linhas = quebrar(mensagem == null ? "" : mensagem);
            this.inicio = System.currentTimeMillis();
            this.alfa = 0;
            setVisible(true);
            timer.restart();
        }

        private void avancar() {
            long t = System.currentTimeMillis() - inicio;
            if (t < 160) alfa = t / 160f;
            else if (t < 160 + duracao) alfa = 1f;
            else if (t < 160 + duracao + 320) alfa = 1f - (t - 160 - duracao) / 320f;
            else {
                timer.stop();
                alfa = 0;
                setVisible(false);
                return;
            }
            repaint();
        }

        private List<String> quebrar(String msg) {
            FontMetrics fm = getFontMetrics(Tema.CORPO_NEGRITO);
            List<String> saida = new ArrayList<>();
            StringBuilder atual = new StringBuilder();
            for (String palavra : msg.trim().split("\\s+")) {
                String tentativa = atual.length() == 0 ? palavra : atual + " " + palavra;
                if (atual.length() > 0 && fm.stringWidth(tentativa) > 460) {
                    saida.add(atual.toString());
                    atual = new StringBuilder(palavra);
                } else {
                    atual = new StringBuilder(tentativa);
                }
            }
            if (atual.length() > 0) saida.add(atual.toString());
            return saida;
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (linhas.isEmpty() || alfa <= 0f) return;
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            g2.setComposite(AlphaComposite.SrcOver.derive(Math.min(1f, alfa)));
            g2.setFont(Tema.CORPO_NEGRITO);
            FontMetrics fm = g2.getFontMetrics();

            int larguraTexto = 0;
            for (String l : linhas) larguraTexto = Math.max(larguraTexto, fm.stringWidth(l));
            int w = 16 + 24 + 12 + larguraTexto + 22;
            int h = linhas.size() * fm.getHeight() + 28;
            int x = getWidth() - w - 36;
            int y = 18 - Math.round((1f - alfa) * 14);

            g2.setColor(new Color(0, 0, 0, 45));
            g2.fill(retangulo(x, y + 4, w, h, 18));
            g2.setColor(Tema.TINTA);
            g2.fill(retangulo(x, y, w, h, 18));

            Color cor = tipo == Tipo.SUCESSO ? Tema.MENTA : tipo == Tipo.ERRO ? new Color(0xFF6B61) : Tema.AMBAR;
            float ix = x + 16, iy = y + 14;
            g2.setColor(cor);
            g2.fill(new Ellipse2D.Float(ix, iy, 24, 24));
            g2.setColor(Tema.TINTA);
            g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (tipo) {
                case SUCESSO:
                    Path2D.Float check = new Path2D.Float();
                    check.moveTo(ix + 6.5, iy + 12.5);
                    check.lineTo(ix + 10.5, iy + 16.5);
                    check.lineTo(ix + 17.5, iy + 8);
                    g2.draw(check);
                    break;
                case ERRO:
                    g2.drawLine(Math.round(ix + 8), Math.round(iy + 8), Math.round(ix + 16), Math.round(iy + 16));
                    g2.drawLine(Math.round(ix + 16), Math.round(iy + 8), Math.round(ix + 8), Math.round(iy + 16));
                    break;
                default:
                    g2.drawLine(Math.round(ix + 12), Math.round(iy + 6), Math.round(ix + 12), Math.round(iy + 13));
                    g2.fill(new Ellipse2D.Float(ix + 10.6f, iy + 16.2f, 2.8f, 2.8f));
                    break;
            }

            g2.setColor(Color.WHITE);
            int ty = y + 14 + fm.getAscent() + (24 - fm.getHeight()) / 2;
            for (String l : linhas) {
                g2.drawString(l, x + 16 + 24 + 12, ty);
                ty += fm.getHeight();
            }
            g2.dispose();
        }
    }
}