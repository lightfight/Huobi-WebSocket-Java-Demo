package com.game.blockchain.websocket.client;

import com.alibaba.fastjson.JSONObject;
import com.game.util.GZipUtil;
import com.game.util.GameUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {


    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketClientHandler.class);

    private final WebSocketClientHandshaker handShaker;
    private ChannelPromise handshakeFuture;

    public WebSocketClientHandler(final WebSocketClientHandshaker handShaker) {
        this.handShaker = handShaker;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        handShaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        System.out.println("WebSocket Client disconnected!");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        final Channel ch = ctx.channel();
        if (!handShaker.isHandshakeComplete()) {
            handShaker.finishHandshake(ch, (FullHttpResponse) msg);
            handshakeFuture.setSuccess();
            return;
        }

        final WebSocketFrame frame = (WebSocketFrame) msg;
        ByteBuf buf = frame.content();

        // ## 需要解压的二进制数据
        String str = GZipUtil.deToString(GameUtil.toBytes(buf));
        System.out.println(str);

        JSONObject json = JSONObject.parseObject(str);
        if (json.containsKey("ping")) {
            JSONObject pong = new JSONObject();
            pong.put("pong", json.get("ping"));

            System.out.println(pong);
            ctx.channel().writeAndFlush(new TextWebSocketFrame(pong.toString()));
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        cause.printStackTrace();

        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }

        ctx.close();
    }
}