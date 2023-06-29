package test.netty.demo.client;

import java.util.concurrent.atomic.AtomicInteger;

public class SequenceIdGenerator {
    private static final AtomicInteger id = new AtomicInteger(0);

    public static int getSequenceId() {
        return id.incrementAndGet();
    }
}
