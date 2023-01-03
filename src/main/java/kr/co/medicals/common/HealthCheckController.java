package kr.co.medicals.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/health")
public class HealthCheckController {
    @RequestMapping
    public String getAdminInfo() {
        log.info("Health Check From Load Balancer");
        return "Health Condition Now";
    }
}
