package cf.bautroixa.maptest.ui.map.bottomsheet;

import android.view.View;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import cf.bautroixa.maptest.R;

public class SpaceManager {
    public static final int SPACE_NONE = -1;
    public static final int SPACE_BOTTOM = 0;
    public static final int SPACE_BOTTOM_SHEET = 1;

    private FragmentManager fragmentManager;
    private View[] spaces = new View[2];
    @IdRes
    private int[] spaceIds;
    private Fragment[] lastFragments = new Fragment[2];

    public SpaceManager(FragmentManager fragmentManager, View parentView) {
        this.fragmentManager = fragmentManager;
        this.spaceIds = new int[]{R.id.bottom_space, R.id.bottom_sheet};
        this.setViewSpace(parentView);
    }

    public void setViewSpace(View parentView) {
        for (int i = 0; i < spaceIds.length; i++) {
            this.spaces[i] = parentView.findViewById(this.spaceIds[i]);
        }
    }

    public void selectActiveViewSpace(int viewSpace, Fragment fragment) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
        for (int i = 0; i < spaces.length; i++) {
//            ViewAnim.toggleHideShow(spaces[i], viewSpace == i, ViewAnim.HIDE_DIRECTION_DOWN);
            if (viewSpace == i) {
                ft.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
                        .replace(spaceIds[i], fragment);
                lastFragments[i] = fragment;
            } else {
                if (lastFragments[i] != null) {
                    ft.remove(lastFragments[i]);
                }
            }
        }
        ft.commit();
    }
}
