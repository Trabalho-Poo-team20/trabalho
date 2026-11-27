package principal;

import controlador.ControladorPlanejamento;
import persistencia.GerenciadorArquivo;
import persistencia.PersistenciaDados;
import visao.TelaPrincipal;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PersistenciaDados persistencia = new GerenciadorArquivo();
            ControladorPlanejamento controlador = new ControladorPlanejamento(persistencia);
            TelaPrincipal tela = new TelaPrincipal(controlador);
            tela.setLocationRelativeTo(null);
            tela.setVisible(true);
        });
    }
}