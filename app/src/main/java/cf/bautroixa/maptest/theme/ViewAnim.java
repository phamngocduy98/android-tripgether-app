package cf.bautroixa.maptest.theme;

import android.view.View;
import android.widget.Button;

public class ViewAnim {
    public static final int DIRECTION_UP = 1;
    public static final int DIRECTION_RIGHT = 2;
    public static final int DIRECTION_DOWN = 3;
    public static final int DIRECTION_LEFT = 4;

    public static void toggleHideShow(View view, boolean isShown, int direction) {
        if (isShown) {
            view.animate().translationY(0).translationX(0).alpha(1);
        } else {
            view.animate().alpha(0)
                    .translationY(view.getHeight() * (direction == DIRECTION_UP ? -1 : (direction == DIRECTION_DOWN ? 1 : 0)))
                    .translationX(view.getWidth() * (direction == DIRECTION_LEFT ? -1 : (direction == DIRECTION_RIGHT ? 1 : 0)));
        }
    }

    public static void toggleLoading(Button button, boolean isLoading, String buttonText) {
        button.setEnabled(!isLoading);
        button.setText(isLoading ? "Loading..." : buttonText);
    }
}
