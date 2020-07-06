package cf.bautroixa.tripgether.ui.adapter.viewholder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.repo.objects.UserPublic;

public class UserPublicVH extends RecyclerView.ViewHolder {

    protected TextView tvName, tvInfo;
    protected Button btnInvite;
    protected AvatarVH avatarVH;

    public UserPublicVH(@NonNull View itemView) {
        super(itemView);
        avatarVH = new AvatarVH(itemView);

        tvName = itemView.findViewById(R.id.tv_name_status_item_friend);
        tvInfo = itemView.findViewById(R.id.tv_info_item_friend);

        btnInvite = itemView.findViewById(R.id.btn_action_item_friend);
        btnInvite.setVisibility(View.GONE);
    }

    public void bind(final UserPublic user) {
        // online indicator
        avatarVH.tvOnlineIndicator.setVisibility(View.GONE);


        tvName.setText(user.getName());
        tvInfo.setText(user.getEmail());
    }
}