package com.joaohen.login_seguro.seguranca;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("registros_acesso")
public class RegistroAcesso {

    @Id
    private String id;

    @Indexed
    private String email;

    private TipoEvento evento;

    private String ip;

    private String navegador;

    private Instant data;

    public RegistroAcesso() {
    }

    public static RegistroAcesso de(String email, TipoEvento evento, String ip, String navegador) {
        RegistroAcesso registro = new RegistroAcesso();
        registro.email = email;
        registro.evento = evento;
        registro.ip = ip;
        registro.navegador = navegador;
        registro.data = Instant.now();
        return registro;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public TipoEvento getEvento() {
        return evento;
    }

    public String getIp() {
        return ip;
    }

    public String getNavegador() {
        return navegador;
    }

    public Instant getData() {
        return data;
    }
}
