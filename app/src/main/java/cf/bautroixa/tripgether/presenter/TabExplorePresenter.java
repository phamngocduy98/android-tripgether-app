package cf.bautroixa.tripgether.presenter;

import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.ui.adapter.PostAdapter;

public interface TabExplorePresenter {
    User getCurrentUser();

    interface View {
        void onUpdating();

        void initAdapter(PostAdapter adapter);

        void onFailed(String reason);
    }
}
