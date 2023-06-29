package test.netty.demo.server.session;

import io.netty.channel.Channel;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GroupSessionImpl implements GroupSession {
    private static ConcurrentHashMap<String, Group> groups;

    static {
        groups = new ConcurrentHashMap<>();
    }

    @Override
    public Group createGroup(String name, Set<String> members) {
        if (validate(name)) {
            return null;
        }
        Group group = new Group(name, members);
        groups.put(name, group);
        return group;
    }

    @Override
    public Group joinGroup(String name, String member) {
        if (!validate(name)) {
            return null;
        }
        Group group = groups.get(name);
        group.getMembers().add(member);
        return group;
    }

    @Override
    public Group removeGroup(String name) {
        if (!validate(name)) {
            return null;
        }
        return groups.remove(name);
    }

    @Override
    public List<Channel> getMemberChannels(String name) {
        if (!validate(name)) {
            return null;
        }
        Group group = groups.get(name);
        return Optional.ofNullable(group.getMembers()).orElse(new HashSet<>())
                .stream()
                .map(var -> SessionFactory.getSession().getChannel(var))
                .collect(Collectors.toList());
    }

    private static boolean validate(String name) {
        return groups.containsKey(name);
    }
}
