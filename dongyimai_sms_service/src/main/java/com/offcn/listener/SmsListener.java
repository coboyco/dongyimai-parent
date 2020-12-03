package com.offcn.listener;

import com.offcn.utils.SmsTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

@Component
public class SmsListener implements MessageListener {

    @Autowired
    private SmsTemplate smsTemplatel;

    @Override
    public void onMessage(Message message) {
        //发送过来的消息是一map的形式存储
        if(message instanceof MapMessage){
            MapMessage mapMessage = (MapMessage) message;
            //获取手机号码和验证码
            try {
                String mobile = mapMessage.getString("mobile");
                String smscode = mapMessage.getString("smscode");
               //收到中间件发送过来的参数信息，就调用发短信服务发送验证码
                String result = smsTemplatel.smsSend(mobile, smscode);
                System.out.println("短信发送：" + result);
            } catch (JMSException e) {
                e.printStackTrace();
            }

        }
    }
}
