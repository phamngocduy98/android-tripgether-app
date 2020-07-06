package cf.bautroixa.maptest.presenter;

import android.graphics.Bitmap;
import android.net.Uri;

import cf.bautroixa.maptest.model.firestore.objects.Place;
import cf.bautroixa.maptest.ui.adapter.PostPlaceAdapter;

public interface CreatePostPresenter {
    void createPost(String body, Uri selectedImageUri, Bitmap selectedImageBitmap);

    void addPlace(Place place);

    void removePlace(Place place);

    void addTrip();

    interface View {
        void initAdapter(PostPlaceAdapter adapter);

        void showPlace(Place place);

        void onLoading(String text);

        void onFailed(String reason);

        void onSuccess();
    }
}
