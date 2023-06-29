package test.netty.demo.message;

import lombok.Data;
import lombok.ToString;

import java.util.Set;

@Data
@ToString
public class GroupCreateRequestMessage extends Message {
    private String groupName;
    private Set<String> members;

    public GroupCreateRequestMessage(String groupName, Set<String> members) {
        this.groupName = groupName;
        this.members = members;
    }

    @Override
    public byte getMessageType() {
        return GroupCreateRequestMessage;
    }
}
