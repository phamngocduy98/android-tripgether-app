package cf.bautroixa.maptest.interfaces;

import cf.bautroixa.maptest.firestore.Data;

public interface DataItemsSelectable<T extends Data> {
    void setOnDataItemSelected(OnDataItemSelected<T> onDataItemSelected);
}
