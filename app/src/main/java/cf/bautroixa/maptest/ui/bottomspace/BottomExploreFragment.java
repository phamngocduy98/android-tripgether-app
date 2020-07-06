package cf.bautroixa.maptest.ui.bottomspace;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.Query;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.core.CollectionManager;
import cf.bautroixa.maptest.model.firestore.objects.Place;
import cf.bautroixa.maptest.ui.adapter.NearbyPlaceAdapter;
import cf.bautroixa.maptest.utils.calculation.GeoHashUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class BottomExploreFragment extends Fragment {
    private static final String TAG = "BottomExploreFragment";
    ModelManager manager;
    RecyclerView rvNearby;
    CollectionManager<Place> nearByPlaceManager;

    public BottomExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = ModelManager.getInstance(context);
        Query query = GeoHashUtils.queryNearby(manager.getBasePlaceManager().getRef(), 21.037027797903008, 105.83436258137226, 100);
        if (nearByPlaceManager == null) {
            nearByPlaceManager = new CollectionManager<>(Place.class, manager.getBasePlaceManager().getRef(), query);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        nearByPlaceManager.clear();
        nearByPlaceManager = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bottom_explore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvNearby = view.findViewById(R.id.rv_nearby);
        NearbyPlaceAdapter adapter = new NearbyPlaceAdapter(nearByPlaceManager.getList());
        rvNearby.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNearby.setAdapter(adapter);
        nearByPlaceManager.attachAdapter(this, adapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
