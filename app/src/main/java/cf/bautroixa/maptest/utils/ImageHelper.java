package cf.bautroixa.maptest.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.firestore.User;

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

    public static void loadCircleImage(String imageUrl, ImageView target, @Nullable Callback callback) {
        loadCircleImage(imageUrl, target, 50, 50, callback);
    }

    public static void loadCircleImage(String imageUrl, ImageView target, int width, int height) {
        loadCircleImage(imageUrl, target, width, height, null);
    }

    public static void loadCircleImage(String imageUrl, ImageView target, int width, int height, @Nullable Callback callback) {
        if (imageUrl == null || imageUrl.length() == 0) imageUrl = User.DEFAULT_AVATAR;
        Picasso.get().load(imageUrl).resize(width, height).centerCrop().transform(new CircleImageTransform()).placeholder(R.drawable.user).into(target, callback);
    }

    public static void loadImage(String imageUrl, ImageView target, int width, int height) {
        if (imageUrl == null || imageUrl.length() == 0) imageUrl = User.DEFAULT_AVATAR;
        Picasso.get().load(imageUrl).resize(width, height).centerCrop().placeholder(R.drawable.user).into(target);
    }

    public static void loadImage(String imageUrl, ImageView target, int width, int height, @Nullable Callback callback) {
        if (imageUrl == null || imageUrl.length() == 0) imageUrl = User.DEFAULT_AVATAR;
        Picasso.get().load(imageUrl).resize(width, height).centerCrop().placeholder(R.drawable.user).into(target, callback);
    }
}
