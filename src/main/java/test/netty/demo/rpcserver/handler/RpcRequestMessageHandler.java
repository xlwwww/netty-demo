package test.netty.demo.rpcserver.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import test.netty.demo.message.RpcRequestMessage;
import test.netty.demo.message.RpcResponseMessage;
import test.netty.demo.rpcserver.ServiceFactory;

import java.lang.reflect.Method;

@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage msg) throws Exception {
        RpcResponseMessage response = new RpcResponseMessage();
        try {
            Object service = ServiceFactory.getService(Class.forName(msg.getInterfaceName()));
            Method method = service.getClass().getMethod(msg.getMethodName(), msg.getParameterTypes());
            Object invoke = method.invoke(service, msg.getParameterValues());
            response.setSequence_id(msg.getSequence_id());
            response.setReturnValue(invoke);
        } catch (Exception e) {
            response.setExceptionValue(new RuntimeException("远程调用出错" + e.getCause().getMessage()));
        }
        ctx.writeAndFlush(response);
    }
}
