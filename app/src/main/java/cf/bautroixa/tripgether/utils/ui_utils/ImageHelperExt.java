package cf.bautroixa.tripgether.utils.ui_utils;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.model.repo.objects.UserPublic;
import cf.bautroixa.tripgether.utils.ContactHelper;
import cf.bautroixa.ui.helpers.ImageHelper;

public class ImageHelperExt extends ImageHelper {

    public static void loadUserAvatar(ImageView imgAvatar, TextView tvNameInAvatar, User user) {
        if (user.getAvatar() != null && !user.getAvatar().equals(User.DEFAULT_AVATAR)) {
            imgAvatar.setVisibility(View.VISIBLE);
            ImageHelper.loadCircleImage(user.getAvatar(), imgAvatar);
        } else {
            imgAvatar.setVisibility(View.INVISIBLE);
            tvNameInAvatar.setText(user.getShortName());
        }
    }

    public static void loadUserAvatar(ImageView imgAvatar, TextView tvNameInAvatar, UserPublic user) {
        if (user.getAvatar() != null && !user.getAvatar().equals(User.DEFAULT_AVATAR)) {
            imgAvatar.setVisibility(View.VISIBLE);
            ImageHelper.loadCircleImage(user.getAvatar(), imgAvatar);
        } else {
            imgAvatar.setVisibility(View.INVISIBLE);
            tvNameInAvatar.setText(user.getShortName());
        }
    }

    public static void loadUserAvatar(ImageView imgAvatar, TextView tvNameInAvatar, ContactHelper.Contact contact) {
        if (contact.getAvatar() != null) {
            imgAvatar.setVisibility(View.VISIBLE);
            Picasso.get().load(contact.getAvatar()).resize(50, 50).centerCrop().into(imgAvatar);
        } else {
            imgAvatar.setVisibility(View.INVISIBLE);
            tvNameInAvatar.setText(contact.getShortName());
        }
    }
}
