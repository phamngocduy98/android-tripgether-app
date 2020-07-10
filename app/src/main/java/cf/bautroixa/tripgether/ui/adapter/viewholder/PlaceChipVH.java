package cf.bautroixa.tripgether.ui.adapter.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.objects.Place;

public class PlaceChipVH extends RecyclerView.ViewHolder {
    Chip chip;

    public PlaceChipVH(@NonNull View itemView) {
        super(itemView);
        chip = itemView.findViewById(R.id.chip_post_place);
    }

    public void bind(Place place, OnChipClickedListener onChipClickedListener) {
        chip.setText(place.getPlaceName());
        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChipClickedListener.onClick(place);
            }
        });
        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChipClickedListener.onRemove(getAdapterPosition(), place);
            }
        });
    }

    public interface OnChipClickedListener {
        void onClick(Place place);

        void onRemove(int position, Place place);
    }
}
