package com.exemplo.usuario.service;

import com.exemplo.usuario.domain.Assinatura;
import com.exemplo.usuario.domain.Curso;
import com.exemplo.usuario.domain.Matricula;
import com.exemplo.usuario.domain.StatusMatricula;
import com.exemplo.usuario.domain.Usuario;
import com.exemplo.usuario.dto.MatriculaResponseDTO;
import com.exemplo.usuario.repository.AssinaturaRepository;
import com.exemplo.usuario.repository.CursoRepository;
import com.exemplo.usuario.repository.MatriculaRepository;
import com.exemplo.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Camada: SERVICE.
// Esta classe orquestra o caso de uso de matricula.
@Service
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final AssinaturaRepository assinaturaRepository;

    public MatriculaService(MatriculaRepository matriculaRepository,
                            UsuarioRepository usuarioRepository,
                            CursoRepository cursoRepository,
                            AssinaturaRepository assinaturaRepository) {
        this.matriculaRepository = matriculaRepository;
        this.usuarioRepository = usuarioRepository;
        this.cursoRepository = cursoRepository;
        this.assinaturaRepository = assinaturaRepository;
    }

    public List<MatriculaResponseDTO> listarPorUsuario(Long usuarioId) {
        return matriculaRepository.findByUsuarioId(usuarioId).stream().map(this::toDTO).toList();
    }

    @Transactional
    public MatriculaResponseDTO matricular(Long usuarioId, Long cursoId, boolean bonus) {
        // 1) Buscar as entidades necessarias.
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new RuntimeException("Curso nao encontrado"));

        Assinatura assinatura = assinaturaRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Assinatura nao encontrada"));

        // 2) Aplicar regra de negocio.
        if (bonus) {
            assinatura.consumirCredito();
        }

        if (matriculaRepository.existsByUsuarioIdAndCursoId(usuarioId, cursoId)) {
            throw new IllegalStateException("O aluno já possui matrícula neste curso.");
        }

        // 3) Criar entidade do dominio.
        Matricula matricula = new Matricula(usuario, curso, bonus);

        // 4) Persistir e devolver DTO.
        return toDTO(matriculaRepository.save(matricula));
    }

    @Transactional
    public MatriculaResponseDTO concluir(Long matriculaId, Double notaFinal) {
        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new RuntimeException("Matricula nao encontrada"));

        matricula.finalizarCurso(notaFinal);

        if (matricula.concluidoComAproveitamento()) {
            Assinatura assinatura = assinaturaRepository.findByUsuarioId(matricula.getUsuario().getId())
                    .orElseThrow(() -> new RuntimeException("Assinatura nao encontrada"));
            assinatura.registrarConclusaoComSucesso();

        }

        return toDTO(matriculaRepository.save(matricula));
    }

    @Transactional
    public MatriculaResponseDTO realizarAS(Long matriculaId, Double nota) {
        Matricula matricula = matriculaRepository.findById(matriculaId)
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada"));

        // Aplica a regra de negócio do Domínio (que define APROVADO ou REPROVADO)
        matricula.realizarAvaliacaoSubstitutiva(nota);

        if (matricula.concluidoComAproveitamento()) {
            Assinatura assinatura = assinaturaRepository.findByUsuarioId(matricula.getUsuario().getId())
                    .orElseThrow(() -> new RuntimeException("Assinatura não encontrada"));
            assinatura.registrarConclusaoComSucesso();

            // Salva a aprovação no banco
            matricula = matriculaRepository.save(matricula);
        } else {
            // Se reprovou na AS, deleta a matrícula para ele poder tentar novamente
            matriculaRepository.delete(matricula);
        }

        // Retorna o DTO para o frontend saber o que aconteceu (mesmo se foi deletado)
        return toDTO(matricula);
    }

    private MatriculaResponseDTO toDTO(Matricula matricula) {
        return new MatriculaResponseDTO(
                matricula.getId(),
                matricula.getUsuario().getId(),
                matricula.getUsuario().getNome(),
                matricula.getCurso().getId(),
                matricula.getCurso().getTitulo(),
                matricula.getStatus().name(),
                matricula.getNotaFinal(),
                matricula.isBonus()
        );
    }
}
