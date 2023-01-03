package kr.co.medicals.common.util;

import kr.co.medicals.common.config.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PropertiesUtil {

    private ServerProperties serverProperties;

    public PropertiesUtil(ServerProperties serverProperties) {
        this.serverProperties = serverProperties;
    }

    private static Map<String, String> propertiesMap = new HashMap<>();

    @Bean
    private void putProperties() {
        propertiesMap.putAll(serverProperties.getPath());
    }

    public static String getProperty(String name) {
        return propertiesMap.get(name);
    }
}
