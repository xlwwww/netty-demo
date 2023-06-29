package test.netty.demo.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import test.netty.demo.server.session.SessionFactory;

@ChannelHandler.Sharable
@Slf4j
public class QuitHandler extends ChannelInboundHandlerAdapter {
    // 连接断开时触发
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // channel移除
        SessionFactory.getSession().unbind(ctx.channel());
        log.info("{} 断开", ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        SessionFactory.getSession().unbind(ctx.channel());
        log.info("异常{} 断开", ctx.channel());
        super.exceptionCaught(ctx, cause);
    }
}
