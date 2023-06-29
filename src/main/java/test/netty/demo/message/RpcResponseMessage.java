package test.netty.demo.message;

import lombok.Data;

@Data
public class RpcResponseMessage extends Message {
    private int sequence_id;
    private Object returnValue;
    private Exception exceptionValue;

    @Override
    public byte getMessageType() {
        return RpcResponseMessage;
    }
}
