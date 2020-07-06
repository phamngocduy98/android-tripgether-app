package cf.bautroixa.maptest.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.objects.User;
import cf.bautroixa.maptest.model.repo.objects.UserPublic;
import cf.bautroixa.maptest.ui.chat.ChatActivity;
import cf.bautroixa.maptest.ui.friends.ProfileActivity;
import cf.bautroixa.maptest.ui.theme.RoundedImageView;
import cf.bautroixa.maptest.utils.ui_utils.ImageHelper;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendVH> {
    ArrayList<User> friends;
    Context context;

    public FriendListAdapter(Context context, ArrayList<User> friends) {
        this.friends = friends;
        this.context = context;
    }

    @NonNull
    @Override
    public FriendListAdapter.FriendVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendListAdapter.FriendVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_with_action_btn, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FriendListAdapter.FriendVH holder, int position) {
        holder.bind(friends.get(position));
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    class FriendVH extends RecyclerView.ViewHolder {
        TextView tvNameInAvatar, tvOnlineIndicator;
        RoundedImageView imgAvatar;
        TextView tvName, tvInfo;
        Button btnAction;


        public FriendVH(@NonNull View itemView) {
            super(itemView);
            tvNameInAvatar = itemView.findViewById(R.id.tv_name_item_avatar);
            imgAvatar = itemView.findViewById(R.id.img_avatar_item_avatar);
            tvOnlineIndicator = itemView.findViewById(R.id.tv_online_indicator_item_avatar);

            tvName = itemView.findViewById(R.id.tv_name_status_item_friend);
            tvInfo = itemView.findViewById(R.id.tv_info_item_friend);

            btnAction = itemView.findViewById(R.id.btn_action_item_friend);
            btnAction.setText("Nhắn tin");
        }

        void bind(final User user) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.putExtra(ProfileActivity.ARG_USER_PUBLIC_DATA, (Parcelable) new UserPublic(user));
                    context.startActivity(intent);
                }
            });
            tvOnlineIndicator.setSelected(user.isOnline());// green background
            ImageHelper.loadUserAvatar(imgAvatar, tvNameInAvatar, user);

            tvName.setText(user.getName());
            tvInfo.setText(user.getEmail());

            btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra(ChatActivity.ARG_TO_USER_ID, user.getId());
                    context.startActivity(intent);
                }
            });
        }
    }
}
