package com.joaohen.login_seguro.usuario;

import com.joaohen.login_seguro.comum.RegraDeNegocioException;

public class EmailJaCadastradoException extends RegraDeNegocioException {

    public EmailJaCadastradoException() {
        super("Já existe uma conta com este e-mail.");
    }
}
