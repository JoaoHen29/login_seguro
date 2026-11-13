package com.joaohen.login_seguro.chamado;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class GeradorChamadosExemplo implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(GeradorChamadosExemplo.class);
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    private static final List<String> TECNICOS = List.of(
            "Ana Ribeiro", "Bruno Carvalho", "Carla Nogueira", "Diego Martins", "Elisa Prado");

    private static final List<String> SETORES = List.of(
            "Financeiro", "Comercial", "Operacional", "Recursos Humanos", "Logística", "Diretoria");

    private static final List<String> NOMES = List.of(
            "Lucas", "Mariana", "Pedro", "Juliana", "Rafael", "Fernanda",
            "Gustavo", "Camila", "Thiago", "Beatriz", "André", "Larissa");

    private static final List<String> SOBRENOMES = List.of(
            "Silva", "Santos", "Oliveira", "Souza", "Costa", "Pereira", "Almeida", "Ferreira", "Gomes", "Barbosa");

    private static final TipoChamado[] TIPOS = TipoChamado.values();
    private static final int[] PESOS_TIPO = {15, 25, 15, 20, 10, 10, 5};

    private static final Prioridade[] PRIORIDADES = Prioridade.values();
    private static final int[] PESOS_PRIORIDADE = {30, 40, 20, 10};

    private static final StatusChamado[] STATUS_ANTIGOS = {
            StatusChamado.RESOLVIDO, StatusChamado.CANCELADO,
            StatusChamado.AGUARDANDO_USUARIO, StatusChamado.EM_ATENDIMENTO};
    private static final int[] PESOS_ANTIGOS = {88, 7, 3, 2};

    private static final StatusChamado[] STATUS_RECENTES = {
            StatusChamado.ABERTO, StatusChamado.EM_ATENDIMENTO, StatusChamado.AGUARDANDO_USUARIO,
            StatusChamado.RESOLVIDO, StatusChamado.CANCELADO};
    private static final int[] PESOS_RECENTES = {30, 30, 15, 22, 3};

    private static final Map<TipoChamado, List<String>> TITULOS = Map.of(
            TipoChamado.HARDWARE, List.of("Computador não liga", "Monitor com imagem piscando",
                    "Teclado com teclas falhando", "Notebook superaquecendo"),
            TipoChamado.SOFTWARE, List.of("Erro ao abrir o sistema ERP", "Excel travando ao salvar",
                    "Instalação de programa solicitada", "Atualização do Windows com falha"),
            TipoChamado.REDE, List.of("Sem acesso à internet", "Wi-Fi caindo com frequência",
                    "VPN não conecta", "Lentidão na rede interna"),
            TipoChamado.ACESSO, List.of("Senha bloqueada", "Acesso a pasta compartilhada",
                    "Criação de usuário para novo colaborador", "Erro de permissão no sistema"),
            TipoChamado.IMPRESSORA, List.of("Impressora não imprime", "Papel atolando na impressora",
                    "Impressora fora da rede", "Digitalização não chega no e-mail"),
            TipoChamado.EMAIL, List.of("E-mails não chegam", "Caixa de entrada cheia",
                    "Outlook pedindo senha repetidamente", "Configuração de assinatura de e-mail"),
            TipoChamado.OUTROS, List.of("Dúvida sobre uso do sistema", "Mudança de estação de trabalho",
                    "Solicitação de novo equipamento", "Ramal telefônico sem sinal"));

    private static final Map<TipoChamado, List<String>> SOLUCOES = Map.of(
            TipoChamado.HARDWARE, List.of("Fonte substituída e equipamento testado.", "Cabo de vídeo trocado.",
                    "Limpeza interna e troca da pasta térmica.", "Teclado substituído por um novo."),
            TipoChamado.SOFTWARE, List.of("Cache limpo e aplicação reinstalada.", "Atualização aplicada e computador reiniciado.",
                    "Programa instalado e licença ativada.", "Arquivo recuperado da versão anterior."),
            TipoChamado.REDE, List.of("Cabo de rede substituído.", "Roteador do setor reiniciado e configurado.",
                    "Perfil da VPN recriado.", "Switch do andar reconfigurado."),
            TipoChamado.ACESSO, List.of("Senha redefinida e usuário orientado.", "Permissão concedida após aprovação do gestor.",
                    "Usuário criado no domínio.", "Grupo de acesso corrigido."),
            TipoChamado.IMPRESSORA, List.of("Driver reinstalado.", "Rolete de tração limpo.",
                    "IP da impressora fixado na rede.", "Configuração de envio por e-mail corrigida."),
            TipoChamado.EMAIL, List.of("Regra de spam ajustada.", "Mensagens antigas movidas para o arquivo morto.",
                    "Credenciais salvas novamente.", "Assinatura configurada no Outlook."),
            TipoChamado.OUTROS, List.of("Usuário orientado sobre o procedimento.", "Equipamentos transferidos e testados.",
                    "Pedido encaminhado para compras.", "Ramal reconfigurado na central."));

    private final ChamadoRepository repositorio;
    private final boolean gerar;
    private final int quantidade;
    private final Random aleatorio = new Random(2026);

    public GeradorChamadosExemplo(ChamadoRepository repositorio,
                                  @Value("${app.chamados.gerar-exemplos:true}") boolean gerar,
                                  @Value("${app.chamados.quantidade-exemplos:400}") int quantidade) {
        this.repositorio = repositorio;
        this.gerar = gerar;
        this.quantidade = quantidade;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!gerar || repositorio.count() > 0) {
            return;
        }
        Instant agora = Instant.now();
        List<Chamado> chamados = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            chamados.add(novoChamado(agora));
        }
        chamados.sort(Comparator.comparing(Chamado::getAbertoEm));
        for (int i = 0; i < chamados.size(); i++) {
            chamados.get(i).setProtocolo(String.format("TD-%06d", i + 1));
        }
        repositorio.saveAll(chamados);
        log.info("{} chamados de exemplo criados.", chamados.size());
    }

    private Chamado novoChamado(Instant agora) {
        TipoChamado tipo = sortear(TIPOS, PESOS_TIPO);
        Prioridade prioridade = sortear(PRIORIDADES, PESOS_PRIORIDADE);
        String setor = escolher(SETORES);
        String solicitante = escolher(NOMES) + " " + escolher(SOBRENOMES);
        String titulo = escolher(TITULOS.get(tipo));
        Instant abertura = dataAbertura(agora);

        Chamado chamado = new Chamado();
        chamado.setTipo(tipo);
        chamado.setPrioridade(prioridade);
        chamado.setTitulo(titulo);
        chamado.setDescricao("Solicitação do setor " + setor + ": " + titulo.toLowerCase() + ".");
        chamado.setSolicitante(solicitante);
        chamado.setSetor(setor);
        chamado.setAbertoEm(abertura);
        chamado.registrar(acompanhamento(abertura, solicitante, null, StatusChamado.ABERTO, "Chamado registrado."));

        long idadeHoras = Duration.between(abertura, agora).toHours();
        StatusChamado status = idadeHoras > 240
                ? sortear(STATUS_ANTIGOS, PESOS_ANTIGOS)
                : sortear(STATUS_RECENTES, PESOS_RECENTES);

        if (status != StatusChamado.ABERTO) {
            status = atender(chamado, status, agora);
        }
        chamado.setStatus(status);
        return chamado;
    }

    private StatusChamado atender(Chamado chamado, StatusChamado status, Instant agora) {
        String tecnico = escolher(TECNICOS);
        Prioridade prioridade = chamado.getPrioridade();
        Instant abertura = chamado.getAbertoEm();

        Instant inicio = antesDe(agora, abertura.plus(Duration.ofMinutes(10 + aleatorio.nextInt(prioridade.getPrazoHoras() * 15))));
        chamado.setTecnicoResponsavel(tecnico);
        chamado.setInicioAtendimentoEm(inicio);
        chamado.registrar(acompanhamento(inicio, tecnico, StatusChamado.ABERTO, StatusChamado.EM_ATENDIMENTO, "Atendimento iniciado."));

        if (status == StatusChamado.AGUARDANDO_USUARIO) {
            Instant pedido = antesDe(agora, inicio.plus(Duration.ofMinutes(30)));
            chamado.registrar(acompanhamento(pedido, tecnico, StatusChamado.EM_ATENDIMENTO, status,
                    "Aguardando retorno do usuário com mais informações."));
            return status;
        }
        if (!status.isEncerrado()) {
            return status;
        }

        double fator = 0.2 + aleatorio.nextDouble() * 1.5;
        Instant fim = abertura.plus(Duration.ofMinutes((long) (prioridade.getPrazoHoras() * 60 * fator)));
        if (!fim.isAfter(inicio)) {
            fim = inicio.plus(Duration.ofMinutes(15));
        }
        if (fim.isAfter(agora)) {
            return StatusChamado.EM_ATENDIMENTO;
        }
        String consideracao = status == StatusChamado.RESOLVIDO
                ? escolher(SOLUCOES.get(chamado.getTipo()))
                : "Chamado cancelado a pedido do solicitante.";
        chamado.setEncerradoEm(fim);
        chamado.registrar(acompanhamento(fim, tecnico, StatusChamado.EM_ATENDIMENTO, status, consideracao));
        return status;
    }

    private Instant dataAbertura(Instant agora) {
        LocalDate dia = LocalDate.ofInstant(agora, FUSO).minusDays(aleatorio.nextInt(180));
        if (dia.getDayOfWeek() == DayOfWeek.SATURDAY || dia.getDayOfWeek() == DayOfWeek.SUNDAY) {
            dia = dia.minusDays(2);
        }
        LocalTime hora = LocalTime.of(8 + aleatorio.nextInt(10), aleatorio.nextInt(60));
        Instant abertura = dia.atTime(hora).atZone(FUSO).toInstant();
        return antesDe(agora.minus(Duration.ofMinutes(30)), abertura);
    }

    private Instant antesDe(Instant limite, Instant data) {
        return data.isAfter(limite) ? limite : data;
    }

    private Acompanhamento acompanhamento(Instant data, String autor, StatusChamado anterior,
                                          StatusChamado novo, String consideracao) {
        Acompanhamento acompanhamento = new Acompanhamento(autor, anterior, novo, consideracao);
        acompanhamento.setData(data);
        return acompanhamento;
    }

    private <T> T escolher(List<T> opcoes) {
        return opcoes.get(aleatorio.nextInt(opcoes.size()));
    }

    private <T> T sortear(T[] opcoes, int[] pesos) {
        int total = 0;
        for (int peso : pesos) {
            total += peso;
        }
        int sorteio = aleatorio.nextInt(total);
        for (int i = 0; i < opcoes.length; i++) {
            sorteio -= pesos[i];
            if (sorteio < 0) {
                return opcoes[i];
            }
        }
        return opcoes[opcoes.length - 1];
    }
}