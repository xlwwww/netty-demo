package test.netty.demo.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class Server {
    public static void main(String[] args) {
        // 处理耗时较长任务，独立group，处理handler任务
        // 同一个channel也会绑定到同一个DefaultEventLoop
        DefaultEventLoop group = new DefaultEventLoop();
        // 启动器，组装netty
        new ServerBootstrap()
                //  1 # bossEventLoopGroup： 只处理accept事件 2# workerEventLoopGroup: 处理读/写事件
                .group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                // 这里是worker执行的操作
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        // 将bytebuf转为string
                        nioSocketChannel.pipeline().addLast(new StringDecoder());
                        // 自定义处理器
                        nioSocketChannel.pipeline().addLast("handler-1", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.info(msg + "");
                                // 将msg传递到下一个handler
                                super.channelRead(ctx, msg);
                            }
                        }).addLast(group, "handler-2", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.info(msg + "");
                                nioSocketChannel.writeAndFlush(ctx.alloc().buffer().writeBytes("sever1..".getBytes(StandardCharsets.UTF_8)));
                                // 使用当前ctx，从当前ctx向前寻找handler
                                // ctx.writeAndFlush(ctx.alloc().buffer().writeBytes("sever2..".getBytes(StandardCharsets.UTF_8)));
                            }
                        }).addLast("handler-3", new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.info("write..", msg);
                                super.write(ctx, msg, promise);
                            }
                        });
                    }
                })
                .bind(8080);
    }
}
