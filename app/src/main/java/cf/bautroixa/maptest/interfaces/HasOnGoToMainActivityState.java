package cf.bautroixa.maptest.interfaces;

import cf.bautroixa.maptest.firestore.Data;

public interface HasOnGoToMainActivityState<T extends Data> {
    OnGoToMainActivityState onGoToMainActivityState = null;

    void setOnGoToMainActivityState(OnGoToMainActivityState<T> onGoToMainActivityState);
}
