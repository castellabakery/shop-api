package kr.co.medicals.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.co.medicals.common.ApiRequestFailExceptionMsg;
import kr.co.medicals.common.constants.ApiResponseCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class ObjectMapperUtil {

    public static <T> T responseConvertDto(ApiResponse apiResponse, Class<T> responseType) {
        if (Objects.equals(apiResponse.getCode(), ApiResponse.SUCCESS)) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                Map<String, Object> responseMap = mapper.convertValue(apiResponse.getData(), Map.class);
                return mapper.convertValue(responseMap, responseType);
            } catch (Exception e) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.CONVERT_FAIL, "CONVERT_FAIL_API_RESPONSE", e);
            }
        } else {
            throw new ApiRequestFailExceptionMsg(apiResponse.getCode(), apiResponse.getMessage(), apiResponse.getData());
        }
    }

    public static Map<String, Object> requestGetMap(ApiRequest apiRequest) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (!ObjectUtils.isEmpty(apiRequest)) {
                result = apiRequest.getMap();
            }
        } catch (Exception e) {
            log.info("ObjectMapperUtil-requestGetMap-fail : {}", apiRequest);
        } finally {
            return result;
        }
    }

    public static <T> T requestConvertDto(ApiRequest apiRequest, Class<T> responseType) {
        try {
            Map<String, Object> map = apiRequest.getMap();
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return mapper.convertValue(map, responseType);
        } catch (Exception e) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.CONVERT_FAIL, "CONVERT_FAIL_API_REQUEST", e);
        }
    }

}
