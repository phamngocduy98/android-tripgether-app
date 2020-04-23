package cf.bautroixa.maptest.interfaces;

import cf.bautroixa.maptest.firestore.Data;

public interface OnGoToMainActivityState<T extends Data> {
    void newState(int state, T... data);
}
