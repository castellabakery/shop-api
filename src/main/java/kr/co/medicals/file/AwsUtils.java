package kr.co.medicals.file;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import kr.co.medicals.common.ApiRequestFailExceptionMsg;
import kr.co.medicals.common.constants.ApiResponseCodeConstants;
import kr.co.medicals.common.enums.FileTypeEnum;
import kr.co.medicals.file.domain.dto.FileManagerDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.MediaType;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
public class AwsUtils {
    private AmazonS3 s3Client;
    private String accessKey = ""; // IAM Access Key
    private String secretKey = "";
    private Regions clientRegion = Regions.AP_NORTHEAST_2; // 사용자 지역
    private String bucket = ""; // 버킷 이름

    private AwsUtils() {
        createS3Client();
    }

    static private AwsUtils instance = null;

    public static AwsUtils getInstance() {
        if (instance == null) {
            return new AwsUtils();
        } else {
            return instance;
        }
    }

    //aws S3 client 생성
    private void createS3Client() {

        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        this.s3Client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(clientRegion)
                .build();
    }

    public List<FileManagerDto> uploadMultipartFile(MultipartFile[] multipartFile, Long relationSeq, int fileType) {

        List<FileManagerDto> fileManagerDtoList = new ArrayList<>();

        if (ObjectUtils.isEmpty(multipartFile)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "file");
        }

        for (MultipartFile file : multipartFile) {

            if (Objects.equals(file.getOriginalFilename(), "")) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "file");
            }

            ObjectMetadata metadata = new ObjectMetadata();

            String getFileName = FilenameUtils.getName(file.getOriginalFilename());

            String fileName = getFileName.substring(0, getFileName.lastIndexOf("."));
            String extension = getFileName.substring(getFileName.indexOf(".") + 1);
            String hashName = UUID.randomUUID().toString().replaceAll("-", "");
            String uploadFileName = hashName + "." + extension;
            Long fileSize = file.getSize();

            if (fileSize >= 1048576) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "FILE SIZE EXCEEDED. MAXIMUM SIZE 1048576.");
            }

            if (extension.equalsIgnoreCase("jpg")) {
                metadata.setContentType(MediaType.IMAGE_JPEG_VALUE);
            } else if (extension.equalsIgnoreCase("gif")) {
                metadata.setContentType(MediaType.IMAGE_GIF_VALUE);
            } else if (extension.equalsIgnoreCase("png")) {
                metadata.setContentType(MediaType.IMAGE_PNG_VALUE);
            }

            metadata.setContentLength(fileSize);
            metadata.setContentDisposition("attachment; filename=" + URLEncoder.encode(getFileName, StandardCharsets.UTF_8));

            StringBuilder builder = new StringBuilder();
            builder.append(FileTypeEnum.getFilePath(fileType));
            builder.append("/");
            builder.append(uploadFileName);

            try {
                this.s3Client.putObject(bucket, builder.toString(), file.getInputStream(), metadata);
            } catch (SdkClientException | IOException e) {
                e.printStackTrace();
            }

            FileManagerDto fileManagerDto =
                    FileManagerDto
                            .byCreate()
                            .fileSize(Long.valueOf(fileSize).intValue())
                            .fileExtension(extension)
                            .orgFileName(fileName)
                            .uploadFileName(uploadFileName)
                            .filePath(FileTypeEnum.getFilePath(fileType))
                            .relationSeq(relationSeq)
                            .fileType(fileType)
                            .build();

            fileManagerDtoList.add(fileManagerDto);
        }
        return fileManagerDtoList;
    }

    public void copy(String orgKey, String copyKey) {
        try {
            //Copy 객체 생성
            CopyObjectRequest copyObjRequest = new CopyObjectRequest(
                    this.bucket, orgKey,
                    this.bucket, copyKey
            );
            //Copy
            this.s3Client.copyObject(copyObjRequest);
        } catch (AmazonServiceException e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
    }

    public void delete(String key) {
        try {
            // 현재 권한이 막혀서 403 오류 남.
            this.s3Client.deleteObject(new DeleteObjectRequest(this.bucket, key));
        } catch (AmazonServiceException e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        }
    }


}
