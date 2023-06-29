package test.netty.demo.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;
import test.netty.demo.message.Message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

@Slf4j
public class MessageCodec extends ByteToMessageCodec<Message> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, ByteBuf byteBuf) throws Exception {
        // 1. check sum
        byteBuf.writeBytes(new byte[]{1, 2, 3, 4});
        // 2. version
        byteBuf.writeByte(1);
        // 3. 序列化方式 0-jdk 1-json
        byteBuf.writeByte(0);
        // 4. 指令类型
        byteBuf.writeByte(message.getMessageType());
        // 5. 请求序号
        byteBuf.writeInt(message.getSequenceId());
        byteBuf.writeByte(0xff); // 填充1字节
        // object序列化
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
        outputStream.writeObject(message);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        // 6. 长度
        byteBuf.writeInt(bytes.length);
        // 7. 正文
        byteBuf.writeBytes(bytes);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int magicNum = byteBuf.readInt();
        byte version = byteBuf.readByte();
        byte serialize_type = byteBuf.readByte();
        byte messageType = byteBuf.readByte();
        int sequenceId = byteBuf.readInt();
        byteBuf.readByte();
        int len = byteBuf.readInt();
        // 反序列化
        byte[] bytes = new byte[len];
        byteBuf.readBytes(bytes, 0, len);
        if (serialize_type == 0) {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            // 字节数组转换为Message对象
            Message message = (Message) ois.readObject();
            // 传入handler
            list.add(message);
            log.debug("{},{},{},{},{},{}", magicNum, version, serialize_type, messageType, sequenceId, len);
            log.debug("{}", message);
        }
    }
}
