package cf.bautroixa.maptest.presenter;

import android.os.Bundle;

import cf.bautroixa.maptest.model.firestore.objects.Discussion;
import cf.bautroixa.maptest.model.firestore.objects.User;
import cf.bautroixa.maptest.ui.adapter.ChatAdapter;

public interface ChatPresenter {
    boolean init(Bundle bundle);

    User getUser(String userId);

    boolean isMe(String userId);

    void initAdapter();

    void sendMessage(String text);

    interface View {
        void setupToolbar(Discussion discussion);
        void setUpAdapter(ChatAdapter adapter);

        void scrollToEnd(int size);

        void onLoading();

        void onFailed(String message);

        void onSuccess();
    }
}
