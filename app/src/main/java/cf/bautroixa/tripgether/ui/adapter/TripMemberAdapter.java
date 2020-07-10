package cf.bautroixa.tripgether.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.repo.objects.UserPublic;
import cf.bautroixa.tripgether.ui.adapter.viewholder.UserPublicVH;
import cf.bautroixa.tripgether.ui.friends.ProfileActivity;

public class TripMemberAdapter extends RecyclerView.Adapter<TripMemberAdapter.TripMemberVH> {
    ArrayList<UserPublic> members;
    Context context;

    public TripMemberAdapter(Context context, ArrayList<UserPublic> members) {
        this.members = members;
        this.context = context;
    }

    @NonNull
    @Override
    public TripMemberAdapter.TripMemberVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TripMemberAdapter.TripMemberVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_with_action_btn, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TripMemberAdapter.TripMemberVH holder, int position) {
        holder.bind(members.get(position));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public class TripMemberVH extends UserPublicVH {

        public TripMemberVH(@NonNull View itemView) {
            super(itemView);
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
        }
    }
}
