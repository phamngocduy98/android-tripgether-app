package cf.bautroixa.maptest.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.ui.theme.RoundedImageView;
import cf.bautroixa.maptest.utils.ImageHelper;

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
        Button btnInvite;


        public FriendVH(@NonNull View itemView) {
            super(itemView);
            tvNameInAvatar = itemView.findViewById(R.id.tv_name_item_friend);
            imgAvatar = itemView.findViewById(R.id.img_avatar_item_friend);
            tvOnlineIndicator = itemView.findViewById(R.id.tv_online_indicator_item_friend);

            tvName = itemView.findViewById(R.id.tv_name_status_item_friend);
            tvInfo = itemView.findViewById(R.id.tv_info_item_friend);

            btnInvite = itemView.findViewById(R.id.btn_action_item_friend);
            btnInvite.setText("Nhắn tin");
        }

        void bind(final User user) {
            // online indicator
            tvOnlineIndicator.setSelected(user.isOnline());// green background
            ImageHelper.loadUserAvatar(imgAvatar, tvNameInAvatar, user);

            tvName.setText(user.getName());
            tvInfo.setText(user.getEmail());

            btnInvite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }
}
