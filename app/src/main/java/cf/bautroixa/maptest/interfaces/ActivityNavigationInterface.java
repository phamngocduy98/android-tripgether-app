package cf.bautroixa.maptest.interfaces;

import cf.bautroixa.maptest.model.firestore.core.Document;

public interface ActivityNavigationInterface {
    void navigate(int tab, int state, Document data);

    void navigate(int tab, int state, String klassName, String documentId);
}