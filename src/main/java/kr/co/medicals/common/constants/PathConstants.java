package kr.co.medicals.common.constants;

public class PathConstants extends PropertiesConstants {
    public static final String GET_ADMIN_LOGIN_DATA = MEMBER_API_ADDR + "/admin/login";
    public static final String GET_BUYER_LOGIN_DATA = MEMBER_API_ADDR + "/buyer/login";
    public static final String TMP_BUYER_SEARCH = MEMBER_API_ADDR + "/tmp-buyer/list"; // 임시회원 리스트 조회
    public static final String TMP_BUYER = MEMBER_API_ADDR + "/tmp-buyer"; // 임시회원 상세 조회, 임시회원 가입,수정 요청
    public static final String TMP_BUYER_APPROVE = MEMBER_API_ADDR + "/tmp-buyer/state"; //임시회원 승인/반려
    public static final String BUYER_SEARCH = MEMBER_API_ADDR + "/buyer/list"; // 회원 리스트 조회, 부계정 리스트 조회
    public static final String BUYER = MEMBER_API_ADDR + "/buyer"; // 회원 상세 조회
    public static final String BUYER_CHANGE_PASSWORD = MEMBER_API_ADDR + "/buyer/password"; // 회원 비밀번호 변경
    public static final String BUYER_INIT_PASSWORD = MEMBER_API_ADDR + "/buyer/password/init"; // 회원 비밀번호 초기화
    public static final String BUYER_CHANGE_STATE = MEMBER_API_ADDR + "/buyer/buyer-state"; //회원 상태 변경
    public static final String BUYER_SUB = MEMBER_API_ADDR + "/buyer/sub"; // 부계정 등록, 부계정 수정
    public static final String EXISTS_BUYER_CODE = MEMBER_API_ADDR + "/buyer/exists/buyer-code"; //회원 코드 존재여부 확인
    public static final String EXISTS_BUYER_IDENTIFICATION_ID = MEMBER_API_ADDR + "/buyer/exists/buyer-identification-id"; //회원 아이디 중복 확인
    public static final String EXISTS_EMAIL = MEMBER_API_ADDR + "/buyer/exists/email"; // 회원 이메일 중복 확인
    public static final String ADMIN_INFO = MEMBER_API_ADDR + "/admin/my/info"; // 관리자 정보 조회
}
