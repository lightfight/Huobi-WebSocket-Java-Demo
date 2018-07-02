package com.game.blockchain.websocket.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.net.URI;

public class WebSocketClient {

    private final URI uri;
    private Channel ch;
    private static final EventLoopGroup group = new NioEventLoopGroup();

    public WebSocketClient(final String uri) {
        this.uri = URI.create(uri);
    }

    public void open() throws Exception {
        Bootstrap b = new Bootstrap();
        String protocol = uri.getScheme();
        if (!"ws".equals(protocol)) {
            throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }

        final WebSocketClientHandler handler =
                new WebSocketClientHandler(
                        WebSocketClientHandshakerFactory.newHandshaker(
                                uri, WebSocketVersion.V13, null, false, HttpHeaders.EMPTY_HEADERS, 1280000));

        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("http-codec", new HttpClientCodec());
                        pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
                        pipeline.addLast("ws-handler", handler);
                    }
                });

        ch = b.connect(uri.getHost(), uri.getPort()).sync().channel();
        handler.handshakeFuture().sync();
    }

    /**
     * 发送文本
     *
     * @param text
     */
    public void sendText(final String text){
        ch.writeAndFlush(new TextWebSocketFrame(text));
    }

    /**
     * 发送二进制数据
     *
     * @param bytes
     */
    public void sendBytes(byte[] bytes) {
        ByteBuf buf = Unpooled.buffer(bytes.length);
        buf.writeBytes(bytes);
        ch.writeAndFlush(new BinaryWebSocketFrame(buf));
    }
}