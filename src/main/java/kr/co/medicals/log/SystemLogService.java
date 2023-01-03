package kr.co.medicals.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemLogService {

    private final SystemLogRepository systemLogRepository;

    @Autowired
    public SystemLogService(SystemLogRepository systemLogRepository) {
        this.systemLogRepository = systemLogRepository;
    }

    public void insertSystemLog(SystemLogDto systemLogDto) {
        systemLogRepository.save(systemLogDto.insertLog());
    }
}
