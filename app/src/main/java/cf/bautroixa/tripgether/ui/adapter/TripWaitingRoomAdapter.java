package cf.bautroixa.tripgether.ui.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.repo.objects.UserPublic;
import cf.bautroixa.tripgether.presenter.WaitingRoomItemPresenter;
import cf.bautroixa.tripgether.presenter.impl.WaitingRoomItemPresenterImpl;
import cf.bautroixa.tripgether.ui.adapter.viewholder.UserPublicVH;
import cf.bautroixa.tripgether.ui.dialogs.LoadingDialogHelper;
import cf.bautroixa.tripgether.ui.friends.ProfileActivity;

public class TripWaitingRoomAdapter extends RecyclerView.Adapter<TripWaitingRoomAdapter.WaitingUserVH> {
    ArrayList<UserPublic> members;
    Context context;

    public TripWaitingRoomAdapter(Context context, ArrayList<UserPublic> members) {
        this.members = members;
        this.context = context;
    }

    @NonNull
    @Override
    public TripWaitingRoomAdapter.WaitingUserVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TripWaitingRoomAdapter.WaitingUserVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_with_action_btn, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TripWaitingRoomAdapter.WaitingUserVH holder, int position) {
        holder.bind(members.get(position));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public class WaitingUserVH extends UserPublicVH implements WaitingRoomItemPresenter.View {
        WaitingRoomItemPresenterImpl waitingRoomItemPresenter;
        private ProgressDialog loadingDialog;

        public WaitingUserVH(@NonNull View itemView) {
            super(itemView);
            waitingRoomItemPresenter = new WaitingRoomItemPresenterImpl(context, this);
        }

        @Override
        public void bind(final UserPublic user) {
            super.bind(user);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.putExtra(ProfileActivity.ARG_USER_PUBLIC_DATA, (Parcelable) user);
                    context.startActivity(intent);
                }
            });
            btnInvite.setVisibility(View.VISIBLE);
            btnInvite.setText("Cho phép");
            btnInvite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    waitingRoomItemPresenter.allowJoinTrip(user);
                }
            });
        }

        @Override
        public void onLoading() {
            loadingDialog = LoadingDialogHelper.create(context, "Vui lòng đợi");
        }

        @Override
        public void onFailed(String reason) {
            loadingDialog.dismiss();
            Toast.makeText(context, reason, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onSuccess() {
            loadingDialog.dismiss();
            btnInvite.setText("Đã tham gia");
            btnInvite.setEnabled(false);
        }
    }
}
