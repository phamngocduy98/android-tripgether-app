package cf.bautroixa.tripgether.ui.posts;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.presenter.post.TabExplorePresenter;
import cf.bautroixa.tripgether.presenter.post.TabExplorePresenterImpl;
import cf.bautroixa.tripgether.ui.adapter.PostAdapter;
import cf.bautroixa.tripgether.ui.adapter.viewholder.AvatarVH;
import cf.bautroixa.ui.dialogs.LoadingDialogHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class TabExploreFragment extends Fragment implements TabExplorePresenter.View {
    TabExplorePresenterImpl tabExplorePresenter;
    View viewNewPost;
    AvatarVH avatarVH;
    RecyclerView rv;
    private ProgressDialog loadingDialog;

    public TabExploreFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tab_explore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewNewPost = view.findViewById(R.id.layout_new_post);
        avatarVH = new AvatarVH(view);
        rv = view.findViewById(R.id.rv_posts);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        viewNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireContext(), PostCreatorActivity.class));
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tabExplorePresenter = new TabExplorePresenterImpl(TabExploreFragment.this, this);
        avatarVH.bind(tabExplorePresenter.getCurrentUser());
    }


    @Override
    public void onUpdating() {
        loadingDialog = LoadingDialogHelper.create(requireContext(), "Vui lòng đợi");
    }

    @Override
    public void initAdapter(PostAdapter adapter) {
        if (loadingDialog != null) loadingDialog.dismiss();
        rv.setAdapter(adapter);
    }

    @Override
    public void onFailed(String reason) {
        if (loadingDialog != null) loadingDialog.dismiss();
        Toast.makeText(requireContext(), reason, Toast.LENGTH_LONG).show();
    }
}
