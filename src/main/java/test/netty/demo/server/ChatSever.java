package test.netty.demo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import test.netty.demo.protocol.MessageCodec;
import test.netty.demo.server.handler.*;


@Slf4j
public class ChatSever {
    public static void main(String[] args) {
        LoggingHandler loggingHandler = new LoggingHandler(LogLevel.INFO);
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup workers = new NioEventLoopGroup();
        try {
            LoginRequestMessageSimpleChannelInboundHandler LOGIN_HANDLER = new LoginRequestMessageSimpleChannelInboundHandler();
            ChatRequestMessageHandler CHAT_HANDLER = new ChatRequestMessageHandler();
            GroupCreateRequestMessageHandler GROUP_CREATE_HANDLER = new GroupCreateRequestMessageHandler();
            GroupChatRequestMessageHandler GROUP_CHAT_HANDLER = new GroupChatRequestMessageHandler();
            QuitHandler quitHandler = new QuitHandler();
            ChannelFuture channelFuture = new ServerBootstrap()
                    .group(boss, workers)
                    .option(ChannelOption.SO_BACKLOG, 2)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            // 用于判断是否读写空闲时间过长,触发IDLEstate#READ_IDLE
                            nioSocketChannel.pipeline().addLast(new IdleStateHandler(5, 0, 0));
                            nioSocketChannel.pipeline().addLast(new ChannelDuplexHandler() {
                                // 用于触发特殊事件
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    IdleStateEvent event = (IdleStateEvent) evt;
                                    if (event.state() == IdleState.READER_IDLE) {
                                        log.info("读空闲事件");
                                    }
                                    super.userEventTriggered(ctx, evt);
                                }
                            });
                            nioSocketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 12, 4));
                            nioSocketChannel.pipeline().addLast(loggingHandler);
                            nioSocketChannel.pipeline().addLast(new MessageCodec());
                            // 处理登陆消息
                            nioSocketChannel.pipeline().addLast(LOGIN_HANDLER);
                            // 处理聊天消息
                            nioSocketChannel.pipeline().addLast(CHAT_HANDLER);
                            // 处理建群消息
                            nioSocketChannel.pipeline().addLast(GROUP_CREATE_HANDLER);
                            // 处理群聊消息
                            nioSocketChannel.pipeline().addLast(GROUP_CHAT_HANDLER);
                            // 处理退出事件
                            nioSocketChannel.pipeline().addLast(quitHandler);
                        }
                    })
                    .bind(8080);
            channelFuture.sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("sever error", e);
        } finally {
            boss.shutdownGracefully();
            workers.shutdownGracefully();
        }
    }

}
