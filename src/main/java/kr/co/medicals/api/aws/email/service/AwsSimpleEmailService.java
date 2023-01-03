package kr.co.medicals.api.aws.email.service;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsSimpleEmailService {

    @Value("${aws.api.email.accessKey}")
    private String awsSesAccessKey;

    @Value("${aws.api.email.secretKey}")
    private String awsSesSecretKey;

    private static final String FROM = "no_reply@xxx.co.kr";

    public void send(String to, String subject, String body) {
        try {
            BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(awsSesAccessKey, awsSesSecretKey);
            AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(basicAWSCredentials);
            AmazonSimpleEmailService client =
                    AmazonSimpleEmailServiceClientBuilder.standard()
                            .withCredentials(credentialsProvider)
                            .withRegion(Regions.AP_NORTHEAST_2).build();
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(new Destination().withToAddresses(to))
                    .withSource(FROM)
                    .withMessage(new Message()
                            .withSubject(new Content().withCharset("UTF-8").withData(subject))
                            .withBody(new Body()
                                    .withHtml(new Content().withCharset("UTF-8").withData(body))));
//                                    .withText(new Content().withCharset("UTF-8").withData("")))
            client.sendEmail(request);
            log.info("===== EMAIL SEND SUCCESS ===== TO:[{}], SUBJECT[{}], TEMPLATE[{}]", to, subject, body);
        } catch (Exception ex) {
            log.error("The email was not sent. Error message: {}", ex.getMessage());
            ex.printStackTrace();
        }
    }
}
