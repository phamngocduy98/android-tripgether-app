package cf.bautroixa.tripgether.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class FirebaseStorageHelper {
//    private static FirebaseStorageManager instance;
//    private final FirebaseStorage storage;
//
//    private FirebaseStorageManager() {
//        storage = FirebaseStorage.getInstance();
//    }
//
//    public static FirebaseStorageManager getInstance() {
//        if (instance == null) {
//            synchronized (FirebaseStorageManager.class) {
//                if (instance == null) {
//                    instance = new FirebaseStorageManager();
//                }
//            }
//        }
//        return instance;
//    }

    public static StorageReference getReference(String imageName) {
        return FirebaseStorage.getInstance().getReference("images/" + imageName);
    }

    public static UploadTask uploadImage(StorageReference storageRef, ImageView imageView) {
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        return uploadImage(storageRef, bitmap);
    }

    public static Task<Uri> uploadImageForResult(StorageReference storageRef, Bitmap bitmap) {
        return uploadImage(storageRef, bitmap).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) throw task.getException();
                return storageRef.getDownloadUrl();
            }
        });
    }

    public static UploadTask uploadImage(StorageReference storageRef, Bitmap bitmap) {
        // compress image and convert to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        // upload bytes
        return storageRef.putBytes(data);
    }

    public static Task<Uri> uploadImageForResult(StorageReference storageRef, Uri uri) {
        return uploadImage(storageRef, uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) throw task.getException();
                return storageRef.getDownloadUrl();
            }
        });
    }

    public static UploadTask uploadImage(StorageReference storageRef, Uri uri) {
        return storageRef.putFile(uri);
    }
}
