package kr.co.medicals.common.enums;

import kr.co.medicals.common.ApiRequestFailExceptionMsg;
import kr.co.medicals.common.constants.ApiResponseCodeConstants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Slf4j
public enum PointStateEnum {

    SAVE_EXPECT("SE", "SAVE_EXPECT", "적립예정"),
    CANCEL_EXPECT("EC", "CANCEL_EXPECT", "적립예정취소"),
    SAVE_POINT("SP", "SAVE_POINT", "적립"),
    USE_POINT("UP", "USE_POINT", "사용"),
    USE_CANCEL("UC", "USE_CANCEL", "사용취소");


    private String code;
    private String enCode;
    private String krCode;

    PointStateEnum(String code, String enCode, String krCode) {
        this.code = code;
        this.enCode = enCode;
        this.krCode = krCode;
    }

    public static String getKrCodeByCode(String krCode) {
        for (PointStateEnum stateEnum : PointStateEnum.values()) {
            if (Objects.equals(stateEnum.getKrCode(), krCode)) {
                return stateEnum.getCode();
            }
        }
        return "";
    }
    public static String getEnCodeByCode(String enCode) {
        for (PointStateEnum stateEnum : PointStateEnum.values()) {
            if (Objects.equals(stateEnum.getEnCode(), enCode)) {
                return stateEnum.getCode();
            }
        }
        return "";
    }

    public static List<String> getByCodeList(String codeStr) {
        String checkState = codeStr.replaceAll(" ", "");
        List<String> searchStateList = new ArrayList<>();
        if(checkState.contains(",")){
            List<String> searchList = List.of(checkState.split(","));
            for(String word : searchList){
                String stateCodeEnCode = PointStateEnum.getEnCodeByCode(word);
                String stateCodeKrCode = PointStateEnum.getKrCodeByCode(word);
                log.info("POINT WORD : {}, STATE CODE EN : {} : , STATE CODE KR : {} : ", word, stateCodeEnCode, stateCodeKrCode);
                if ( !ObjectUtils.isEmpty(stateCodeEnCode) ) {
                    searchStateList.add(stateCodeEnCode);
                }else if( !ObjectUtils.isEmpty(stateCodeKrCode) ){
                    searchStateList.add(stateCodeKrCode);
                }
            }
            log.info("SEARCH STATE LIST : {} : ", searchStateList);
            return searchStateList;
        }else{
            String stateCodeEnCode = PointStateEnum.getEnCodeByCode(checkState);
            String stateCodeKrCode = PointStateEnum.getKrCodeByCode(checkState);
            if ( !ObjectUtils.isEmpty(stateCodeEnCode) ) {
                searchStateList.add(stateCodeEnCode);
            }else if( !ObjectUtils.isEmpty(stateCodeKrCode) ){
                searchStateList.add(stateCodeKrCode);
            }
            return searchStateList;
        }
    }

    public static String getEnCode(String code) {
        for (PointStateEnum stateEnum : PointStateEnum.values()) {
            if (Objects.equals(stateEnum.getCode(), code)) {
                return stateEnum.getEnCode();
            }
        }
        throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "요청한 포인트 상태는 없는 상태 코드.", code);
    }

    public static String getCode(String enCode) {
        for (PointStateEnum stateEnum : PointStateEnum.values()) {
            if (Objects.equals(stateEnum.getEnCode(), enCode)) {
                return stateEnum.getCode();
            }
        }
        throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "요청한 포인트 상태는 없는 상태 코드.", enCode);
    }


}
