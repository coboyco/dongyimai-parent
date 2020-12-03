package com.offcn.search.listener;

import com.alibaba.fastjson.JSON;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;

@Component
public class MyMessageListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        String messageText = null;
        try {
            messageText = textMessage.getText();
            //将接收到的textMessage转为List集合
            List<TbItem> itemList = JSON.parseArray(messageText, TbItem.class);
            //遍历list集合
            for (TbItem item : itemList) {
                String spec = item.getSpec();
                Map map = JSON.parseObject(spec, Map.class);
                item.setSpecMap(map);
            }
            //导入到索引库中
            itemSearchService.importList(itemList);
            System.out.println("成功导入到索引库中");
        } catch (JMSException e) {
            e.printStackTrace();
        }


    }
}
