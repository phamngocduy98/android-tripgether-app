package cf.bautroixa.maptest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText editText;
    Button btnLogin;
    SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPref = getSharedPreferences(getString(R.string.shared_preference_name), Context.MODE_PRIVATE);
        if (!"notLogin".equals(sharedPref.getString("userName", "notLogin"))){
            onLoginSuccess();
        }

        editText = findViewById(R.id.edit_username_activity_login);
        btnLogin = findViewById(R.id.btn_login_activity_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("userName", editText.getText().toString());
                editor.apply();
                onLoginSuccess();
            }
        });
    }

    public void onLoginSuccess(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
