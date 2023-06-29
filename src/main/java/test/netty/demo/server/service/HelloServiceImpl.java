package test.netty.demo.server.service;

public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        String s = "hello" + name;
        System.out.println(s);
        return s;
    }
}
