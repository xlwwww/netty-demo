package test.netty.demo.message;

public class LoginResponseMessage extends AbstractResponseMessage {
    public LoginResponseMessage(boolean success, String reason) {
        super(success, reason);
    }

    @Override
    public byte getMessageType() {
        return LoginResponseMessage;
    }
}
