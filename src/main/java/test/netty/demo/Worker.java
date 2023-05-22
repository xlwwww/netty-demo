package test.netty.demo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Worker implements Runnable {
    private Thread thread;

    private Selector selector;

    private String name;
    private volatile boolean start = false;
    // 在两个线程之间传递数据
    private ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

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
        // 向队列添加任务，任务没有立刻执行
        queue.add(() -> {
            try {
                sc.register(selector, SelectionKey.OP_READ + SelectionKey.OP_WRITE, null);
            } catch (ClosedChannelException e) {
                e.printStackTrace();
            }
        });
        selector.wakeup();
    }

    @Override
    public void run() {
        // 检测读写事件
        while (true) {
            try {
                selector.select();
                Runnable poll = queue.poll();
                if (poll != null) {
                    poll.run(); ///执行 sc.register(selector, SelectionKey.OP_READ + SelectionKey.OP_WRITE, null);
                }
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();
                    if (selectionKey.isReadable()) {
                        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                        channel.read(byteBuffer);
                    } else if (selectionKey.isWritable()) {

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


