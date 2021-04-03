package cf.bautroixa.tripgether.ui.adapter.viewholder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.objects.Comment;
import cf.bautroixa.tripgether.model.firestore.objects.Place;
import cf.bautroixa.tripgether.model.repo.RepositoryManager;
import cf.bautroixa.tripgether.model.repo.UserRepository;
import cf.bautroixa.tripgether.model.repo.objects.UserPublic;
import cf.bautroixa.tripgether.presenter.post.PostPresenter;
import cf.bautroixa.tripgether.presenter.post.PostPresenterImpl;
import cf.bautroixa.tripgether.ui.adapter.PostPlaceAdapter;
import cf.bautroixa.tripgether.utils.ui_utils.DateFormatter;
import cf.bautroixa.ui.RoundedImageView;
import cf.bautroixa.ui.helpers.ImageHelper;

/**
 * layout xml : R.layout.item_comment
 */
public class CommentVH extends RecyclerView.ViewHolder implements PostPresenter.View {
    TextView tvName, tvContent, tvTime, btnLike, btnReply, tvReplyTo;
    RoundedImageView imgAvatar;

    Context context;
    PostPresenterImpl postPresenter;
    UserRepository userRepository;

    public CommentVH(@NonNull View itemView) {
        super(itemView);
        context = itemView.getContext();
        postPresenter = new PostPresenterImpl(context, this);
        userRepository = RepositoryManager.getInstance(itemView.getContext()).getUserRepository();
        imgAvatar = itemView.findViewById(R.id.img_avatar_item_comment);
        tvName = itemView.findViewById(R.id.tv_name_item_comment);
        tvContent = itemView.findViewById(R.id.tv_content_item_comment);
        tvTime = itemView.findViewById(R.id.tv_time_item_comment);

        btnLike = itemView.findViewById(R.id.tv_btn_like_item_comment);
        btnReply = itemView.findViewById(R.id.tv_btn_comment_item_comment);

        tvReplyTo = itemView.findViewById(R.id.tv_reply_to_user_item_comment);
    }

    public void bind(Comment comment, OnReplyClickedListener onReplyClickedListener) {
        UserPublic owner = userRepository.get(comment.getOwnerRef().getId());
        if (owner != null) {
            ImageHelper.loadCircleImage(owner.getAvatar(), imgAvatar);
            tvName.setText(owner.getName());
        }

        if (comment.getReplyComment() != null) {
            tvReplyTo.setVisibility(View.VISIBLE);
            UserPublic replyToUser = userRepository.get(comment.getReplyComment().getOwnerRef().getId());
            if (replyToUser != null) {
                tvReplyTo.setText(String.format("đang trả lời %s", replyToUser.getName()));
            } else {
                tvReplyTo.setText("đang trả lời");
            }

        } else {
            tvReplyTo.setVisibility(View.GONE);
        }

        tvContent.setText(comment.getBody());
        if (comment.getTime() != null) {
            tvTime.setText(DateFormatter.format(comment.getTime()));
        } else {
            tvTime.setText("Vừa xong");
        }
        if (comment.isUserLiked()) {
            btnLike.setText(String.format("Đã thích %d", comment.getLikes().size()));
            btnLike.setTextColor(context.getColor(R.color.colorTextButton));
        } else {
            btnLike.setText(String.format("Thích %d", comment.getLikes().size()));
            btnLike.setTextColor(context.getColor(R.color.colorText));
        }

        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postPresenter.likeComment(comment);
            }
        });

        btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReplyClickedListener.onReply(comment);
            }
        });
    }

    @Override
    public void bindAdapter(PostPlaceAdapter adapter) {

    }

    @Override
    public void showPlace(Place place) {

    }

    @Override
    public void onSendingLike() {

    }

    @Override
    public void onLikeSuccess() {

    }

    @Override
    public void onFailed(String reason) {

    }

    public interface OnReplyClickedListener {
        void onReply(Comment comment);
    }
}
