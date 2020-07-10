package cf.bautroixa.tripgether.presenter.post;

import cf.bautroixa.tripgether.model.firestore.objects.Comment;
import cf.bautroixa.tripgether.model.firestore.objects.Place;
import cf.bautroixa.tripgether.model.firestore.objects.Post;
import cf.bautroixa.tripgether.ui.adapter.PostPlaceAdapter;

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
