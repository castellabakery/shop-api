package kr.co.medicals.common.login.service;

import kr.co.medicals.admin.main.dto.AdminDto;
import kr.co.medicals.buyer.domain.dto.BuyerIdentificationDto;
import kr.co.medicals.common.ApiRequestFailExceptionMsg;
import kr.co.medicals.common.constants.ApiResponseCodeConstants;
import kr.co.medicals.common.constants.BuyerStateConstants;
import kr.co.medicals.common.constants.PathConstants;
import kr.co.medicals.common.enums.BuyerStateEnum;
import kr.co.medicals.common.util.ApiResponse;
import kr.co.medicals.common.util.Encrypt;
import kr.co.medicals.common.util.ObjectMapperUtil;
import kr.co.medicals.common.util.WebClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@Slf4j
@Service
public class LoginService implements UserDetailsService {

    private final WebClientUtils webClientUtils;

    @Autowired
    public LoginService(WebClientUtils webClientUtils) {
        this.webClientUtils = webClientUtils;
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        List<GrantedAuthority> roles = new ArrayList<>();
        return new User(userId, "", roles);
    }

    public Map<String, Object> loadUserByUsername(String userId, String userPassword, String userType) throws UsernameNotFoundException, URISyntaxException {
        Map<String, Object> result = new HashMap<>();
        List<GrantedAuthority> roles = new ArrayList<>();
        BuyerIdentificationDto buyerIdentificationDto;
        AdminDto adminDto;
        String realPassword = "";
        String realUserId = "";

        if ("admin".equals(userType)) {
            adminDto = (AdminDto) this.getUserDto(userId, userType);
            if (adminDto.getAdminId() == null) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.LOGIN_FAILED, "계정 정보가 일치하지 않습니다.");
            }
            realUserId = adminDto.getAdminId();
            realPassword = adminDto.getPassword();
            roles.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            result.put("admin", adminDto);
        } else if ("buyer".equals(userType)) {
            buyerIdentificationDto = (BuyerIdentificationDto) this.getUserDto(userId, userType);

            if (Objects.equals(BuyerStateConstants.STOP, buyerIdentificationDto.getBuyer().getBuyerState())) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.LOGIN_FAILED, "정지된 계정 정보입니다.");
            }

            if (!Objects.equals(BuyerStateConstants.DONE, buyerIdentificationDto.getBuyerIdentificationState())) {
                String state = buyerIdentificationDto.getBuyerIdentificationState();
                String msg = BuyerStateEnum.getKrCodeByCode(state);
                String resultMsg = ObjectUtils.isEmpty(msg) ? ("로그인 할 수 없는 정보 입니다.") : (msg + " 상태의 계정 정보 입니다.");
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.LOGIN_FAILED, resultMsg);
            }

            if (buyerIdentificationDto.getBuyerIdentificationId() == null) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.LOGIN_FAILED, "계정 정보가 일치하지 않습니다.");
            }

            realUserId = buyerIdentificationDto.getBuyerIdentificationId();
            realPassword = buyerIdentificationDto.getPassword();
            roles.add(new SimpleGrantedAuthority("ROLE_USER"));
            result.put("buyer", buyerIdentificationDto);
        }

        log.info("userPassword : " + userPassword);
        log.info("Encrypt.sha256(userPassword) : " + Encrypt.sha256(userPassword));

        log.info("user.getUserId vs userId - [{}] vs [{}]", realUserId, userId);
        log.info("user.getUserPassword vs userPassword - [{}] vs [{}]", realPassword, Encrypt.sha256(userPassword));

        if (userId.equals(realUserId) && Encrypt.sha256(userPassword).equals(realPassword)) {
            result.put("userCheck", true);
        } else {
            result.put("userCheck", false);
        }

        UserDetails user = new User(userId, realPassword, roles);
        result.put("user", user);
        return result;
    }

    public Object getUserDto(String userId, String userType) throws UsernameNotFoundException, URISyntaxException {
        Mono<ResponseEntity<ApiResponse>> response = null;
        if ("admin".equals(userType)) {
            response = webClientUtils.requestWebClient(HttpMethod.GET, new URI(PathConstants.GET_ADMIN_LOGIN_DATA + "?adminId=" + userId));
        } else if ("buyer".equals(userType)) {
            response = webClientUtils.requestWebClient(HttpMethod.GET, new URI(PathConstants.GET_BUYER_LOGIN_DATA + "?buyerId=" + userId));
        }

        ApiResponse apiResponse = response.block().getBody();
        if (apiResponse.getCode() == 1000) {
            if ("admin".equals(userType)) {
                return ObjectMapperUtil.responseConvertDto(apiResponse, AdminDto.class);
            } else if ("buyer".equals(userType)) {
                return ObjectMapperUtil.responseConvertDto(apiResponse, BuyerIdentificationDto.class);
            } else {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.LOGIN_FAILED, "로그인 할 수 없는 계정 정보입니다. 관리자에게 문의해주세요.");
            }
        } else {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.LOGIN_FAILED, "로그인 할 수 없는 계정 정보입니다. 관리자에게 문의해주세요.");
        }
    }
}
