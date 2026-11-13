(function () {
    'use strict';

    const painel = document.getElementById('painel');
    const API = painel.dataset.api || '/api';
    const LOGIN = painel.dataset.login || '/login';
    const formulario = document.getElementById('filtros');
    const campos = formulario.elements;
    const dialogo = document.getElementById('detalhe-chamado');

    const INTERVALO_ATUALIZACAO = 60000;
    const TAMANHO_PAGINA = 10;
    const FUSO = 'America/Sao_Paulo';
    const SEM_ANIMACAO = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const FILTROS = ['inicio', 'fim', 'status', 'tipo', 'prioridade', 'tecnico'];

    const CORES_STATUS = {
        ABERTO: '--status-aberto',
        EM_ATENDIMENTO: '--status-atendimento',
        AGUARDANDO_USUARIO: '--status-aguardando',
        RESOLVIDO: '--status-resolvido',
        CANCELADO: '--status-cancelado'
    };

    const inteiro = new Intl.NumberFormat('pt-BR');
    const decimal = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 1 });
    const dataHora = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short', timeZone: FUSO });
    const hora = new Intl.DateTimeFormat('pt-BR', { timeStyle: 'medium', timeZone: FUSO });

    const graficos = {};
    let descricoesStatus = {};
    let descricoesAgrupamento = {};
    let pagina = 0;
    let temporizador = null;
    let carregando = false;

    function cor(variavel) {
        return getComputedStyle(document.documentElement).getPropertyValue(variavel).trim();
    }

    function elemento(tag, classe, texto) {
        const novo = document.createElement(tag);
        if (classe) {
            novo.className = classe;
        }
        if (texto !== undefined && texto !== null) {
            novo.textContent = texto;
        }
        return novo;
    }

    function dataIso(data) {
        const mes = String(data.getMonth() + 1).padStart(2, '0');
        const dia = String(data.getDate()).padStart(2, '0');
        return `${data.getFullYear()}-${mes}-${dia}`;
    }

    function diasAtras(dias) {
        const data = new Date();
        data.setDate(data.getDate() - (dias - 1));
        return dataIso(data);
    }

    function formatarHoras(horas) {
        if (horas === null || horas === undefined) {
            return '–';
        }
        if (horas < 1) {
            return `${Math.round(horas * 60)} min`;
        }
        if (horas < 48) {
            return `${decimal.format(horas)} h`;
        }
        return `${decimal.format(horas / 24)} dias`;
    }

    function tempoDesde(instante) {
        const horas = (Date.now() - new Date(instante).getTime()) / 3600000;
        return formatarHoras(Math.max(horas, 0));
    }

    function formatarData(instante) {
        return instante ? dataHora.format(new Date(instante)) : null;
    }

    function parametros(extras) {
        const resultado = new URLSearchParams();
        FILTROS.forEach((nome) => {
            const valor = campos[nome].value;
            if (valor) {
                resultado.set(nome, valor);
            }
        });
        Object.entries(extras || {}).forEach(([chave, valor]) => resultado.set(chave, valor));
        return resultado;
    }

    async function buscar(caminho, params) {
        const consulta = params ? params.toString() : '';
        const resposta = await fetch(API + caminho + (consulta ? `?${consulta}` : ''), {
            headers: { Accept: 'application/json' }
        });
        let corpo = null;
        try {
            corpo = await resposta.json();
        } catch (erro) {
            corpo = null;
        }
        if (!resposta.ok) {
            const falha = new Error(corpo && corpo.mensagem ? corpo.mensagem : `Erro ${resposta.status} ao consultar a API.`);
            falha.status = resposta.status;
            throw falha;
        }
        return corpo;
    }

    function mostrarErro(erro) {
        const caixa = document.getElementById('erro-dashboard');
        caixa.replaceChildren();
        if (erro.status === 401) {
            caixa.append('Sua sessão expirou. ');
            const link = elemento('a', null, 'Entrar novamente');
            link.href = LOGIN;
            caixa.append(link);
        } else if (erro instanceof TypeError) {
            caixa.textContent = 'Não foi possível falar com o servidor. Confira se a aplicação está em execução.';
        } else {
            caixa.textContent = erro.message;
        }
        caixa.hidden = false;
    }

    function esconderErro() {
        document.getElementById('erro-dashboard').hidden = true;
    }

    function opcoesBase() {
        return {
            responsive: true,
            maintainAspectRatio: false,
            animation: SEM_ANIMACAO ? false : { duration: 350 },
            plugins: {
                legend: { labels: { color: cor('--tinta'), boxWidth: 12, boxHeight: 12 } },
                tooltip: {
                    backgroundColor: cor('--tinta'),
                    titleColor: cor('--papel'),
                    bodyColor: cor('--papel'),
                    padding: 10
                }
            }
        };
    }

    function eixos(horizontal) {
        const marcas = { color: cor('--tinta-suave') };
        const valor = { beginAtZero: true, grid: { color: cor('--linha') }, ticks: { color: marcas.color, precision: 0 } };
        const categoria = { grid: { display: false }, ticks: marcas };
        return horizontal ? { x: valor, y: categoria } : { x: categoria, y: valor };
    }

    function desenharGrafico(id, configuracao) {
        if (graficos[id]) {
            graficos[id].data = configuracao.data;
            graficos[id].options = configuracao.options;
            graficos[id].update();
            return;
        }
        graficos[id] = new Chart(document.getElementById(id), configuracao);
    }

    function preencher(chave, valor, nota) {
        document.querySelector(`[data-valor="${chave}"]`).textContent = valor;
        document.querySelector(`[data-nota="${chave}"]`).textContent = nota;
    }

    function destacar(chave, ligado) {
        document.querySelector(`[data-indicador="${chave}"]`).classList.toggle('indicador--alerta', ligado);
    }

    function desenharIndicadores(dados) {
        preencher('total', inteiro.format(dados.totalChamados), `${inteiro.format(dados.cancelados)} cancelados`);
        preencher('pendentes', inteiro.format(dados.pendentes),
            dados.criticosPendentes === 1 ? '1 crítico aguardando' : `${inteiro.format(dados.criticosPendentes)} críticos aguardando`);
        preencher('resolucao', `${decimal.format(dados.taxaResolucao)}%`, `${inteiro.format(dados.resolvidos)} resolvidos`);
        preencher('prazo', `${decimal.format(dados.percentualForaDoPrazo)}%`, `${inteiro.format(dados.foraDoPrazo)} passaram do prazo`);
        preencher('primeiro', formatarHoras(dados.tempoMedioPrimeiroAtendimentoHoras), 'média da abertura ao início');
        preencher('resolucaoMedia', formatarHoras(dados.tempoMedioResolucaoHoras), 'média da abertura à solução');
        destacar('prazo', dados.percentualForaDoPrazo > 25);
        destacar('pendentes', dados.criticosPendentes > 0);
        document.getElementById('sem-resultados').hidden = dados.totalChamados > 0;
    }

    function desenharPeriodo(serie) {
        const agrupamento = (descricoesAgrupamento[serie.agrupamento] || '').toLowerCase();
        document.getElementById('resumo-periodo').textContent =
            `${inteiro.format(serie.totalAbertos)} abertos e ${inteiro.format(serie.totalResolvidos)} resolvidos, ${agrupamento}`;
        desenharGrafico('grafico-periodo', {
            type: 'bar',
            data: {
                labels: serie.pontos.map((ponto) => ponto.rotulo),
                datasets: [
                    {
                        type: 'bar',
                        label: 'Abertos',
                        data: serie.pontos.map((ponto) => ponto.abertos),
                        backgroundColor: cor('--grafico-barra'),
                        borderRadius: 3,
                        order: 2
                    },
                    {
                        type: 'line',
                        label: 'Resolvidos',
                        data: serie.pontos.map((ponto) => ponto.resolvidos),
                        borderColor: cor('--status-resolvido'),
                        backgroundColor: cor('--status-resolvido'),
                        borderWidth: 2,
                        pointRadius: 2,
                        tension: 0.3,
                        order: 1
                    }
                ]
            },
            options: { ...opcoesBase(), interaction: { mode: 'index', intersect: false }, scales: eixos(false) }
        });
    }

    function botaoProtocolo(protocolo) {
        const botao = elemento('button', 'link-protocolo', protocolo);
        botao.type = 'button';
        botao.setAttribute('aria-label', `Ver detalhes do chamado ${protocolo}`);
        botao.addEventListener('click', () => abrirDetalhe(protocolo));
        return botao;
    }

    function desenharSituacao(situacao) {
        const itens = situacao.porStatus;
        document.getElementById('total-situacao').textContent = inteiro.format(situacao.total);
        const base = opcoesBase();
        desenharGrafico('grafico-situacao', {
            type: 'doughnut',
            data: {
                labels: itens.map((item) => item.descricao),
                datasets: [{
                    data: itens.map((item) => item.total),
                    backgroundColor: itens.map((item) => cor(CORES_STATUS[item.status])),
                    borderColor: cor('--papel-campo'),
                    borderWidth: 2
                }]
            },
            options: { ...base, cutout: '64%', plugins: { ...base.plugins, legend: { display: false } } }
        });

        const legenda = document.getElementById('legenda-situacao');
        legenda.replaceChildren();
        itens.forEach((item) => {
            const linha = elemento('li');
            linha.append(
                elemento('span', `marcador marcador--${item.status}`),
                elemento('span', null, item.descricao),
                elemento('span', 'legenda__total', inteiro.format(item.total)),
                elemento('span', 'legenda__percentual', `${decimal.format(item.percentual)}%`)
            );
            legenda.append(linha);
        });

        const fila = document.getElementById('lista-antigos');
        fila.replaceChildren();
        if (situacao.pendentesMaisAntigos.length === 0) {
            fila.append(elemento('li', 'fila__vazia', 'Nenhum chamado pendente com esses filtros.'));
            return;
        }
        situacao.pendentesMaisAntigos.forEach((chamado) => {
            const linha = elemento('li');
            const info = elemento('div', 'fila__info');
            info.append(
                elemento('span', 'fila__titulo', chamado.titulo),
                elemento('span', 'fila__meta',
                    `${chamado.tipoDescricao}, prioridade ${chamado.prioridadeDescricao.toLowerCase()}, ${chamado.statusDescricao.toLowerCase()}`)
            );
            const classeTempo = chamado.prazoEstourado ? 'fila__tempo fila__tempo--estourado' : 'fila__tempo';
            linha.append(botaoProtocolo(chamado.protocolo), info, elemento('span', classeTempo, `aberto há ${tempoDesde(chamado.abertoEm)}`));
            fila.append(linha);
        });
    }

    function desenharTipos(tipos) {
        desenharGrafico('grafico-tipos', {
            type: 'bar',
            data: {
                labels: tipos.map((tipo) => tipo.descricao),
                datasets: [
                    { label: 'Total', data: tipos.map((tipo) => tipo.total), backgroundColor: cor('--grafico-barra'), borderRadius: 3 },
                    { label: 'Resolvidos', data: tipos.map((tipo) => tipo.resolvidos), backgroundColor: cor('--status-resolvido'), borderRadius: 3 }
                ]
            },
            options: { ...opcoesBase(), indexAxis: 'y', scales: eixos(true) }
        });

        const corpo = document.getElementById('tabela-tipos');
        corpo.replaceChildren();
        tipos.forEach((tipo) => {
            const linha = elemento('tr');
            linha.append(
                elemento('td', null, tipo.descricao),
                elemento('td', 'numero', `${decimal.format(tipo.percentual)}%`),
                elemento('td', 'numero', formatarHoras(tipo.tempoMedioResolucaoHoras))
            );
            corpo.append(linha);
        });
    }

    async function carregarChamados() {
        const resultado = await buscar('/chamados', parametros({ pagina, tamanho: TAMANHO_PAGINA }));
        if (resultado.totalPaginas > 0 && pagina >= resultado.totalPaginas) {
            pagina = resultado.totalPaginas - 1;
            return carregarChamados();
        }

        const corpo = document.getElementById('tabela-chamados');
        corpo.replaceChildren();
        if (resultado.itens.length === 0) {
            const linha = elemento('tr');
            const celula = elemento('td', 'tabela__vazia', 'Nenhum chamado com esses filtros.');
            celula.colSpan = 8;
            linha.append(celula);
            corpo.append(linha);
        }
        resultado.itens.forEach((chamado) => {
            const linha = elemento('tr');
            const protocolo = elemento('td');
            protocolo.append(botaoProtocolo(chamado.protocolo));
            const status = elemento('td');
            status.append(elemento('span', `etiqueta etiqueta--${chamado.status}`, chamado.statusDescricao));
            const cancelado = chamado.status === 'CANCELADO';
            const prazo = cancelado
                ? elemento('td', null, '–')
                : elemento('td', chamado.prazoEstourado ? 'prazo--estourado' : 'prazo--ok',
                    chamado.prazoEstourado ? 'Estourado' : 'No prazo');
            linha.append(
                protocolo,
                elemento('td', null, chamado.titulo),
                elemento('td', null, chamado.tipoDescricao),
                elemento('td', `prioridade--${chamado.prioridade}`, chamado.prioridadeDescricao),
                status,
                elemento('td', null, chamado.tecnicoResponsavel || 'Sem técnico'),
                elemento('td', null, formatarData(chamado.abertoEm)),
                prazo
            );
            corpo.append(linha);
        });

        const totalPaginas = Math.max(resultado.totalPaginas, 1);
        document.getElementById('resumo-chamados').textContent =
            `${inteiro.format(resultado.totalItens)} chamados, dos mais recentes para os mais antigos`;
        document.getElementById('pagina-atual').textContent = `Página ${resultado.pagina + 1} de ${totalPaginas}`;
        document.getElementById('pagina-anterior').disabled = resultado.pagina === 0;
        document.getElementById('pagina-proxima').disabled = resultado.pagina + 1 >= totalPaginas;
    }

    async function trocarPagina(passo) {
        pagina = Math.max(pagina + passo, 0);
        try {
            await carregarChamados();
        } catch (erro) {
            mostrarErro(erro);
        }
    }

    async function abrirDetalhe(protocolo) {
        try {
            const detalhe = await buscar(`/chamados/${encodeURIComponent(protocolo)}`);
            const chamado = detalhe.chamado;
            document.getElementById('detalhe-protocolo').textContent = chamado.protocolo;
            document.getElementById('detalhe-titulo').textContent = chamado.titulo;
            document.getElementById('detalhe-descricao').textContent = detalhe.descricao;
            const status = document.getElementById('detalhe-status');
            status.className = `etiqueta etiqueta--${chamado.status}`;
            status.textContent = chamado.statusDescricao;

            const dados = document.getElementById('detalhe-dados');
            dados.replaceChildren();
            [
                ['Tipo', chamado.tipoDescricao],
                ['Prioridade', `${chamado.prioridadeDescricao}, prazo de ${detalhe.prazoHoras} h`],
                ['Solicitante', `${chamado.solicitante}, ${chamado.setor}`],
                ['Técnico', chamado.tecnicoResponsavel || 'Sem técnico'],
                ['Aberto em', formatarData(chamado.abertoEm)],
                ['Início do atendimento', formatarData(detalhe.inicioAtendimentoEm) || 'Ainda não iniciado'],
                ['Encerrado em', formatarData(chamado.encerradoEm) || 'Em aberto'],
                ['Prazo', chamado.prazoEstourado ? 'Estourado' : 'Dentro do prazo']
            ].forEach(([rotulo, valor]) => {
                const item = elemento('div');
                item.append(elemento('dt', null, rotulo), elemento('dd', null, valor));
                dados.append(item);
            });

            const historico = document.getElementById('detalhe-historico');
            historico.replaceChildren();
            detalhe.historico.forEach((acompanhamento) => {
                const novo = descricoesStatus[acompanhamento.statusNovo] || acompanhamento.statusNovo;
                const anterior = descricoesStatus[acompanhamento.statusAnterior] || acompanhamento.statusAnterior;
                const transicao = acompanhamento.statusAnterior ? `${anterior} → ${novo}` : novo;
                const item = elemento('li');
                item.append(
                    elemento('span', 'historico__transicao', transicao),
                    elemento('span', null, acompanhamento.consideracao),
                    elemento('span', 'historico__meta', `${acompanhamento.autor}, ${formatarData(acompanhamento.data)}`)
                );
                historico.append(item);
            });
            dialogo.showModal();
        } catch (erro) {
            mostrarErro(erro);
        }
    }

    async function atualizar() {
        if (carregando) {
            return;
        }
        carregando = true;
        painel.setAttribute('aria-busy', 'true');
        esconderErro();
        try {
            const [indicadores, serie, situacao, tipos] = await Promise.all([
                buscar('/dashboard/indicadores', parametros()),
                buscar('/dashboard/atendimentos-por-periodo', parametros({ agrupamento: campos.agrupamento.value })),
                buscar('/dashboard/situacao', parametros()),
                buscar('/dashboard/tipos-mais-atendidos', parametros({ limite: 7 }))
            ]);
            desenharIndicadores(indicadores);
            desenharPeriodo(serie);
            desenharSituacao(situacao);
            desenharTipos(tipos);
            await carregarChamados();
            document.getElementById('ultima-atualizacao').textContent = `Atualizado às ${hora.format(new Date())}`;
        } catch (erro) {
            mostrarErro(erro);
        } finally {
            carregando = false;
            painel.removeAttribute('aria-busy');
        }
    }

    function marcarAtalho(dias) {
        document.querySelectorAll('.atalho').forEach((botao) => {
            botao.setAttribute('aria-pressed', String(Number(botao.dataset.dias) === dias));
        });
    }

    function aplicarPeriodo(dias) {
        campos.inicio.value = diasAtras(dias);
        campos.fim.value = dataIso(new Date());
        campos.agrupamento.value = dias <= 31 ? 'DIA' : 'SEMANA';
        marcarAtalho(dias);
    }

    function adicionarOpcoes(lista, itens) {
        itens.forEach((item) => {
            const opcao = elemento('option', null, item.descricao);
            opcao.value = item.codigo;
            lista.append(opcao);
        });
    }

    function preencherFiltros(opcoes) {
        adicionarOpcoes(campos.status, opcoes.status);
        adicionarOpcoes(campos.tipo, opcoes.tipos);
        adicionarOpcoes(campos.prioridade, opcoes.prioridades);
        adicionarOpcoes(campos.agrupamento, opcoes.agrupamentos);
        opcoes.tecnicos.forEach((nome) => {
            const opcao = elemento('option', null, nome);
            opcao.value = nome;
            campos.tecnico.append(opcao);
        });
        descricoesStatus = Object.fromEntries(opcoes.status.map((item) => [item.codigo, item.descricao]));
        descricoesAgrupamento = Object.fromEntries(opcoes.agrupamentos.map((item) => [item.codigo, item.descricao]));
        const hoje = dataIso(new Date());
        campos.inicio.max = hoje;
        campos.fim.max = hoje;
        if (opcoes.primeiraData) {
            campos.inicio.min = opcoes.primeiraData;
            campos.fim.min = opcoes.primeiraData;
        }
    }

    function configurarAutoAtualizacao() {
        clearInterval(temporizador);
        temporizador = null;
        if (document.getElementById('auto-atualizar').checked) {
            temporizador = setInterval(() => {
                if (!document.hidden) {
                    atualizar();
                }
            }, INTERVALO_ATUALIZACAO);
        }
    }

    function ligarEventos() {
        formulario.addEventListener('submit', (evento) => {
            evento.preventDefault();
            pagina = 0;
            atualizar();
        });
        document.querySelectorAll('.atalho').forEach((botao) => {
            botao.addEventListener('click', () => {
                aplicarPeriodo(Number(botao.dataset.dias));
                pagina = 0;
                atualizar();
            });
        });
        campos.inicio.addEventListener('input', () => marcarAtalho(null));
        campos.fim.addEventListener('input', () => marcarAtalho(null));
        document.getElementById('limpar-filtros').addEventListener('click', () => {
            formulario.reset();
            aplicarPeriodo(90);
            pagina = 0;
            atualizar();
        });
        document.getElementById('atualizar-agora').addEventListener('click', atualizar);
        document.getElementById('auto-atualizar').addEventListener('change', configurarAutoAtualizacao);
        document.getElementById('pagina-anterior').addEventListener('click', () => trocarPagina(-1));
        document.getElementById('pagina-proxima').addEventListener('click', () => trocarPagina(1));
        document.getElementById('fechar-detalhe').addEventListener('click', () => dialogo.close());
        dialogo.addEventListener('click', (evento) => {
            if (evento.target === dialogo) {
                dialogo.close();
            }
        });
        document.addEventListener('visibilitychange', () => {
            if (!document.hidden && document.getElementById('auto-atualizar').checked) {
                atualizar();
            }
        });
    }

    async function iniciar() {
        if (typeof Chart === 'undefined') {
            mostrarErro(new Error('Biblioteca de gráficos não encontrada. Confira o arquivo static/js/chart.umd.js.'));
            return;
        }
        Chart.defaults.font.family = '"Schibsted Grotesk", system-ui, sans-serif';
        Chart.defaults.color = cor('--tinta-suave');
        ligarEventos();
        try {
            preencherFiltros(await buscar('/dashboard/filtros'));
        } catch (erro) {
            mostrarErro(erro);
            return;
        }
        aplicarPeriodo(90);
        await atualizar();
        configurarAutoAtualizacao();
    }

    iniciar();
})();