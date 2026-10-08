package com.joaohen.login_seguro.usuario;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.text.Normalizer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

@Document("usuarios")
public class Usuario {

    private static final int TAMANHO_MRZ = 44;

    @Id
    private String id;

    private String nome;

    @Indexed(unique = true)
    private String email;

    private String senhaHash;

    private Perfil perfil = Perfil.USUARIO;

    private boolean ativo = true;

    private int tentativasFalhas;

    private Instant bloqueadoAte;

    private Instant criadoEm;

    private Instant ultimoAcesso;

    public boolean estaBloqueado() {
        return bloqueadoAte != null && bloqueadoAte.isAfter(Instant.now());
    }

    public boolean isAdmin() {
        return perfil == Perfil.ADMIN;
    }

    public boolean isPodeModerar() {
        return perfil == Perfil.ADMIN || perfil == Perfil.MODERADOR;
    }

    public String getIniciais() {
        if (nome == null || nome.isBlank()) {
            return "?";
        }
        String[] partes = nome.trim().split("\\s+");
        String primeira = partes[0].substring(0, 1);
        String ultima = partes.length > 1 ? partes[partes.length - 1].substring(0, 1) : "";
        return (primeira + ultima).toUpperCase(Locale.ROOT);
    }

    public String getMrzLinha1() {
        String[] partes = semAcento(nome).split("\\s+");
        String sobrenome = partes.length > 1 ? partes[partes.length - 1] : partes[0];
        String prenomes = partes.length > 1
                ? Arrays.stream(partes, 0, partes.length - 1).collect(Collectors.joining("<"))
                : "";
        return completar("LS<BRA" + sobrenome + "<<" + prenomes);
    }

    public String getMrzLinha2() {
        String codigo = id == null ? "000000000" : id.substring(Math.max(0, id.length() - 9));
        String data = criadoEm == null ? "000000"
                : DateTimeFormatter.ofPattern("yyMMdd").withZone(ZoneId.of("America/Sao_Paulo")).format(criadoEm);
        return completar(codigo + "<" + perfil.getCodigo() + data + (ativo ? "A" : "I"));
    }

    private static String semAcento(String texto) {
        String base = texto == null ? "" : texto.trim();
        return Normalizer.normalize(base, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z ]", "");
    }

    private static String completar(String linha) {
        String limpa = linha.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9<]", "<");
        if (limpa.length() >= TAMANHO_MRZ) {
            return limpa.substring(0, TAMANHO_MRZ);
        }
        return limpa + "<".repeat(TAMANHO_MRZ - limpa.length());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public int getTentativasFalhas() {
        return tentativasFalhas;
    }

    public void setTentativasFalhas(int tentativasFalhas) {
        this.tentativasFalhas = tentativasFalhas;
    }

    public Instant getBloqueadoAte() {
        return bloqueadoAte;
    }

    public void setBloqueadoAte(Instant bloqueadoAte) {
        this.bloqueadoAte = bloqueadoAte;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public Instant getUltimoAcesso() {
        return ultimoAcesso;
    }

    public void setUltimoAcesso(Instant ultimoAcesso) {
        this.ultimoAcesso = ultimoAcesso;
    }
}
