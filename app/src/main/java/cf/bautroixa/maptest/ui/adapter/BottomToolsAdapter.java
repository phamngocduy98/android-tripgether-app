package cf.bautroixa.maptest.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Arrays;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.interfaces.NavigationInterfaces;
import cf.bautroixa.maptest.model.firestore.Checkpoint;
import cf.bautroixa.maptest.model.firestore.Document;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.Trip;
import cf.bautroixa.maptest.model.firestore.User;
import cf.bautroixa.maptest.model.ui_item.StateToolItem;
import cf.bautroixa.maptest.model.ui_item.ToolItem;
import cf.bautroixa.maptest.ui.dialogs.SosRequestEditDialogFragment;
import cf.bautroixa.maptest.ui.map.TabMapFragment;
import cf.bautroixa.maptest.ui.notifications.NotificationActivity;
import cf.bautroixa.maptest.ui.settings.SettingActivity;
import cf.bautroixa.maptest.utils.AlarmHelper;
import cf.bautroixa.maptest.utils.DarkModeHelper;

public class BottomToolsAdapter extends RecyclerView.Adapter<BottomToolsAdapter.ToolVH> {
    ArrayList<ToolItem> tools;
    Context context;
    ModelManager manager;

    public BottomToolsAdapter(final Context context, final FragmentManager fragmentManager, final LifecycleOwner lifecycleOwner, final NavigationInterfaces navigationInterfaces) {
        this.setHasStableIds(true);
        this.manager = ModelManager.getInstance();
        this.context = context;
        tools = new ArrayList<>();
        final SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_preference_name), Context.MODE_PRIVATE);
        // 0
        tools.add(new ToolItem(0, lifecycleOwner, R.drawable.ic_people_white_24dp, "Thành viên", new ToolItem.OnToolItemClicked() {
            @Override
            public void onClick(View v, boolean isActivated) {
                navigationInterfaces.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_FRIEND_LIST_EXPANDED);
            }
        }));
        // 1
        tools.add(new ToolItem(1, lifecycleOwner, R.drawable.ic_place_black_24dp, "Địa điểm", new ToolItem.OnToolItemClicked() {
            @Override
            public void onClick(View v, boolean isActivated) {
                if (isActivated) {
                    manager.getCurrentTrip().getActiveCheckpoint().addOnCompleteListener(new OnCompleteListener<Checkpoint>() {
                        @Override
                        public void onComplete(@NonNull Task<Checkpoint> task) {
                            navigationInterfaces.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_CHECKPOINT, task.getResult());
                        }
                    });
                } else {
                    navigationInterfaces.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_CHECKPOINT_LIST_EXPANDED);
                }

            }
        }) {
            @Override
            protected void onCreate() {
                manager.getCurrentTrip().attachListener(lifecycleOwner, new Document.OnValueChangedListener<Trip>() {
                    @Override
                    public void onValueChanged(@NonNull Trip currentTrip) {
                        boolean hasActiveCheckpoint = currentTrip.isAvailable() && currentTrip.getActiveCheckpointRef() != null;
                        if (setActivated(hasActiveCheckpoint)) {
                            if (hasActiveCheckpoint) setText("Tập hợp");
                            else setText("Địa điểm");
                            notifyItemChanged(1);
                        }
                    }
                });
            }
        });
        // 2
        tools.add(new ToolItem(2, lifecycleOwner, R.drawable.ic_help, "Gửi hỗ trợ", new ToolItem.OnToolItemClicked() {
            @Override
            public void onClick(View v, boolean isActivated) {
                new SosRequestEditDialogFragment().show(fragmentManager, "add edit sos");
            }
        }) {
            @Override
            protected void onCreate() {
                manager.getCurrentUser().attachListener(lifecycleOwner, new Document.OnValueChangedListener<User>() {
                    @Override
                    public void onValueChanged(@NonNull User currentUser) {
                        boolean hasSosRequest = currentUser.isAvailable() && currentUser.getSosRequest() != null && !currentUser.getSosRequest().isResolved();
                        setActivated(hasSosRequest);
                        // TODO : fix update bug and optimze number of update
                        notifyItemChanged(2);
                    }
                });
            }
        });
        final boolean isLocationServiceOn = AlarmHelper.isOn(context, sharedPref);
        tools.add(new ToolItem(3, lifecycleOwner, R.drawable.ic_notifications_black_24dp, "Thông báo", new ToolItem.OnToolItemClicked() {
            @Override
            public void onClick(View v, boolean isActivated) {
                context.startActivity(new Intent(context, NotificationActivity.class));
            }
        }));
        tools.add(new ToolItem(4, lifecycleOwner, R.drawable.ic_weather_sun, "Thời tiết", new ToolItem.OnToolItemClicked() {
            @Override
            public void onClick(View v, boolean isActivated) {
                navigationInterfaces.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_WEATHER);
            }
        }));
        tools.add(new ToolItem(5, lifecycleOwner, isLocationServiceOn, R.drawable.ic_my_location_black_24dp, "Bật chia sẻ", new ToolItem.OnToolItemClicked() {
            @Override
            public void onClick(View v, boolean isActivated) {
                if (!isActivated) AlarmHelper.turnOff(context, sharedPref);
                else AlarmHelper.turnOn(context, sharedPref);
            }
        }));
        tools.add(new StateToolItem(6,
                lifecycleOwner,
                Arrays.asList(context.getResources().getStringArray(R.array.setting_dark_mode)),
                context.getResources().obtainTypedArray(R.array.dark_mode_drawables),
                new StateToolItem.OnStateToolItemClickedListener() {
                    @Override
                    public void onClick(View v, int mode) {
                        DarkModeHelper.applyMode(context, sharedPref, mode);
                    }
                },
                DarkModeHelper.getCurrentMode(context),
                new StateToolItem.OnChangeStateListener() {
                    @Override
                    public int newState(int oldState) {
                        return (oldState + 1) % DarkModeHelper.nightModes.size();
                    }
                }
        ));
        tools.add(new ToolItem(7, lifecycleOwner, R.drawable.ic_settings_black_24dp, "Cài đặt", new ToolItem.OnToolItemClicked() {
            @Override
            public void onClick(View v, boolean isActivated) {
                context.startActivity(new Intent(context, SettingActivity.class));
            }
        }));
    }

    @NonNull
    @Override
    public ToolVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ToolVH(context, LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tool, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ToolVH holder, final int position) {
        final ToolItem toolItem = tools.get(position);
        holder.bind(toolItem);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolItem.onClick(v);
                // TODO : fix update bug and optimze number of update
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tools.size();
    }

    @Override
    public long getItemId(int position) {
        return tools.get(position).getId();
    }

    class ToolVH extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView tvTitle;
        Context context;

        public ToolVH(Context context, @NonNull View itemView) {
            super(itemView);
            this.context = context;
            icon = itemView.findViewById(R.id.ic_item_tool);
            tvTitle = itemView.findViewById(R.id.tv_title_item_tool);
        }

        void bind(ToolItem toolItem) {
            boolean isActivated = toolItem.isActivated();
            icon.setActivated(isActivated);
            icon.setImageResource(toolItem.getIcon());
            tvTitle.setText(toolItem.getText());
        }
    }
}
