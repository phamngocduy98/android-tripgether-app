package cf.bautroixa.tripgether.presenter.impl;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.core.DocumentsManager;
import cf.bautroixa.tripgether.model.firestore.managers.PostManager;
import cf.bautroixa.tripgether.model.firestore.objects.Post;
import cf.bautroixa.tripgether.model.firestore.objects.User;
import cf.bautroixa.tripgether.model.repo.RepositoryManager;
import cf.bautroixa.tripgether.model.repo.UserRepository;
import cf.bautroixa.tripgether.presenter.TabExplorePresenter;
import cf.bautroixa.tripgether.ui.adapter.PostAdapter;

public class TabExplorePresenterImpl implements TabExplorePresenter {
    ModelManager manager;
    Fragment fragment;
    View view;

    public TabExplorePresenterImpl(Fragment fragment, View view) {
        this.fragment = fragment;
        this.view = view;
        this.manager = ModelManager.getInstance(fragment.requireContext());
        UserRepository userRepository = RepositoryManager.getInstance(fragment.requireContext()).getUserRepository();
        PostManager postManager = manager.getBasePostsManager();
        PostAdapter postAdapter = new PostAdapter(postManager.getList(), fragment.getChildFragmentManager());
        postManager.attachAdapter(fragment, postAdapter);
        postManager.attachListener(fragment, new DocumentsManager.OnListChangedListener<Post>() {
            @Override
            public void onItemInserted(int position, Post post) {
                userRepository.updateRepo(post.getOwnerRef().getId()).addOnCompleteListener(fragment.requireActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) postAdapter.notifyItemChanged(position);
                        else view.onFailed(task.getException().getMessage());
                    }
                });
            }

            @Override
            public void onDataSetChanged(ArrayList<Post> list) {
                ArrayList<String> ownerIds = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    ownerIds.add(list.get(i).getOwnerRef().getId());
                }
                userRepository.updateRepo(ownerIds).addOnCompleteListener(fragment.requireActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) postAdapter.notifyDataSetChanged();
                        else view.onFailed(task.getException().getMessage());
                    }
                });
            }
        });
        view.initAdapter(postAdapter);
    }

    @Override
    public User getCurrentUser() {
        return manager.getCurrentUser();
    }
}
