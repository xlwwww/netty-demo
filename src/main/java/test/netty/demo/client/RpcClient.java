package test.netty.demo.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import test.netty.demo.message.RpcRequestMessage;
import test.netty.demo.protocol.ShareableMessageCodec;
import test.netty.demo.rpcserver.handler.RpcResponseMessageHandler;
import test.netty.demo.server.service.HelloService;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

@Slf4j
public class RpcClient {
    private static Channel channel;
    private static final Object lock = new Object();

    public static void main(String[] args) {
        // 初始化channel
        initChannel();

        // 目标
        HelloService helloService = getService(HelloService.class);
        String result1 = helloService.sayHello("zhangsan");
        String result2 = helloService.sayHello("lisi");
        // 实际进行的步骤--> channel.writeAndFlush(msg)，因此在client侧创建Service的代理对象
        System.out.println(result1);
        System.out.println(result2);
    }

    public static <T> T getService(Class<T> service) {
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service},
                (proxy, method, args) -> {
                    // 实际进行的步骤
                    // 1. 将method调用转化为消息对象发出
                    Channel channel = getChannel();
                    int sequenceId = SequenceIdGenerator.getSequenceId();
                    channel.writeAndFlush(new RpcRequestMessage(
                            sequenceId,
                            service.getName(),
                            method.getName(),
                            method.getReturnType(),
                            method.getParameterTypes(),
                            args
                    ));
                    // 指定promise异步接受结果的线程
                    Promise<Object> promise = new DefaultPromise<>(channel.eventLoop());
                    RpcResponseMessageHandler.promiseMap.put(sequenceId, promise);
                    promise.addListener(future -> {
                    });
                    // 同步阻塞等待接受结果 msg-> RpcResponseMessageHandler -> await
                     promise.await();
                    if (promise.isSuccess()) {
                        return promise.getNow();
                    } else {
                        return new RuntimeException(promise.cause());
                    }
                });
    }

    public static Channel getChannel() {
        if (channel != null) {
            return channel;
        }
        synchronized (lock) {
            if (channel != null) {
                return channel;
            }
            initChannel();
        }
        return channel;
    }

    private static void initChannel() {
        NioEventLoopGroup workers = new NioEventLoopGroup();
        LoggingHandler loggingHandler = new LoggingHandler(LogLevel.INFO);
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();
        try {
            ChannelFuture channelFuture = new Bootstrap()
                    .channel(NioSocketChannel.class)
                    .group(workers)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 12, 4));
                            ch.pipeline().addLast(loggingHandler);
                            ch.pipeline().addLast(new ShareableMessageCodec());
                            ch.pipeline().addLast(RPC_HANDLER);
                        }
                    })
                    .connect(new InetSocketAddress("localhost", 8080));
            channel = channelFuture.sync().channel();
            channel.closeFuture().addListener(future -> workers.shutdownGracefully());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
