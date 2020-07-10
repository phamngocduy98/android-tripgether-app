package cf.bautroixa.tripgether.utils.ui_utils;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

/**
 * https://medium.com/over-engineering/detecting-snap-changes-with-androids-recyclerview-snaphelper-9e9f5e95c424
 */
public class RecyclerViewOnScrollListener extends RecyclerView.OnScrollListener {
    private int lastPos = 0, minHeight = 0;
    private int mode;
    private SnapHelper snapHelper;
    private OnNewPosition onNewPosition;
    public interface ScrollMode {
        int SCROLLING = 0;
        int SCROLL_IDLE = 1;
    }

    public RecyclerViewOnScrollListener(int mode, SnapHelper snapHelper, OnNewPosition onNewPosition) {
        this.mode = mode;
        this.snapHelper = snapHelper;
        this.onNewPosition = onNewPosition;
    }

    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        if (mode == ScrollMode.SCROLL_IDLE && newState == RecyclerView.SCROLL_STATE_IDLE) handleNewPosition(recyclerView);
//        final int newHeight = recyclerView.getMeasuredHeight();
//        if (0 != newHeight && minHeight < newHeight) {
//            // keep track the height and prevent recycler view optimizing by resizing
//            minHeight = newHeight;
//            recyclerView.setMinimumHeight(minHeight);
//        }
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (mode == ScrollMode.SCROLLING) handleNewPosition(recyclerView);
    }

    public void handleNewPosition(@NonNull RecyclerView recyclerView){
        int newPos = getSnapPosition(recyclerView, snapHelper);
        if (newPos != lastPos){
            lastPos = newPos;
            onNewPosition.onNewPosition(newPos);
        }
    }

    public static int getSnapPosition(RecyclerView recyclerView, SnapHelper snapHelper) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager == null) return RecyclerView.NO_POSITION;
        View snapView = snapHelper.findSnapView(layoutManager);
        if (snapView == null) return RecyclerView.NO_POSITION;
        return layoutManager.getPosition(snapView);
    }

    public interface OnNewPosition {
        void onNewPosition(int position);
    }
}
