package cf.bautroixa.maptest.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Binder;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;

import cf.bautroixa.maptest.R;

interface ILayoutEditor {
    void edit(View view);
}

public class CreateMarker {
    public static Bitmap createBitmapFromLayout(Context context, @LayoutRes int layoutResId, ILayoutEditor editor){
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(layoutResId, null);
        editor.edit(view);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    public static Bitmap createMarker(Context context, @LayoutRes int layoutResId, @DrawableRes final int avatarRes) {
        return createBitmapFromLayout(context, layoutResId, new ILayoutEditor() {
            @Override
            public void edit(View view) {
                ImageView markerImage = (ImageView) view.findViewById(R.id.img_marker_avatar);
                markerImage.setImageResource(avatarRes);
            }
        });
    }

    public static Bitmap createMarker(Context context, @DrawableRes final int iconRes, int width, int height){
        BitmapDrawable bitmapdraw = (BitmapDrawable)context.getResources().getDrawable(iconRes);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        return smallMarker;
    }
}
