package test.netty.demo.server.session;

public class GroupSessionFactory {
    private static GroupSession groupSession = new GroupSessionImpl();

    public static GroupSession getGroupSession() {
        return groupSession;
    }
}
