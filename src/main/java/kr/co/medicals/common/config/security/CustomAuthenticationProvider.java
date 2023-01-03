package kr.co.medicals.common.config.security;

import kr.co.medicals.admin.main.dto.AdminDto;
import kr.co.medicals.buyer.domain.dto.BuyerIdentificationDto;
import kr.co.medicals.common.login.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final LoginService loginService;

    public CustomAuthenticationProvider(LoginService loginService) {
        this.loginService = loginService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userId = (String) authentication.getPrincipal();
        String userPassword = (String) authentication.getCredentials();
        String userType = ((CustomWebAuthenticationDetails) authentication.getDetails()).getUserType();

        Map<String, Object> result = null;
        try {
            result = loginService.loadUserByUsername(userId, userPassword, userType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        UserDetails user = (UserDetails) result.get("user");
        if (Objects.isNull(user)) {
            return null;
        }

        boolean userCheck = (boolean) result.get("userCheck");
        if (userCheck) {
            if ("admin".equals(userType)) {
                AdminDto adminDto = (AdminDto) result.get("admin");
                ((CustomWebAuthenticationDetails) authentication.getDetails()).setUserCode(adminDto.getAdminCode());
                ((CustomWebAuthenticationDetails) authentication.getDetails()).setPreUserCode(adminDto.getAdminCode());
            } else if ("buyer".equals(userType)) {
                BuyerIdentificationDto buyerIdentificationDto = (BuyerIdentificationDto) result.get("buyer");
                ((CustomWebAuthenticationDetails) authentication.getDetails()).setUserCode(buyerIdentificationDto.getBuyerIdentificationCode());
                ((CustomWebAuthenticationDetails) authentication.getDetails()).setPreUserCode(buyerIdentificationDto.getBuyer().getBuyerCode());
            }
            log.info("roles... - [{}]", user.getAuthorities());
            return new UsernamePasswordAuthenticationToken(user.getUsername(), userPassword, user.getAuthorities());
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }
}
