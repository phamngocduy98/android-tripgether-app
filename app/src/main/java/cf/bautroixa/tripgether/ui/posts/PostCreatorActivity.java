package cf.bautroixa.tripgether.ui.posts;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;

import cf.bautroixa.tripgether.R;
import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.model.firestore.objects.Place;
import cf.bautroixa.tripgether.presenter.CreatePostPresenter;
import cf.bautroixa.tripgether.presenter.impl.CreatePostPresenterImpl;
import cf.bautroixa.tripgether.ui.adapter.PostPlaceAdapter;
import cf.bautroixa.tripgether.ui.adapter.viewholder.AvatarVH;
import cf.bautroixa.tripgether.ui.dialogs.CheckpointEditDialogFragment;
import cf.bautroixa.tripgether.ui.dialogs.LoadingDialogHelper;
import cf.bautroixa.tripgether.ui.dialogs.OneChooseImageDialog;
import cf.bautroixa.tripgether.ui.theme.OneLiteAppbarActivity;

public class PostCreatorActivity extends OneLiteAppbarActivity implements CreatePostPresenter.View, Toolbar.OnMenuItemClickListener {
    ModelManager manager;
    Bitmap selectedImageBitmap;
    Uri selectedImageUri;
    CreatePostPresenterImpl createPostPresenter;

    AvatarVH avatarVH;
    TextView tvName;
    EditText etBody;
    ImageView imgMedia;
    View containerMedia, containerAddToPost, containerAddToPostExt;
    View addImage, addPlace, addTrip;
    RecyclerView rvPlaces;
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_creator);
        setTitle("Tạo bài viết");
        setToolbarMenu(R.menu.activity_post_creator);
        getToolbar().setOnMenuItemClickListener(this);

        manager = ModelManager.getInstance(this);

        avatarVH = new AvatarVH(findViewById(android.R.id.content).getRootView());
        avatarVH.bind(manager.getCurrentUser());
        tvName = findViewById(R.id.tv_name_item_post);
        tvName.setText(manager.getCurrentUser().getName());

        addImage = findViewById(R.id.linear_add_image_to_post);
        addPlace = findViewById(R.id.linear_add_place_to_post);
        addTrip = findViewById(R.id.linear_add_trip_to_post);

        etBody = findViewById(R.id.et_body_item_post);
        containerAddToPost = findViewById(R.id.container_add_to_post);
        containerAddToPostExt = findViewById(R.id.container_add_to_post_expanded);
        containerMedia = findViewById(R.id.container_media_item_post);
        imgMedia = findViewById(R.id.img_media_item_post);

        rvPlaces = findViewById(R.id.rv_places);
        rvPlaces.setLayoutManager(ChipsLayoutManager.newBuilder(this).setRowStrategy(ChipsLayoutManager.STRATEGY_FILL_SPACE).build());

        editBodyUI();
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OneChooseImageDialog chooseImageDialog = new OneChooseImageDialog();
                chooseImageDialog.setOnImagePickedListener(new OneChooseImageDialog.OnImagePickedListener() {
                    @Override
                    public void onPicked(@Nullable Uri uri, Bitmap bitmap) {
                        selectedImageUri = uri;
                        selectedImageBitmap = bitmap;
                        imgMedia.setImageBitmap(selectedImageBitmap);
                        containerMedia.setVisibility(View.VISIBLE);
                    }
                });
                chooseImageDialog.show(getSupportFragmentManager(), "choose_image");
            }
        });
        addPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckpointEditDialogFragment.newInstance(new CheckpointEditDialogFragment.OnCheckpointSetListener() {
                    @Override
                    public void onCheckpointSet(Checkpoint checkpoint) {
                        createPostPresenter.addPlace(new Place(checkpoint));
                        containerAddToPostExt.setVisibility(View.GONE);
                    }
                }).show(getSupportFragmentManager(), "add place");
            }
        });
        addTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPostPresenter.addTrip();
                containerAddToPostExt.setVisibility(View.GONE);
            }
        });

        createPostPresenter = new CreatePostPresenterImpl(this, this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.menu_create_post) {
            createPostPresenter.createPost(etBody.getText().toString(), selectedImageUri, selectedImageBitmap);
        }
        return true;
    }

    @Override
    public void initAdapter(PostPlaceAdapter adapter) {
        rvPlaces.setAdapter(adapter);
    }

    @Override
    public void showPlace(Place place) {
        Toast.makeText(this, place.getPlaceAddress(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLoading(String text) {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialogHelper.create(this, text);
        } else {
            loadingDialog.setMessage(text);
        }
    }

    @Override
    public void onFailed(String reason) {
        if (loadingDialog != null) loadingDialog.dismiss();
        Toast.makeText(this, reason, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSuccess() {
        if (loadingDialog != null) loadingDialog.dismiss();
        Toast.makeText(PostCreatorActivity.this, "Đã đăng!", Toast.LENGTH_LONG).show();
        finish();
    }

    private void editBodyUI() {
        etBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 85) {
                    if (etBody.getTextSize() != 20) {
                        etBody.setTextSize(20);
                        etBody.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
                    }
                } else {
                    if (etBody.getTextSize() != 14) {
                        etBody.setTextSize(14);
                        etBody.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        etBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                containerAddToPostExt.setVisibility(View.GONE);
            }
        });
        containerAddToPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                containerAddToPostExt.setVisibility(View.VISIBLE); //containerAddToPostExt.getVisibility() == View.VISIBLE ? View.GONE :
            }
        });
    }
}
