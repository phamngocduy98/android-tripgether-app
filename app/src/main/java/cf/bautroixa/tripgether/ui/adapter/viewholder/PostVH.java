package cf.bautroixa.tripgether.ui.adapter.viewholder;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;

import java.util.List;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.objects.Place;
import cf.bautroixa.tripgether.model.firestore.objects.Post;
import cf.bautroixa.tripgether.model.repo.RepositoryManager;
import cf.bautroixa.tripgether.model.repo.UserRepository;
import cf.bautroixa.tripgether.model.repo.objects.UserPublic;
import cf.bautroixa.tripgether.presenter.PostPresenter;
import cf.bautroixa.tripgether.presenter.impl.PostPresenterImpl;
import cf.bautroixa.tripgether.ui.adapter.PostPlaceAdapter;
import cf.bautroixa.tripgether.ui.dialogs.PlaceViewDialogFragment;
import cf.bautroixa.tripgether.ui.dialogs.PostCommentsDialogFragment;
import cf.bautroixa.tripgether.utils.ui_utils.DateFormatter;
import cf.bautroixa.tripgether.utils.ui_utils.ImageHelper;

public class PostVH extends RecyclerView.ViewHolder implements PostPresenter.View {
    TextView tvName, tvTime, tvBody;
    ImageView imgMedia;
    AvatarVH avatarVH;
    Button btnLike, btnComment, btnShare;
    CardView containerMedia;
    RecyclerView rvPlaces;

    PostPresenterImpl postPresenter;
    Context context;
    FragmentManager fragmentManager;
    Post post;

    UserRepository userRepository;

    public PostVH(@NonNull View itemView, FragmentManager fragmentManager) {
        super(itemView);
        avatarVH = new AvatarVH(itemView);
        context = itemView.getContext();
        userRepository = RepositoryManager.getInstance(context).getUserRepository();

        tvName = itemView.findViewById(R.id.tv_name_item_post);
        tvTime = itemView.findViewById(R.id.tv_time_item_post);
        tvBody = itemView.findViewById(R.id.tv_body_item_post);
        containerMedia = itemView.findViewById(R.id.container_media_item_post);
        imgMedia = itemView.findViewById(R.id.img_media_item_post);
        btnLike = itemView.findViewById(R.id.btn_like_item_post);
        btnComment = itemView.findViewById(R.id.btn_comment);
        btnShare = itemView.findViewById(R.id.btn_share_item_post);
        rvPlaces = itemView.findViewById(R.id.rv_places);

        postPresenter = new PostPresenterImpl(itemView.getContext(), this);

        this.fragmentManager = fragmentManager;
    }

    public void bind(final Post post) {
        this.post = post;
        if (post.getPlaces() != null && post.getPlaces().size() > 0) {
            postPresenter.bindPlaceAdapter(post);
            rvPlaces.setVisibility(View.VISIBLE);
        } else {
            rvPlaces.setVisibility(View.GONE);
        }
        UserPublic userPublic = userRepository.get(post.getOwnerRef().getId());
        if (userPublic != null) {
            avatarVH.bind(userPublic.getAvatar(), userPublic.getShortName());
            tvName.setText(userPublic.getName());
        } else {
            // TODO: loading animation
        }

        tvTime.setText(DateFormatter.format(post.getTime()));
        tvBody.setText(post.getBody());
        List<Post.Media> medias = post.getMedias();
        if (medias != null && medias.size() > 0) {
            Post.Media media = medias.get(0);
            containerMedia.setVisibility(View.VISIBLE);
            ImageHelper.loadImage(media.getUrl(), imgMedia, 200, 200);
        } else {
            containerMedia.setVisibility(View.GONE);
        }
        bindLikeView(post.isUserLiked(), post.getLikes().size());

        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postPresenter.like(post);
            }
        });

        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostCommentsDialogFragment.newInstance(post.getId()).show(fragmentManager, "comment dialog");
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postPresenter.share(post);
            }
        });
    }

    @Override
    public void bindAdapter(PostPlaceAdapter adapter) {
        rvPlaces.setAdapter(adapter);
        rvPlaces.setLayoutManager(ChipsLayoutManager.newBuilder(context).build());
    }

    @Override
    public void showPlace(Place place) {
        PlaceViewDialogFragment.newInstance(place).show(fragmentManager, "place");
    }

    private void bindLikeView(boolean isUserLiked, int likes) {
        if (isUserLiked) {
            btnLike.setText(Html.fromHtml("<b>" + likes + "</b>"));
            btnLike.setTextColor(context.getColor(R.color.colorTextButton));
        } else {
            btnLike.setText(String.valueOf(likes));
            btnLike.setTextColor(context.getColor(R.color.colorText));
        }
    }

    @Override
    public void onSendingLike() {
        btnLike.setEnabled(false);
        btnLike.setText("");
    }

    @Override
    public void onLikeSuccess() {
        btnLike.setEnabled(true);
//        post.setUserLiked(!post.isUserLiked());
        bindLikeView(post.isUserLiked(), post.getLikes().size());
    }

    @Override
    public void onFailed(String reason) {
        btnLike.setEnabled(true);
        btnLike.setTextColor(context.getColor(R.color.colorWarning));
        Toast.makeText(itemView.getContext(), reason, Toast.LENGTH_LONG).show();
    }
}
