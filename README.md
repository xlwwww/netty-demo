# netty-demo
Java NIO系统的核心在于：通道(Channel)和缓冲区(Buffer)。通道表示打开到 IO 设备(例如：文件、套接字)的连接。若需要使用 NIO 系统，需要获取用于连接 IO 设备的通道以及用于容纳数据的缓冲区。然后操作buffer，对数据进行处理。简而言之，通道负责传输，缓冲区负责存储。

**常见Channel**
1. FileChannel:用于文件传输
2. DatagramChannel:用于网络通信
3. SocketChannel:用于网络通信
4. ServerSocketChannel:用于网络通信

**常见Buffer**
1. ByteBuffer
2. MappedByteBuffer
3. DirectByteBuffer
4. HeapByteBuffer
5. ShortBuffer
6. IntBuffer
7. LongBuffer
8. FloatBuffer
9. DoubleBuffer
10. CharBuffer


## Buffer
ByteBuffer是一个可读可写的数组缓冲区，是非线程安全的。需要给每个channel维护单独的Buffer。

#### api
1. flip():切换读写模式,将limit变为position，position变为0
2. get()/put():从缓冲区读/写


## 多路复用
Selector
- 单线程+事件实现管理多个channel，channel需配置非阻塞
- select()是阻塞方法，当有事件就绪，会唤醒线程

SelectionKey
- Selector通过SelectionKey管理channel，通过interest set指定关心的事件类型
- key要么被处理，要么被cancel，否则无法从事件集中移除（会不断select到）
- 客户端断开连接/io异常创建读事件，需判断key.cancel()

```java
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
                        socketChannel.register(selector, SelectionKey.OP_READ, buffer);
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
                                // 动态扩容
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
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
```
* 半包、黏包问题
包前缀增加包字节长度
