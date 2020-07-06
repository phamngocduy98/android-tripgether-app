package cf.bautroixa.maptest.presenter;

import cf.bautroixa.maptest.model.firestore.objects.User;
import cf.bautroixa.maptest.ui.adapter.PostAdapter;

public interface TabExplorePresenter {
    User getCurrentUser();

    interface View {
        void onUpdating();

        void initAdapter(PostAdapter adapter);

        void onFailed(String reason);
    }
}
