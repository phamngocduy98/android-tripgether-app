package cf.bautroixa.maptest.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.objects.Comment;
import cf.bautroixa.maptest.ui.adapter.viewholder.CommentVH;

public class PostCommentAdapter extends RecyclerView.Adapter<CommentVH> {
    ArrayList<Comment> comments;
    CommentVH.OnReplyClickedListener onReplyClickedListener;

    public PostCommentAdapter(ArrayList<Comment> comments, CommentVH.OnReplyClickedListener onReplyClickedListener) {
        this.comments = comments;
        this.onReplyClickedListener = onReplyClickedListener;
    }

    @NonNull
    @Override
    public CommentVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CommentVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CommentVH holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment, onReplyClickedListener);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }
}
