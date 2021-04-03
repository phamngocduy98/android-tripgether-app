package cf.bautroixa.tripgether.ui.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.core.DocumentsManager;
import cf.bautroixa.tripgether.model.firestore.managers.PostManager;
import cf.bautroixa.tripgether.model.firestore.objects.Comment;
import cf.bautroixa.tripgether.model.firestore.objects.Post;
import cf.bautroixa.tripgether.model.repo.RepositoryManager;
import cf.bautroixa.tripgether.model.repo.UserRepository;
import cf.bautroixa.tripgether.model.repo.objects.UserPublic;
import cf.bautroixa.tripgether.ui.adapter.PostCommentAdapter;
import cf.bautroixa.tripgether.ui.adapter.viewholder.CommentVH;
import cf.bautroixa.ui.dialogs.OneBottomSheetDialog;

public class PostCommentsDialogFragment extends OneBottomSheetDialog {
    private static final String POST_ID = "postId";
    PostCommentAdapter adapter;
    ModelManager manager;
    Post post;
    String postId;

    RecyclerView rv;
    EditText etComment;
    Button btnSendComment;
    ImageButton btnDiscardReply;
    TextView tvReplyUserName;
    View linearReply;
    DocumentReference replyCommentRef;
    private UserRepository userRepository;

    public PostCommentsDialogFragment() {

    }

    public static PostCommentsDialogFragment newInstance(String postId) {
        PostCommentsDialogFragment fragment = new PostCommentsDialogFragment();
        Bundle args = new Bundle();
        args.putString(POST_ID, postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getString(POST_ID, null);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = ModelManager.getInstance(context);
        userRepository = RepositoryManager.getInstance(requireContext()).getUserRepository();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_post_comments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rv = view.findViewById(R.id.rv_comments);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        etComment = view.findViewById(R.id.et_comment);
        btnSendComment = view.findViewById(R.id.btn_send_comment);
        tvReplyUserName = view.findViewById(R.id.tv_reply_user_name);
        linearReply = view.findViewById(R.id.linear_reply_user);
        btnDiscardReply = view.findViewById(R.id.btn_discard_reply);

        btnDiscardReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replyCommentRef = null;
                linearReply.setVisibility(View.GONE);
            }
        });

        btnSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentText = etComment.getText().toString();
                sendComment(commentText);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (postId == null) dismiss();
        manager.getBasePostsManager().requestGet(postId).addOnCompleteListener(requireActivity(), new OnCompleteListener<Post>() {
            @Override
            public void onComplete(@NonNull Task<Post> task) {
                if (task.isSuccessful()) initView(task.getResult());
                else onFailed(task.getException().getMessage());
            }
        });
    }

    private void initView(Post post) {
        this.post = post;
        post.initSubManager(manager.getCurrentUserRef(), manager.getBasePlaceManager());
        adapter = new PostCommentAdapter(post.getCommentsManager().getList(), new CommentVH.OnReplyClickedListener() {
            @Override
            public void onReply(Comment comment) {
                replyCommentRef = comment.getRef();
                linearReply.setVisibility(View.VISIBLE);
                UserPublic userPublic = userRepository.get(comment.getOwnerRef().getId());
                if (userPublic != null) {
                    tvReplyUserName.setText(userPublic.getName());
                }
            }
        });
        PostManager<Comment> commentManager = post.getCommentsManager();
        commentManager.attachAdapter(PostCommentsDialogFragment.this, adapter);
        commentManager.attachListener(PostCommentsDialogFragment.this, new DocumentsManager.OnListChangedListener<Comment>() {
            @Override
            public void onItemInserted(int position, Comment post) {
                userRepository.updateRepo(post.getOwnerRef().getId()).addOnCompleteListener(requireActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) adapter.notifyItemChanged(position);
                    }
                });
            }

            @Override
            public void onDataSetChanged(ArrayList<Comment> list) {
                ArrayList<String> ownerIds = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    ownerIds.add(list.get(i).getOwnerRef().getId());
                }
                userRepository.updateRepo(ownerIds).addOnCompleteListener(requireActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) adapter.notifyDataSetChanged();
                    }
                });
            }
        });
        rv.setAdapter(adapter);
    }

    private void sendComment(String commentText) {
        if (commentText.length() > 0) {
            post.getCommentsManager()
                    .create(new Comment(manager.getCurrentUserRef(), commentText, replyCommentRef))
                    .addOnCompleteListener(requireActivity(), new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            etComment.setText("");
                            replyCommentRef = null;
                            linearReply.setVisibility(View.GONE);
                            if (!task.isSuccessful()) {
                                onFailed(task.getException().getMessage());
                            }
                        }
                    });
        }
    }

    public void onFailed(String reason) {
        Toast.makeText(requireContext(), reason, Toast.LENGTH_LONG).show();
    }
}
