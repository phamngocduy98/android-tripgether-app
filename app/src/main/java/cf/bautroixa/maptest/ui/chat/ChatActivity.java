package cf.bautroixa.maptest.ui.chat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.Discussion;
import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.presenter.ChatPresenter;
import cf.bautroixa.maptest.presenter.impl.ChatPresenterImpl;
import cf.bautroixa.maptest.ui.adapter.ChatAdapter;
import cf.bautroixa.maptest.ui.theme.OneLiteAppbarActivity;

public class ChatActivity extends OneLiteAppbarActivity implements ChatPresenter.View {
    public static final String ARG_DISCUSSION_ID = "discussionId";
    ChatPresenterImpl chatPresenter;
    private RecyclerView rvMessages;
    private EditText editMessage;

    public ChatActivity() {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatPresenter = new ChatPresenterImpl(this, this, this);
        editMessage = findViewById(R.id.edit_enter_message_frag_chat);
        Button btnSendMessage = findViewById(R.id.btn_send_frag_chat);
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
        if (!chatPresenter.handleIntent(args)) finish();
        chatPresenter.initAdapter();
    }

    @Override
    public void setupToolbar(Discussion discussion) {
        if (discussion.getTripRef() != null) {
            setTitle(discussion.getName());
        } else {
            ArrayList<User> members = discussion.getMembersManager().getList();
            for (int i = 0; i < members.size(); i++) {
                User user = members.get(i);
                if (!chatPresenter.isMe(user.getId())) {
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
        rvMessages.setLayoutManager(new LinearLayoutManager(ChatActivity.this, RecyclerView.VERTICAL, false));
    }
}
