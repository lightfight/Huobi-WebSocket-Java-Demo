package com.game.blockchain.websocket.client;

import com.alibaba.fastjson.JSONObject;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * <pre>
 *
 *
 *  web socket 客户端测试类
 *
 *
 * </pre>
 *
 * @author ursobeautiful@qq.com
 * @since 2018/6/30 上午11:10
 */
public class WebSocketClientTest {

    @Test
    public void connect() throws Exception {

        final String url = "ws://api.huobi.pro:80/ws";
        final WebSocketClient client = new WebSocketClient(url);
        client.open();

        // ## 发送string
        JSONObject sub = new JSONObject();
        sub.put("sub", "market.btcusdt.kline.1min");
        sub.put("id", "id1000");
        client.sendText(sub.toString());

        // ## 阻塞住线程
        CountDownLatch latch = new CountDownLatch(1);
        latch.await();
    }

}
