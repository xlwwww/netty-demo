package test.netty.demo.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import test.netty.demo.config.Config;
import test.netty.demo.message.LoginRequestMessage;
import test.netty.demo.message.Message;

import java.io.IOException;

@Slf4j
public class TestMessageCodec {
    public static void main(String[] args) throws Exception {
        LoggingHandler loggingHandler = new LoggingHandler(LogLevel.DEBUG);
        EmbeddedChannel channel = new EmbeddedChannel(
                // 入站，解析包
                new LengthFieldBasedFrameDecoder(1024, 12, 4),
                // 入站、出战
                loggingHandler,
                // 入站、出战
                new ShareableMessageCodec(),
                // 入站
                new SimpleChannelInboundHandler<LoginRequestMessage>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, LoginRequestMessage loginRequestMessage) throws Exception {
                        log.info("msg read..{},{}", loginRequestMessage.getUserName(), loginRequestMessage.getPassword());
                    }
                }
        );
        LoginRequestMessage message = new LoginRequestMessage("zhangsan", "123456");
//         // 测试encode方法
//        channel.writeOutbound(message);
        // decode
        ByteBuf buf = messageToByteBuf(message);
        // 写入了一条入站消息，触发了MessageCodec#decode()
        channel.writeInbound(buf);
    }

    private static ByteBuf messageToByteBuf(Message msg) throws IOException {
        ByteBuf out = ByteBufAllocator.DEFAULT.buffer();

        out.writeBytes(new byte[]{1, 2, 3, 4}); // 4字节的 魔数
        out.writeByte(1);                    // 1字节的 版本
        out.writeByte(Config.getSerializerAlgorithm().ordinal()); // 1字节的 序列化方式 0-jdk,1-json
        out.writeByte(msg.getMessageType()); // 1字节的 指令类型
        out.writeInt(msg.getSequenceId());   // 4字节的 请求序号 【大端】
        out.writeByte(0xff);                 // 1字节的 对其填充，只为了非消息内容 是2的整数倍


        final byte[] bytes = Config.getSerializerAlgorithm().serialize(msg);

        // 写入内容 长度
        out.writeInt(bytes.length);
        // 写入内容
        out.writeBytes(bytes);

        return out;
    }
}
