package test.netty.demo.server.session;

import io.netty.channel.Channel;

public interface Session {
    void bind(Channel channel,String userName);

    void unbind(Channel channel);

    Channel getChannel(String name);
}
