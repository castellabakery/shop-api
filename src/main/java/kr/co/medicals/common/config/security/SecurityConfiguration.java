package kr.co.medicals.common.config.security;

import kr.co.medicals.common.config.security.jwt.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final CustomAuthenticationProvider customAuthenticationProvider;
    private final CustomWebAuthenticationDetailsSource customWebAuthenticationDetailsSource;
    private final JwtProvider jwtProvider;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    public SecurityConfiguration(CustomAuthenticationProvider customAuthenticationProvider, CustomWebAuthenticationDetailsSource customWebAuthenticationDetailsSource, JwtProvider jwtProvider, CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
        this.customAuthenticationProvider = customAuthenticationProvider;
        this.customWebAuthenticationDetailsSource = customWebAuthenticationDetailsSource;
        this.jwtProvider = jwtProvider;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
    }

    @Bean
    public WebSecurityCustomizer ignoringCustomizer() {
        return (web) -> web.ignoring().antMatchers("/css/**", "/js/**", "/images/**", "/health");
    }

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {

        http.sessionManagement()
                .maximumSessions(-1)// ?????? ?????? ?????? ?????? ???, -1??? ?????? ????????? ?????? ??????
                .maxSessionsPreventsLogin(false) // ?????? ????????? ??????, false??? ?????? ?????? ?????? ??????
                .expiredUrl("/login/**")
                .sessionRegistry(sessionRegistry()); // ?????? ???????????? ????????? Logout ??? ?????? Login ??? ??? "Maximum sessions of 1 for this principal exceeded" ????????? ??????????????? ????????? ?????? ????????????.

        http.authorizeRequests()
                .antMatchers("/login/**").authenticated()
                .antMatchers("/checkplus/**", "/buyer/tmp/add", "/buyer/order/return", "/buyer/exists/buyer-identification-id", "/file/aws/**", "/buyer/exists/email", "/buyer/terms/detail").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/buyer/**").hasRole("USER")
                .anyRequest().authenticated(); // ??? ?????? URL??? ???????????? ???????????? ??????

        http.formLogin().loginPage("/login") // ????????? ????????? ????????? URL??? ?????????. ????????? ????????? URL ?????? ???????????? ???????????? ???????????? ?????? ???.
                .loginProcessingUrl("/login-process") // ????????? ????????? ????????? ????????? URL
                .usernameParameter("username")
                .passwordParameter("password")
                .authenticationDetailsSource(customWebAuthenticationDetailsSource)
                .defaultSuccessUrl("/login/success") // ????????? ?????? ??? ????????? URL
                .successHandler(customAuthenticationSuccessHandler)
                .failureUrl("/login/fail").permitAll(); // ????????? ?????? ??? ?????? ??? URL. permitAll() ???????????? ???????????? ??????.

        http.logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login/success")
                .invalidateHttpSession(true);

        http.authenticationProvider(customAuthenticationProvider);

//        http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
        http.csrf().disable();

        http.httpBasic().disable().cors().configurationSource(corsConfigurationSource());

//        http.addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
}