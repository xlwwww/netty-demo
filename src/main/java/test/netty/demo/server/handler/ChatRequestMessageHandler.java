package test.netty.demo.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import test.netty.demo.message.ChatRequestMessage;
import test.netty.demo.message.ChatResponseMessage;
import test.netty.demo.server.session.SessionFactory;

@ChannelHandler.Sharable
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ChatRequestMessage chatRequestMessage) throws Exception {
        String to = chatRequestMessage.getTo();
        Channel channel = SessionFactory.getSession().getChannel(to);
        if (channel == null) {
            // 用户不在线，给发送者发送消息
            channelHandlerContext.writeAndFlush(new ChatResponseMessage(false, "用户不在线"));
        } else {
            channel.writeAndFlush(new ChatResponseMessage(chatRequestMessage.getFrom(), chatRequestMessage.getContent()));
        }
    }
}
