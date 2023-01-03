package kr.co.medicals.common.login.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping("/login")
public class LoginController {

    @ResponseBody
    @GetMapping
    public String adminLoginView() {
//        log.info("loginView 화면 호출");
        return "GO_LOGIN_PAGE";
    }

    @ResponseBody
    @GetMapping("/success")
    public String loginSuccess() {
        log.info("login success");
        return "LOGIN_SUCCESS";
    }

//    @RequestMapping("/pass")
//    public @ResponseBody
//    String loginPagePass(HttpSession session) {
//        log.info("login page pass 호출");
//
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        log.info("Authentication : " + authentication);
//        log.info("Session : " + session.getId());
//        log.info("Session : " + session.getAttribute("accessToken"));
//        log.info("Session : " + session.getAttribute("refreshIdx"));
//
//        String userId = authentication.getPrincipal().toString();
//        log.info("userId : " + userId);
//
//        return userId;
//    }

    @ResponseBody
    @GetMapping("/fail")
    public String failAuthenticationView() {
        log.info("로그인 실패.");
        return "GO_LOGIN_PAGE_BY_ERROR";
    }

    @ResponseBody
    @GetMapping("/denied")
    public String deniedAuthenticationView() {
        log.info("권한 없음.");
        return "GO_LOGIN_PAGE_BY_DENIED";
    }

}
