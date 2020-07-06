package cf.bautroixa.maptest.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.objects.Place;
import cf.bautroixa.maptest.ui.adapter.viewholder.PlaceVH;
import cf.bautroixa.maptest.ui.theme.OneRecyclerView;

public class NearbyPlaceAdapter extends OneRecyclerView.Adapter<PlaceVH> {
    ArrayList<Place> places;

    public NearbyPlaceAdapter(ArrayList<Place> places) {
        this.places = places;
    }

    @NonNull
    @Override
    public PlaceVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaceVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceVH holder, int position) {
        holder.bind(places.get(position), null);
    }

    @Override
    public int getItemCount() {
        return places.size();
    }
}
