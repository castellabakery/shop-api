package kr.co.medicals.common.config.security;

import kr.co.medicals.common.config.security.jwt.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    public CustomAuthenticationSuccessHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        AuthenticationSuccessHandler.super.onAuthenticationSuccess(request, response, chain, authentication);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String userType = ((CustomWebAuthenticationDetails) authentication.getDetails()).getUserType();
        String userId = (String) authentication.getPrincipal();
        String userCode = ((CustomWebAuthenticationDetails) authentication.getDetails()).getUserCode();
        String preUserCode = ((CustomWebAuthenticationDetails) authentication.getDetails()).getPreUserCode();

        HttpSession session = request.getSession();
        session.setAttribute("userId", userId);
        session.setAttribute("userType", userType);
        session.setAttribute("userCode", userCode);
        session.setAttribute("preUserCode", preUserCode);

        log.info("userId, userType, userCode, preUserCode : [{}], [{}], [{}], [{}]", userId, userType, userCode, preUserCode);
    }
}
