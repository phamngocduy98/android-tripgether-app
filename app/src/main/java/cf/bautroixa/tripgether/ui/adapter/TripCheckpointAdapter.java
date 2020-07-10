package cf.bautroixa.tripgether.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.vipulasri.timelineview.TimelineView;

import java.util.ArrayList;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.interfaces.ActivityNavigationInterface;
import cf.bautroixa.tripgether.model.repo.objects.CheckpointPublic;
import cf.bautroixa.tripgether.ui.adapter.viewholder.CheckpointVH;
import cf.bautroixa.tripgether.ui.dialogs.PlaceViewDialogFragment;

public class TripCheckpointAdapter extends RecyclerView.Adapter<TripCheckpointAdapter.TripViewCheckpointVH> {
    private ArrayList<CheckpointPublic> checkpoints;
    private ActivityNavigationInterface activityNavigationInterface;
    private FragmentManager fragmentManager;

    public TripCheckpointAdapter(ArrayList<CheckpointPublic> checkpoints, ActivityNavigationInterface activityNavigationInterface, FragmentManager fragmentManager) {
        this.checkpoints = checkpoints;
        this.activityNavigationInterface = activityNavigationInterface;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public TripViewCheckpointVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TripViewCheckpointVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkpoint_with_timeline, parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewCheckpointVH holder, int position) {
        holder.bind(checkpoints.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }

    @Override
    public int getItemCount() {
        return checkpoints.size();
    }

    public class TripViewCheckpointVH extends CheckpointVH {
        public TimelineView mTimelineView;
        TextView tvName, tvTime;
        ImageButton btnEdit;

        public TripViewCheckpointVH(@NonNull View itemView, int viewType) {
            super(itemView, viewType);
        }

        @Override
        public void bind(CheckpointPublic checkpointPublic) {
            super.bind(checkpointPublic);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PlaceViewDialogFragment.newInstance(checkpointPublic).show(fragmentManager, "place");
//                    activityNavigationInterface.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_SEARCH_RESULT, checkpoint.toSearchResult());
                }
            });
        }
    }
}
