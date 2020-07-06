package cf.bautroixa.maptest.interfaces;

public interface NavigationInterface {
    void navigate(int tab, int state, Object... data);

    void navigate(int tab, int state, String className, String id);
}
