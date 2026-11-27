package modelo;

import java.io.Serializable;
import java.util.Objects;

public class Participante implements Serializable {
    private static final long serialVersionUID = 1L;

    private int codigo;
    private String nome;
    private double orcamento;

    public Participante(int codigo, String nome, double orcamento) {
        if (codigo <= 0) {
            throw new IllegalArgumentException("O código do participante deve ser maior que zero.");
        }
        if (orcamento < 0) {
            throw new IllegalArgumentException("O orçamento deve ser maior ou igual a zero.");
        }
        this.codigo = codigo;
        setNome(nome);
        this.orcamento = orcamento;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public double getOrcamento() {
        return orcamento;
    }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do participante não pode estar vazio.");
        }
        this.nome = nome.trim();
    }

    public void setOrcamento(double orcamento) {
        if (orcamento < 0) {
            throw new IllegalArgumentException("O orçamento deve ser maior ou igual a zero.");
        }
        this.orcamento = orcamento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Participante)) return false;
        Participante that = (Participante) o;
        return codigo == that.codigo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return codigo + " - " + nome;
    }
}