package kr.co.medicals.common.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public final class RequestUtil {

    public RequestUtil() {
    }

    public static String extractIp(final HttpServletRequest request) {

        final String remoteAddress = request.getRemoteAddr();

        // 프록시나 Load Balancer 같은것을 겨쳐 오게 되는 경우 위의 방법으로는 정확한 아이피를 가져 오지 못하므로 아래 방법을 사용
        final String xForwardFor = request.getHeader("x-forwarded-for");
        final String proxyClientIp = request.getHeader("Proxy-Client-IP");
        final String wlProxyClientIp = request.getHeader("WL-Proxy-Client-IP"); // 웹로직

//        log.info("xForwardFor : " + xForwardFor);
//        log.info("proxyClientIp : " + proxyClientIp);
//        log.info("wlProxyClientIp : " + wlProxyClientIp);

        String requestIp = xForwardFor;

        if (ObjectCheck.isBlank(requestIp)) {
            requestIp = proxyClientIp;
        }

        if (ObjectCheck.isBlank(requestIp)) {
            requestIp = wlProxyClientIp;
        }

        if (ObjectCheck.isBlank(requestIp)) {
            requestIp = remoteAddress;
        }

        return requestIp;
    }
}
