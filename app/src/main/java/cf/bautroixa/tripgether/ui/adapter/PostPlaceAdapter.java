package cf.bautroixa.tripgether.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.objects.Place;
import cf.bautroixa.tripgether.ui.adapter.viewholder.PlaceChipVH;

public class PostPlaceAdapter extends RecyclerView.Adapter<PlaceChipVH> {
    ArrayList<Place> places;
    PlaceChipVH.OnChipClickedListener onChipClickedListener;

    public PostPlaceAdapter(ArrayList<Place> places, PlaceChipVH.OnChipClickedListener onChipClickedListener) {
        this.places = places;
        this.onChipClickedListener = onChipClickedListener;
    }

    @NonNull
    @Override
    public PlaceChipVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ViewType.EDITABLE) {
            return new PlaceChipVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_place_editable, parent, false));
        }
        return new PlaceChipVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_place, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceChipVH holder, int position) {
        Place place = places.get(position);
        holder.bind(place, onChipClickedListener);
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    @Override
    public int getItemViewType(int position) {
        return ViewType.EDITABLE;
    }

    public interface ViewType {
        int EDITABLE = 1;
        int STATIC = 0;
    }
}
