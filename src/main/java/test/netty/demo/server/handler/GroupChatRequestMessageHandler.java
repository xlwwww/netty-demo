package test.netty.demo.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import test.netty.demo.message.GroupChatRequestMessage;
import test.netty.demo.message.GroupChatResponseMessage;
import test.netty.demo.server.session.GroupSessionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@ChannelHandler.Sharable
public class GroupChatRequestMessageHandler extends SimpleChannelInboundHandler<GroupChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, GroupChatRequestMessage groupChatRequestMessage) throws Exception {
        String from = groupChatRequestMessage.getFrom();
        String groupName = groupChatRequestMessage.getGroupName();
        String content = groupChatRequestMessage.getContent();
        List<Channel> memberChannels = GroupSessionFactory.getGroupSession().getMemberChannels(groupName);
        for (Channel c : memberChannels) {
            c.writeAndFlush(new GroupChatResponseMessage(from, content));
        }
    }

}
