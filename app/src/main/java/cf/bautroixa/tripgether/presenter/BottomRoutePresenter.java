package cf.bautroixa.tripgether.presenter;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public interface BottomRoutePresenter {
    void getDirectionTo(double latitude, double longitude);

    interface View {
        void setUpView(String routeDistance, String routeDuration, List<LatLng> latLngs);

        void onLoading();

        void onLoadingFailed();
    }
}
