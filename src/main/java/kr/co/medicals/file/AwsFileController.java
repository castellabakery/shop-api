package kr.co.medicals.file;

import kr.co.medicals.common.util.ApiResponse;
import kr.co.medicals.common.util.SessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/file/aws")
public class AwsFileController {

    private final AwsFileService awsFileService;

    @Autowired
    public AwsFileController(AwsFileService awsFileService) {
        this.awsFileService = awsFileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> upload(@RequestParam MultipartFile[] multipartFileList, @RequestParam Long relationSeq, @RequestParam int fileType) {
        awsFileService.upload(multipartFileList, relationSeq, fileType, SessionUtil.getUserCode());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/upload/without")
    public ResponseEntity<ApiResponse> uploadWithout(@RequestParam MultipartFile[] multipartFileList, @RequestParam Long relationSeq, @RequestParam int fileType) {
        awsFileService.upload(multipartFileList, relationSeq, fileType, null);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/update")
    public ResponseEntity<ApiResponse> uploadAndDelete(@RequestParam(value = "multipartFileList", required = false) MultipartFile[] multipartFileList,
                                              @RequestParam(value = "relationSeq", required = false) Long relationSeq,
                                              @RequestParam(value = "fileType", required = false, defaultValue = "0") Integer fileType,
                                              @RequestParam(value = "alreadyFileName", required = false) List<String> alreadyFileName) {
        awsFileService.uploadAndDelete(multipartFileList, relationSeq, fileType, alreadyFileName);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Deprecated
    @PostMapping("/delete")
    public ResponseEntity<ApiResponse> delete(@RequestParam List<String> fileName) {
        awsFileService.delete(fileName);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /*
     * @deprecated 테스트용으로만 사용. 해당 controller 를 호출하지 않아도 내부 로직으로 처리하고 있음.
     */
    @Deprecated
    @PostMapping("/getFile")
    public ResponseEntity<ApiResponse> getFileList(@RequestParam Long relationSeq, @RequestParam List<Integer> fileTypes) {
        return ResponseEntity.ok(ApiResponse.success(awsFileService.getFileListByRelationSeqAndFileTypes(relationSeq, fileTypes)));
    }

}
