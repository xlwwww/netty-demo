package test.netty.demo.message;

public class LoginRequestMessage extends Message {
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String password;

    public LoginRequestMessage(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }


    @Override
    public byte getMessageType() {
        return Message.LoginRequestMessage;
    }

}
