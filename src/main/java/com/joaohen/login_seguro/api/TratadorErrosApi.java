package com.joaohen.login_seguro.api;

import com.joaohen.login_seguro.chamado.ChamadoNaoEncontradoException;
import com.joaohen.login_seguro.chamado.FiltroInvalidoException;
import com.joaohen.login_seguro.comum.RegraDeNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(basePackageClasses = TratadorErrosApi.class)
public class TratadorErrosApi {

    private static final Logger log = LoggerFactory.getLogger(TratadorErrosApi.class);

    @ExceptionHandler(ChamadoNaoEncontradoException.class)
    public ResponseEntity<ErroApi> naoEncontrado(ChamadoNaoEncontradoException e, HttpServletRequest requisicao) {
        return resposta(404, "Not Found", e.getMessage(), requisicao, Map.of());
    }

    @ExceptionHandler(FiltroInvalidoException.class)
    public ResponseEntity<ErroApi> filtroInvalido(FiltroInvalidoException e, HttpServletRequest requisicao) {
        return resposta(400, "Bad Request", e.getMessage(), requisicao, Map.of());
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroApi> regraDeNegocio(RegraDeNegocioException e, HttpServletRequest requisicao) {
        return resposta(422, "Unprocessable Content", e.getMessage(), requisicao, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroApi> corpoInvalido(MethodArgumentNotValidException e, HttpServletRequest requisicao) {
        return camposInvalidos(e.getBindingResult(), requisicao);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErroApi> parametrosInvalidos(BindException e, HttpServletRequest requisicao) {
        return camposInvalidos(e.getBindingResult(), requisicao);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroApi> tipoInvalido(MethodArgumentTypeMismatchException e, HttpServletRequest requisicao) {
        String mensagem = "Valor inválido para o parâmetro '" + e.getName() + "': '" + e.getValue() + "'.";
        return resposta(400, "Bad Request", mensagem, requisicao, Map.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroApi> jsonInvalido(HttpMessageNotReadableException e, HttpServletRequest requisicao) {
        return resposta(400, "Bad Request",
                "Corpo da requisição inválido. Verifique o JSON e os valores dos campos.", requisicao, Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroApi> erroInesperado(Exception e, HttpServletRequest requisicao) {
        log.error("Erro inesperado em {}", requisicao.getRequestURI(), e);
        return resposta(500, "Internal Server Error",
                "Erro interno ao processar a requisição. Tente novamente em instantes.", requisicao, Map.of());
    }

    private ResponseEntity<ErroApi> camposInvalidos(BindingResult resultado, HttpServletRequest requisicao) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : resultado.getFieldErrors()) {
            String mensagem = erro.isBindingFailure()
                    ? "Valor inválido: '" + erro.getRejectedValue() + "'."
                    : erro.getDefaultMessage();
            campos.putIfAbsent(erro.getField(), mensagem);
        }
        return resposta(400, "Bad Request", "Há campos inválidos na requisição.", requisicao, campos);
    }

    private ResponseEntity<ErroApi> resposta(int status, String erro, String mensagem,
                                             HttpServletRequest requisicao, Map<String, String> campos) {
        ErroApi corpo = new ErroApi(Instant.now(), status, erro, mensagem, requisicao.getRequestURI(), campos);
        return ResponseEntity.status(status).body(corpo);
    }
}