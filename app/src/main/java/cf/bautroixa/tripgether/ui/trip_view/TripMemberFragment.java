package cf.bautroixa.tripgether.ui.trip_view;

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
import cf.bautroixa.tripgether.model.repo.objects.UserPublic;
import cf.bautroixa.tripgether.ui.adapter.TripMemberAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class TripMemberFragment extends Fragment {
    public static final String ARG_MEMBER = "members";
    RecyclerView rvTripMember;
    TripMemberAdapter adapter;
    ArrayList<UserPublic> members;

    public TripMemberFragment() {
    }

    public static TripMemberFragment newInstance(ArrayList<UserPublic> members) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_MEMBER, members);
        TripMemberFragment fragment = new TripMemberFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip_member, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvTripMember = view.findViewById(R.id.rv_trip_member);
        rvTripMember.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            members = args.getParcelableArrayList(ARG_MEMBER);
            if (members != null) {
                adapter = new TripMemberAdapter(requireContext(), members);
                rvTripMember.setAdapter(adapter);
            }
        }
    }
}
