package com.xloru.nio;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Demo {
    private String x;
    public static void main(String[] args) throws IOException {
        final Demo demo = new Demo();
        final String s = demo.getClass().getResource("/test.txt").toString();
        System.out.println(s);
        Path path = Paths.get(
                URI.create( demo.getClass().getResource("/file.txt").toString()));
        AsynchronousFileChannel fileChannel
                = AsynchronousFileChannel.open(path, StandardOpenOption.READ);

        ByteBuffer buffer = ByteBuffer.allocate(1024);
    }
}
