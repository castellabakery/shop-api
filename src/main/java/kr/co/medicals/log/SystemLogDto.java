package kr.co.medicals.log;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SystemLogDto {

    private Long seq;
    private String requestIp;
    private String requestUrl;
    private String request;
    private String response;
    private String state;
    private String etc;
    private String user;

    public SystemLogDto() {
    }

    @Builder(builderClassName = "byCreate", builderMethodName = "byCreate")
    public SystemLogDto(Long seq, String requestIp, String requestUrl, String request, String response, String state, String etc, String user) {
        this.seq = seq;
        this.requestIp = requestIp;
        this.requestUrl = requestUrl;
        this.request = request;
        this.response = response;
        this.state = state;
        this.etc = etc;
        this.user = user;
    }

    public SystemLog insertLog() {
        return SystemLog
                .builder()
                .requestIp(this.requestIp)
                .requestUrl(this.requestUrl)
                .request(this.request)
                .response(this.response)
                .state(this.state)
                .etc(this.etc)
                .user(this.user)
                .build();

    }
}
