package kr.co.medicals.common.util;

import kr.co.medicals.common.ApiRequestFailExceptionMsg;
import kr.co.medicals.common.constants.ApiResponseCodeConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
public class RegExpUtil {


    // 영문
    public static void regularEnglish(String value, String name){
        String enPattern = "^[a-zA-Z]*$";
        patternMatches(value, enPattern, name);
    }

    //한글
    public static void regularKorean(String value, String name){
        String krPattern = "^[가-힣]*$";
        patternMatches(value, krPattern, name);
    }

    // 한글 + 영문
    public static void regularEnglishOrKorean(String value, String name){
        String enOrKrPattern = "^[a-zA-Zㄱ-ㅎ가-힣]*$";
        patternMatches(value, enOrKrPattern, name);
    }

    // 숫자
    public static void regularInteger(String value, String name){
        String integerPattern = "^[0-9]*$";
        patternMatches(value, integerPattern, name);
    }

    //핸드폰 번호
    public static void regularPhoneNo(String value, String name){
        ObjectCheck.stringBlankLengthCheck(value, 11, 11, "PHONE_NO");
        String phoneNoPattern = "^01(?:0|1|[6-9])[.-]?(\\d{3}|\\d{4})[.-]?(\\d{4})$";
        patternMatches(value, phoneNoPattern, name);
    }

    // 전화번호
    public static void regularTelNo(String value, String name){
        if(Objects.equals("02", value.substring(0, 2))){
            ObjectCheck.stringBlankLengthCheck(value, 10, 10, "TEL_NO");
        }else{
            ObjectCheck.stringBlankLengthCheck(value, 11, 11, "TEL_NO");
        }
        String telNoPattern = "^(\\d{2,3})[.-]?(\\d{3,4})[.-]?(\\d{4})$";
        patternMatches(value, telNoPattern, name);
    }

    // 비밀번호 - 자리수 체크 X, 문자, 특수문자, 숫자 최소 1개씩 포함.
    public static void regularPasswordCheck(String value, String name){
//        String passwordCheck = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,}$"; -- 8자리 제한
        String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]$";
        patternMatches(value, passwordPattern, name);
    }
    
    // 이메일
    public static void regularEmail(String value, String name){
        String emailPattern = "\\w+@\\w+\\.\\w+(\\.\\w+)?";
        patternMatches(value, emailPattern, name);
    }

    private static void patternMatches(String value, String pattern, String name){
        if(!Pattern.matches(pattern, value)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "PLEASE CHECK THE FORMAT : "+name);
        }
    }


}
