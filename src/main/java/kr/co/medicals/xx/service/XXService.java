package kr.co.medicals.xx.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class XXService {

    public Map CTFCcheckHash(String buyReqamt, String buyItemnm, String orderno) {
        return new HashMap<>();
    }

    private String createHash(String xx, String xx2, String amt) {
        return amt;
    }

    private String createUri(String Path) {
        return Path;
    }

    public Map<String, Object> paymentReturn(Map<String, Object> responseMap) {
        return responseMap;
    }

    private Map<String, Object> createApproveMap(Map<String, Object> paramMap) {
        return paramMap;
    }

    public Map<String, Object> paymentCancel(String paymentMethod, String cancelAmount, String xx) {
        return new HashMap<>();
    }

}
