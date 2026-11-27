package persistencia;

import modelo.Planejamento;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GerenciadorArquivo implements PersistenciaDados {
    private static final String NOME_ARQUIVO = "planejamento.ser";

    @Override
    public void salvar(Planejamento planejamento) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(NOME_ARQUIVO))) {
            oos.writeObject(planejamento);
        }
    }

    @Override
    public Planejamento carregar() throws IOException, ClassNotFoundException {
        File arquivo = new File(NOME_ARQUIVO);
        if (!arquivo.exists()) {
            throw new FileNotFoundException("Arquivo '" + NOME_ARQUIVO + "' não encontrado.");
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arquivo))) {
            Object obj = ois.readObject();
            if (!(obj instanceof Planejamento)) {
                throw new IOException("Conteúdo inválido no arquivo '" + NOME_ARQUIVO + "'.");
            }
            return (Planejamento) obj;
        }
    }
}