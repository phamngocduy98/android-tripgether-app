package cf.bautroixa.tripgether.ui.sortedlist;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import java.util.Objects;

import cf.bautroixa.tripgether.model.firestore.objects.Message;

public class MessageSortedListAdapterCallback extends SortedListAdapterCallback<Message> {
    public MessageSortedListAdapterCallback(RecyclerView.Adapter adapter) {
        super(adapter);
    }

    @Override
    public int compare(Message o1, Message o2) {
        if (Objects.equals(o1.getId(), o2.getId())) return 0;
        if ((o1.getTime() == null) ^ (o2.getTime() == null)) {
            return o1.getTime() == null ? -1 : 1;
        }
        if ((o1.getTime() == null) && (o2.getTime() == null)) return 0;
        return -o1.getTime().compareTo(o2.getTime());
    }

    @Override
    public boolean areContentsTheSame(Message oldItem, Message newItem) {
        boolean val = Objects.equals(oldItem.getFromUser(), newItem.getFromUser()) && Objects.equals(oldItem.getText(), newItem.getText()) && Objects.equals(oldItem.getTime(), newItem.getTime());
        return val;
    }

    @Override
    public boolean areItemsTheSame(Message item1, Message item2) {
        boolean val = Objects.equals(item1.getId(), item2.getId());
        return val;
    }
}
