package cf.bautroixa.maptest.interfaces;

import cf.bautroixa.maptest.firestore.Data;

public interface OnDataItemSelected<T extends Data> {
    void selectItem(T data);
}
