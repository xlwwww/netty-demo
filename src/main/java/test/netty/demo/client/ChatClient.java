package test.netty.demo.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import test.netty.demo.message.*;
import test.netty.demo.protocol.MessageCodec;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ChatClient {
    public static void main(String[] args) {
        NioEventLoopGroup workers = new NioEventLoopGroup();
        LoggingHandler loggingHandler = new LoggingHandler(LogLevel.INFO);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean LOGIN = new AtomicBoolean(false);
        try {
            ChannelFuture channelFuture = new Bootstrap()
                    .channel(NioSocketChannel.class)
                    .group(workers)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,500)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            nioSocketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 12, 4));
                            nioSocketChannel.pipeline().addLast(new MessageCodec());
                            // 写空闲
                            nioSocketChannel.pipeline().addLast(new IdleStateHandler(0, 3, 0));
                            nioSocketChannel.pipeline().addLast(new ChannelDuplexHandler() {
                                // 用于触发特殊事件
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    IdleStateEvent event = (IdleStateEvent) evt;
                                    if (event.state() == IdleState.WRITER_IDLE) {
                                        // 心跳包
                                        ctx.writeAndFlush(new HeartBeatMessage());
                                    }
                                    super.userEventTriggered(ctx, evt);
                                }
                            });
                            // 业务handler
                            nioSocketChannel.pipeline().addLast("client_handler", new ChannelInboundHandlerAdapter() {
                                // 连接建立后触发
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    // 负责接受用户控制台输入并发送消息
                                    new Thread(() -> {
                                        Scanner scanner = new Scanner(System.in);
                                        System.out.println("输入用户名：");
                                        String name = scanner.nextLine();
                                        System.out.println("输入密码：");
                                        String password = scanner.nextLine();
                                        LoginRequestMessage loginRequestMessage = new LoginRequestMessage(name, password);
                                        // 发送消息
                                        ctx.writeAndFlush(loginRequestMessage);
                                        // 阻塞等待接受服务端消息，确认后续操作
                                        try {
                                            latch.await();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        if (!LOGIN.get()) {
                                            ctx.channel().close(); // 触发的是 channelFuture.channel().closeFuture().sync();
                                            return;
                                        }
                                        // 功能
                                        while (true) {
                                            System.out.println("============ 功能菜单 ============");
                                            System.out.println("send [username] [content]");
                                            System.out.println("gsend [group name] [content]");
                                            System.out.println("gcreate [group name] [m1,m2,m3...]");
                                            System.out.println("gmembers [group name]");
                                            System.out.println("gjoin [group name]");
                                            System.out.println("gquit [group name]");
                                            System.out.println("quit");
                                            System.out.println("==================================");
                                            String command = scanner.nextLine();
                                            String[] s = command.split(" ");
                                            switch (s[0]) {
                                                case "send": // 发送消息
                                                    ctx.writeAndFlush(new ChatRequestMessage(name, s[1], s[2]));
                                                    break;
                                                case "gsend": // 群里 发送消息
                                                    ctx.writeAndFlush(new GroupChatRequestMessage(name, s[1], s[2]));
                                                    break;
                                                case "gcreate": // 创建群
                                                    final Set<String> set = new HashSet(Arrays.asList(s[2].split(",")));
                                                    set.add(name);
                                                    ctx.writeAndFlush(new GroupCreateRequestMessage(s[1], set));
                                                    break;
                                                case "gmembers": // 查看群列表
                                                    ctx.writeAndFlush(new GroupMembersRequestMessage(s[1]));
                                                    break;
                                                case "gjoin":
                                                    ctx.writeAndFlush(new GroupJoinRequestMessage(name, s[1]));
                                                    break;
                                                case "gquit":
                                                    ctx.writeAndFlush(new GroupQuitRequestMessage(name, s[1]));
                                                    break;
                                                case "quit":
                                                    ctx.channel().close(); // 触发 【channel.closeFuture().sync(); 向下运行】
                                                    return;
                                            }

                                        }
                                    }, "business").start();
                                    super.channelActive(ctx);
                                }

                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    log.info("msg = {}", msg);
                                    if (msg instanceof LoginResponseMessage) {
                                        LoginResponseMessage responseMessage = (LoginResponseMessage) msg;
                                        LOGIN.set(responseMessage.isSuccess());
                                    }
                                    // 接收到服务端信息，业务线程可以向下运行
                                    latch.countDown();
                                    super.channelRead(ctx, msg);
                                }
                            });
                        }
                    })
                    .connect(new InetSocketAddress("localhost", 8080));
            channelFuture.sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.info("client error", e);
        } finally {
            workers.shutdownGracefully();
        }
    }
}
