const AUTH = "Basic " + btoa("admin:123456");

async function api(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            "Authorization": AUTH,
            ...(options.headers || {})
        }
    });

    if (response.status === 204) return null;
    const text = await response.text();
    if (!response.ok) throw new Error(text || "Erro na requisição");
    return text ? JSON.parse(text) : null;
}

async function criarUsuario() {
    try {
        const payload = {
            nome: document.getElementById("nome").value,
            email: document.getElementById("email").value,
            senha: document.getElementById("senha").value
        };
        await api("/api/usuarios", { method: "POST", body: JSON.stringify(payload) });
        alert("Usuário criado com sucesso!");
        listarUsuarios();
    } catch (error) {
        alert("Erro ao criar usuário: " + error.message);
    }
}

async function listarUsuarios() {
    try {
        const usuarios = await api("/api/usuarios");
        document.getElementById("usuarios").innerHTML = usuarios.map(u =>
            `<li><strong>ID: ${u.id}</strong> | Nome: ${u.nome} | Plano: ${u.plano} | Créditos: ${u.creditosCursos} | Concluídos: ${u.cursosConcluidosComSucesso}</li>`
        ).join("");
    } catch (error) {
        console.error("Erro ao listar usuários", error);
    }
}

async function criarCurso() {
    try {
        const payload = {
            titulo: document.getElementById("tituloCurso").value,
            descricao: document.getElementById("descricaoCurso").value
        };
        await api("/api/cursos", { method: "POST", body: JSON.stringify(payload) });
        alert("Curso criado com sucesso!");
        listarCursos();
    } catch (error) {
        alert("Erro ao criar curso: " + error.message);
    }
}

async function listarCursos() {
    try {
        const cursos = await api("/api/cursos");
        document.getElementById("cursos").innerHTML = cursos.map(c =>
            // Adicionado a exibição da descrição aqui!
            `<li><strong>ID: ${c.id}</strong> | Título: ${c.titulo} | Descrição: ${c.descricao || 'N/A'}</li>`
        ).join("");
    } catch (error) {
        console.error("Erro ao listar cursos", error);
    }
}

async function matricular() {
    try {
        const usuarioIdVal = document.getElementById("matUsuarioId").value;
        const cursoIdVal = document.getElementById("matCursoId").value;

        if(!usuarioIdVal || !cursoIdVal || isNaN(usuarioIdVal) || isNaN(cursoIdVal)) {
            alert("Por favor, digite NÚMEROS válidos para o ID do Usuário e do Curso.");
            return;
        }

        const payload = {
            usuarioId: Number(usuarioIdVal),
            cursoId: Number(cursoIdVal),
            bonus: document.getElementById("matBonus").value === "true"
        };
        const result = await api("/api/matriculas", { method: "POST", body: JSON.stringify(payload) });
        alert(`Sucesso! Matrícula criada com o ID: ${result.id}`);
    } catch (error) {
        alert("Erro ao matricular: " + error.message);
    }
}

async function concluirMatricula() {
    try {
        const id = document.getElementById("matriculaId").value;
        // Pega o valor e substitui vírgula por ponto, para evitar erros de digitação!
        const notaRaw = document.getElementById("notaFinal").value.replace(",", ".");
        const notaFormatada = Number(notaRaw);

        if(!id || isNaN(notaFormatada)) {
            alert("Por favor, digite um ID de matrícula e uma Nota válida (ex: 7.5).");
            return;
        }

        const payload = { notaFinal: notaFormatada };
        const result = await api(`/api/matriculas/${id}/concluir`, { method: "PUT", body: JSON.stringify(payload) });
        alert(`Matrícula ${result.id} concluída! Novo Status: ${result.status}`);
    } catch (error) {
        alert("Erro ao concluir matrícula: " + error.message);
    }
}

async function listarMatriculasUsuario() {
    try {
        const usuarioId = document.getElementById("consultaUsuarioId").value;
        if(!usuarioId) {
            alert("Digite o ID do usuário para consultar.");
            return;
        }
        const matriculas = await api(`/api/matriculas/usuario/${usuarioId}`);
        document.getElementById("matriculas").innerHTML = matriculas.map(m =>
            `<li><strong>ID Matrícula: ${m.id}</strong> | Curso: ${m.cursoTitulo} | Status: <strong>${m.status}</strong> | Nota: ${m.notaFinal ?? 'Ainda não avaliado'} | Bônus: ${m.bonus ? 'Sim' : 'Não'}</li>`
        ).join("");
    } catch (error) {
        alert("Erro ao consultar matrículas: " + error.message);
    }
}

async function realizarAS() {
    try {
        const id = document.getElementById("matriculaIdAS").value;
        const notaRaw = document.getElementById("notaAS").value.replace(",", ".");
        const notaFormatada = Number(notaRaw);

        if(!id || isNaN(notaFormatada)) {
            alert("Por favor, digite um ID de matrícula e uma Nota válida (ex: 7.5).");
            return;
        }

        // Reutilizamos o payload do ConcluirMatriculaRequestDTO
        const payload = { notaFinal: notaFormatada };
        const result = await api(`/api/matriculas/${id}/substitutiva`, { method: "PUT", body: JSON.stringify(payload) });

        if (result.status === "REPROVADO") {
            alert(`Atenção: Matrícula ${result.id} reprovada e CANCELADA! O aluno já pode se matricular no curso novamente.`);
        } else {
            alert(`Sucesso! Matrícula ${result.id} aprovada na AS! Novo Status: ${result.status}`);
        }
    } catch (error) {
        alert("Erro na Avaliação Substitutiva: " + error.message);
    }
}