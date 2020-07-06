package cf.bautroixa.maptest.ui.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.objects.Checkpoint;
import cf.bautroixa.maptest.ui.theme.OneDialog;
import cf.bautroixa.maptest.utils.ui_utils.DateFormatter;

public class SelectCheckpointDialogFragment extends OneDialog {
    RecyclerView rv;
    ModelManager manager;
    OnCheckpointSelectedListener onCheckpointSelectedListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setButtonClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = ModelManager.getInstance(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View body = inflater.inflate(R.layout.dialog_body_select_checkpoint, container, false);
        rv = body.findViewById(R.id.rv_dialog_body_select_checkpoint);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new CheckPointAdapter(new ArrayList<>(manager.getCurrentTrip().getCheckpointsManager().getList())));
        setCustomBody(body);
        setTitleRes(R.string.dialog_title_select_checkpoint);
        setPosBtnRes(R.string.btn_cancel);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void setOnCheckpointSelectedListener(OnCheckpointSelectedListener onCheckpointSelectedListener) {
        this.onCheckpointSelectedListener = onCheckpointSelectedListener;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onCheckpointSelectedListener = null;
    }

    public static class CheckPointViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime, tvLocation;
        View view;

        public CheckPointViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            tvName = itemView.findViewById(R.id.tv_name_item_checkpoint);
            tvLocation = itemView.findViewById(R.id.tv_location_item_checkpoint);
            tvTime = itemView.findViewById(R.id.tv_time_item_checkpoint);
        }

        public void bind(Checkpoint checkpoint) {
            tvName.setText(checkpoint.getName());
            tvTime.setText(DateFormatter.format(checkpoint.getTime()));
            tvLocation.setText(checkpoint.getLocation());
        }
    }

    public interface OnCheckpointSelectedListener {
        void onCheckpointSelected(Checkpoint checkpoint);
    }

    public class CheckPointAdapter extends RecyclerView.Adapter<CheckPointViewHolder> {

        private final int TYPE_CHECKPOINT_ITEM = 0;
        private final int TYPE_ADD_CHECKPOINT_BTN = 1;
        ArrayList<Checkpoint> checkpoints;

        public CheckPointAdapter(ArrayList<Checkpoint> checkpoints) {
            this.checkpoints = checkpoints;
        }

        @NonNull
        @Override
        public CheckPointViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CheckPointViewHolder(getLayoutInflater().inflate(R.layout.item_place, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull CheckPointViewHolder holder, int position) {
            final Checkpoint checkpoint = this.checkpoints.get(position);
            holder.bind(checkpoint);
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onCheckpointSelectedListener != null) {
                        onCheckpointSelectedListener.onCheckpointSelected(checkpoint);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return this.checkpoints.size();
        }
    }
}
