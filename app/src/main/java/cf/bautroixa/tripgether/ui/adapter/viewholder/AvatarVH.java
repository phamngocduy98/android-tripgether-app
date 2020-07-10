package cf.bautroixa.tripgether.ui.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.model.repo.objects.UserPublic;
import cf.bautroixa.tripgether.ui.theme.RoundedImageView;
import cf.bautroixa.tripgether.utils.ui_utils.ImageHelper;

public class AvatarVH extends RecyclerView.ViewHolder {
    public TextView tvNameInAvatar, tvOnlineIndicator;
    public RoundedImageView imgAvatar;

    public AvatarVH(@NonNull View itemView) {
        super(itemView);
        tvNameInAvatar = itemView.findViewById(R.id.tv_name_item_avatar);
        imgAvatar = itemView.findViewById(R.id.img_avatar_item_avatar);
        tvOnlineIndicator = itemView.findViewById(R.id.tv_online_indicator_item_avatar);
    }

    public void bind(String avatar, String shortName) {
        if (avatar != null && !avatar.equals(User.DEFAULT_AVATAR)) {
            imgAvatar.setVisibility(View.VISIBLE);
            ImageHelper.loadCircleImage(avatar, imgAvatar);
        } else {
            imgAvatar.setVisibility(View.INVISIBLE);
            tvNameInAvatar.setText(shortName);
        }
    }

    public void bind(UserPublic userPublic) {
        bind(userPublic.getAvatar(), userPublic.getShortName());
        tvOnlineIndicator.setVisibility(View.INVISIBLE);
    }

    public void bind(User user) {
        bind(user.getAvatar(), user.getShortName());
        tvOnlineIndicator.setEnabled(user.isOnline());
    }
}
