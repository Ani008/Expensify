package com.example.expensify;



public class Group {
    public String groupId;
    public String groupName;
    public String description;
    public int memberCount;

    // Default constructor required for calls to DataSnapshot.getValue(Group.class)
    public Group() {
    }

    public Group(String groupId, String groupName, String description, int memberCount) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.description = description;
        this.memberCount = memberCount;
    }
}
