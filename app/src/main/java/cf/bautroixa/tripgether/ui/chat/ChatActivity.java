package cf.bautroixa.tripgether.ui.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.objects.Discussion;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.presenter.ChatPresenter;
import cf.bautroixa.tripgether.presenter.impl.ChatPresenterImpl;
import cf.bautroixa.tripgether.ui.adapter.ChatAdapter;
import cf.bautroixa.tripgether.ui.dialogs.LoadingDialogHelper;
import cf.bautroixa.tripgether.ui.theme.LoadingDialogFragment;
import cf.bautroixa.tripgether.ui.theme.OneLiteAppbarActivity;

public class ChatActivity extends OneLiteAppbarActivity implements ChatPresenter.View, Toolbar.OnMenuItemClickListener {
    public static final String ARG_DISCUSSION_ID = "discussionId";
    public static final String ARG_TO_USER_ID = "userId";
    ChatPresenterImpl chatPresenter;
    LinearLayoutManager linearLayoutManager;
    private RecyclerView rvMessages;
    private EditText editMessage;
    private User userOfDialogue;
    private LoadingDialogFragment loadingDialog;

    public ChatActivity() {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        editMessage = findViewById(R.id.edit_enter_message_frag_chat);
        linearLayoutManager = new LinearLayoutManager(ChatActivity.this, RecyclerView.VERTICAL, true);
        Button btnSendMessage = findViewById(R.id.btn_send_frag_chat);

        chatPresenter = new ChatPresenterImpl(this, this, this);

        editMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollToEnd(0);
            }
        });
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = editMessage.getText().toString();
                if (messageText.length() == 0) return;
                chatPresenter.sendMessage(messageText);
                editMessage.setText("");
            }
        });
        rvMessages = findViewById(R.id.rv_messages);

        Bundle args = getIntent().getExtras();
        if (!chatPresenter.init(args)) finish();
    }

    @Override
    public void setupToolbar(Discussion discussion) {
        if (discussion.getTripRef() != null) {
            setTitle(discussion.getName());
        } else {
            setToolbarMenu(R.menu.activity_chat);
            getToolbar().setOnMenuItemClickListener(this);
            ArrayList<User> members = discussion.getMembersManager().getList();
            for (int i = 0; i < members.size(); i++) {
                User user = members.get(i);
                if (!chatPresenter.isMe(user.getId())) {
                    userOfDialogue = user;
                    setTitle(user.getName());
                    setAvatarImage(user.getAvatar());
                    return;
                }
            }
        }
    }

    @Override
    public void setUpAdapter(ChatAdapter adapter) {
        rvMessages.setAdapter(adapter);
        rvMessages.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void scrollToEnd(int size) {
        if (size <= 0) return;
        linearLayoutManager.scrollToPosition(0);
//        rvMessages.smoothScrollToPosition(0);
    }

    @Override
    public void onLoading() {
        loadingDialog = LoadingDialogHelper.create(getSupportFragmentManager());
    }

    @Override
    public void onFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        loadingDialog.dismiss();
    }

    @Override
    public void onSuccess() {
        loadingDialog.dismiss();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_call:
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + userOfDialogue.getPhoneNumber()));
                startActivity(intent);
                return true;
            case R.id.menu_sms:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", userOfDialogue.getPhoneNumber(), null)));
                return true;
            default:
                return false;
        }
    }
}
