package com.joaohen.login_seguro.chamado;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ChamadoRepository extends MongoRepository<Chamado, String> {

    Optional<Chamado> findByProtocolo(String protocolo);

    Optional<Chamado> findTopByOrderByProtocoloDesc();
}