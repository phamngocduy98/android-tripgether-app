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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Arrays;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.interfaces.NavigationInterface;
import cf.bautroixa.maptest.model.constant.RequestCodes;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.firestore.core.Document;
import cf.bautroixa.maptest.model.firestore.managers.NotificationsManager;
import cf.bautroixa.maptest.model.firestore.objects.Checkpoint;
import cf.bautroixa.maptest.model.firestore.objects.Trip;
import cf.bautroixa.maptest.model.firestore.objects.User;
import cf.bautroixa.maptest.model.sharedpref.SPDarkMode;
import cf.bautroixa.maptest.model.ui_item.StateToolItem;
import cf.bautroixa.maptest.model.ui_item.ToolItem;
import cf.bautroixa.maptest.ui.adapter.pager_adapter.MainActivityPagerAdapter;
import cf.bautroixa.maptest.ui.dialogs.SosRequestEditDialogFragment;
import cf.bautroixa.maptest.ui.map.TabMapFragment;
import cf.bautroixa.maptest.ui.notifications.NotificationActivity;
import cf.bautroixa.maptest.ui.settings.SettingActivity;

public class BottomToolsAdapter extends RecyclerView.Adapter<BottomToolsAdapter.ToolVH> {
    ArrayList<ToolItem> tools;
    Context context;
    ModelManager manager;

    public BottomToolsAdapter(final Context context, final Fragment fragment, final NavigationInterface navigationInterface) {
        this.setHasStableIds(true);
        this.manager = ModelManager.getInstance(context);
        this.context = context;
        tools = new ArrayList<>();
        final SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_preference_name), Context.MODE_PRIVATE);
        // 0
        tools.add(new ToolItem(0, fragment, R.drawable.ic_people_white_24dp, "Thành viên", new ToolItem.OnToolItemClicked() {
            @Override
            public void onClick(View v, boolean isActivated) {
                navigationInterface.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_FRIEND_LIST_EXPANDED);
            }
        }));
        // 1
        tools.add(new ToolItem(1, fragment, R.drawable.ic_place_black_24dp, "Địa điểm", new ToolItem.OnToolItemClicked() {
            @Override
            public void onClick(View v, boolean isActivated) {
                if (isActivated) {
                    manager.getCurrentTrip().getActiveCheckpoint().addOnCompleteListener(new OnCompleteListener<Checkpoint>() {
                        @Override
                        public void onComplete(@NonNull Task<Checkpoint> task) {
                            navigationInterface.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_CHECKPOINT, task.getResult());
                        }
                    });
                } else {
                    navigationInterface.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_CHECKPOINT_LIST_EXPANDED);
                }

            }
        }) {
            @Override
            protected void onCreate(ToolItem toolItem) {
                manager.getCurrentTrip().attachListener(fragment, new Document.OnValueChangedListener<Trip>() {
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
        tools.add(new ToolItem(2, fragment, R.drawable.ic_help, "Gửi hỗ trợ", new ToolItem.OnToolItemClicked() {
            @Override
            public void onClick(View v, boolean isActivated) {
                new SosRequestEditDialogFragment().show(fragment.getChildFragmentManager(), "add edit sos");
            }
        }) {
            @Override
            protected void onCreate(ToolItem toolItem) {
                manager.getCurrentUser().attachListener(fragment, new Document.OnValueChangedListener<User>() {
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
        tools.add(new ToolItem(3, fragment, R.drawable.ic_notifications_black_24dp, "Thông báo", new ToolItem.OnToolItemClicked() {
            @Override
            public void onClick(View v, boolean isActivated) {
                fragment.startActivityForResult(new Intent(context, NotificationActivity.class), RequestCodes.TOOL_NOTIFICATION);
            }
        }) {
            @Override
            protected void onCreate(final ToolItem toolItem) {
                final int[] userNotSeen = {0};
                final int[] tripNotSeen = {0};
                manager.getCurrentUser().getUserNotificationsManager().attachOnNotificationCountChangedListener(fragment, new NotificationsManager.OnNotificationCountChanged() {
                    @Override
                    public void onChanged(int notSeenCount) {
                        userNotSeen[0] = notSeenCount;
                        toolItem.setBadgeNumber(userNotSeen[0] + tripNotSeen[0]);
                        notifyItemChanged(3);
                    }
                });
                if (manager.getCurrentTrip().isAvailable()) {
                    manager.getCurrentTrip().getTripNotificationsManager().attachOnNotificationCountChangedListener(fragment, new NotificationsManager.OnNotificationCountChanged() {
                        @Override
                        public void onChanged(int notSeenCount) {
                            tripNotSeen[0] = notSeenCount;
                            toolItem.setBadgeNumber(userNotSeen[0] + tripNotSeen[0]);
                            notifyItemChanged(3);
                        }
                    });
                }
            }
        });
        tools.add(new ToolItem(4, fragment, R.drawable.ic_explore_black_24dp, "Khám phá", new ToolItem.OnToolItemClicked() {
            @Override
            public void onClick(View v, boolean isActivated) {
                navigationInterface.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_EXPLORE);
            }
        }));

        tools.add(new ToolItem(4, fragment, R.drawable.ic_weather_sun, "Thời tiết", new ToolItem.OnToolItemClicked() {
            @Override
            public void onClick(View v, boolean isActivated) {
                navigationInterface.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_WEATHER);
            }
        }));
//        final boolean isLocationServiceOn = AlarmHelper.isOn(sharedPref);
//        tools.add(new ToolItem(5, fragment, isLocationServiceOn, R.drawable.ic_my_location_black_24dp, "Bật chia sẻ", new ToolItem.OnToolItemClicked() {
//            @Override
//            public void onClick(View v, boolean isActivated) {
//                if (!isActivated) AlarmHelper.turnOff(context, sharedPref);
//                else AlarmHelper.turnOn(context, sharedPref);
//            }
//        }));
        tools.add(new StateToolItem(6,
                fragment,
                Arrays.asList(context.getResources().getStringArray(R.array.setting_dark_mode)),
                context.getResources().obtainTypedArray(R.array.dark_mode_drawables),
                new StateToolItem.OnStateToolItemClickedListener() {
                    @Override
                    public void onClick(View v, int mode) {
                        SPDarkMode.applyMode(sharedPref, mode);
                    }
                },
                SPDarkMode.getCurrentMode(sharedPref),
                new StateToolItem.OnChangeStateListener() {
                    @Override
                    public int newState(int oldState) {
                        return (oldState + 1) % SPDarkMode.nightModes.size();
                    }
                }
        ));
        tools.add(new ToolItem(7, fragment, R.drawable.ic_settings_black_24dp, "Cài đặt", new ToolItem.OnToolItemClicked() {
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
        TextView tvTitle, badge;
        Context context;

        public ToolVH(Context context, @NonNull View itemView) {
            super(itemView);
            this.context = context;
            icon = itemView.findViewById(R.id.ic_item_tool);
            tvTitle = itemView.findViewById(R.id.tv_title_item_tool);
            badge = itemView.findViewById(R.id.badge_item_tool);
        }

        void bind(ToolItem toolItem) {
            boolean isActivated = toolItem.isActivated();
            icon.setActivated(isActivated);
            icon.setImageResource(toolItem.getIcon());
            tvTitle.setText(toolItem.getText());
            if (toolItem.getBadgeNumber() != null) {
                badge.setVisibility(View.VISIBLE);
                badge.setText(String.valueOf(toolItem.getBadgeNumber()));
            } else {
                badge.setVisibility(View.INVISIBLE);
            }
        }
    }
}
