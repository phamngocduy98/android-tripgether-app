package cf.bautroixa.maptest.interfaces;

import cf.bautroixa.maptest.firestore.Data;

public interface NavigableToState<T extends Data> {
    void setOnNavigationToState(OnNavigationToState<T> onNavigationToState);
}
