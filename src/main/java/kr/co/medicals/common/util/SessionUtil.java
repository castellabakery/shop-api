package kr.co.medicals.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class SessionUtil {

    public static String getUserId() {
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return req.getSession().getAttribute("userId").toString();
    }

    public static String getUserCode() {
        String result = "NOT_REG_USER";
        try {
            HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            result = req.getSession().getAttribute("userCode").toString();
        } catch (Exception e) {
            log.error("SessionUtil : UserCode is null.");
        }
        return result;
    }

    public static String getPreUserCode() {
        String result = "NOT_REG_USER";
        try {
            HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            result = req.getSession().getAttribute("preUserCode").toString();
        } catch (Exception e) {
            log.error("SessionUtil : PreUserCode is null.");
        }
        return result;
    }

    public static String getUserType() {
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return req.getSession().getAttribute("userType").toString();
    }


}
