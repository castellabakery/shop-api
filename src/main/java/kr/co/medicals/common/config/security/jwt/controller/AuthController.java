package kr.co.medicals.common.config.security.jwt.controller;

import kr.co.medicals.common.config.security.jwt.JwtProvider;
import kr.co.medicals.common.config.security.jwt.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final JwtProvider jwtProvider;
    private final AuthService authService;

    public AuthController(JwtProvider jwtProvider, AuthService authService) {
        this.jwtProvider = jwtProvider;
        this.authService = authService;
    }

//    @PostMapping("/login")
//    public ApiResponse login(@RequestBody MemberDto memberDto) throws Exception {
//        return authService.login(memberDto);
//    }

    @ResponseBody
    @RequestMapping("/token")
    public String validateToken(HttpServletRequest request, @RequestParam String token) {
        String result = "NoToken";
        if (token != null && jwtProvider.validateJwtToken(request, token)) {
            return token;
        }
        if (request.getAttribute("exception") != null && !"".equals(request.getAttribute("exception"))) {
            result = (String) request.getAttribute("exception");
        }
        log.info("Validate Token result - [{}]", result);
        return result;
    }

    @ResponseBody
    @RequestMapping("/get/token")
    public String getToken(Authentication authentication) {
        return (String)authService.getAccessToken((String)authentication.getPrincipal()).get("accessToken");
    }

//    @PostMapping("/refreshToken")
//    public ApiResponse newAccessToken(@RequestBody MemberDto memberDto, HttpServletRequest request) {
//        return authService.newAccessToken(memberDto, request);
//    }
}
