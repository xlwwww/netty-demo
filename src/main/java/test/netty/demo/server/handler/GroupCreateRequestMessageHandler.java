package test.netty.demo.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import test.netty.demo.message.ChatResponseMessage;
import test.netty.demo.message.GroupCreateRequestMessage;
import test.netty.demo.message.GroupCreateResponseMessage;
import test.netty.demo.server.session.Group;
import test.netty.demo.server.session.GroupSessionFactory;
import test.netty.demo.server.session.SessionFactory;

import java.util.List;
import java.util.Set;

@ChannelHandler.Sharable
public class GroupCreateRequestMessageHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, GroupCreateRequestMessage groupCreateRequestMessage) throws Exception {
        Set<String> members = groupCreateRequestMessage.getMembers();
        String groupName = groupCreateRequestMessage.getGroupName();
        Group group = GroupSessionFactory.getGroupSession()
                .createGroup(groupName, members);
        if (group != null) {
            // 对members拉群
            List<Channel> memberChannels = GroupSessionFactory.getGroupSession().getMemberChannels(groupName);
            for (Channel channel : memberChannels) {
                channel.writeAndFlush(new GroupCreateResponseMessage(true, "您已被拉入" + groupName));
            }
            channelHandlerContext.writeAndFlush(new GroupCreateResponseMessage(true, "成功建群"));
        } else {
            channelHandlerContext.writeAndFlush(new GroupCreateResponseMessage(false, "重复建群"));
        }
    }
}
