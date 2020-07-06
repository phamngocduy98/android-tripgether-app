package cf.bautroixa.tripgether.interfaces;

import cf.bautroixa.tripgether.model.firestore.core.Document;

public interface ActivityNavigationInterface {
    void navigate(int tab, int state, Document data);

    void navigate(int tab, int state, String klassName, String documentId);
}