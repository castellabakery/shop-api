package kr.co.medicals.common.config.security.jwt;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefreshToken {
    private long idx;
    private String id;
    private String accessToken;
    private String refreshToken;
    private String refreshTokenExpirationAt;
}
