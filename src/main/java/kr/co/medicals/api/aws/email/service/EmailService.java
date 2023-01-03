package kr.co.medicals.api.aws.email.service;

import kr.co.medicals.api.aws.email.enums.EmailTemplateType;
import kr.co.medicals.common.util.DateFormatUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${xxx.domain}")
    private String xxxDomainUrl;

    private static final String EMAIL_TEMPLATE_PATH = "/static/";

    final HttpServletRequest httpServletRequest;

    final AwsSimpleEmailService awsSimpleEmailService;

    /**
     * 회원가입 승인시 발송
     * @param to
     * @param userId
     */
    public void sendForSignUpAccept(String to, String userId) {
        String emailTempalte =  this.getHtmlBody(EmailTemplateType.SIGNUP_ACCEPT)
                .replace("${loginLink}", xxxDomainUrl)
                .replace("${userid}", userId);

        log.info("===== EMAIL SEND REQUEST ===== : TO:[{}], SUBJECT[{}], TEMPLATE[{}],", to, EmailTemplateType.SIGNUP_ACCEPT.getSubject(), emailTempalte);
        awsSimpleEmailService.send(to, EmailTemplateType.SIGNUP_ACCEPT.getSubject(), emailTempalte);
    }

    /**
     * 회원가입 반려시 발송
     * @param to
     * @param rejectMsg
     */
    public void sendForSignUpReject(String to, String rejectMsg) {
        String emailTempalte =  this.getHtmlBody(EmailTemplateType.SIGNUP_REJECT)
                .replace("${rejectMsg}", rejectMsg);

        log.info("===== EMAIL SEND REQUEST ===== TO:[{}], SUBJECT[{}], TEMPLATE[{}]", to, EmailTemplateType.SIGNUP_REJECT.getSubject(), emailTempalte);
        awsSimpleEmailService.send(to, EmailTemplateType.SIGNUP_REJECT.getSubject(), emailTempalte);
    }

    /**
     * 상품 구매시 발송
     */
    public void sendForItemPurchase(String to, String userName, String orderNo, String itemDesc
            , String usePoint, String paymentAmtCard, String paymentAmt) {
        String emailTempalte =  this.getHtmlBody(EmailTemplateType.ITEM_PURCHASE)
                .replace("${loginLink}", xxxDomainUrl)
                .replace("${userName}", userName)
                .replace("${orderNo}", orderNo)
                .replace("${purchaseDate}", LocalDateTime.now().format(DateFormatUtils.PERIOD_DATE))
                .replace("${itemDesc}", itemDesc)
                .replace("${usePoint}", usePoint)
                .replace("${paymentAmtCard}", paymentAmtCard)
                .replace("${paymentAmt}", paymentAmt);

        log.info("===== EMAIL SEND REQUEST ===== : TO:[{}], SUBJECT[{}], TEMPLATE[{}],", to, EmailTemplateType.ITEM_PURCHASE.getSubject(), emailTempalte);
        awsSimpleEmailService.send(to, EmailTemplateType.ITEM_PURCHASE.getSubject(), emailTempalte);
    }

    /**
     * 상품 구매 취소시 발송
     */
    public void sendForItemPurchaseCancel(String to, String userName, String orderNo, String itemDesc
            , String usePoint, String paymentAmt, String paymentMethod) {
        String emailTempalte =  this.getHtmlBody(EmailTemplateType.ITEM_PURCHASE_CANCEL)
                .replace("${loginLink}", xxxDomainUrl)
                .replace("${userName}", userName)
                .replace("${orderNo}", orderNo)
                .replace("${purchaseDate}", LocalDateTime.now().format(DateFormatUtils.PERIOD_DATE))
                .replace("${itemDesc}", itemDesc)
                .replace("${usePoint}", usePoint)
                .replace("${paymentAmtCard}", paymentMethod.equals("VA") ? "0" : paymentAmt)
                .replace("${paymentAmt}", paymentMethod.equals("VA") ? paymentAmt : "0");

        log.info("===== EMAIL SEND REQUEST ===== : TO:[{}], SUBJECT[{}], TEMPLATE[{}],", to, EmailTemplateType.ITEM_PURCHASE_CANCEL.getSubject(), emailTempalte);
        awsSimpleEmailService.send(to, EmailTemplateType.ITEM_PURCHASE_CANCEL.getSubject(), emailTempalte);
    }

    /**
     * 상품 구매 환불시 발송
     */
    public void sendForItemPurchaseRefund(String to, String userName, String orderNo, String itemDesc
            , String usePoint, String paymentAmt, String paymentMethod) {
        String emailTempalte =  this.getHtmlBody(EmailTemplateType.ITEM_PURCHASE_REFUND)
                .replace("${loginLink}", xxxDomainUrl)
                .replace("${userName}", userName)
                .replace("${orderNo}", orderNo)
                .replace("${purchaseDate}", LocalDateTime.now().format(DateFormatUtils.PERIOD_DATE))
                .replace("${itemDesc}", itemDesc)
                .replace("${usePoint}", usePoint)
                .replace("${paymentAmtCard}", paymentMethod.equals("VA") ? "0" : paymentAmt)
                .replace("${paymentAmt}", paymentMethod.equals("VA") ? paymentAmt : "0");

        log.info("===== EMAIL SEND REQUEST ===== : TO:[{}], SUBJECT[{}], TEMPLATE[{}],", to, EmailTemplateType.ITEM_PURCHASE_REFUND.getSubject(), emailTempalte);
        awsSimpleEmailService.send(to, EmailTemplateType.ITEM_PURCHASE_REFUND.getSubject(), emailTempalte);
    }

    /**
     * 가입완료 이메일 템플릿
     * @param emailTemplateType
     * @return
     * @throws IOException
     */
    private String getHtmlBody(EmailTemplateType emailTemplateType) {
        try {
            String result = IOUtils.toString(getClass().getResourceAsStream(EMAIL_TEMPLATE_PATH + emailTemplateType.getTemplateFileName()), "UTF-8")
                    .replace("${imagePath}", this.makeImagePath());
//            log.info("=== Make Email Body template SUCCESS === : result [{}]", result);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("=== Make Email Body template FAIL ===");
            throw new RuntimeException(e);
        }
    }

    /**
     * 이미지 경로
     * @return
     */
    private String makeImagePath() {
        String domain = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName();
        String imagePath = domain;
        if (!"".equals(String.valueOf(httpServletRequest.getServerPort()))) {
            imagePath = imagePath + ":" + httpServletRequest.getServerPort();
        }
        return imagePath;
    }
}
