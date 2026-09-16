package com.exemplo.usuario.domain;

import jakarta.persistence.*;

// Camada: DOMINIO.
// Matricula liga Usuario e Curso.
@Entity
@Table(name = "matriculas")
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "curso_id")
    private Curso curso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusMatricula status;

    @Column
    private Double notaFinal;

    @Column(nullable = false)
    private boolean bonus;

    @Column
    private Double notaAS;

    @Column(nullable = false)
    private int cursosExtras = 0;

    public Matricula() {
    }

    public Matricula(Usuario usuario, Curso curso, boolean bonus) {
        this.usuario = usuario;
        this.curso = curso;
        this.bonus = bonus;
        this.status = StatusMatricula.EM_ANDAMENTO;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Curso getCurso() {
        return curso;
    }

    public StatusMatricula getStatus() {
        return status;
    }

    public Double getNotaFinal() {
        return notaFinal;
    }

    public Double getNotaAS() {
        return notaAS;
    }

    public boolean isBonus() {
        return bonus;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public void setStatus(StatusMatricula status) {
        this.status = status;
    }

    public void setNotaFinal(Double notaFinal) {
        this.notaFinal = notaFinal;
    }

    public void setBonus(boolean bonus) {
        this.bonus = bonus;
    }

    public void setNotaAS(Double notaAS) {
        this.notaAS = notaAS;
    }

    public int getCursosExtras() {
        return cursosExtras;
    }

    public void setCursosExtras(int cursosExtras) {
        this.cursosExtras = cursosExtras;
    }

    public void finalizarCurso(Double notaObtida) {
        this.notaFinal = notaObtida;

        if (this.notaFinal > 7.0 && this.usuario != null) {
            this.status = StatusMatricula.APROVADO;
            this.cursosExtras = 3;
        }
        else if (this.notaFinal <= 7.0 && this.notaFinal >= 0.0) {
            this.status = StatusMatricula.EM_RECUPERACAO;
            this.cursosExtras = 0;
        }
    }

    // Método que avalia a nota da AS
    public void realizarAvaliacaoSubstitutiva(Double nota) {
        if (this.status == StatusMatricula.EM_RECUPERACAO || this.status == StatusMatricula.EM_ANDAMENTO) {
            this.notaAS = nota;

            if (this.notaAS != null && this.notaAS > 7.0) {
                this.status = StatusMatricula.APROVADO;
                this.cursosExtras = 3;
            } else {
                this.status = StatusMatricula.REPROVADO;
                this.cursosExtras = 0;
            }
        } else {
            throw new IllegalStateException("Aluno não está em recuperação.");
        }
    }

    public boolean concluidoComAproveitamento() {
        return StatusMatricula.APROVADO.equals(this.status);
    }
}
