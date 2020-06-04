package cf.bautroixa.maptest.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.IOException;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.model.firestore.User;

public class ImageHelper {
    /**
     * https://stackoverflow.com/a/26112408/9385297
     */
    public static class CircleImageTransform implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap,
                    Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }

    public static void loadCircleImage(String imageUrl, ImageView target) {
        loadCircleImage(imageUrl, target, 50, 50, null);
    }

    public static Task<Void> loadCircleImageAsync(String imageUrl, ImageView target) {
        final TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        loadCircleImage(imageUrl, target, 50, 50, new Callback() {
            @Override
            public void onSuccess() {
                taskCompletionSource.setResult(null);
            }

            @Override
            public void onError(Exception e) {
                taskCompletionSource.setException(e);
            }
        });
        return taskCompletionSource.getTask();
    }

    public static void loadCircleImage(String imageUrl, ImageView target, int width, int height) {
        loadCircleImage(imageUrl, target, width, height, null);
    }

    public static void loadCircleImage(String imageUrl, ImageView target, int width, int height, @Nullable Callback callback) {
        if (imageUrl == null || imageUrl.length() == 0) imageUrl = User.DEFAULT_AVATAR;
        Picasso.get().load(imageUrl).resize(width, height).centerCrop().transform(new CircleImageTransform()).into(target, callback);
    }

    public static void loadCircleImage(@DrawableRes int imgResId, ImageView target, int width, int height) {
        Picasso.get().load(imgResId).resize(width, height).centerCrop().transform(new CircleImageTransform()).into(target);
    }

    public static void loadImage(String imageUrl, ImageView target, int width, int height) {
        if (imageUrl == null || imageUrl.length() == 0) imageUrl = User.DEFAULT_AVATAR;
        Picasso.get().load(imageUrl).resize(width, height).centerCrop().into(target);
    }

    public static void loadImage(String imageUrl, ImageView target, int width, int height, @Nullable Callback callback) {
        if (imageUrl == null || imageUrl.length() == 0) imageUrl = User.DEFAULT_AVATAR;
        Picasso.get().load(imageUrl).resize(width, height).centerCrop().placeholder(R.drawable.user).into(target, callback);
    }

    public static void loadUserAvatar(ImageView imgAvatar, TextView tvNameInAvatar, User user) {
        if (user.getAvatar() != null && !user.getAvatar().equals(User.DEFAULT_AVATAR)) {
            setVisibility(imgAvatar, View.VISIBLE);
            ImageHelper.loadCircleImage(user.getAvatar(), imgAvatar);
        } else {
            setVisibility(imgAvatar, View.INVISIBLE);
            tvNameInAvatar.setText(user.getShortName());
        }
    }

    public static void loadUserAvatar(ImageView imgAvatar, TextView tvNameInAvatar, ContactHelper.Contact contact) {
        if (contact.getAvatar() != null) {
            setVisibility(imgAvatar, View.VISIBLE);
            Picasso.get().load(contact.getAvatar()).resize(50, 50).centerCrop().into(imgAvatar);
        } else {
            setVisibility(imgAvatar, View.INVISIBLE);
            tvNameInAvatar.setText(contact.getShortName());
        }
    }

    public static void setVisibility(ImageView imageView, int visibility) {
        if (imageView.getVisibility() != visibility) imageView.setVisibility(visibility);
    }

    @Nullable
    public static Bitmap getLocalImageFromUri(Context context, @NonNull Uri imageUri) {
        try {
            return MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
