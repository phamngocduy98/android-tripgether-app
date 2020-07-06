package cf.bautroixa.tripgether.ui.trip_invite;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.ui.adapter.TripInvitationFriendsAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class TripInvitationFriendsFragment extends Fragment {
    ModelManager manager;
    RecyclerView rvFriends;
    TripInvitationFriendsAdapter adapter;
    ArrayList<User> friends;

    public TripInvitationFriendsFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = ModelManager.getInstance(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip_invitation_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        assert manager.getCurrentUser().getFriendsManager() != null;
        friends = manager.getCurrentUser().getFriendsManager().getList();

        rvFriends = view.findViewById(R.id.rv_friends_frag_trip_invitation_friends);
        adapter = new TripInvitationFriendsAdapter(requireContext(), friends);
        rvFriends.setAdapter(adapter);
        rvFriends.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

}
