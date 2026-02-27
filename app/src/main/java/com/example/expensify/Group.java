package com.example.expensify;

public class Group {
    public String groupId;
    public String groupName;
    public String description;
    public int memberCount;
    public String creatorId; // <--- 1. Add this field

    // Default constructor required for Firebase
    public Group() {
    }

    // 2. Update this constructor to include creatorId
    public Group(String groupId, String groupName, String description, int memberCount, String creatorId) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.description = description;
        this.memberCount = memberCount;
        this.creatorId = creatorId;
    }
}