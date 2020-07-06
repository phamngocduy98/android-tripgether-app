package cf.bautroixa.tripgether.interfaces;

public interface OnAppbarStateChanged {
    void newState(int state);

    interface State {
        int COLLAPSED = 0;
        int EXTENDED = 1;
    }
}
