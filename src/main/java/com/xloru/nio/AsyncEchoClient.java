package com.xloru.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
public class AsyncEchoClient {
    private AsynchronousSocketChannel client;
    private Future<Void> future;
    private static AsyncEchoClient instance;

    private AsyncEchoClient() {
        try {
            client = AsynchronousSocketChannel.open();
            final InetSocketAddress socketAddress = new InetSocketAddress("localhost", 4999);
            future = client.connect(socketAddress);
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static AsyncEchoClient getInstance() {
        if (instance == null) {
            instance = new AsyncEchoClient();
        }
        return instance;
    }

    private void start() {
        try {
            future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String sendMessage(String message) {
        final byte[] byteMsg = message.getBytes();
        final ByteBuffer buffer = ByteBuffer.wrap(byteMsg);
        final Future<Integer> writeResult = client.write(buffer);

        try {
            writeResult.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        buffer.flip();
        final Future<Integer> readResult = client.read(buffer);
        try {
            readResult.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        final String echo = new String(buffer.array()).trim();
        buffer.clear();
        return echo;
    }

    public void stop() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        final AsyncEchoClient client = AsyncEchoClient.getInstance();
        client.start();
        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        log.debug("Message to Server:");
        while ((line = br.readLine()) != null) {
            final String response = client.sendMessage(line);
            log.debug("response from Server: " + response);
            log.debug("Message to Server:");
        }
    }
}
