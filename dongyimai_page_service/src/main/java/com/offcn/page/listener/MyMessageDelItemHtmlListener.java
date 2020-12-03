package com.offcn.page.listener;

import com.offcn.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.io.Serializable;

@Component
public class MyMessageDelItemHtmlListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {

        try {
            ObjectMessage objectMessage = (ObjectMessage) message;
            Long[] ids = (Long[]) objectMessage.getObject();
            boolean flag = itemPageService.delItemHtml(ids);
            if (flag){
                System.out.println("删除成功");
            }else {
                System.out.println("删除失败");
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
