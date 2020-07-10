package cf.bautroixa.tripgether.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.vipulasri.timelineview.TimelineView;

import java.util.ArrayList;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.presenter.trip.CreateTripPresenterImpl;
import cf.bautroixa.tripgether.ui.adapter.viewholder.CheckpointVH;
import cf.bautroixa.tripgether.ui.dialogs.CheckpointEditDialogFragment;

public class CreateTripCheckpointsAdapter extends RecyclerView.Adapter<CreateTripCheckpointsAdapter.CreateTripCheckpointVH> {
    private CreateTripPresenterImpl createTripPresenter;
    private ArrayList<Checkpoint> checkpoints;
    private FragmentManager fragmentManager;

    public CreateTripCheckpointsAdapter(CreateTripPresenterImpl createTripPresenter, FragmentManager fragmentManager) {
        this.createTripPresenter = createTripPresenter;
        this.fragmentManager = fragmentManager;
    }

    public void setCheckpoints(ArrayList<Checkpoint> checkpoints) {
        this.checkpoints = checkpoints;
    }

    @NonNull
    @Override
    public CreateTripCheckpointVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CreateTripCheckpointVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkpoint_with_timeline, parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull CreateTripCheckpointVH holder, int position) {
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

    public class CreateTripCheckpointVH extends CheckpointVH {

        public CreateTripCheckpointVH(@NonNull View itemView, int viewType) {
            super(itemView, viewType);
        }

        @Override
        public void bind(Checkpoint checkpoint) {
            super.bind(checkpoint);
            mTimelineView.setMarker(mTimelineView.getContext().getResources().getDrawable(R.drawable.bg_item_message_outcoming));
            btnEdit.setVisibility(View.VISIBLE);
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckpointEditDialogFragment.newInstance(checkpoint, new CheckpointEditDialogFragment.OnCheckpointSetListener() {
                        @Override
                        public void onCheckpointSet(Checkpoint newCheckpoint) {
                            checkpoints.set(getAdapterPosition(), newCheckpoint);
                            notifyItemChanged(getAdapterPosition());
                        }
                    }, new CheckpointEditDialogFragment.OnDeleteCheckpointListener() {
                        @Override
                        public void onCheckpointDeleted() {
                            checkpoints.remove(checkpoint);
                            notifyItemRemoved(getAdapterPosition());
                        }
                    }).show(fragmentManager, "edit checkpoint");
                }
            });
        }
    }
}