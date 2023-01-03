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
                .maximumSessions(-1)// 최대 허용 가능 세션 수, -1인 경우 무제한 세션 허용
                .maxSessionsPreventsLogin(false) // 동시 로그인 차단, false인 경우 기존 세션 만료
                .expiredUrl("/login/**")
                .sessionRegistry(sessionRegistry()); // 이걸 붙여주지 않으면 Logout 후 다시 Login 할 때 "Maximum sessions of 1 for this principal exceeded" 에러를 발생시키며 로그인 되지 않습니다.

        http.authorizeRequests()
                .antMatchers("/login/**").authenticated()
                .antMatchers("/checkplus/**", "/buyer/tmp/add", "/buyer/order/return", "/buyer/exists/buyer-identification-id", "/file/aws/**", "/buyer/exists/email", "/buyer/terms/detail").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/buyer/**").hasRole("USER")
                .anyRequest().authenticated(); // 그 외의 URL은 허용하지 않겠다는 의미

        http.formLogin().loginPage("/login") // 로그인 시도할 페이지 URL을 적는다. 여기에 페이지 URL 지정 안해주면 시큐리티 기본제공 창이 뜸.
                .loginProcessingUrl("/login-process") // 사용자 이름과 암호를 제출할 URL
                .usernameParameter("username")
                .passwordParameter("password")
                .authenticationDetailsSource(customWebAuthenticationDetailsSource)
                .defaultSuccessUrl("/login/success") // 로그인 성공 후 이동할 URL
                .successHandler(customAuthenticationSuccessHandler)
                .failureUrl("/login/fail").permitAll(); // 로그인 실패 후 이동 할 URL. permitAll() 인해주면 권한문제 발생.

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