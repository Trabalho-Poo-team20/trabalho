package modelo;

import java.io.Serializable;

public class Participante implements Serializable {
    private static final long serialVersionUID = 1L;

    private int codigo;
    private String nome;
    private double orcamento;

    public Participante(int codigo, String nome, double orcamento) {
        if (orcamento < 0) {
            throw new IllegalArgumentException("O orçamento deve ser maior ou igual a zero.");
        }
        this.codigo = codigo;
        setNome(nome);
        this.orcamento = orcamento;
    }

    public int getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public double getOrcamento() { return orcamento; }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome não pode estar vazio.");
        }
        this.nome = nome;
    }

    public void setOrcamento(double orcamento) {
        if (orcamento < 0) {
            throw new IllegalArgumentException("O orçamento deve ser maior ou igual a zero.");
        }
        this.orcamento = orcamento;
    }

    @Override
    public String toString() {
        return codigo + " - " + nome;
    }
}