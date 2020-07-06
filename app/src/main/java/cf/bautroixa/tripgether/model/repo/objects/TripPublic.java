package cf.bautroixa.tripgether.model.repo.objects;

import java.util.ArrayList;

public class TripPublic {
    String name;
    ArrayList<UserPublic> members;
    ArrayList<CheckpointPublic> checkpoints;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<UserPublic> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<UserPublic> members) {
        this.members = members;
    }

    public ArrayList<CheckpointPublic> getCheckpoints() {
        return checkpoints;
    }

    public void setCheckpoints(ArrayList<CheckpointPublic> checkpoints) {
        this.checkpoints = checkpoints;
    }
}
