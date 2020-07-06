package cf.bautroixa.tripgether.ui.friends;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.constant.RequestCodes;
import cf.bautroixa.tripgether.ui.adapter.SyncContactAdapter;
import cf.bautroixa.tripgether.ui.theme.OneAppbarActivity;
import cf.bautroixa.tripgether.ui.theme.OneDialog;
import cf.bautroixa.tripgether.utils.ContactHelper;

public class SyncContactActivity extends OneAppbarActivity {
    RecyclerView rvContacts;
    SyncContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_contact);
        setTitle("Đồng bộ danh bạ");
        setSubtitle("Mời bạn bè và người thân tham gia");

        rvContacts = findViewById(R.id.rv_contacts);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
    }

    void setupAdapter() {
        if (adapter == null) {
            ArrayList<ContactHelper.Contact> contacts = ContactHelper.getAllContacts(this);
            adapter = new SyncContactAdapter(contacts, this, this);
            rvContacts.setAdapter(adapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                OneDialog permissionDialog = new OneDialog.Builder()
                        .title(R.string.dialog_title_permission_read_contact)
                        .message(R.string.dialog_message_permission_read_contact)
                        .enableNegativeButton(true).buttonClickListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == DialogInterface.BUTTON_POSITIVE) {
                                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, RequestCodes.CONTACT_PERMISSION);
                                    dialog.dismiss();
                                } else {
                                    dialog.dismiss();
                                    finish();
                                }
                            }
                        }).build();
                permissionDialog.show(getSupportFragmentManager(), "request CONTACT_PERMISSION");
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, RequestCodes.CONTACT_PERMISSION);
            }
        } else {
            setupAdapter();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RequestCodes.CONTACT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupAdapter();
            }
        }
    }
}
