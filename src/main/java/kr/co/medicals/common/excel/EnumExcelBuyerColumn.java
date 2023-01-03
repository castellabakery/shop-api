package kr.co.medicals.common.excel;

import lombok.Getter;

@Getter
public enum EnumExcelBuyerColumn {
    SEQ("seq", "번호", 0, "3000"),
    USER_DIVISION("user_division", "유저구분", 1, "3000"),
    ID("id", "ID", 2, "3000"),
    NAME("name", "업체명", 3, "3000"),
    CUSTOMER("customer", "담당자", 4, "3000"),
    EMAIL("email", "이메일", 5, "4000"),
    REG_DATE("regDate", "가입일자", 6, "4000"),
    STATUS("status", "계정상태", 7, "3000");

    private String code;
    private String colName;
    private int seq;
    private String width;

    EnumExcelBuyerColumn(String code, String colName, int seq, String width) {
        this.code = code;
        this.colName = colName;
        this.seq = seq;
        this.width = width;
    }

    public static String getNameByCode(String code) {
        for (EnumExcelBuyerColumn enumExcelPaymentColumn : EnumExcelBuyerColumn.values()) {
            if (enumExcelPaymentColumn.getCode().equals(code)) {
                return enumExcelPaymentColumn.getColName();
            }
        }
        return ExcelConstants.notFound;
    }

    public static String getCodeBySeq(int seq) {
        for (EnumExcelBuyerColumn enumExcelPaymentColumn : EnumExcelBuyerColumn.values()) {
            if (enumExcelPaymentColumn.getSeq() == seq) {
                return enumExcelPaymentColumn.getCode();
            }
        }
        return ExcelConstants.notFound;
    }

    public static int getSequenceByName(String colName) {
        for (EnumExcelBuyerColumn enumExcelPaymentColumn : EnumExcelBuyerColumn.values()) {
            if (enumExcelPaymentColumn.getColName().equals(colName)) {
                return enumExcelPaymentColumn.getSeq();
            }
        }
        return ExcelConstants.noSequence;
    }

    public static String getWidthBySequence(int seq) {
        for (EnumExcelBuyerColumn enumExcelPaymentColumn : EnumExcelBuyerColumn.values()) {
            if (enumExcelPaymentColumn.getSeq() == seq) {
                return enumExcelPaymentColumn.getWidth();
            }
        }
        return ExcelConstants.defaultWidth;
    }
}
