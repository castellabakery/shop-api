package kr.co.medicals.common.constants;

import lombok.Getter;

@Getter
public class BuyerStateConstants {
    public static final String READY = "R"; // 가입승인대기
    public static final String WAIT = "W"; // 수정승인대기
    public static final String DONE = "D"; // 관리자 확인 완료
    public static final String REJECTED = "J"; // 관리자 반려
    public static final String STOP = "S"; // 계정 정지

    public static final String EXPIRED = "X"; // 삭제

}
