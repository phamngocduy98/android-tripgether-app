package cf.bautroixa.maptest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SlidrConfig config = new SlidrConfig.Builder()
//                .primaryColor(getResources().getColor(R.color.colorPrimary))
//                .secondaryColor(getResources().getColor(R.color.colorAccent))
                .position(SlidrPosition.TOP)
                .build();

        Slidr.attach(this);
    }
}
