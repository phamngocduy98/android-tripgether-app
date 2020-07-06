package cf.bautroixa.maptest.ui.sortedlist;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import java.util.Objects;

import cf.bautroixa.maptest.model.firestore.objects.Checkpoint;

public class CheckpointSortedListAdapterCallback extends SortedListAdapterCallback<Checkpoint> {
    public CheckpointSortedListAdapterCallback(RecyclerView.Adapter adapter) {
        super(adapter);
    }

    @Override
    public int compare(Checkpoint o1, Checkpoint o2) {
        if (Objects.equals(o1.getId(), o2.getId())) return 0;
        return o1.getTime().compareTo(o2.getTime());
    }

    @Override
    public boolean areContentsTheSame(Checkpoint oldItem, Checkpoint newItem) {
        return oldItem.equals(newItem);
    }

    @Override
    public boolean areItemsTheSame(Checkpoint item1, Checkpoint item2) {
        return item1.getId().equals(item2.getId());
    }
}
