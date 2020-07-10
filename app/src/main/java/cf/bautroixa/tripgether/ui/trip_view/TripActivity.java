package cf.bautroixa.tripgether.ui.trip_view;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.interfaces.ActivityNavigationInterface;
import cf.bautroixa.tripgether.interfaces.ActivityNavigationInterfaceOwner;
import cf.bautroixa.tripgether.model.firestore.core.Document;
import cf.bautroixa.tripgether.model.repo.objects.TripPublic;
import cf.bautroixa.tripgether.presenter.trip.TripPresenter;
import cf.bautroixa.tripgether.presenter.trip.TripPresenterImpl;
import cf.bautroixa.tripgether.ui.adapter.pager_adapter.TripPagerAdapter;
import cf.bautroixa.tripgether.ui.dialogs.LoadingDialogHelper;
import cf.bautroixa.tripgether.ui.theme.OneAppbarActivity;
import cf.bautroixa.tripgether.utils.NavigableHelper;

public class TripActivity extends OneAppbarActivity implements TripPresenter.View {
    public static final String ARG_TRIP_ID = "tripId";
    public static final String ARG_JOIN_CODE = "joinCode";
    TripPresenterImpl tripPresenter;
    ViewPager2 pager;
    TabLayout tabLayout;
    TripPagerAdapter adapter;
    Button btnJoinTrip;
    private ProgressDialog loadingDialog;
    private String tripId, joinCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        pager = findViewById(R.id.pager_trip_activity);
        btnJoinTrip = findViewById(R.id.btn_join_trip_activity_trip);
        tabLayout = findViewById(R.id.tab_layout_activity_trip);

        pager.setSaveEnabled(false);
        pager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        tripPresenter = new TripPresenterImpl(this, this);
        Uri data = getIntent().getData();
        Bundle args = getIntent().getExtras();
        if (data != null && data.getScheme() != null) {
            String scheme = data.getScheme(); // "http" or "tripgether"
            List<String> params = data.getPathSegments();
            if (scheme.equals("http") && params.size() >= 3 && params.get(1).equals("trips")) {
                tripId = params.get(2);
                if (params.size() >= 5 && params.get(3).equals("join")) {
                    joinCode = params.get(3);
                    tripPresenter.init(tripId, joinCode);
                } else {
                    tripPresenter.init(tripId, null);
                }
                return;
            } else if (scheme.equals("tripgether") && params.size() > 0) {
                tripId = params.get(0);
                if (params.size() >= 3 && params.get(1).equals("join")) {
                    joinCode = params.get(2);
                    tripPresenter.init(tripId, joinCode);
                } else {
                    tripPresenter.init(tripId, null);
                }
                return;
            } else {
                Toast.makeText(this, "Lỗi: invalid web intent", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        if (args != null) {
            tripId = args.getString(ARG_TRIP_ID, null);
            joinCode = args.getString(ARG_JOIN_CODE, null);
            if (tripId != null) tripPresenter.init(tripId, joinCode);
        }

        btnJoinTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tripPresenter.joinTrip();
            }
        });
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof ActivityNavigationInterfaceOwner) {
            ((ActivityNavigationInterfaceOwner) fragment).setActivityNavigationInterface(new ActivityNavigationInterface() {
                @Override
                public void navigate(int tab, int state, Document data) {
                    NavigableHelper.navigate(TripActivity.this, tab, state, data);
                    finish();
                }

                @Override
                public void navigate(int tab, int state, String klassName, String documentId) {
                    NavigableHelper.navigate(TripActivity.this, tab, state, klassName, documentId);
                }
            });
        }
    }

    @Override
    public void setupView(TripPublic tripPublic) {
        loadingDialog.dismiss();
        setTitle(tripPublic.getName());
        adapter = new TripPagerAdapter(TripActivity.this, tripPublic);
        pager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, pager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                String tabName = TripPagerAdapter.Tabs.names[position];
                tab.setText(tabName);
            }
        }).attach();
    }

    @Override
    public void onLoading() {
        loadingDialog = LoadingDialogHelper.create(this, "Vui lòng đợi");
    }

    @Override
    public void onJoinComplete() {
        loadingDialog.dismiss();
        Toast.makeText(this, "Thành công", Toast.LENGTH_LONG).show();
//        btnJoinTrip.setText("Thành công");
//        btnJoinTrip.setEnabled(false);
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void onFailed(String message) {
        loadingDialog.dismiss();
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}
