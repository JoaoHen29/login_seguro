package com.joaohen.login_seguro.config;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component("datas")
public class Datas {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm").withZone(FUSO);
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(FUSO);

    public String dataHora(Instant instante) {
        return instante == null ? "Nunca" : DATA_HORA.format(instante);
    }

    public String data(Instant instante) {
        return instante == null ? "-" : DATA.format(instante);
    }
}
