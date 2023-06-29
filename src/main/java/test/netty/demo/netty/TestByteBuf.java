package test.netty.demo.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class TestByteBuf {
    public static void main(String[] args) {
        // 支持扩容
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
    }
}
