package cf.bautroixa.tripgether.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.presenter.trip.TripInvitationFriendsPresenter;
import cf.bautroixa.tripgether.presenter.trip.TripInvitationFriendsPresenterImpl;
import cf.bautroixa.tripgether.ui.theme.RoundedImageView;
import cf.bautroixa.tripgether.ui.theme.ViewAnim;
import cf.bautroixa.tripgether.utils.ui_utils.ImageHelper;

public class TripInvitationFriendsAdapter extends RecyclerView.Adapter<TripInvitationFriendsAdapter.FriendVH> {
    ArrayList<User> friends;
    Context context;

    public TripInvitationFriendsAdapter(Context context, ArrayList<User> friends) {
        this.friends = friends;
        this.context = context;
    }

    @NonNull
    @Override
    public FriendVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_with_action_btn, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FriendVH holder, int position) {
        holder.bind(friends.get(position));
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    class FriendVH extends RecyclerView.ViewHolder implements TripInvitationFriendsPresenter.View {
        TripInvitationFriendsPresenterImpl tripInvitationFriendsPresenter;
        TextView tvNameInAvatar, tvOnlineIndicator;
        RoundedImageView imgAvatar;
        TextView tvName, tvInfo;
        Button btnInvite;


        public FriendVH(@NonNull View itemView) {
            super(itemView);
            tripInvitationFriendsPresenter = new TripInvitationFriendsPresenterImpl(context, this);
//            progressBattery = itemView.findViewById(R.id.progress_battery_item_friend);
            tvNameInAvatar = itemView.findViewById(R.id.tv_name_item_avatar);
            imgAvatar = itemView.findViewById(R.id.img_avatar_item_avatar);
//            imgIsLeader = itemView.findViewById(R.id.img_is_leader_item_friend);
            tvOnlineIndicator = itemView.findViewById(R.id.tv_online_indicator_item_avatar);

            tvName = itemView.findViewById(R.id.tv_name_status_item_friend);
            tvInfo = itemView.findViewById(R.id.tv_info_item_friend);

            btnInvite = itemView.findViewById(R.id.btn_action_item_friend);
        }

        void bind(final User user) {
            // online indicator and lastUpdateTime
            if (user.getLastUpdate() != null) {
                if (Calendar.getInstance().getTimeInMillis() - user.getLastUpdate().toDate().getTime() < 5 * 60 * 1000) { // online in less than 5 mins
                    tvOnlineIndicator.setSelected(true);// green background
                } else {
                    tvOnlineIndicator.setSelected(false);// red background
                }
            }
            // avatar
            if (user.getAvatar() != null && !user.getAvatar().equals(User.DEFAULT_AVATAR)) {
                // user change avatar
                imgAvatar.setVisibility(View.VISIBLE);
                ImageHelper.loadCircleImage(user.getAvatar(), imgAvatar);
            }
            // others
            tvNameInAvatar.setText(user.getShortName());
            tvName.setText(user.getName());
            tvInfo.setText(user.getEmail());

            btnInvite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tripInvitationFriendsPresenter.inviteFriend(user);
                }
            });
        }

        @Override
        public void onInviting() {
            ViewAnim.toggleLoading(context, btnInvite, true, "Đang mời...");
        }

        @Override
        public void onInvited() {
            btnInvite.setText("Đã mời");
        }

        @Override
        public void onInviteFailed() {
            ViewAnim.toggleLoading(context, btnInvite, false, "Mời lại");
        }
    }
}
