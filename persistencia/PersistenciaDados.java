package persistencia;

import modelo.Planejamento;

public interface PersistenciaDados {
    void salvar(Planejamento planejamento) throws Exception;
    Planejamento carregar() throws Exception;
}