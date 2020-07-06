package cf.bautroixa.maptest.ui.posts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.ui.theme.OneBottomSheetDialog;

public class AddToPostBottomSheet extends OneBottomSheetDialog {
    View addImage, addPlace, addTrip;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_add_to_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addImage = view.findViewById(R.id.linear_add_image_to_post);
        addPlace = view.findViewById(R.id.linear_add_place_to_post);
        addTrip = view.findViewById(R.id.linear_add_trip_to_post);
    }
}
