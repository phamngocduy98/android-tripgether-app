package cf.bautroixa.tripgether.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import cf.bautroixa.tripgether.interfaces.NavigationInterface;
import cf.bautroixa.tripgether.model.firestore.core.Document;
import cf.bautroixa.tripgether.ui.MainActivity;

public class NavigableHelper {
    public static final String ARG_TAB = "tab";
    public static final String ARG_STATE = "state";
    public static final String ARG_DATA_CLASS_NAME = "data_class";
    public static final String ARG_DATA_ID = "data_id";

    public static void navigate(Context context, int tab, int state, Document data) {
        navigate(context, tab, state, data.getClass().getSimpleName(), data.getId());
    }

    public static void navigate(Context context, int tab, int state, String className, String id) {
        Intent intent = new Intent(context, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(ARG_TAB, tab);
        intent.putExtra(ARG_STATE, state);
        intent.putExtra(ARG_DATA_CLASS_NAME, className);
        intent.putExtra(ARG_DATA_ID, id);
        context.startActivity(intent);
    }

    public static Intent getNavigableResultIntent(int tab, int state, Document data) {
        Intent intent = new Intent();
        intent.putExtra(ARG_TAB, tab);
        intent.putExtra(ARG_STATE, state);
        intent.putExtra(ARG_DATA_CLASS_NAME, data.getClass().getSimpleName());
        intent.putExtra(ARG_DATA_ID, data.getId());
        return intent;
    }

    public static Intent getNavigableResultIntent(int tab, int state, String klassName, String documentId) {
        Intent intent = new Intent();
        intent.putExtra(ARG_TAB, tab);
        intent.putExtra(ARG_STATE, state);
        intent.putExtra(ARG_DATA_CLASS_NAME, klassName);
        intent.putExtra(ARG_DATA_ID, documentId);
        return intent;
    }

    public static void handleNavigation(Intent intent, NavigationInterface navigationInterface) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            int tab = extras.getInt(ARG_TAB, -999);
            if (tab == -999) return; // not a navigation intent, just ignore
            int state = extras.getInt(ARG_STATE);
            String dataClassName = extras.getString(ARG_DATA_CLASS_NAME, "");
            String dataId = extras.getString(ARG_DATA_ID, "");
            navigationInterface.navigate(tab, state, dataClassName, dataId);
        }
    }
}
