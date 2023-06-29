package test.netty.demo.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import test.netty.demo.message.LoginRequestMessage;
import test.netty.demo.message.LoginResponseMessage;
import test.netty.demo.server.service.UserServiceFactory;
import test.netty.demo.server.session.SessionFactory;

@ChannelHandler.Sharable
public class LoginRequestMessageSimpleChannelInboundHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, LoginRequestMessage loginRequestMessage) throws Exception {
        String userName = loginRequestMessage.getUserName();
        boolean login = UserServiceFactory.getUserService().login(userName, loginRequestMessage.getPassword());
        LoginResponseMessage responseMessage = login ?
                new LoginResponseMessage(true, "success") :
                new LoginResponseMessage(false, "false");
        if (login) {
            // 保存username-channel关系
            SessionFactory.getSession().bind(channelHandlerContext.channel(), userName);
        }
        channelHandlerContext.writeAndFlush(responseMessage);
    }
}
