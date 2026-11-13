package com.joaohen.login_seguro.api;

import com.joaohen.login_seguro.chamado.FiltroInvalidoException;
import com.joaohen.login_seguro.chamado.TipoChamado;
import com.joaohen.login_seguro.dashboard.Agrupamento;
import com.joaohen.login_seguro.dashboard.DashboardRespostas.ContagemTipo;
import com.joaohen.login_seguro.dashboard.DashboardRespostas.Indicadores;
import com.joaohen.login_seguro.dashboard.DashboardRespostas.OpcoesFiltro;
import com.joaohen.login_seguro.dashboard.DashboardRespostas.SeriePeriodo;
import com.joaohen.login_seguro.dashboard.DashboardRespostas.SituacaoChamados;
import com.joaohen.login_seguro.dashboard.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    private final DashboardService dashboard;

    public DashboardApiController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/indicadores")
    public Indicadores indicadores(@ModelAttribute ParametrosFiltro parametros) {
        return dashboard.indicadores(parametros.paraFiltro());
    }

    @GetMapping("/atendimentos-por-periodo")
    public SeriePeriodo atendimentosPorPeriodo(@ModelAttribute ParametrosFiltro parametros,
                                               @RequestParam(name = "agrupamento", defaultValue = "DIA")
                                               Agrupamento agrupamento) {
        return dashboard.atendimentosPorPeriodo(parametros.paraFiltro(), agrupamento);
    }

    @GetMapping("/situacao")
    public SituacaoChamados situacao(@ModelAttribute ParametrosFiltro parametros) {
        return dashboard.situacao(parametros.paraFiltro());
    }

    @GetMapping("/tipos-mais-atendidos")
    public List<ContagemTipo> tiposMaisAtendidos(@ModelAttribute ParametrosFiltro parametros,
                                                 @RequestParam(name = "limite", defaultValue = "5") int limite) {
        int maximo = TipoChamado.values().length;
        if (limite < 1 || limite > maximo) {
            throw new FiltroInvalidoException("O limite deve estar entre 1 e " + maximo + ".");
        }
        return dashboard.tiposMaisAtendidos(parametros.paraFiltro(), limite);
    }

    @GetMapping("/filtros")
    public OpcoesFiltro filtros() {
        return dashboard.opcoes();
    }
}