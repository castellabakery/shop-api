package kr.co.medicals.common.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;

public class CustomWebAuthenticationDetails extends WebAuthenticationDetails {

    @Getter
    @Setter
    private String userType;
    @Getter
    @Setter
    private HttpServletRequest request;
    @Getter
    @Setter
    private String userCode;
    @Getter
    @Setter
    private String preUserCode;


    public CustomWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        userType = request.getParameter("userType");
        this.request = request;
    }
}
