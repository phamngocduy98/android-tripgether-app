package cf.bautroixa.tripgether.presenter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import cf.bautroixa.tripgether.model.firestore.ModelManager;
import cf.bautroixa.tripgether.model.firestore.objects.SosRequest;
import cf.bautroixa.tripgether.presenter.SosPresenter;
import cf.bautroixa.tripgether.utils.FirebaseStorageHelper;

public class SosPresenterImpl implements SosPresenter {
    View view;
    Activity activity;
    private ModelManager manager;

    public SosPresenterImpl(View view, Activity activity) {
        this.manager = ModelManager.getInstance(activity);
        this.view = view;
        this.activity = activity;
        SosRequest sosRequest = manager.getCurrentUser().getSosRequest();
        view.updateView(sosRequest);
        if (sosRequest == null || (sosRequest.isResolved() || sosRequest.getImageUrl().length() == 0)) {
            view.staticMap(manager.getCurrentUser().getCurrentCoord());
        }
    }

    @Override
    public Task<Uri> uploadImage(@Nullable Uri uri, Bitmap bitmap) {
        StorageReference storageRef = FirebaseStorageHelper.getReference("sos/" + manager.getCurrentUser().getId());
        UploadTask uploadTask;
        if (uri != null) {
            uploadTask = FirebaseStorageHelper.uploadImage(storageRef, uri);
        } else {
            uploadTask = FirebaseStorageHelper.uploadImage(storageRef, bitmap);
        }
        return uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) throw task.getException();
                return storageRef.getDownloadUrl();
            }
        });
    }

    @Override
    public void resolveSos() {
        view.onSending("Đang cập nhật...");
        manager.getCurrentUser().sendUpdate(null, SosRequest.RESOLVED, true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    view.onSuccess();
                } else {
                    view.onFailed(task.getException().getMessage());
                }
            }
        });
    }

    @Override
    public void updateSos(boolean createNew, int lever, String desc, Uri imageUri, Bitmap imageBitmap) {
        Task<Void> updateTask;
        if (imageUri != null || imageBitmap != null) {
            view.onSending("Đang tải lên...");
            updateTask = uploadImage(imageUri, imageBitmap).continueWithTask(new Continuation<Uri, Task<Void>>() {
                @Override
                public Task<Void> then(@NonNull Task<Uri> task) throws Exception {
                    if (!task.isSuccessful()) throw task.getException();
                    view.onSending("Đang cập nhật...");
                    return manager.sendSosRequest(activity, new SosRequest(lever, desc, task.getResult().toString(), false));
                }
            });
        } else {
            view.onSending("Đang cập nhật...");
            SosRequest oldSosReq = manager.getCurrentUser().getSosRequest();
            if (!createNew && oldSosReq != null) {
                updateTask = manager.sendSosRequest(activity, new SosRequest(lever, desc, oldSosReq.getImageUrl(), false));
            } else {
                updateTask = manager.sendSosRequest(activity, new SosRequest(lever, desc, "", false));
            }
        }
        updateTask.addOnCompleteListener(activity, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    view.onSuccess();
                } else {
                    view.onFailed(task.getException().getMessage());
                }
            }
        });
    }
}
