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

    @Test
    void naoDevePermitirAsSeNaoEstiverEmRecuperacao() {
        // Arrange: Finaliza o curso com nota alta para o status ficar como APROVADO
        matricula.finalizarCurso(8.0);

        // Assert: Agora o status é APROVADO, então ele vai cair no 'else' e lançar a exceção!
        assertThrows(IllegalStateException.class, () -> {
            matricula.realizarAvaliacaoSubstitutiva(8.0);
        });
    }

    @Test
    void deveRetornarVerdadeiroSeConcluidoComAproveitamento() {
        // Arrange
        matricula.finalizarCurso(8.5); // Isso muda o status para APROVADO

        // Act
        boolean aproveitamento = matricula.concluidoComAproveitamento();

        // Assert
        assertTrue(aproveitamento);
    }

    @Test
    void deveRetornarFalsoSeNaoTiverAproveitamento() {
        // Arrange
        matricula.finalizarCurso(5.0); // Isso muda o status para EM_RECUPERACAO

        // Act
        boolean aproveitamento = matricula.concluidoComAproveitamento();

        // Assert
        assertFalse(aproveitamento);
    }
}