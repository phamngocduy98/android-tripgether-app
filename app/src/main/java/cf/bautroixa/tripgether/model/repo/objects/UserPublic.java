package cf.bautroixa.tripgether.model.repo.objects;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

import cf.bautroixa.tripgether.model.firestore.objects.User;

public class UserPublic implements Serializable, Parcelable {
    private String id, name, avatar, email;

    public UserPublic() {
    }

    public UserPublic(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.avatar = user.getAvatar();
        this.email = user.getEmail();
    }

    public UserPublic(String id, String name, String avatar, String email) {
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

    public String getShortName() {
        String[] names = getName().split(" ");
        if (names.length >= 2) {
            return "" + names[0].charAt(0) + names[names.length - 1].charAt(0);
        } else {
            return getName();
        }
    }


    public UserPublic(Parcel in) {
        super();
        readFromParcel(in);
    }

    public static final Parcelable.Creator<UserPublic> CREATOR = new Parcelable.Creator<UserPublic>() {
        public UserPublic createFromParcel(Parcel in) {
            return new UserPublic(in);
        }

        public UserPublic[] newArray(int size) {
            return new UserPublic[size];
        }

    };

    public void readFromParcel(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.avatar = in.readString();
        this.email = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.avatar);
        dest.writeString(this.email);
    }
}
