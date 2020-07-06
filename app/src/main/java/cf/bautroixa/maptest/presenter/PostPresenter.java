package cf.bautroixa.maptest.presenter;

import cf.bautroixa.maptest.model.firestore.objects.Comment;
import cf.bautroixa.maptest.model.firestore.objects.Place;
import cf.bautroixa.maptest.model.firestore.objects.Post;
import cf.bautroixa.maptest.ui.adapter.PostPlaceAdapter;

public interface PostPresenter {
    void like(Post post);

    void likeComment(Comment comment);

    void share(Post post);

    interface View {
        void bindAdapter(PostPlaceAdapter adapter);

        void showPlace(Place place);

        void onSendingLike();

        void onLikeSuccess();

        void onFailed(String reason);
    }
}
