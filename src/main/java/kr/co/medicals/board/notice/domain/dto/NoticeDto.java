package kr.co.medicals.board.notice.domain.dto;

import kr.co.medicals.board.notice.domain.entity.Notice;
import kr.co.medicals.common.util.ObjectCheck;
import kr.co.medicals.common.util.SessionUtil;
import lombok.*;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;


@ToString
@NoArgsConstructor
@Setter
@Getter
public class NoticeDto {
    private Long seq;
    private String boardType;
    private String title;
    private String content;
    private String displayYn;
    private String delYn;
    private String createdId;
    private String modifiedId;
    private LocalDateTime createdDatetime;
    private LocalDateTime modifiedDatetime;
    private Long listCnt;

    @Builder
    public NoticeDto(Long seq, String boardType, String title, String content, String displayYn, String delYn, String createdId, String modifiedId, LocalDateTime createdDatetime, LocalDateTime modifiedDatetime) {
        this.seq = seq;
        this.boardType = boardType;
        this.title = title;
        this.content = content;
        this.displayYn = displayYn;
        this.delYn = delYn;
        this.createdId = createdId;
        this.modifiedId = modifiedId;
        this.createdDatetime = createdDatetime;
        this.modifiedDatetime = modifiedDatetime;
    }

    @Builder(builderClassName = "entityConvertDto", builderMethodName = "entityConvertDto")
    public NoticeDto(Notice notice) {
        this.seq = notice.getSeq();
        this.boardType = notice.getBoardType();
        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.displayYn = notice.getDisplayYn();
        this.delYn = notice.getDelYn();
        this.createdId = notice.getCreatedId();
        this.modifiedId = notice.getModifiedId();
        this.createdDatetime = notice.getCreatedDatetime();
        this.modifiedDatetime = notice.getModifiedDatetime();
    }

    public Notice insertEntity(){

        ObjectCheck.isBlankStringException(this.boardType, "BOARD_TYPE");
        ObjectCheck.isBlankStringException(this.title, "TITLE");
        ObjectCheck.isBlankStringException(this.content, "CONTENT");
        ObjectCheck.isBlankStringException(this.displayYn, "DISPLAY_YN");

        return Notice
                .builder()
                .boardType(this.boardType)
                .title(this.title)
                .content(this.content)
                .displayYn(this.displayYn)
                .delYn("N")
                .createdId(SessionUtil.getUserCode())
                .createdDatetime(LocalDateTime.now())
                .build();
    }

    public Notice updateEntity(NoticeDto newDto){

        return Notice
                .builder()
                .seq(this.seq)
                .boardType(ObjectUtils.isEmpty(newDto.getBoardType()) ? this.boardType : newDto.getBoardType())
                .title(ObjectUtils.isEmpty(newDto.getTitle()) ? this.title : newDto.getTitle())
                .content(ObjectUtils.isEmpty(newDto.getContent()) ? this.content : newDto.getContent())
                .displayYn(ObjectUtils.isEmpty(newDto.getDisplayYn()) ? this.displayYn : newDto.getDisplayYn())
                .delYn("N")
                .createdId(this.createdId)
                .createdDatetime(this.createdDatetime)
                .modifiedId(SessionUtil.getUserCode())
                .modifiedDatetime(LocalDateTime.now())
                .build();
    }

    public Notice updateDelY(String buyerIdentificationCode) {
        return Notice
                .builder()
                .seq(this.seq)
                .boardType(this.boardType)
                .title(this.title)
                .content(this.content)
                .displayYn(this.displayYn)
                .delYn("Y")
                .createdId(this.createdId)
                .createdDatetime(this.createdDatetime)
                .modifiedId(buyerIdentificationCode)
                .modifiedDatetime(LocalDateTime.now())
                .build();
    }
}
