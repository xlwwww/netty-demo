package test.netty.demo.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class Worker implements Runnable {
    private Thread thread;

    private Selector selector;

    private String name;
    private volatile boolean start = false;

    public Worker(String name) {
        this.name = name;
    }

    public void register(SocketChannel sc) throws IOException {
        if (!start) {
            this.thread = new Thread(this, name);
            this.selector = Selector.open();
            start = true;
            thread.start();
        }
        // wakeup ：先wakeup后select，select不会阻塞
        selector.wakeup();
        sc.register(selector, SelectionKey.OP_READ, null);
    }

    @Override
    public void run() {
        // 检测读写事件
        while (true) {
            try {
                selector.select();
                System.out.println(this.name + "begin read.. ");
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();
                    if (selectionKey.isReadable()) {
                        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                        channel.read(byteBuffer);
                        byteBuffer.flip();
                        final String string = Charset.defaultCharset().decode(byteBuffer).toString();
                        System.out.println(this.name + "读取内容： " + string);
                    } else if (selectionKey.isWritable()) {

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


