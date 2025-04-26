package com.maidf.demo.ctr;

import java.io.IOException;

import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class Hello {


    @GetMapping("hello/{msg}")
    public String sayHello(@PathVariable String msg) {
        return msg;
    }

    @GetMapping("send/{msg}")
    public String sendMsg(@PathVariable String msg) throws ClientException, IOException {
        String endpoint = "localhost:8080";
        // 需要提前创建topic
        String topic = "TestTopic";
        ClientServiceProvider provider = ClientServiceProvider.loadService();
        ClientConfigurationBuilder builder = ClientConfiguration.newBuilder().setEndpoints(endpoint);
        ClientConfiguration configuration = builder.build();
        // 初始化Producer时需要设置通信配置以及预绑定的Topic。
        Producer producer = provider.newProducerBuilder()
                .setTopics(topic)
                .setClientConfiguration(configuration)
                .build();

        Message msgObj = provider.newMessageBuilder()
                .setTopic(topic)
                // 设置消息索引键，可根据关键字精确查找某条消息。
                .setKeys("messageKey")
                // 设置消息Tag，用于消费端根据指定Tag过滤消息。
                .setTag("messageTag")
                // 消息体。
                .setBody(msg.getBytes())
                .build();
        try {
            SendReceipt sendReceipt = producer.send(msgObj);
            log.info("Send message successfully, messageId={}", sendReceipt.getMessageId());
        } catch (Exception e) {
            log.error("发送失败");
            return e.getMessage();
        }
        producer.close();
        return new String("发送成功！");
    }

}
