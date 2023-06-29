package test.netty.demo.rpcserver.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import test.netty.demo.message.RpcResponseMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ChannelHandler.Sharable
public class RpcResponseMessageHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {
    public static final Map<Integer, Promise<Object>> promiseMap = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) throws Exception {
        log.info("msg = {}", msg);
        // 拿到return object
        Promise<Object> promise = promiseMap.remove(msg.getSequence_id());
        // 从io线程向业务线程通过promise传递消息
        if (promise != null) {
            if (msg.getReturnValue() != null) {
                promise.setSuccess(msg.getReturnValue());
            } else if (msg.getExceptionValue() != null) {
                promise.setFailure(msg.getExceptionValue());
            }
        }
    }
}
