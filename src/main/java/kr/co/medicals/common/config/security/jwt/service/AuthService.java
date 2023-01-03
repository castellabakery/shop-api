package kr.co.medicals.common.config.security.jwt.service;

import kr.co.medicals.common.config.security.jwt.JwtProvider;
import kr.co.medicals.common.config.security.jwt.RefreshToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AuthService {
    private final JwtProvider jwtProvider;

    public AuthService(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    // 토큰을 생성해서 반환
    public Map<String, Object> getAccessToken(String userId) {

        Map<String, String> accessTokenMap = jwtProvider.createAccessToken(userId);
        String accessToken = accessTokenMap.get("jwt");
        String refreshToken = accessTokenMap.get("refreshToken");
        String refreshTokenExpirationAt = accessTokenMap.get("refreshTokenExpirationAt");

        RefreshToken insertRefreshToken = RefreshToken.builder()
                .id(userId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .refreshTokenExpirationAt(refreshTokenExpirationAt)
                .build();

        Map result = new HashMap();
        result.put("accessToken", accessToken);
        result.put("refreshIdx", insertRefreshToken.getIdx());
        result.put("userCode", userId);
        result.put("entity", userId);

        return result;
    }
}
