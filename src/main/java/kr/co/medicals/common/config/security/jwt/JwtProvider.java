package kr.co.medicals.common.config.security.jwt;

import io.jsonwebtoken.*;
import kr.co.medicals.common.login.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
public class JwtProvider {
    private final String secretKey = "secretkey";
    private long accessExpireTime = (60 * 60 * 1000L) * 3; // 3시간 후
    private final long refreshExpireTime = 60 * 60 * 1000L;   // 2분
    private final LoginService loginService;

    public JwtProvider(LoginService loginService) {
        this.loginService = loginService;
    }

    public Map<String, String> createAccessToken(String userId) {

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "token");

        Map<String, Object> payloads = new HashMap<>();
        payloads.put("id", userId);

        Date expirationJwt = new Date();
        expirationJwt.setTime(expirationJwt.getTime() + accessExpireTime);

        String jwt = Jwts
                .builder()
                .setHeader(headers)
                .setClaims(payloads)
                .setSubject("user")
                .setExpiration(expirationJwt)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();

        Date expirationRefreshToken = new Date();
        expirationRefreshToken.setTime(expirationRefreshToken.getTime() + refreshExpireTime);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        String refreshTokenExpirationAt = simpleDateFormat.format(expirationRefreshToken);

        String refreshToken = Jwts
                .builder()
                .setHeader(headers)
                .setClaims(payloads)
                .setSubject("user")
                .setExpiration(expirationRefreshToken)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();


        Map<String, String> result = new HashMap<>();
        result.put("jwt", jwt);
        result.put("refreshToken", refreshToken);
        result.put("refreshTokenExpirationAt", refreshTokenExpirationAt);

        return result;
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = loginService.loadUserByUsername(this.getUserInfo(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUserInfo(String token) {
        return (String) Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().get("id");
    }

    public String resolveToken(HttpServletRequest request) {
        return request.getHeader("token");
    }

    public boolean validateJwtToken(ServletRequest request, String authToken) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            request.setAttribute("exception", "MalformedJwtException");
        } catch (ExpiredJwtException e) {
            request.setAttribute("exception", "ExpiredJwtException");
        } catch (UnsupportedJwtException e) {
            request.setAttribute("exception", "UnsupportedJwtException");
        } catch (IllegalArgumentException e) {
            request.setAttribute("exception", "IllegalArgumentException");
        }
        log.info("TEST FILTER - 1.1 " + request.getAttribute("exception"));
        return false;
    }
}
