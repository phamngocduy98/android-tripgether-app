package cf.bautroixa.maptest.ui.sortedlist;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import java.util.Objects;

import cf.bautroixa.maptest.model.firestore.objects.SosRequest;
import cf.bautroixa.maptest.model.firestore.objects.User;

public class UserSortedListAdapterCallback extends SortedListAdapterCallback<User> {
    public UserSortedListAdapterCallback(RecyclerView.Adapter adapter) {
        super(adapter);
    }

//    @Override
//    public int compare(User o1, User o2) {
//        SosRequest sos1 = o1.getSosRequest(), sos2 = o2.getSosRequest();
//        if (sos1.isResolved() ^ sos2.isResolved()) {
//            return sos1.isResolved() ? -1 : 1;
//        }
//        if (sos1.getLever() != sos2.getLever()) {
//            return sos1.getLever() < sos2.getLever() ? -1 : 1;
//        }
//        return sos1.getTime().compareTo(sos1.getTime());
//    }

    @Override
    public int compare(User o1, User o2) {
        if (Objects.equals(o1.getId(), o2.getId())) return 0;
        SosRequest sos1 = o1.getSosRequest(), sos2 = o2.getSosRequest();
        if ((sos1 != null) ^ (sos2 != null)) {
            return sos2 != null ? -1 : 1;
        }
        if (sos1 == null) return 0;
        if (sos1.isResolved() ^ sos2.isResolved()) {
            return sos1.isResolved() ? -1 : 1;
        }
        if (sos1.getLever() != sos2.getLever()) {
            return sos1.getLever() < sos2.getLever() ? -1 : 1;
        }
        return sos1.getTime().compareTo(sos1.getTime());
    }

    @Override
    public boolean areContentsTheSame(User oldItem, User newItem) {
        return oldItem.equals(newItem);
    }

    @Override
    public boolean areItemsTheSame(User item1, User item2) {
        return item1.getId().equals(item2.getId());
    }
}
