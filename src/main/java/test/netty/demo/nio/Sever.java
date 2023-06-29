package test.netty.demo.nio;

import test.netty.demo.utils.ByteBufferUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class Sever {
    public static void main(String[] args) throws IOException {
        //测试黏包、半包的拆包
        //test1();
        testSelector();
    }

    public static void testSelector() throws IOException {
        // 获得服务器通道
        try (ServerSocketChannel server = ServerSocketChannel.open()) {
            server.bind(new InetSocketAddress(8080));
            // 创建选择器
            Selector selector = Selector.open();
            // 通道必须设置为非阻塞模式
            server.configureBlocking(false);
            // 将通道注册到选择器中，并设置感兴趣的实践
            server.register(selector, SelectionKey.OP_ACCEPT);
            // 为serverKey设置感兴趣的事件
            while (true) {
                // 若没有事件就绪，线程会被阻塞，反之不会被阻塞。从而避免了CPU空转
                int ready = selector.select();
                System.out.println("selector ready counts : " + ready);
                // 获取所有事件
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                // 使用迭代器遍历事件
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    // 判断key的类型
                    if (key.isAcceptable()) {
                        // 获得key对应的channel
                        ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                        // 获取连接
                        SocketChannel socketChannel = channel.accept();
                        // 配置非阻塞模式，selector需要搭配使用
                        socketChannel.configureBlocking(false);
                        // 将buffer作为attach与key关联
                        ByteBuffer buffer = ByteBuffer.allocate(16);
                        socketChannel.register(selector, SelectionKey.OP_READ + SelectionKey.OP_WRITE, buffer);
                    } else if (key.isReadable()) {
                        try {
                            SocketChannel channel = (SocketChannel) key.channel();
                            ByteBuffer buffer = (ByteBuffer) key.attachment();
                            int read = channel.read(buffer);
                            // 客户端断开后，仍会产生读事件
                            if (read == -1) {
                                key.cancel();
                            } else {
                                split(buffer);
                                // 需要扩容
                                if (buffer.position() == buffer.limit()) {
                                    buffer.flip(); // buffer切换读模式，从position : 0开始读
                                    ByteBuffer newByteBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                                    newByteBuffer.put(buffer);
                                    key.attach(newByteBuffer);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            key.cancel();
                        }
                    } else if (key.isWritable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        channel.write(buffer);
                        if (!buffer.hasRemaining()) {
                            key.attach(null);// 清理，for gc
                            key.interestOps(key.interestOps() - SelectionKey.OP_WRITE); // 不需关注可写事件
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void test1() {
        ByteBuffer source = ByteBuffer.allocate(32);
        source.put("hello,world\nzhangsan\nho".getBytes(StandardCharsets.UTF_8));
        split(source);
    }

    public static void split(ByteBuffer source) {
        // 切换读模式
        source.flip();
        for (int i = 0; i < source.limit(); i++) {
            if (source.get(i) == '\n') {
                int len = i + 1 - source.position();
                ByteBuffer dest = ByteBuffer.allocate(len);
                for (int j = 0; j < len; j++) {
                    dest.put(source.get());
                }
                ByteBufferUtil.debugAll(dest);
            }
        }
        source.compact();
    }
}
