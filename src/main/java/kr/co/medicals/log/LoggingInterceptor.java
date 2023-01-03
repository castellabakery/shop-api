package kr.co.medicals.log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.medicals.common.util.SessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;
    private final SystemLogRepository systemLogRepository;

    @Autowired
    public LoggingInterceptor(ObjectMapper objectMapper, SystemLogRepository systemLogRepository) {
        this.objectMapper = objectMapper;
        this.systemLogRepository = systemLogRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        this.preHandleLog(request, response, handler);
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        this.postHandleLog(request, response, handler, modelAndView);
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        this.afterCompletionLog(request, response, handler, ex);
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    private void preHandleLog(HttpServletRequest request, HttpServletResponse response, Object handler) {

        log.info("=== preHandleLog === SessionId  : {} ", request.getRequestedSessionId());
        log.info("=== preHandleLog === RequestURL : {}", request.getRequestURL().toString());

        if (!request.isRequestedSessionIdValid()) {
            log.warn("Invalid client session ID");
        }

    }

    private void postHandleLog(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws IOException {

        SystemLogDto systemLogDto = new SystemLogDto();

        log.info("=== postHandleLog === SessionId  : {} ", request.getRequestedSessionId());
        log.info("=== postHandleLog === RequestURL : {}", request.getRequestURL().toString());

        systemLogDto.setRequestUrl(request.getRequestURL().toString());

        if (!request.isRequestedSessionIdValid()) {
            log.warn("Invalid client session ID");
        }

        try {
            if (request.getClass().getName().contains("SecurityContextHolderAwareRequestWrapper")) return;

            String requestBodyText = "";

            final StringBuilder paramsBuilder = new StringBuilder();
            final Enumeration<String> paramNames = request.getParameterNames();

            if (paramNames.hasMoreElements()) {
                log.info("=== postHandleLog === RequestParam");
                while (paramNames.hasMoreElements()) {
                    if (paramsBuilder.length() != 0) {
                        paramsBuilder.append(", ");
                    } else {
                        paramsBuilder.append("{");
                    }
                    final String paramName = paramNames.nextElement();
                    final String paramValue = request.getParameter(paramName);
                    paramsBuilder.append("\"").append(paramName).append("\":\"").append(paramValue).append("\"");
                }
                paramsBuilder.append("}");
                requestBodyText = paramsBuilder.toString();
                JsonNode jsonNode = objectMapper.readTree(requestBodyText);
                requestBodyText = jsonNode.toPrettyString();

            } else {
                log.info("=== postHandleLog === RequestBody");
                final ContentCachingRequestWrapper cachingRequest = (ContentCachingRequestWrapper) request;
                if (cachingRequest.getContentType() != null && cachingRequest.getContentType().contains("application/json")) {
                    if (cachingRequest.getContentAsByteArray() != null && cachingRequest.getContentAsByteArray().length != 0) {
                        JsonNode jsonNode = objectMapper.readTree(cachingRequest.getContentAsByteArray());
                        requestBodyText = jsonNode.toPrettyString();
                    }
                }

            }

            log.info("=== postHandleLog === RequestText : {}", requestBodyText);

        } catch (Exception e) {
            log.error("API PostHandler Log Fail", e);
        }
    }


    private void afterCompletionLog(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        String sessionId = request.getRequestedSessionId();
        String url = request.getRequestURL().toString();
        String requestBodyText = "";
        String responseBodyText = "";
        String state = "0000";
        String etc = "";
        String user = "";

        log.debug("=== afterCompletionLog === sessionId : {}", sessionId);
        log.debug("=== afterCompletionLog === url : {}", url);

        if (!request.isRequestedSessionIdValid()) {
            log.warn("Invalid client session ID");
            user = "NOT_LOGIN";
        } else {
            user = SessionUtil.getUserCode();
        }

        try {

            if (request.getClass().getName().contains("SecurityContextHolderAwareRequestWrapper")) return;

            final StringBuilder paramsBuilder = new StringBuilder();
            final Enumeration<String> paramNames = request.getParameterNames();

            if (paramNames.hasMoreElements()) {
                log.debug("=== afterCompletionLog === RequestParam");
                while (paramNames.hasMoreElements()) {
                    if (paramsBuilder.length() != 0) {
                        paramsBuilder.append(", ");
                    }
                    final String paramName = paramNames.nextElement();
                    final String paramValue = request.getParameter(paramName);
                    paramsBuilder.append(paramName).append(" : ").append(paramValue);
                }

                requestBodyText = paramsBuilder.toString();
            } else {
                log.debug("=== afterCompletionLog === RequestBody");
                final ContentCachingRequestWrapper cachingRequest = (ContentCachingRequestWrapper) request;
                if (cachingRequest.getContentType() != null && cachingRequest.getContentType().contains("application/json")) {
                    if (cachingRequest.getContentAsByteArray() != null && cachingRequest.getContentAsByteArray().length != 0) {
                        JsonNode jsonNode = objectMapper.readTree(cachingRequest.getContentAsByteArray());
                        requestBodyText = jsonNode.toPrettyString();
                    }
                }

            }

            log.debug("=== afterCompletionLog === RequestText : {}", requestBodyText);

            final ContentCachingResponseWrapper cachingResponse = (ContentCachingResponseWrapper) response;
            if (cachingResponse.getContentType() != null && cachingResponse.getContentType().contains("application/json")) {
                if (cachingResponse.getContentAsByteArray() != null && cachingResponse.getContentAsByteArray().length != 0) {
                    JsonNode jsonNode = objectMapper.readTree(cachingResponse.getContentAsByteArray());
                    JsonNode statCodeNode = jsonNode.path("code");
                    if (!statCodeNode.isMissingNode()) {
                        state = statCodeNode.asText();
                    }
                    responseBodyText = jsonNode.toPrettyString();
                }
            }

            log.debug("=== afterCompletionLog === ResponseText : {}", responseBodyText);
            etc = "SUCCESS";
        } catch (Exception e) {
            etc = "INTERCEPTOR_EXCEPTION";
            log.error("API After Fail.", e);
            responseBodyText = e.toString();
        } finally {

            if(responseBodyText.length() >= 200){
                responseBodyText = responseBodyText.substring(0, 200);
            }

            SystemLogDto systemLogDto
                    = new SystemLogDto
                    .byCreate()
                    .requestIp(sessionId)
                    .requestUrl(url)
                    .request(requestBodyText)
                    .response(responseBodyText)
                    .state(state)
                    .etc(etc)
                    .user(user)
                    .build();

            log.info("=== INTERCEPTOR-SYSTEM-LOG : {}", systemLogDto);

            systemLogRepository.save(systemLogDto.insertLog());

        }

    }

}
