package cf.bautroixa.tripgether.ui.adapter.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.vipulasri.timelineview.TimelineView;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.model.repo.objects.CheckpointPublic;
import cf.bautroixa.tripgether.ui.theme.OneRecyclerView;
import cf.bautroixa.tripgether.utils.ui_utils.DateFormatter;

public class CheckpointVH extends OneRecyclerView.ViewHolder {
    public TimelineView mTimelineView;
    TextView tvName, tvTime, tvDate, tvAddress;
    protected ImageButton btnEdit;
    protected Context context;

    public CheckpointVH(@NonNull View itemView, int viewType) {
        super(itemView, viewType);
        context = itemView.getContext();
        tvName = itemView.findViewById(R.id.tv_name_item_checkpoint);
        tvTime = itemView.findViewById(R.id.tv_time_item_checkpoint);
        tvDate = itemView.findViewById(R.id.tv_date_item_checkpoint);
        tvAddress = itemView.findViewById(R.id.tv_address_item_checkpoint);
        btnEdit = itemView.findViewById(R.id.btn_edit_item_checkpoint);
        mTimelineView = itemView.findViewById(R.id.timeline);
        mTimelineView.initLine(viewType);
    }

    public void bind(final Checkpoint checkpoint) {
        tvAddress.setText(checkpoint.getLocation());
        tvName.setText(checkpoint.getName());
        tvTime.setText(DateFormatter.formatTime(checkpoint.getTime()));
        tvDate.setText(DateFormatter.formatDate(checkpoint.getTime()));
    }

    public void bind(final CheckpointPublic checkpointPublic) {
        tvAddress.setText(checkpointPublic.getLocation());
        tvName.setText(checkpointPublic.getName());
        tvTime.setText(DateFormatter.formatTime(checkpointPublic.getTime().toFirebaseTimestamp()));
        tvDate.setText(DateFormatter.formatDate(checkpointPublic.getTime().toFirebaseTimestamp()));
    }

    public void setActiveCheckpoint(boolean active) {

    }
}