package cn.itcast.core.listener;

import cn.itcast.core.service.SolrManagerService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

public class ItemSearchListener implements MessageListener {
    @Autowired
    private SolrManagerService solrManagerService;
    @Override
    public void onMessage(Message message) {
        //为了方便获取文件 将message装换为activemq对象
        ActiveMQTextMessage amt=(ActiveMQTextMessage)message;
        try {
            String goodsId = amt.getText();
            //将根据商品id查到的商品详情保存到solr中
            solrManagerService.saveItemToSolr(Long.valueOf(goodsId));
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
