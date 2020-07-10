package cf.bautroixa.tripgether.ui.bottomsheet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import java.util.ArrayList;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.interfaces.NavigationInterface;
import cf.bautroixa.tripgether.interfaces.NavigationInterfaceOwner;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.core.DocumentsManager;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.ui.trip_invite.TripInvitationActivity;

public class BottomSheetMemberListFragment extends Fragment implements NavigationInterfaceOwner {
    private static final String TAG = "FriendListStatusFrag";

    // data and state
    private ModelManager manager;
    private ArrayList<User> members;

    // listener
    private DocumentsManager.OnListChangedListener<User> onMembersChangedListener;
    private NavigationInterface navigationInterface;
//    private OnFilterUser onFilterUser, defaultUserFilter;

    // view
    private RecyclerView rvFriendList, rvFriendListLite;
    private View btnInviteToTrip;

    // adapter
    private MemberListRecyclerView.MembersAdapter membersAdapter, membersLiteAdapter;

    public BottomSheetMemberListFragment() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = ModelManager.getInstance(context);
        members = manager.getCurrentTrip().getMembersManager().getList();
//        defaultUserFilter = new OnFilterUser() {
//            @Override
//            public boolean onUserFiltering(User user) {
//                return true;
//            }
//        };
//        onFilterUser = defaultUserFilter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_sheet_member_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        membersAdapter = new MemberListRecyclerView(manager, navigationInterface).getAdapter();

        rvFriendList = view.findViewById(R.id.rv_friend_list);
        rvFriendList.setAdapter(membersAdapter);
        rvFriendList.setLayoutManager(new LinearLayoutManager(getContext()));

        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(rvFriendListLite);

        manager.getCurrentTrip().getMembersManager().attachAdapter(this, membersAdapter);

        btnInviteToTrip = view.findViewById(R.id.btn_invite_to_trip);
        btnInviteToTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), TripInvitationActivity.class));
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        this.removeFilter();
        navigationInterface = null;
    }

//    public void applyFilter(OnFilterUser onFilterUser) {
//        this.onFilterUser = onFilterUser;
//        members.clear();
//        for (User user : manager.getMembers()) {
//            if (onFilterUser.onUserFiltering(user)) {
//                members.add(user);
//            }
//        }
//        if (this.friendStatusAdapter != null) this.friendStatusAdapter.notifyDataSetChanged();
//        if (this.friendStatusLiteAdapter != null)
//            this.friendStatusLiteAdapter.notifyDataSetChanged();
//    }
//
//    public void removeFilter() {
//        onFilterUser = defaultUserFilter;
//        members.clear();
//        members.addAll(manager.getMembers());
//        if (this.friendStatusAdapter != null) this.friendStatusAdapter.notifyDataSetChanged();
//        if (this.friendStatusLiteAdapter != null)
//            this.friendStatusLiteAdapter.notifyDataSetChanged();
//    }

    public void setNavigationInterface(NavigationInterface navigationInterface) {
        this.navigationInterface = navigationInterface;
    }

    public interface OnFilterUser {
        boolean onUserFiltering(User user);
    }

    public static class Payload {
        public static final int UPDATED = 1;
    }
}
