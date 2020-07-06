package cf.bautroixa.maptest.ui.trip_invite;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.ui.adapter.pager_adapter.TripInvitationPagerAdapter;
import cf.bautroixa.maptest.ui.theme.OneAppbarActivity;

public class TripInvitationActivity extends OneAppbarActivity {
    Button btnFinish;
    TabLayout tabLayout;
    ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_invitation);
        setTitle("Mời bạn bè");
        setSubtitle("Tham gia chuyến đi cùng bạn");
        tabLayout = findViewById(R.id.tab_layout_activity_trip_invitation);
        viewPager = findViewById(R.id.pager_activity_trip_invitation);
        btnFinish = findViewById(R.id.btn_finish_activity_trip_invitation);

        setupTab();
        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    void setupTab() {
        viewPager.setAdapter(new TripInvitationPagerAdapter(this));
        viewPager.setSaveEnabled(false);
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(TripInvitationPagerAdapter.Tabs.tabNames[position]);
            }
        }).attach();
    }

}
