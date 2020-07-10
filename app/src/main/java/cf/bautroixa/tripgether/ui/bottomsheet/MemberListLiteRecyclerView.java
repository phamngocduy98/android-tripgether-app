package cf.bautroixa.tripgether.ui.bottomsheet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.List;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.interfaces.NavigationInterface;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.ui.adapter.pager_adapter.MainActivityPagerAdapter;
import cf.bautroixa.tripgether.ui.map.TabMapFragment;
import cf.bautroixa.tripgether.utils.ui_utils.ImageHelper;

public class MemberListLiteRecyclerView extends MemberListRecyclerView {
    public MemberListLiteRecyclerView(ModelManager manager, NavigationInterface navigationInterface) {
        super(manager, navigationInterface);
        this.adapter = new MembersLiteAdapter();
    }

    public class MembersLiteAdapter extends MembersAdapter {

        @NonNull
        @Override
        public MemberLiteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_bottom_sheet_member_list_item_lite, parent, false);
            return new MemberLiteViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberViewHolder holder, int position, @NonNull List<Object> payloads) {
            holder.bind(position);
        }

        @Override
        public int getItemCount() {
            return super.getItemCount() + 1;
        }
    }

    public class MemberLiteViewHolder extends MemberViewHolder {
        public MemberLiteViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void findView() {
            tvNameInAvatar = itemView.findViewById(R.id.tv_name_item_avatar);
            imgAvatar = itemView.findViewById(R.id.img_avatar_item_avatar);
            imgIsLeader = itemView.findViewById(R.id.img_is_leader_item_friend);
            tvOnlineIndicator = itemView.findViewById(R.id.tv_online_indicator_item_avatar);
        }

        @Override
        public void bind(int position) {
            if (position == 0) {
                imgAvatar.setImageResource(R.drawable.ic_photo_camera_black_24dp);
                tvNameInAvatar.setVisibility(View.INVISIBLE);
                tvOnlineIndicator.setVisibility(View.INVISIBLE);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                return;
            }
            final User user = users.get(position - 1);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigationInterface.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_MEMBER_STATUS, user);
                }
            });
            ImageHelper.loadCircleImage(user.getAvatar(), imgAvatar);
            // leader
            if (user.getId().equals(manager.getCurrentTrip().getLeader().getId())) {
                imgIsLeader.setVisibility(View.VISIBLE);
            } else {
                imgIsLeader.setVisibility(View.GONE);
            }
            this.update(user);
        }

        @Override
        public void update(User user) {
            if (user.getLastUpdate() != null && Calendar.getInstance().getTimeInMillis() - user.getLastUpdate().toDate().getTime() < 5 * 60 * 1000) {
                // online in less than 5 mins
                tvOnlineIndicator.setSelected(true);// green background
            } else {
                tvOnlineIndicator.setSelected(false);// red background
            }
        }
    }
}
