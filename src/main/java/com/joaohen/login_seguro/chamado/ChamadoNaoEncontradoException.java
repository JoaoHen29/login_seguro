package com.joaohen.login_seguro.chamado;

import com.joaohen.login_seguro.comum.RegraDeNegocioException;

public class ChamadoNaoEncontradoException extends RegraDeNegocioException {

    public ChamadoNaoEncontradoException(String protocolo) {
        super("Chamado " + protocolo + " não encontrado.");
    }
}