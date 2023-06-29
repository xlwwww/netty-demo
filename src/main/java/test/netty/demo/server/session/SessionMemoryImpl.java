package test.netty.demo.server.session;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话管理
 */
public class SessionMemoryImpl implements Session {
    private static ConcurrentHashMap<String, Channel> userName2ChannelMap;
    private static ConcurrentHashMap<Channel, String> channel2UserNameMap;

    static {
        userName2ChannelMap = new ConcurrentHashMap<>();
        channel2UserNameMap = new ConcurrentHashMap<>();
    }

    @Override
    public void bind(Channel channel, String userName) {
        userName2ChannelMap.put(userName, channel);
        channel2UserNameMap.put(channel, userName);
    }

    @Override
    public void unbind(Channel channel) {
        String name = channel2UserNameMap.remove(channel);
        userName2ChannelMap.remove(name);
    }

    @Override
    public Channel getChannel(String name) {
        return userName2ChannelMap.get(name);
    }
}
