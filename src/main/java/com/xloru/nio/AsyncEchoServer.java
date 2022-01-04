package com.xloru.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;

public class AsyncEchoServer {
    private AsynchronousServerSocketChannel serverChannel;
    private AsynchronousSocketChannel clientChannel;

    /**
     * serverSocketChannel 主要提供 accept 方法阻塞监听客户端连接
     * 一旦建立连接，实际上是在用 socketChannel 进行数据交互
     * socketChannel 提供具体的读写方法
     * 下面的 completionHandler 使用一种递归的机制完成持续监听
     */
    public AsyncEchoServer() {
        try {
            serverChannel = AsynchronousServerSocketChannel.open();
            final InetSocketAddress socketAddress = new InetSocketAddress("localhost", 4999);
            serverChannel.bind(socketAddress);
            while (true) {
                serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
                    @Override
                    public void completed(AsynchronousSocketChannel result, Object attachment) {
                        if (serverChannel.isOpen()) {
                            serverChannel.accept(null, this);
                        }
                        clientChannel = result;
                        if ((clientChannel != null) && (clientChannel.isOpen())) {
                            final ReadWriteHandler handler = new ReadWriteHandler();
                            final ByteBuffer buffer = ByteBuffer.allocate(1024);
                            final Map<String, Object> readInfo = new HashMap<>();
                            readInfo.put("action", "read");
                            readInfo.put("buffer", buffer);
                            clientChannel.read(buffer, readInfo, handler);
                        }
                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {

                    }
                });
                System.in.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ReadWriteHandler implements CompletionHandler<Integer, Map<String, Object>> {
        @Override
        public void completed(Integer result, Map<String, Object> attachment) {
            Map<String, Object> actionInfo = attachment;
            final String action = (String) actionInfo.get("action");
            if ("read".equals(action)) {
                final ByteBuffer buffer = (ByteBuffer) actionInfo.get("buffer");
                buffer.flip();
                actionInfo.put("action", "write");
                clientChannel.write(buffer, actionInfo, this);
                buffer.clear();
            } else if ("write".equals(action)) {
                final ByteBuffer buffer = ByteBuffer.allocate(32);
                actionInfo.put("action", "read");
                actionInfo.put("buffer", buffer);
                clientChannel.read(buffer, actionInfo, this);
            }
        }

        @Override
        public void failed(Throwable exc, Map<String, Object> attachment) {

        }
    }

    public static void main(String[] args) {
        new AsyncEchoServer();
    }
}
