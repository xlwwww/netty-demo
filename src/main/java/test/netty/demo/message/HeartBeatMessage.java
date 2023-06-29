package test.netty.demo.message;

public class HeartBeatMessage extends Message {
    @Override
    public byte getMessageType() {
        return HeartbeatMessage;
    }
}
