package com.joaohen.login_seguro.config;

import com.joaohen.login_seguro.seguranca.LoginFalhaHandler;
import com.joaohen.login_seguro.seguranca.LoginSucessoHandler;
import com.joaohen.login_seguro.seguranca.LogoutRegistroHandler;
import com.joaohen.login_seguro.seguranca.RespostaSegurancaApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] ROTAS_PUBLICAS = {
            "/login", "/cadastro", "/css/**", "/js/**", "/temas/**", "/img/**", "/favicon.ico", "/error"
    };

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain filtroDaApi(HttpSecurity http, RespostaSegurancaApi respostas) throws Exception {
        http
                .securityMatcher("/api/**")
                .authorizeHttpRequests(rotas -> rotas
                        .anyRequest().hasAnyRole("ADMIN", "MODERADOR"))
                .httpBasic(basic -> basic.authenticationEntryPoint(respostas))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(respostas)
                        .accessDeniedHandler(respostas));
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filtroDeSeguranca(HttpSecurity http,
                                                 LoginSucessoHandler sucesso,
                                                 LoginFalhaHandler falha,
                                                 LogoutRegistroHandler registroLogout) throws Exception {
        http
                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers(ROTAS_PUBLICAS).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/moderacao/**", "/dashboard/**").hasAnyRole("ADMIN", "MODERADOR")
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("senha")
                        .successHandler(sucesso)
                        .failureHandler(falha)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .addLogoutHandler(registroLogout)
                        .logoutSuccessUrl("/login?saiu")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("SESSION", "JSESSIONID")
                        .permitAll())
                .sessionManagement(sessao -> sessao
                        .sessionFixation(fixacao -> fixacao.changeSessionId()))
                .exceptionHandling(erros -> erros.accessDeniedPage("/acesso-negado"))
                .headers(cabecalhos -> cabecalhos
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; style-src 'self' https://fonts.googleapis.com; "
                                        + "font-src https://fonts.gstatic.com; img-src 'self' data:; "
                                        + "form-action 'self'; frame-ancestors 'none'")));
        return http.build();
    }
}