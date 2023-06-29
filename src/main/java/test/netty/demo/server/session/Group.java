package test.netty.demo.server.session;

import io.netty.channel.Channel;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天室
 */
public class Group {
    private int id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getMembers() {
        return members;
    }

    public void setMembers(Set<String> members) {
        this.members = members;
    }

    private String name;
    private Set<String> members;


    public Group(String name, Set<String> members) {
        this.name = name;
        this.members = members;
    }
}
