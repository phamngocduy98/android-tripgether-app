package cf.bautroixa.tripgether.presenter.impl;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.core.DocumentsManager;
import cf.bautroixa.tripgether.model.firestore.core.RefsArrayManager;
import cf.bautroixa.tripgether.model.firestore.objects.Comment;
import cf.bautroixa.tripgether.model.firestore.objects.Place;
import cf.bautroixa.tripgether.model.firestore.objects.Post;
import cf.bautroixa.tripgether.model.http.PostHttpService;
import cf.bautroixa.tripgether.presenter.PostPresenter;
import cf.bautroixa.tripgether.ui.adapter.PostPlaceAdapter;
import cf.bautroixa.tripgether.ui.adapter.viewholder.PlaceChipVH;
import cf.bautroixa.tripgether.utils.IntentHelper;

public class PostPresenterImpl implements PostPresenter {
    Context context;
    View view;
    ModelManager manager;

    public PostPresenterImpl(Context context, View view) {
        this.context = context;
        this.view = view;
        manager = ModelManager.getInstance(context);
    }

    public void bindPlaceAdapter(Post post) {
        post.initSubManager(manager.getCurrentUserRef(), manager.getBasePlaceManager());
        RefsArrayManager<Place> placeManager = post.getPlaceManager();
        PostPlaceAdapter adapter = new PostPlaceAdapter(placeManager.getList(), new PlaceChipVH.OnChipClickedListener() {
            @Override
            public void onClick(Place place) {
                view.showPlace(place);
            }

            @Override
            public void onRemove(int position, Place place) {
                // do nothing, this never be called
            }
        }) {
            @Override
            public int getItemViewType(int position) {
                return ViewType.STATIC;
            }
        };
        view.bindAdapter(adapter);
        placeManager.addOneTimeInitCompleteListener(new DocumentsManager.OnInitCompleteListener<Place>() {
            @Override
            public void onComplete(ArrayList<Place> list) {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void like(Post post) {
        view.onSendingLike();
        boolean userLiked = post.isUserLiked();
        PostHttpService.likePost(post.getId(), null, !userLiked).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    view.onLikeSuccess();
                } else {
                    view.onFailed(task.getException().getMessage());
                }
            }
        });
    }

    @Override
    public void likeComment(Comment comment) {
        view.onSendingLike();
        DocumentReference postRef = comment.getRef().getParent().getParent();
        boolean userLiked = comment.isUserLiked();
        PostHttpService.likePost(postRef.getId(), comment.getId(), !userLiked).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    view.onLikeSuccess();
                } else {
                    view.onFailed(task.getException().getMessage());
                }
            }
        });
    }

    @Override
    public void share(Post post) {
        IntentHelper.sendPost(context, post.getId());
    }
}
