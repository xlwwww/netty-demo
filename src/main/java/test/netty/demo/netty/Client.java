package test.netty.demo.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Scanner;

@Slf4j
public class Client {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        Channel channel = new Bootstrap()
                .group(eventExecutors)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    // 建立连接后调用
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        // 将string转化为bytebuf
                        nioSocketChannel.pipeline().addLast(new StringEncoder());
                    }
                })
                // 主线程在此处异步非阻塞，调用后直接返回，建立连接的是NioEventLoopGroup
                .connect(new InetSocketAddress("localhost", 8080))
                // 异步方式，addListener增加回调,由NioEventLoopGroup建立连接后调用
//                .addListener((ChannelFuture future) -> {
//                    log.info("channel connect");
//                    future.channel().writeAndFlush("hello,world");
//                });
                // 阻塞方法，连接建立完成后返回
                .sync().channel();

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String nextLine = scanner.nextLine();
                if (nextLine.trim().equals("q")) {
                    // 异步非阻塞
                    channel.close();
                    break;
                }
                channel.writeAndFlush(nextLine);
            }
        }, "input").start();
        //关闭连接
        ChannelFuture closeFuture = channel.closeFuture();
//        // 1. 同步等待连接关闭
//        closeFuture.sync();
        // 2. 异步关闭
        closeFuture.addListener((ChannelFuture future) -> {
            // 关闭后操作
            log.info("处理关闭后操作");
            eventExecutors.shutdownGracefully();
        });
    }
}
