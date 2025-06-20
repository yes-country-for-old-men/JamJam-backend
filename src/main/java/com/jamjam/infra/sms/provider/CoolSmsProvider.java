package com.jamjam.infra.sms.provider;


import com.jamjam.global.exception.ApiException;
import com.jamjam.user.exception.UserError;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CoolSmsProvider {

    @Value("${spring.cool-sms.api-key}")
    private String apiKey;

    @Value("${spring.cool-sms.api-secret}")
    private String apiSecret;

    @Value("${spring.cool-sms.caller-number}")
    private String callerNumber;

    public void sendVerificationCode(String phoneNumber, String verificationCode) throws Exception {
        DefaultMessageService messageService =  NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
        Message message = new Message();
        message.setFrom(callerNumber);
        message.setTo(phoneNumber);
        message.setText("[퐁당] 인증번호 [" + verificationCode + "]를 입력해주세요.");

        try {
            messageService.send(message);
        } catch (NurigoMessageNotReceivedException exception) {
            System.out.println(exception.getFailedMessageList());
            throw new ApiException(UserError.SMS_SEND_FAILED);
        }
    }
}
