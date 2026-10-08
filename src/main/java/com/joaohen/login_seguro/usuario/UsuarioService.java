package com.joaohen.login_seguro.usuario;

import com.joaohen.login_seguro.comum.RegraDeNegocioException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class UsuarioService {

    public static final int LIMITE_TENTATIVAS = 5;
    public static final Duration TEMPO_BLOQUEIO = Duration.ofMinutes(15);

    private final UsuarioRepository repositorio;
    private final PasswordEncoder codificador;

    public UsuarioService(UsuarioRepository repositorio, PasswordEncoder codificador) {
        this.repositorio = repositorio;
        this.codificador = codificador;
    }

    public static String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public Usuario cadastrar(CadastroForm form) {
        return criar(form.getNome(), form.getEmail(), form.getSenha(), Perfil.USUARIO);
    }

    public Usuario criar(String nome, String email, String senha, Perfil perfil) {
        String emailNormalizado = normalizarEmail(email);
        if (repositorio.existsByEmail(emailNormalizado)) {
            throw new EmailJaCadastradoException();
        }
        Usuario usuario = new Usuario();
        usuario.setNome(nome.trim());
        usuario.setEmail(emailNormalizado);
        usuario.setSenhaHash(codificador.encode(senha));
        usuario.setPerfil(perfil);
        usuario.setCriadoEm(Instant.now());
        return repositorio.save(usuario);
    }

    public boolean existeEmail(String email) {
        return repositorio.existsByEmail(normalizarEmail(email));
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return repositorio.findByEmail(normalizarEmail(email));
    }

    public List<Usuario> listarTodos() {
        return repositorio.findAllByOrderByCriadoEmDesc();
    }

    public long contarPorPerfil(Perfil perfil) {
        return repositorio.countByPerfil(perfil);
    }

    public void registrarAcessoComSucesso(String email) {
        repositorio.findByEmail(normalizarEmail(email)).ifPresent(usuario -> {
            usuario.setTentativasFalhas(0);
            usuario.setBloqueadoAte(null);
            usuario.setUltimoAcesso(Instant.now());
            repositorio.save(usuario);
        });
    }

    public boolean registrarFalhaDeAcesso(String email) {
        Optional<Usuario> encontrado = repositorio.findByEmail(normalizarEmail(email));
        if (encontrado.isEmpty()) {
            return false;
        }
        Usuario usuario = encontrado.get();
        int tentativas = usuario.getTentativasFalhas() + 1;
        boolean bloqueou = tentativas >= LIMITE_TENTATIVAS;
        usuario.setTentativasFalhas(bloqueou ? 0 : tentativas);
        if (bloqueou) {
            usuario.setBloqueadoAte(Instant.now().plus(TEMPO_BLOQUEIO));
        }
        repositorio.save(usuario);
        return bloqueou;
    }

    public void alterarPerfil(String id, Perfil novoPerfil, String emailResponsavel) {
        Usuario usuario = buscarPorId(id);
        if (usuario.getEmail().equals(normalizarEmail(emailResponsavel))) {
            throw new RegraDeNegocioException("Você não pode alterar o seu próprio perfil.");
        }
        if (usuario.isAdmin() && novoPerfil != Perfil.ADMIN && repositorio.countByPerfil(Perfil.ADMIN) <= 1) {
            throw new RegraDeNegocioException("O sistema precisa de pelo menos um administrador.");
        }
        usuario.setPerfil(novoPerfil);
        repositorio.save(usuario);
    }

    public Usuario alternarAtivo(String id, String emailResponsavel) {
        Usuario usuario = buscarPorId(id);
        if (usuario.getEmail().equals(normalizarEmail(emailResponsavel))) {
            throw new RegraDeNegocioException("Você não pode desativar a sua própria conta.");
        }
        usuario.setAtivo(!usuario.isAtivo());
        usuario.setTentativasFalhas(0);
        usuario.setBloqueadoAte(null);
        return repositorio.save(usuario);
    }

    private Usuario buscarPorId(String id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RegraDeNegocioException("Usuário não encontrado."));
    }
}
