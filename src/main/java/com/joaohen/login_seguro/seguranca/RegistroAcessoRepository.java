package com.joaohen.login_seguro.seguranca;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RegistroAcessoRepository extends MongoRepository<RegistroAcesso, String> {

    List<RegistroAcesso> findTop8ByEmailOrderByDataDesc(String email);

    List<RegistroAcesso> findTop30ByOrderByDataDesc();
}
