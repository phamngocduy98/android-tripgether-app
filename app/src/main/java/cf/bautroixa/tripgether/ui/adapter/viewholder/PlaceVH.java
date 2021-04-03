package cf.bautroixa.tripgether.ui.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.objects.Place;
import cf.bautroixa.tripgether.model.types.SearchResult;
import cf.bautroixa.ui.OneRecyclerView;

public class PlaceVH extends OneRecyclerView.ViewHolder {
    TextView tvName, tvTime, tvLocation;

    public PlaceVH(@NonNull View itemView, int viewType) {
        super(itemView, viewType);
        tvName = itemView.findViewById(R.id.tv_name_item_checkpoint);
        tvLocation = itemView.findViewById(R.id.tv_location_item_checkpoint);
        tvTime = itemView.findViewById(R.id.tv_time_item_checkpoint);
    }

    public void bind(SearchResult searchResult, View.OnClickListener onClickListener) {
        tvName.setText(searchResult.getPlaceName());
        tvLocation.setText(searchResult.getPlaceAddress());
        tvTime.setText("");
        itemView.setOnClickListener(onClickListener);
    }

    public void bind(Place place, View.OnClickListener onClickListener) {
        tvName.setText(place.getPlaceName());
        tvLocation.setText(place.getPlaceAddress());
        tvTime.setText("");
        itemView.setOnClickListener(onClickListener);
    }
}
