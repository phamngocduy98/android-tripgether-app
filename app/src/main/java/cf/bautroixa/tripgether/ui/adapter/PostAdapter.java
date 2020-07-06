package cf.bautroixa.tripgether.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.objects.Post;
import cf.bautroixa.tripgether.ui.adapter.viewholder.PostVH;

public class PostAdapter extends RecyclerView.Adapter<PostVH> {
    ArrayList<Post> posts;
    FragmentManager fragmentManager;

    public PostAdapter(ArrayList<Post> posts, FragmentManager fragmentManager) {
        this.posts = posts;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public PostVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PostVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false), fragmentManager);
    }

    @Override
    public void onBindViewHolder(@NonNull PostVH holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}
