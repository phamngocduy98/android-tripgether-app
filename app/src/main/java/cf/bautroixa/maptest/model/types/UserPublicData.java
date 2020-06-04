package cf.bautroixa.maptest.model.types;

import java.io.Serializable;

import cf.bautroixa.maptest.model.firestore.User;

public class UserPublicData implements Serializable {
    private String id, name, avatar, email;

    public UserPublicData() {
    }

    public UserPublicData(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.avatar = user.getAvatar();
        this.email = user.getEmail();
    }

    public UserPublicData(String id, String name, String avatar, String email) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
