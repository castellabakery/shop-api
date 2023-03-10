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
     * ?????? ??????
     */
    public void upload(MultipartFile[] multipartFileList, Long relationSeq, int fileType, String userCode) {

        // ???????????? ?????? ???????????? ?????? ?????? ??? ?????? ?????? ???????????? buyerIdentificationCode ???????????? ???.(???????????? ?????? ?????????)
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

        // ?????? ????????? ????????? userCode ??????????????? ??????.
        if (ObjectUtils.isEmpty(userCode)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY");
        }

        List<FileManagerDto> fileManagerDtoList = awsUtils.uploadMultipartFile(multipartFileList, relationSeq, fileType);
        addFilesInfo(fileManagerDtoList, userCode);
    }

    /**
     * ?????? ?????????, ?????? ?????? = ????????? ???????????? ?????????????????? ?????????, ??????????????? ????????? ????????? ????????? ???????????? ??????
     */
    public void uploadAndDelete(MultipartFile[] multipartFileList, Long relationSeq, int fileType, List<String> alreadyFileName) {

        // ?????? ??????????????? ?????? ????????? ?????? ??? ??? ??????.
        if (ObjectUtils.isEmpty(relationSeq) || fileType == 0) {
            if (ObjectUtils.isEmpty(alreadyFileName)) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "multipartFileList");
            }
        }

        // ?????? ?????? ????????? ?????????
        if (!ObjectUtils.isEmpty(relationSeq) && fileType > 0) {
            List<FileManagerDto> fileManagerDtoList = awsUtils.uploadMultipartFile(multipartFileList, relationSeq, fileType);
            addFilesInfo(fileManagerDtoList, SessionUtil.getUserCode());
        }

        // ?????????????????? ????????? ?????????
        if (!ObjectUtils.isEmpty(alreadyFileName)) {
            this.delete(alreadyFileName);
        }

    }

    /**
     * ?????? ?????? ?????? ??? ?????? ?????? ??????
     */
    private void copyFile(FileManagerDto beforeFileInfo){

        FileTypeEnum fileTypeEnum = FileTypeEnum.getMoveFileTypeEnum(beforeFileInfo.getFileType());

        // ?????? ?????? ????????? ????????????
        fileManagerRepository.save(beforeFileInfo.updateDelYEntity(SessionUtil.getUserCode()));

        // ?????? ??? ?????? ?????? ?????? ??????
        fileManagerRepository.save(new FileManagerDto().copyDataInsertEntity(beforeFileInfo, fileTypeEnum));

        // ?????? ??????
        String orgKey = beforeFileInfo.getFilePath() + "/" + beforeFileInfo.getUploadFileName();
        String copyKey = fileTypeEnum.getPath() + "/" + beforeFileInfo.getUploadFileName();
        awsUtils.copy(orgKey, copyKey);

        // ?????? ??????
        String deleteKey = beforeFileInfo.getFilePath() + "/" + beforeFileInfo.getUploadFileName();
        awsUtils.delete(deleteKey);
    }

    /**
     * ?????? ?????? - ????????? ??????
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
     * ?????? ?????? - ??? ??????
     */
    private void deleteFile(FileManagerDto fileInfo){

        fileManagerRepository.save(fileInfo.updateDelYEntity(SessionUtil.getUserCode()));

        String deleteKey = fileInfo.getFilePath() + "/" + fileInfo.getUploadFileName();
        awsUtils.delete(deleteKey);

    }


    /**
     * ?????? ?????? ?????? ??????
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

        // ????????? ????????? ???????????? ?????? ????????? ?????? ????????? ??? ??? ??????.
        for (FileManagerDto dto : fileManagerDtoList) {
            if (Objects.equals(SessionUtil.getUserType(), "buyer")
                    && !Objects.equals(dto.getCreatedId(), SessionUtil.getUserCode())) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "Unauthorized");
            }
        }

        this.deleteFiles(fileManagerDtoList);

    }

    /**
     * ?????? ??? ????????? ?????? ?????? ??????/?????? ??????
     */
    public void copyTmpBuyerToBuyer(Long tmpSeq, Long buyerIdentificationSeq, String changeState) {

        List<FileManagerDto> tmpFileList = this.getFileListByRelationSeqAndFileTypes(tmpSeq, FileTypeEnum.getTmpBuyerFileType());
        List<FileManager> updateFileManagerList = new ArrayList<>();

        String userCode = SessionUtil.getUserCode();

        if (!ObjectUtils.isEmpty(tmpFileList)) {
            if (Objects.equals(changeState, BuyerStateConstants.REJECTED)) { // ???????????? ?????? ??? ?????? ??? ?????? ??????.
                for (FileManagerDto dto : tmpFileList) {
                    updateFileManagerList.add(dto.updateDelYEntity(userCode)); // ?????? ?????? ?????? ?????? ??????
                    awsUtils.delete(dto.getFilePath() + "/" + dto.getUploadFileName());
                }
                fileManagerRepository.saveAll(updateFileManagerList);
            } else if (Objects.equals(changeState, BuyerStateConstants.DONE)) {
                for (FileManagerDto dto : tmpFileList) { // ???????????? ?????? ??? ?????? ??????????????? ?????? ?????? ??? ????????? ??????.
                    String copyKey = "";
                    if (Objects.equals(FileTypeEnum.TMP_BUYER_BUSINESS_REGISTRATION.getCode(), dto.getFileType())) {
                        copyKey = FileTypeEnum.getFilePath(FileTypeEnum.BUYER_BUSINESS_REGISTRATION.getCode()) + "/" + dto.getUploadFileName();
                    } else if (Objects.equals(FileTypeEnum.TMP_LICENSE.getCode(), dto.getFileType())) {
                        copyKey = FileTypeEnum.getFilePath(FileTypeEnum.BUYER_LICENSE.getCode()) + "/" + dto.getUploadFileName();
                    }
                    String orgKey = dto.getFilePath() + "/" + dto.getUploadFileName();
                    awsUtils.copy(orgKey, copyKey); // ???????????? ??????????????? ??????.
                    updateFileManagerList.add(dto.updateDelYEntity(userCode)); // ?????? ?????? ?????? ?????? ??????.
                    awsUtils.delete(dto.getFilePath() + "/" + dto.getUploadFileName());
                }
                fileManagerRepository.saveAll(updateFileManagerList);

                // ?????? ?????? ?????? insert ?????? ????????? ?????? ?????? ???????????? ?????? ?????? ??????.
                List<FileManagerDto> buyerFileList = this.getFileListByRelationSeqAndFileTypes(buyerIdentificationSeq, FileTypeEnum.getBuyerFileType());

                if (!ObjectUtils.isEmpty(buyerFileList)) {

                    for (FileManagerDto tmpDto : tmpFileList) {
                        for (FileManagerDto buyerDto : buyerFileList) { // ?????? ????????? ???????????? ??? ?????? ???????????? ?????? ?????? ?????? ?????? ???????????? ??????.
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

                // ?????? insert.
                List<FileManager> insertFileManagerList = new ArrayList<>();
                for (FileManagerDto dto : tmpFileList) {
                    
                    dto.setRelationSeq(buyerIdentificationSeq);
                    dto.setFilePath(FileTypeEnum.getMoveFilePath(dto.getFileType()));
                    dto.setFileType(FileTypeEnum.getMoveFileCode(dto.getFileType()));
                    
                    insertFileManagerList.add(dto.insertEntity(userCode)); // ?????? ????????? ?????? ?????? insert.
                }
                fileManagerRepository.saveAll(insertFileManagerList);
            }
        }else{
            log.info("?????? ??????/?????? ??? ????????? ?????? ?????? ??????.");
        }

    }

    /**
     * ?????? ????????? ?????? = ????????? ??????????????? + ?????? ??????
     */
    public List<FileManagerDto> getFileListByRelationSeqAndFileTypes(Long relationSeq, List<Integer> fileTypes) {
        if( ObjectUtils.isEmpty(relationSeq) || ObjectUtils.isEmpty(fileTypes) || fileTypes.size() == 0 ){
            return new ArrayList<>();
        }
        return fileManagerRepositorySupport.findFileList(relationSeq, fileTypes, null);
    }

    /**
     * ?????? ????????? ?????? = ????????? ????????? ????????? (????????? ???????????? ??????.)
     */
    public List<FileManagerDto> getFileListByFileUploadNames(List<String> fileUploadNames) {
        return fileManagerRepositorySupport.findFileList(null, null, fileUploadNames);
    }


}
