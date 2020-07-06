package cf.bautroixa.maptest.ui.adapter.pager_adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import cf.bautroixa.maptest.ui.trip_invite.TripInvitationFriendsFragment;
import cf.bautroixa.maptest.ui.trip_invite.TripInvitationQrFragment;

public class TripInvitationPagerAdapter extends FragmentStateAdapter {
    private TripInvitationQrFragment qrFragment;
    private TripInvitationFriendsFragment friendsFragment;

    public TripInvitationPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                qrFragment = new TripInvitationQrFragment();
                return qrFragment;
            default:
                friendsFragment = new TripInvitationFriendsFragment();
                return friendsFragment;
        }
    }

    public TripInvitationQrFragment getQrFragment() {
        return qrFragment;
    }

    public TripInvitationFriendsFragment getFriendsFragment() {
        return friendsFragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public interface Tabs {
        String[] tabNames = {"Mời bạn bè", "Quét mã"};
        int TAB_FRIEND_INVITATION = 0, TAB_SCAN_QR = 1;
    }
}