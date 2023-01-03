package kr.co.medicals.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
public enum FileTypeEnum {

    ETC(0, "ETC", "etc"),
    PRODUCT_IMG_MAIN(1, "PRODUCT_IMG_MAIN", "product_img_main"),
    PRODUCT_IMG_SUB(2, "PRODUCT_IMG_SUB", "product_img_sub"),
    NOTICE_FILE(3, "NOTICE_FILE", "notice_file"),
    FAQ_FILE(4, "FAQ_FILE", "faq_file"),
    QNA_FILE(5, "QNA_FILE", "qna_file"),
    TMP_BUYER_BUSINESS_REGISTRATION(6, "BUYER_BUSINESS_REGISTRATION", "tmp_buyer_business_registration"),
    TMP_LICENSE(7, "LICENSE", "tmp_license"),
    BUYER_BUSINESS_REGISTRATION(8, "BUYER_BUSINESS_REGISTRATION", "buyer_business_registration"),
    BUYER_LICENSE(9, "BUYER_LICENSE", "buyer_license");

    private int code;
    private String type;
    private String path;

    FileTypeEnum(int code, String type, String path) {
        this.code = code;
        this.type = type;
        this.path = path;
    }

    public static String getFilePath(int code) {
        for (FileTypeEnum fileTypeEnum : FileTypeEnum.values()) {
            if (fileTypeEnum.getCode() == code) {
                return fileTypeEnum.getPath();
            }
        }
        return FileTypeEnum.ETC.getPath();
    }

    public static String getMoveFilePath(int code) {
        if(Objects.equals(FileTypeEnum.TMP_BUYER_BUSINESS_REGISTRATION.getCode(), code)){
            return FileTypeEnum.BUYER_BUSINESS_REGISTRATION.getPath();
        }
        if(Objects.equals(FileTypeEnum.TMP_LICENSE.getCode(), code)){
            return FileTypeEnum.BUYER_LICENSE.getPath();
        }
        return FileTypeEnum.ETC.getPath();
    }

    public static int getMoveFileCode(int code) {
        if(Objects.equals(FileTypeEnum.TMP_BUYER_BUSINESS_REGISTRATION.getCode(), code)){
            return FileTypeEnum.BUYER_BUSINESS_REGISTRATION.getCode();
        }
        if(Objects.equals(FileTypeEnum.TMP_LICENSE.getCode(), code)){
            return FileTypeEnum.BUYER_LICENSE.getCode();
        }
        return FileTypeEnum.ETC.getCode();
    }

    public static FileTypeEnum getMoveFileTypeEnum(int code){
        if(Objects.equals(FileTypeEnum.TMP_BUYER_BUSINESS_REGISTRATION.getCode(), code)){
            return FileTypeEnum.BUYER_BUSINESS_REGISTRATION;
        }
        if(Objects.equals(FileTypeEnum.TMP_LICENSE.getCode(), code)){
            return FileTypeEnum.BUYER_LICENSE;
        }
        return FileTypeEnum.ETC;
    }

    public static List<Integer> getTmpBuyerFileType() {
        return Arrays.asList(FileTypeEnum.TMP_BUYER_BUSINESS_REGISTRATION.getCode(), FileTypeEnum.TMP_LICENSE.getCode());
    }

    public static List<Integer> getBuyerFileType() {
        return Arrays.asList(FileTypeEnum.BUYER_BUSINESS_REGISTRATION.getCode(), FileTypeEnum.BUYER_LICENSE.getCode());
    }

    public static List<Integer> getProductMainFileType() {
        return Arrays.asList(FileTypeEnum.PRODUCT_IMG_MAIN.getCode());
    }

    public static List<Integer> getProductAllFileType() {
        return Arrays.asList(FileTypeEnum.PRODUCT_IMG_MAIN.getCode(), FileTypeEnum.PRODUCT_IMG_SUB.getCode());
    }

}
