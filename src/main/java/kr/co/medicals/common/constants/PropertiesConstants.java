package kr.co.medicals.common.constants;

import kr.co.medicals.common.util.PropertiesUtil;

public class PropertiesConstants {
    public static final String MEMBER_API_ADDR = PropertiesUtil.getProperty("url.member");
    public static final String FRONT_ADDR = PropertiesUtil.getProperty("url.front");
    public static final String FILE_ADDR = PropertiesUtil.getProperty("url.file");
    public static final String CHECK_PLUS = PropertiesUtil.getProperty("check.plus");
    public static final String CHECK_HASH = PropertiesUtil.getProperty("check.hash");
    public static final String XX_MID = PropertiesUtil.getProperty("xx.mid");
    public static final String XX_KEY = PropertiesUtil.getProperty("xx.key");
    public static final String XX_DOMAIN = PropertiesUtil.getProperty("xx.domain");

}

