package kr.co.medicals.log;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@ToString
@Entity
@NoArgsConstructor
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Table(name = "SYSTEM_LOG")
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;
    private String requestIp;
    private String requestUrl;
    private String request;
    private String response;
    private String state;
    private String etc;
    private String user;

    @CreatedDate
    private LocalDateTime createdDatetime;

    @Builder
    public SystemLog(Long seq, String requestIp, String requestUrl, String request, String response, String state, String etc, String user) {
        this.seq = seq;
        this.requestIp = requestIp;
        this.requestUrl = requestUrl;
        this.request = request;
        this.response = response;
        this.state = state;
        this.etc = etc;
        this.user = user;
    }
}
