package de.trist.clans.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Clan {

    private final String tag;
    private final UUID owner;
    private int memberSlots;
    private final List<UUID> members = new ArrayList<>();

    public Clan(String tag, UUID owner, int memberSlots) {
        this.tag = tag;
        this.owner = owner;
        this.memberSlots = memberSlots;

        // Owner ist automatisch Member
        this.members.add(owner);
    }

    public String getTag() {
        return tag;
    }

    public UUID getOwner() {
        return owner;
    }

    public int getMemberSlots() {
        return memberSlots;
    }

    public void setMemberSlots(int memberSlots) {
        this.memberSlots = memberSlots;
    }

    public List<UUID> getMembers() {
        return members;
    }
}