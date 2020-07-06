package cf.bautroixa.tripgether.model.ui_item;

import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

public class ToolItem implements LifecycleObserver {
    protected int id;
    @Nullable
    int activated = 0;
    @DrawableRes
    int icon;
    String text;
    @Nullable
    Integer badgeNumber;
    OnToolItemClicked onToolItemClicked;

    public ToolItem() {
    }

    public ToolItem(int id, LifecycleOwner lifecycleOwner, @DrawableRes int icon, String text, OnToolItemClicked onToolItemClicked) {
        this.id = id;
        this.activated = Activated.LOCKED;
        this.icon = icon;
        this.text = text;
        this.onToolItemClicked = onToolItemClicked;
        lifecycleOwner.getLifecycle().addObserver(this);
        onCreate(this);
    }

    public ToolItem(int id, LifecycleOwner lifecycleOwner, boolean activated, @DrawableRes int icon, String text, OnToolItemClicked onToolItemClicked) {
        this.id = id;
        this.activated = activated ? Activated.ACTIVATED : Activated.DEACTIVATED;
        this.icon = icon;
        this.text = text;
        this.onToolItemClicked = onToolItemClicked;
        lifecycleOwner.getLifecycle().addObserver(this);
    }

    public ToolItem(int id, LifecycleOwner lifecycleOwner, @DrawableRes int icon, String text, int badgeNumber, OnToolItemClicked onToolItemClicked) {
        this.id = id;
        this.activated = Activated.LOCKED;
        this.icon = icon;
        this.text = text;
        this.badgeNumber = badgeNumber;
        this.onToolItemClicked = onToolItemClicked;
    }

    protected void onCreate(ToolItem toolItem) {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    protected void onResume() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    protected void onPause() {

    }

    public int getId() {
        return id;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Nullable
    public Integer getBadgeNumber() {
        return badgeNumber;
    }

    public void setBadgeNumber(@Nullable Integer badgeNumber) {
        this.badgeNumber = badgeNumber;
    }

    public boolean isActivated() {
        return activated == Activated.ACTIVATED || activated == Activated.LOCKED_ACTIVATED;
    }

    /**
     * setActivated
     *
     * @param activated
     * @return true if activated value changed
     */
    public boolean setActivated(boolean activated) {
        if (isActivated() == activated) return false;
        this.activated = (activated ? 2 : 1) * (this.activated > 0 ? 1 : -1); // ACTIVATED = 2 or -2, DEACTIVATED = 1 or -1; negative value for locked, positive value for unlocked
        return true;
    }

    public void onClick(View v) {
        if (activated > 0)
            activated = 3 - activated; //DEACTIVATED = 1; ACTIVATED = 2; toggle activated
        onToolItemClicked.onClick(v, isActivated());
    }

    interface Activated {
        int LOCKED = -1;
        int LOCKED_ACTIVATED = -2;
        int DEACTIVATED = 1;
        int ACTIVATED = 2;
    }

    public interface OnToolItemClicked {
        void onClick(View view, boolean isActivated);
    }
}
