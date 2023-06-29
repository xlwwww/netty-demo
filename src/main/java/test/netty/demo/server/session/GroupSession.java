package test.netty.demo.server.session;

import io.netty.channel.Channel;

import java.util.List;
import java.util.Set;

public interface GroupSession {
    Group createGroup(String name, Set<String> members);

    Group joinGroup(String name, String member);

    Group removeGroup(String name);

    List<Channel> getMemberChannels(String name);
}
