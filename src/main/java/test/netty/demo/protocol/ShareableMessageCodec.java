package test.netty.demo.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;
import test.netty.demo.config.Config;
import test.netty.demo.message.Message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * MessageToMessageCodec 从一个完整消息message解码成另一个消息
 * 必须和LengthFieldBasedFrameDecoder之后使用，确保消息是完整的
 * 该类是可共享的
 */
@Slf4j
public class ShareableMessageCodec extends MessageToMessageCodec<ByteBuf, Message> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, List<Object> list) throws Exception {
        ByteBuf byteBuf = channelHandlerContext.alloc().buffer();
        // 1. check sum
        byteBuf.writeBytes(new byte[]{1, 2, 3, 4});
        // 2. version
        byteBuf.writeByte(1);
        Serializer.Algorithm serializerAlgorithm = Config.getSerializerAlgorithm();
        // 3. 序列化方式 0-jdk 1-json
        byteBuf.writeByte(serializerAlgorithm.ordinal());
        // 4. 指令类型
        byteBuf.writeByte(message.getMessageType());
        // 5. 请求序号
        byteBuf.writeInt(message.getSequenceId());
        byteBuf.writeByte(0xff); // 填充1字节
        // object序列化
        byte[] bytes = serializerAlgorithm.serialize(message);
        // 6. 长度
        byteBuf.writeInt(bytes.length);
        // 7. 正文
        byteBuf.writeBytes(bytes);
        list.add(byteBuf);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int magicNum = byteBuf.readInt();
        byte version = byteBuf.readByte();
        byte serialize_type = byteBuf.readByte();
        byte messageType = byteBuf.readByte();
        Class<?> messageClass = Message.getMessageClass(messageType);
        int sequenceId = byteBuf.readInt();
        byteBuf.readByte();
        int len = byteBuf.readInt();
        // 反序列化
        byte[] bytes = new byte[len];
        byteBuf.readBytes(bytes, 0, len);
        Serializer.Algorithm algorithm = Serializer.Algorithm.values()[serialize_type];
        // 确定消息类型
        Message message = (Message) algorithm.deserialize(messageClass, bytes);
        // 传入handler
        list.add(message);
        log.debug("{},{},{},{},{},{}", magicNum, version, serialize_type, messageType, sequenceId, len);
        log.debug("{}", message);
    }
}
