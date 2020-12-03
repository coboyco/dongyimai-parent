package com.offcn.page.listener;

import com.offcn.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Component
public class MyMessageGenHtmlListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {




        try {
            TextMessage textMessage = (TextMessage) message;
            String strGoodsId = strGoodsId = textMessage.getText();
            boolean flag = itemPageService.genItemHtml(Long.valueOf(strGoodsId));
            if(flag){
                System.out.println("页面生成成功");
            }else {
                System.out.println("页面生成失败");
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
