package com.exemplo.usuario.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatriculaTest {

    private Usuario usuario;
    private Curso curso;
    private Matricula matricula;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("Test", "test@test.com", "senha123");
        curso = new Curso(); // Supondo um construtor padrão
        matricula = new Matricula(usuario, curso, false);
    }

    @Test
    void deveConcederTresCursosExtrasQuandoAprovado() {
        // Act
        matricula.finalizarCurso(8.0); // Método que vai processar a nota

        // Assert
        assertEquals(StatusMatricula.APROVADO, matricula.getStatus());
        assertEquals(3, matricula.getCursosExtras()); // Atributo novo
    }

    @Test
    void deveEncaminharParaASQuandoNotaInsuficiente() {
        // Act
        matricula.finalizarCurso(6.5);

        // Assert
        assertEquals(StatusMatricula.EM_RECUPERACAO, matricula.getStatus());
        assertNull(matricula.getNotaAS()); // Atributo novo
    }

    @Test
    void deveAprovarAposAsComNotaExtra() {
        // Arrange
        matricula.finalizarCurso(5.0);

        // Act
        matricula.realizarAvaliacaoSubstitutiva(7.5);

        // Assert
        assertEquals(StatusMatricula.APROVADO, matricula.getStatus());
        assertEquals(3, matricula.getCursosExtras());
    }

    @Test
    void deveReprovarAposAsComNotaInsuficiente() {
        // Arrange
        matricula.finalizarCurso(4.0);

        // Act
        matricula.realizarAvaliacaoSubstitutiva(6.0);

        // Assert
        assertEquals(StatusMatricula.REPROVADO, matricula.getStatus());
    }
}