package cf.bautroixa.tripgether.ui.adapter.pager_adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import cf.bautroixa.tripgether.model.repo.objects.TripPublic;
import cf.bautroixa.tripgether.ui.trip_view.TripCheckpointFragment;
import cf.bautroixa.tripgether.ui.trip_view.TripMemberFragment;

public class TripPagerAdapter extends FragmentStateAdapter {
    private final TripPublic tripPublic;

    public interface Tabs {
        int TAB_MEMBERS = 0, TAB_CHECKPOINTS = 1;
        String[] names = {"Thành viên", "Địa điểm"};
    }

    public TripPagerAdapter(@NonNull FragmentActivity fragmentActivity, TripPublic tripPublic) {
        super(fragmentActivity);
        this.tripPublic = tripPublic;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return TripMemberFragment.newInstance(tripPublic.getMembers());
        }
        return TripCheckpointFragment.newInstance(tripPublic.getCheckpoints());
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
