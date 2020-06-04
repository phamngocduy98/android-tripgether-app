package cf.bautroixa.maptest.presenter;

import android.os.Bundle;

import cf.bautroixa.maptest.model.firestore.Discussion;
import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.ui.adapter.ChatAdapter;

public interface ChatPresenter {
    boolean handleIntent(Bundle bundle);

    User getUser(String userId);

    boolean isMe(String userId);

    void initAdapter();

    void sendMessage(String text);

    interface View {
        void setupToolbar(Discussion discussion);

        void setUpAdapter(ChatAdapter adapter);
    }
}
