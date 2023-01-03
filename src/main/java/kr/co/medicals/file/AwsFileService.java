package kr.co.medicals.file;

import kr.co.medicals.buyer.domain.dto.TmpBuyerDto;
import kr.co.medicals.common.ApiRequestFailExceptionMsg;
import kr.co.medicals.common.constants.ApiResponseCodeConstants;
import kr.co.medicals.common.constants.BuyerStateConstants;
import kr.co.medicals.common.constants.PathConstants;
import kr.co.medicals.common.enums.FileTypeEnum;
import kr.co.medicals.common.util.ApiResponse;
import kr.co.medicals.common.util.ObjectMapperUtil;
import kr.co.medicals.common.util.SessionUtil;
import kr.co.medicals.common.util.WebClientUtils;
import kr.co.medicals.file.domain.dto.FileManagerDto;
import kr.co.medicals.file.domain.entity.FileManager;
import kr.co.medicals.file.domain.repository.FileManagerRepository;
import kr.co.medicals.file.domain.repository.FileManagerRepositorySupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AwsFileService {

    private final FileManagerRepository fileManagerRepository;
    private final FileManagerRepositorySupport fileManagerRepositorySupport;
    private final WebClientUtils webClientUtils;
    public AwsUtils awsUtils = AwsUtils.getInstance();

    @Autowired
    public AwsFileService(FileManagerRepository fileManagerRepository, FileManagerRepositorySupport fileManagerRepositorySupport, WebClientUtils webClientUtils) {
        this.fileManagerRepository = fileManagerRepository;
        this.fileManagerRepositorySupport = fileManagerRepositorySupport;
        this.webClientUtils = webClientUtils;
    }

    /**
     * 파일 등록
     */
    public void upload(MultipartFile[] multipartFileList, Long relationSeq, int fileType, String userCode) {

        // 임시회원 파일 타입으로 등록 요청 할 때만 회원 정보에서 buyerIdentificationCode 꺼내와야 함.(신규가입 요청 때문에)
        if (Objects.equals(fileType, FileTypeEnum.TMP_BUYER_BUSINESS_REGISTRATION.getCode())
                || Objects.equals(fileType, FileTypeEnum.TMP_LICENSE.getCode())) {

            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                    .fromHttpUrl(PathConstants.TMP_BUYER)
                    .queryParam("tmpSeq", relationSeq)
                    .encode();

            Mono<ResponseEntity<ApiResponse>> response = webClientUtils.requestWebClient(HttpMethod.GET, uriComponentsBuilder.build().toUri());
            TmpBuyerDto getTmp = ObjectMapperUtil.responseConvertDto(response.block().getBody(), TmpBuyerDto.class);
            userCode = getTmp.getBuyerIdentificationCode();
        }

        // 임시 내용이 아닌데 userCode 비어있으면 안됨.
        if (ObjectUtils.isEmpty(userCode)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY");
        }

        List<FileManagerDto> fileManagerDtoList = awsUtils.uploadMultipartFile(multipartFileList, relationSeq, fileType);
        addFilesInfo(fileManagerDtoList, userCode);
    }

    /**
     * 파일 업로드, 파일 삭제 = 업로드 해야하는 내용이있으면 업로드, 삭제해야할 내용이 있으면 파일명 조회해서 삭제
     */
    public void uploadAndDelete(MultipartFile[] multipartFileList, Long relationSeq, int fileType, List<String> alreadyFileName) {

        // 전부 비어있으면 아무 내용도 수행 할 수 없음.
        if (ObjectUtils.isEmpty(relationSeq) || fileType == 0) {
            if (ObjectUtils.isEmpty(alreadyFileName)) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "multipartFileList");
            }
        }

        // 신규 등록 내용이 있다면
        if (!ObjectUtils.isEmpty(relationSeq) && fileType > 0) {
            List<FileManagerDto> fileManagerDtoList = awsUtils.uploadMultipartFile(multipartFileList, relationSeq, fileType);
            addFilesInfo(fileManagerDtoList, SessionUtil.getUserCode());
        }

        // 삭제해야하는 내용이 있다면
        if (!ObjectUtils.isEmpty(alreadyFileName)) {
            this.delete(alreadyFileName);
        }

    }

    /**
     * 파일 내용 복사 후 기존 파일 삭제
     */
    private void copyFile(FileManagerDto beforeFileInfo){

        FileTypeEnum fileTypeEnum = FileTypeEnum.getMoveFileTypeEnum(beforeFileInfo.getFileType());

        // 파일 정보 삭제로 업데이트
        fileManagerRepository.save(beforeFileInfo.updateDelYEntity(SessionUtil.getUserCode()));

        // 이동 된 파일 정보 신규 등록
        fileManagerRepository.save(new FileManagerDto().copyDataInsertEntity(beforeFileInfo, fileTypeEnum));

        // 파일 복사
        String orgKey = beforeFileInfo.getFilePath() + "/" + beforeFileInfo.getUploadFileName();
        String copyKey = fileTypeEnum.getPath() + "/" + beforeFileInfo.getUploadFileName();
        awsUtils.copy(orgKey, copyKey);

        // 파일 삭제
        String deleteKey = beforeFileInfo.getFilePath() + "/" + beforeFileInfo.getUploadFileName();
        awsUtils.delete(deleteKey);
    }

    /**
     * 파일 삭제 - 리스트 단위
     */
    private void deleteFiles(List<FileManagerDto> fileInfos){

        List<FileManager> updateEntity = fileInfos.stream().map(item -> item.updateDelYEntity(SessionUtil.getUserCode())).collect(Collectors.toList());
        fileManagerRepository.saveAll(updateEntity);

        for(FileManagerDto dto : fileInfos){
            String deleteKey = dto.getFilePath() + "/" + dto.getUploadFileName();
            awsUtils.delete(deleteKey);
        }

    }

    /**
     * 파일 삭제 - 건 단위
     */
    private void deleteFile(FileManagerDto fileInfo){

        fileManagerRepository.save(fileInfo.updateDelYEntity(SessionUtil.getUserCode()));

        String deleteKey = fileInfo.getFilePath() + "/" + fileInfo.getUploadFileName();
        awsUtils.delete(deleteKey);

    }


    /**
     * 파일 등록 실제 구간
     */
    private void addFilesInfo(List<FileManagerDto> fileManagerDtoList, String userCode) {
        List<FileManager> fileManagerList = fileManagerDtoList.stream().map(list -> list.insertEntity(userCode)).collect(Collectors.toList());
        fileManagerRepository.saveAll(fileManagerList);
    }

    public void delete(List<String> uploadFileName) {

        List<FileManagerDto> fileManagerDtoList = fileManagerRepositorySupport.findFileForUploadName(uploadFileName);

        if (ObjectUtils.isEmpty(fileManagerDtoList)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "file manager list");
        }

        // 회원은 본인이 등록하지 않은 파일에 대한 삭제는 할 수 없음.
        for (FileManagerDto dto : fileManagerDtoList) {
            if (Objects.equals(SessionUtil.getUserType(), "buyer")
                    && !Objects.equals(dto.getCreatedId(), SessionUtil.getUserCode())) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "Unauthorized");
            }
        }

        this.deleteFiles(fileManagerDtoList);

    }

    /**
     * 승인 및 반려에 따라 파일 이동/삭제 조치
     */
    public void copyTmpBuyerToBuyer(Long tmpSeq, Long buyerIdentificationSeq, String changeState) {

        List<FileManagerDto> tmpFileList = this.getFileListByRelationSeqAndFileTypes(tmpSeq, FileTypeEnum.getTmpBuyerFileType());
        List<FileManager> updateFileManagerList = new ArrayList<>();

        String userCode = SessionUtil.getUserCode();

        if (!ObjectUtils.isEmpty(tmpFileList)) {
            if (Objects.equals(changeState, BuyerStateConstants.REJECTED)) { // 반려일때 등록 된 파일 및 이력 파기.
                for (FileManagerDto dto : tmpFileList) {
                    updateFileManagerList.add(dto.updateDelYEntity(userCode)); // 임시 파일 사용 안함 처리
                    awsUtils.delete(dto.getFilePath() + "/" + dto.getUploadFileName());
                }
                fileManagerRepository.saveAll(updateFileManagerList);
            } else if (Objects.equals(changeState, BuyerStateConstants.DONE)) {
                for (FileManagerDto dto : tmpFileList) { // 승인일때 등록 된 파일 이동하면서 기존 파일 및 이력은 파기.
                    String copyKey = "";
                    if (Objects.equals(FileTypeEnum.TMP_BUYER_BUSINESS_REGISTRATION.getCode(), dto.getFileType())) {
                        copyKey = FileTypeEnum.getFilePath(FileTypeEnum.BUYER_BUSINESS_REGISTRATION.getCode()) + "/" + dto.getUploadFileName();
                    } else if (Objects.equals(FileTypeEnum.TMP_LICENSE.getCode(), dto.getFileType())) {
                        copyKey = FileTypeEnum.getFilePath(FileTypeEnum.BUYER_LICENSE.getCode()) + "/" + dto.getUploadFileName();
                    }
                    String orgKey = dto.getFilePath() + "/" + dto.getUploadFileName();
                    awsUtils.copy(orgKey, copyKey); // 임시파일 회원파일로 이동.
                    updateFileManagerList.add(dto.updateDelYEntity(userCode)); // 임시 파일 사용 안함 처리.
                    awsUtils.delete(dto.getFilePath() + "/" + dto.getUploadFileName());
                }
                fileManagerRepository.saveAll(updateFileManagerList);

                // 신규 파일 정보 insert 전에 기존에 있던 내용 확인해서 사용 안함 처리.
                List<FileManagerDto> buyerFileList = this.getFileListByRelationSeqAndFileTypes(buyerIdentificationSeq, FileTypeEnum.getBuyerFileType());

                if (!ObjectUtils.isEmpty(buyerFileList)) {

                    for (FileManagerDto tmpDto : tmpFileList) {
                        for (FileManagerDto buyerDto : buyerFileList) { // 파일 하나만 수정해야 할 때를 대비해서 사용 안함 처리 파일 타입별로 나눔.
                            if (Objects.equals(FileTypeEnum.TMP_BUYER_BUSINESS_REGISTRATION.getCode(), tmpDto.getFileType())
                                    && Objects.equals(FileTypeEnum.BUYER_BUSINESS_REGISTRATION.getCode(), buyerDto.getFileType())) {
                                updateFileManagerList.add(buyerDto.updateDelYEntity(userCode));
                                awsUtils.delete(buyerDto.getFilePath() + "/" + buyerDto.getUploadFileName());
                                continue;
                            }
                            if (Objects.equals(FileTypeEnum.TMP_LICENSE.getCode(), tmpDto.getFileType())
                                    && Objects.equals(FileTypeEnum.BUYER_LICENSE.getCode(), buyerDto.getFileType())) {
                                updateFileManagerList.add(buyerDto.updateDelYEntity(userCode));
                                awsUtils.delete(buyerDto.getFilePath() + "/" + buyerDto.getUploadFileName());
                            }
                        }
                    }

                    fileManagerRepository.saveAll(updateFileManagerList);
                }

                // 새로 insert.
                List<FileManager> insertFileManagerList = new ArrayList<>();
                for (FileManagerDto dto : tmpFileList) {
                    
                    dto.setRelationSeq(buyerIdentificationSeq);
                    dto.setFilePath(FileTypeEnum.getMoveFilePath(dto.getFileType()));
                    dto.setFileType(FileTypeEnum.getMoveFileCode(dto.getFileType()));
                    
                    insertFileManagerList.add(dto.insertEntity(userCode)); // 신규 파일에 대한 정보 insert.
                }
                fileManagerRepository.saveAll(insertFileManagerList);
            }
        }else{
            log.info("회원 승인/반려 시 처리할 파일 내용 없음.");
        }

    }

    /**
     * 파일 리스트 조회 = 하나의 연관시퀀스 + 파일 타입
     */
    public List<FileManagerDto> getFileListByRelationSeqAndFileTypes(Long relationSeq, List<Integer> fileTypes) {
        if( ObjectUtils.isEmpty(relationSeq) || ObjectUtils.isEmpty(fileTypes) || fileTypes.size() == 0 ){
            return new ArrayList<>();
        }
        return fileManagerRepositorySupport.findFileList(relationSeq, fileTypes, null);
    }

    /**
     * 파일 리스트 조회 = 업로드 파일명 리스트 (확장자 포함되어 있음.)
     */
    public List<FileManagerDto> getFileListByFileUploadNames(List<String> fileUploadNames) {
        return fileManagerRepositorySupport.findFileList(null, null, fileUploadNames);
    }


}
