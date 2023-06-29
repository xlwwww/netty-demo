package test.netty.demo.server.service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class UserServiceImpl implements UserService {
    private static ConcurrentHashMap<String, String> users;

    static {
        users = new ConcurrentHashMap<>();
        users.put("wangwu", "123");
        users.put("zhangsan", "123");
        users.put("lisi", "123");
    }

    @Override

    public boolean login(String username, String password) {
        return users.containsKey(username) && users.get(username).equals(password);
    }
}
