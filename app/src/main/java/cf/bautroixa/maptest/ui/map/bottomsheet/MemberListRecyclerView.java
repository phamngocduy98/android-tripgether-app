package cf.bautroixa.maptest.ui.map.bottomsheet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gauravbhola.ripplepulsebackground.RipplePulseLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.interfaces.NavigationInterfaces;
import cf.bautroixa.maptest.model.firestore.Checkpoint;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.SosRequest;
import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.model.firestore.Visit;
import cf.bautroixa.maptest.ui.adapter.MainActivityPagerAdapter;
import cf.bautroixa.maptest.ui.map.TabMapFragment;
import cf.bautroixa.maptest.ui.theme.RoundedImageView;
import cf.bautroixa.maptest.utils.DateFormatter;
import cf.bautroixa.maptest.utils.ImageHelper;

public class MemberListRecyclerView {
    protected NavigationInterfaces navigationInterfaces;
    protected ArrayList<User> users;
    protected MembersAdapter adapter;
    ModelManager manager;

    public MemberListRecyclerView(ModelManager manager, NavigationInterfaces navigationInterfaces) {
        this.manager = manager;
        this.navigationInterfaces = navigationInterfaces;
        this.users = manager.getCurrentTrip().getMembersManager().getList();
        this.adapter = new MembersAdapter();
    }

    public MembersAdapter getAdapter() {
        return adapter;
    }

    public class MemberViewHolder extends RecyclerView.ViewHolder {
        protected User currentUser;

        protected TextView tvNameInAvatar, tvOnlineIndicator;
        protected RoundedImageView imgAvatar;
        protected ImageView imgIsLeader;

        protected TextView tvName, tvLocation, tvLastUpdate;
        protected ImageView imgIsCheckedIn;
        protected RipplePulseLayout ripplePulseSos;
        private Checkpoint activeCheckpoint;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            this.findView();
        }

        public void findView() {
            tvNameInAvatar = itemView.findViewById(R.id.tv_name_item_friend);
            imgAvatar = itemView.findViewById(R.id.img_avatar_item_friend);
            tvOnlineIndicator = itemView.findViewById(R.id.tv_online_indicator_item_friend);

            ripplePulseSos = itemView.findViewById(R.id.ripple_pulse_sos);

            tvName = itemView.findViewById(R.id.tv_name_status_item_friend);
            tvLocation = itemView.findViewById(R.id.tv_location_status_item_friend);
            tvLastUpdate = itemView.findViewById(R.id.tv_last_update_item_friend);

            imgIsLeader = itemView.findViewById(R.id.img_is_leader_item_friend);
            imgIsCheckedIn = itemView.findViewById(R.id.img_ticker_item_friend);
        }

        public void update(User user) {
            currentUser = user;
            manager.getCurrentTrip().getActiveCheckpoint().addOnCompleteListener(new OnCompleteListener<Checkpoint>() {
                @Override
                public void onComplete(@NonNull Task<Checkpoint> task) {
                    if (task.isSuccessful()) {
                        activeCheckpoint = task.getResult();
                    }
                }
            });
            // online indicator and lastUpdateTime
            if (user.getLastUpdate() != null) {
                tvLastUpdate.setText(DateFormatter.format(user.getLastUpdate()));
                if (Calendar.getInstance().getTimeInMillis() - user.getLastUpdate().toDate().getTime() < 5 * 60 * 1000) { // online in less than 5 mins
                    tvOnlineIndicator.setSelected(true);// green background
                } else {
                    tvOnlineIndicator.setSelected(false);// red background
                }
            }
            // avatar, battery and location
            if (!user.getAvatar().equals(currentUser.getAvatar())) {
                // user change avatar
                imgAvatar.setVisibility(View.VISIBLE);
                ImageHelper.loadCircleImage(user.getAvatar(), imgAvatar);
            }
            tvLocation.setText(user.getCurrentLocation());

            // sos
            SosRequest sosRequest = user.getSosRequest();
            if (sosRequest != null && !sosRequest.isResolved()) {
                ripplePulseSos.post(new Runnable() {
                    @Override
                    public void run() {
                        ripplePulseSos.startRippleAnimation();
                    }
                });
            } else {
                ripplePulseSos.post(new Runnable() {
                    @Override
                    public void run() {
                        ripplePulseSos.stopRippleAnimation();
                    }
                });
            }
//             leader
            if (user.getId().equals(manager.getCurrentTrip().getLeader().getId())) {
                imgIsLeader.setVisibility(View.VISIBLE);
            } else {
                imgIsLeader.setVisibility(View.GONE);
            }
            imgIsCheckedIn.setVisibility(View.INVISIBLE);
            if (activeCheckpoint != null) {
                Visit userVisit = activeCheckpoint.getVisitsManager().get(user.getId());
                if (userVisit != null && manager.isReadyToCheckIn(activeCheckpoint)) {
                    tvLocation.setText(String.format("Gần %s từ lúc %s", activeCheckpoint.getName(), DateFormatter.format(userVisit.getTime())));
                    imgIsCheckedIn.setVisibility(View.VISIBLE);
                }
            }
        }

        public void bind(int position) {
            currentUser = users.get(position);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigationInterfaces.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_MEMBER_STATUS, currentUser);
                }
            });
            tvName.setText(currentUser.getName());
            if (currentUser.getAvatar() == null || Objects.equals(currentUser.getAvatar(), User.DEFAULT_AVATAR)) {
                imgAvatar.setVisibility(View.INVISIBLE);
                tvNameInAvatar.setText(currentUser.getShortName());
            }
            ImageHelper.loadCircleImage(currentUser.getAvatar(), imgAvatar);
            this.update(currentUser);
        }
    }

    public class MembersAdapter extends RecyclerView.Adapter<MemberViewHolder> {
        public MembersAdapter() {
        }

        @NonNull
        @Override
        public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member_list, parent, false);
            return new MemberViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberViewHolder holder, final int position) {
            holder.bind(position);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (!payloads.isEmpty()) {
                final User user = users.get(position);
                holder.update(user);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        navigationInterfaces.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_MEMBER_STATUS, user);
                    }
                });
            } else {
                super.onBindViewHolder(holder, position, payloads);
            }

        }

        @Override
        public int getItemCount() {
            return users.size();
        }
    }
}
