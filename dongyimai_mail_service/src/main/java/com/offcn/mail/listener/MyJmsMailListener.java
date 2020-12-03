package com.offcn.mail.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

@Component
public class MyJmsMailListener implements MessageListener {

    @Autowired
    private JavaMailSenderImpl javaMailSender;

    @Override
    public void onMessage(Message message) {

        try {
            //接收收件的邮箱和正文
            MapMessage mapMessage = (MapMessage) message;
            //接收要发送人的邮箱 和正文
            String toEmail = mapMessage.getString("toEmail");
            String text = mapMessage.getString("text");
            //发送邮箱和邮箱信息
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("coboyco@126.com");
            msg.setTo(toEmail);
            msg.setSubject("东易买注册消息官方提示");
            msg.setText(text);
            //发送邮件
            javaMailSender.send(msg);
            System.out.println("发送完毕");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}

