package cf.bautroixa.tripgether.ui.theme;

import android.content.Context;
import android.view.View;
import android.widget.Button;

public class ViewAnim {
    public static final int HIDE_DIRECTION_UP = 1;
    public static final int HIDE_DIRECTION_RIGHT = 2;
    public static final int HIDE_DIRECTION_DOWN = 3;
    public static final int HIDE_DIRECTION_LEFT = 4;

    public static void toggleHideShow(View view, boolean isShown, int hideDirection) {
        if (isShown) {
            view.animate().translationY(0).translationX(0).alpha(1);
        } else {
            view.animate().alpha(0)
                    .translationY(view.getHeight() * (hideDirection == HIDE_DIRECTION_UP ? -1 : (hideDirection == HIDE_DIRECTION_DOWN ? 1 : 0)))
                    .translationX(view.getWidth() * (hideDirection == HIDE_DIRECTION_LEFT ? -1 : (hideDirection == HIDE_DIRECTION_RIGHT ? 1 : 0)));
        }
    }

    public static void toggleHideShow(View view, boolean isShown, int hideDirection, Runnable onComplete) {
        if (isShown) {
            view.animate().translationY(0).translationX(0).alpha(1);
        } else {
            view.animate().alpha(0)
                    .translationY(view.getHeight() * (hideDirection == HIDE_DIRECTION_UP ? -1 : (hideDirection == HIDE_DIRECTION_DOWN ? 1 : 0)))
                    .translationX(view.getWidth() * (hideDirection == HIDE_DIRECTION_LEFT ? -1 : (hideDirection == HIDE_DIRECTION_RIGHT ? 1 : 0)))
                    .withEndAction(onComplete);
        }
    }

    public static void toggleLoading(Context context, Button button, boolean isLoading, String buttonText) {
        button.setEnabled(!isLoading);
        button.setText(isLoading ? "Đang gửi" : buttonText);
    }
}
