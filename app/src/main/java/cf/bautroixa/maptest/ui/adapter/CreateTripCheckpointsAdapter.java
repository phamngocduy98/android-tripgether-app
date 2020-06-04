package cf.bautroixa.maptest.ui.adapter;

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

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.Checkpoint;
import cf.bautroixa.maptest.presenter.impl.CreateTripPresenterImpl;
import cf.bautroixa.maptest.ui.dialogs.CheckpointEditDialogFragment;
import cf.bautroixa.maptest.utils.DateFormatter;

public class CreateTripCheckpointsAdapter extends RecyclerView.Adapter<CreateTripCheckpointsAdapter.CheckpointVH> {
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
    public CreateTripCheckpointsAdapter.CheckpointVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CreateTripCheckpointsAdapter.CheckpointVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkpoint_with_timeline, parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull CreateTripCheckpointsAdapter.CheckpointVH holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }

    @Override
    public int getItemCount() {
        return checkpoints.size();
    }

    public class CheckpointVH extends RecyclerView.ViewHolder {
        public TimelineView mTimelineView;
        TextView tvName, tvTime, tvLocation;
        ImageButton btnEdit;
        View view;

        public CheckpointVH(@NonNull View itemView, int viewType) {
            super(itemView);
            view = itemView;
            tvName = itemView.findViewById(R.id.tv_name_item_checkpoint);
//            tvLocation = itemView.findViewById(R.id.tv_location_item_checkpoint);
            tvTime = itemView.findViewById(R.id.tv_time_item_checkpoint);
            btnEdit = itemView.findViewById(R.id.btn_edit_item_checkpoint);
            mTimelineView = itemView.findViewById(R.id.timeline);
            mTimelineView.initLine(viewType);
        }

        public void bind(final int position) {
            final Checkpoint checkpoint = checkpoints.get(position);
            tvName.setText(checkpoint.getName());
            tvTime.setText(DateFormatter.format(checkpoint.getTime()));
//            tvLocation.setText(checkpoint.getLocation());

            mTimelineView.setMarker(mTimelineView.getContext().getResources().getDrawable(R.drawable.bg_item_message_outcoming));
            btnEdit.setVisibility(View.VISIBLE);
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckpointEditDialogFragment.newInstance(checkpoint, new CheckpointEditDialogFragment.OnCheckpointSetListener() {
                        @Override
                        public void onCheckpointSet(Checkpoint newCheckpoint) {
                            checkpoints.set(position, newCheckpoint);
                            notifyItemChanged(position);
                        }
                    }, new CheckpointEditDialogFragment.OnDeleteCheckpointListener() {
                        @Override
                        public void onCheckpointDeleted() {
                            checkpoints.remove(checkpoint);
                            notifyItemRemoved(position);
                        }
                    }).show(fragmentManager, "edit checkpoint");
                }
            });
        }
    }
}