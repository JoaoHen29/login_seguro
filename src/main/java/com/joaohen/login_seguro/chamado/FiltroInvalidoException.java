package com.joaohen.login_seguro.chamado;

import com.joaohen.login_seguro.comum.RegraDeNegocioException;

public class FiltroInvalidoException extends RegraDeNegocioException {

    public FiltroInvalidoException(String mensagem) {
        super(mensagem);
    }
}