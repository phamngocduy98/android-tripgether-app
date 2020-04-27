package cf.bautroixa.maptest.utils;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;

import java.util.List;

public class NoFilterArrayAdapter extends ArrayAdapter {
    public NoFilterArrayAdapter(@NonNull Context context, int resource, @NonNull List objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new NoFilter();
    }

    private static class NoFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence arg0) {
            return null;
        }

        @Override
        protected void publishResults(CharSequence arg0, FilterResults arg1) {
        }
    }
}
