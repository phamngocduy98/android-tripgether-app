package cf.bautroixa.tripgether.presenter.post;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.objects.Checkpoint;
import cf.bautroixa.tripgether.model.firestore.objects.Place;
import cf.bautroixa.tripgether.model.firestore.objects.Post;
import cf.bautroixa.tripgether.ui.adapter.PostPlaceAdapter;
import cf.bautroixa.tripgether.ui.adapter.viewholder.PlaceChipVH;
import cf.bautroixa.tripgether.utils.FirebaseStorageHelper;
import cf.bautroixa.tripgether.utils.TaskHelper;

public class CreatePostPresenterImpl implements CreatePostPresenter {
    Activity activity;
    View view;
    ModelManager manager;
    ArrayList<Place> places;
    DocumentReference selectedTripRef;
    PostPlaceAdapter placeAdapter;

    public CreatePostPresenterImpl(Activity activity, View view) {
        this.activity = activity;
        this.view = view;
        this.manager = ModelManager.getInstance(activity);
        places = new ArrayList<>();
        placeAdapter = new PostPlaceAdapter(places, new PlaceChipVH.OnChipClickedListener() {
            @Override
            public void onClick(Place place) {
                view.showPlace(place);
            }

            @Override
            public void onRemove(int position, Place place) {
                places.remove(position);
                placeAdapter.notifyItemRemoved(position);
            }
        });
        view.initAdapter(placeAdapter);
    }

    @Override
    public void createPost(String body, Uri selectedImageUri, Bitmap selectedImageBitmap) {
        Task<List<DocumentReference>> createPlaceTask;
        Task<Uri> uploadImageTask;
        view.onLoading("Đang tạo bài viết...");
        if (places.size() > 0) {
            createPlaceTask = createPlaces();
        } else {
            createPlaceTask = TaskHelper.getCompletedTask(null);
        }
        if (selectedImageBitmap != null) {
            uploadImageTask = uploadMedia(selectedImageUri, selectedImageBitmap);
        } else {
            uploadImageTask = TaskHelper.getCompletedTask(null);
        }

        Tasks.whenAllSuccess(createPlaceTask, uploadImageTask).continueWithTask(new Continuation<List<Object>, Task<DocumentReference>>() {
            @Override
            public Task<DocumentReference> then(@NonNull Task<List<Object>> task) throws Exception {
                if (task.isSuccessful()) {
                    List<Object> result = task.getResult();
                    List<DocumentReference> placeRefs = (List<DocumentReference>) result.get(0);
                    Uri imageUri = (Uri) result.get(1);
                    if (imageUri != null) {
                        Post.Media media = new Post.Media(Post.Media.Type.IMAGE, imageUri.toString());
                        return manager.getBasePostsManager().create(new Post(manager.getCurrentUserRef(), body, Collections.singletonList(media), selectedTripRef, placeRefs));
                    }
                    return manager.getBasePostsManager().create(new Post(manager.getCurrentUserRef(), body, null, selectedTripRef, placeRefs));
                }
                throw task.getException();
            }
        }).addOnCompleteListener(activity, new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    view.onSuccess();
                } else {
                    view.onFailed(task.getException().getMessage());
                }
            }
        });
    }

    @Override
    public void addPlace(Place place) {
        places.add(place);
        placeAdapter.notifyItemInserted(places.size() - 1);
    }

    @Override
    public void removePlace(Place place) {
        int index = places.indexOf(place);
        if (index >= 0) {
            places.remove(index);
            placeAdapter.notifyItemRemoved(index);
        }
    }

    @Override
    public void addTrip() {
        selectedTripRef = manager.getCurrentUser().getActiveTripRef();
        ArrayList<Checkpoint> checkpoints = manager.getCurrentTrip().getCheckpointsManager().getList();
        for (int i = 0; i < checkpoints.size(); i++) {
            addPlace(new Place(manager.getBasePlaceManager().getRef(), checkpoints.get(i)));
        }
    }

    private Task<Uri> uploadMedia(Uri selectedImageUri, Bitmap selectedImageBitmap) {
        String imageName = UUID.randomUUID().toString();
        StorageReference storageRef = FirebaseStorageHelper.getReference("post/" + imageName);
        UploadTask uploadTask;
        if (selectedImageUri != null) {
            uploadTask = FirebaseStorageHelper.uploadImage(storageRef, selectedImageUri);
        } else {
            uploadTask = FirebaseStorageHelper.uploadImage(storageRef, selectedImageBitmap);
        }
        return uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) throw task.getException();
                return storageRef.getDownloadUrl();
            }
        });
    }

    private Task<List<DocumentReference>> createPlaces() {
        List<Task<DocumentReference>> tasks = new ArrayList<>();
        for (int i = 0; i < places.size(); i++) {
            tasks.add(manager.getBasePlaceManager().getOrCreatePlace(places.get(i)));
        }
        return Tasks.whenAllSuccess(tasks);
    }
}
