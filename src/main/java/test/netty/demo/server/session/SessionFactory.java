package test.netty.demo.server.session;

public class SessionFactory {
    private static Session session = new SessionMemoryImpl();

    public static Session getSession() {
        return session;
    }
}
