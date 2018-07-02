package com.game.util;

import io.netty.buffer.ByteBuf;

public class GameUtil {

    public static byte[] toBytes(ByteBuf buf){
        byte[] bytes = new byte[buf.capacity()];
        buf.readBytes(bytes);
        return bytes;
    }
}
