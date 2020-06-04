package cf.bautroixa.maptest.ui.theme;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import cf.bautroixa.maptest.R;

public abstract class OneRecyclerView extends RecyclerView {
    public static final int TYPE_TOP = 0;
    public static final int TYPE_FULL = 1;
    public static final int TYPE_NORMAL = 2;
    public static final int TYPE_BOT = 3;

    public OneRecyclerView(@NonNull Context context) {
        super(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            switch (viewType) {
                case TYPE_TOP:
                    itemView.setBackgroundResource(R.drawable.bg_radius_top_white);
                    break;
                case TYPE_NORMAL:
                    itemView.setBackgroundResource(R.drawable.bg_radius_none_white);
                    break;
                case TYPE_BOT:
                    itemView.setBackgroundResource(R.drawable.bg_radius_bot_white);
                    break;
                default:
                    itemView.setBackgroundResource(R.drawable.bg_radius_full_white_with_border);
            }
        }
    }

    public static abstract class Adapter<T extends OneRecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {
        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                if (getItemCount() > 1) return TYPE_TOP;
                else return TYPE_FULL;
            } else if (position == getItemCount() - 1) {
                return TYPE_BOT;
            }
            return TYPE_NORMAL;
        }
    }
}
