package cf.bautroixa.maptest.interfaces;

import cf.bautroixa.maptest.firestore.Data;

public interface OnNavigationToMainTab {
    void navigate(int tab, int state, Data... data);
}
