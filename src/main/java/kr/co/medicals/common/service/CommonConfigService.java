package kr.co.medicals.common.service;

import kr.co.medicals.common.domain.repository.ConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Component
@RequiredArgsConstructor
public class CommonConfigService {

    private final ConfigRepository configRepository;
}
