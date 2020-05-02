package cf.bautroixa.maptest;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import cf.bautroixa.maptest.data.SearchResult;
import cf.bautroixa.maptest.firestore.MainAppManager;
import cf.bautroixa.maptest.interfaces.OnButtonClickedListener;
import cf.bautroixa.maptest.theme.OneRecyclerView;
import cf.bautroixa.maptest.theme.RoundedImageView;
import cf.bautroixa.maptest.theme.ViewAnim;
import cf.bautroixa.maptest.utils.ImageHelper;
import cf.bautroixa.maptest.utils.KeyboardHelper;
import cf.bautroixa.maptest.utils.PixelDPConverter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SearchFragment extends Fragment {
    public static final int STATE_SEARCH = 1;
    public static final int STATE_COLLAPSED = 0;
    private static final String TAG = "SearchFragment";
    private static final String ARG_AVATAR_URL = "avatar_url";
    OnSearchItemClickedListener onSearchItemClickedListener = null;
    OnButtonClickedListener onAvatarClickedListener;
    int currentState = STATE_COLLAPSED;
    String avatarUrl = "";
    boolean showToolbar = true;
    RecyclerView rvSearchResult;
    SearchResultAdapter searchResultAdapter;
    ConstraintLayout root;
    Toolbar toolbar;
    EditText editSearch;
    ImageButton btnBack, btnClear;
    RoundedImageView imgAvatar;
    private MainAppManager manager;

    public SearchFragment() {
        manager = MainAppManager.getInstance();
    }

    public void setOnAvatarClickedListener(OnButtonClickedListener onAvatarClickedListener) {
        this.onAvatarClickedListener = onAvatarClickedListener;
    }

    public void showHideCompletely(boolean isShown) {
        ViewAnim.toggleHideShow(root, isShown, ViewAnim.DIRECTION_UP);
    }

    public void showHideToolbar(boolean isShown) {
        showToolbar = isShown;
        ViewAnim.toggleHideShow(toolbar, isShown, ViewAnim.DIRECTION_UP);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        root = view.findViewById(R.id.root_frag_search);
        toolbar = view.findViewById(R.id.toolbar_frag_search);
        btnBack = view.findViewById(R.id.btn_back_frag_search);
        btnClear = view.findViewById(R.id.btn_clear_frag_search);
        imgAvatar = view.findViewById(R.id.img_avatar_frag_search);
        editSearch = view.findViewById(R.id.edit_search_location_frag_search);

        toolbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewAnim.toggleHideShow(toolbar, false, ViewAnim.DIRECTION_UP);
            }
        }, 5000);

        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onAvatarClickedListener != null) onAvatarClickedListener.onClick(imgAvatar);
            }
        });

        editSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentState == STATE_COLLAPSED) handleState(STATE_SEARCH);
            }
        });

        editSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (currentState == STATE_COLLAPSED) handleState(STATE_SEARCH);
                }
            }
        });

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    btnClear.setVisibility(View.INVISIBLE);
                } else {
                    btnClear.setVisibility(View.VISIBLE);
                    MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
                            .accessToken(getString(R.string.config_mapbox_map_api_key))
                            .query(s.toString())
                            .build();
                    mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
                        @Override
                        public void onResponse(@NotNull Call<GeocodingResponse> call, @NotNull Response<GeocodingResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<CarmenFeature> results = response.body().features();
                                if (results.size() > 0) {
                                    ArrayList<SearchResult> newSearchResults = new ArrayList<>();
                                    for (CarmenFeature feature : results) {
                                        if (feature.placeName() == null || feature.center() == null)
                                            continue;
                                        String[] placeNames = feature.placeName().split(",");
                                        String placeName = placeNames[0];
                                        String placeAddress = placeNames.length == 1 ? "" : feature.placeName().substring(placeName.length() + 2);
                                        newSearchResults.add(new SearchResult(placeName, placeAddress, feature.center().latitude(), feature.center().longitude()));
                                        Log.d(TAG, "address = " + feature.placeName());
                                    }
                                    searchResultAdapter.changeDataSet(newSearchResults);
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NotNull Call<GeocodingResponse> call, @NotNull Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleState(STATE_COLLAPSED);
                editSearch.setText("");
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editSearch.setText("");
            }
        });

        rvSearchResult = view.findViewById(R.id.rv_search_results);
        searchResultAdapter = new SearchResultAdapter();
        rvSearchResult.setAdapter(searchResultAdapter);
        rvSearchResult.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // TODO: this is temporary fix
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                handleState(STATE_COLLAPSED);
            }
        }, 1000);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (manager.isLoggedIn()) {
            if (!avatarUrl.equals(manager.getCurrentUser().getAvatar())) {
                avatarUrl = manager.getCurrentUser().getAvatar();
                ImageHelper.loadImage(avatarUrl, imgAvatar);
            }
        }
    }

    public void handleState(int state) {
        this.currentState = state;
        int _50dp = (int) PixelDPConverter.convertDpToPixel(50, getContext());
        switch (state) {
            case STATE_COLLAPSED:
                showHideToolbar(showToolbar);
                btnBack.setVisibility(View.GONE);
                editSearch.setPadding((int) PixelDPConverter.convertDpToPixel(24, getContext()), 0, _50dp, 0);
                ViewAnim.toggleHideShow(rvSearchResult, false, ViewAnim.DIRECTION_UP);
                editSearch.clearFocus();
                KeyboardHelper.hideSoftKeyboard(editSearch);
                break;
            case STATE_SEARCH:
                ViewAnim.toggleHideShow(toolbar, true, ViewAnim.DIRECTION_UP);
                btnBack.setVisibility(View.VISIBLE);
                editSearch.setPadding(_50dp, 0, _50dp, 0);
                ViewAnim.toggleHideShow(rvSearchResult, true, ViewAnim.DIRECTION_UP);
                break;
        }
    }

    public void setOnSearchItemClickedListener(OnSearchItemClickedListener onSearchItemClickedListener) {
        this.onSearchItemClickedListener = onSearchItemClickedListener;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.onSearchItemClickedListener = null;
    }

    public interface OnSearchItemClickedListener {
        void onSearchItemClicked(SearchResult searchResult);
    }

    public class SearchResultViewHolder extends OneRecyclerView.ViewHolder {

        TextView tvName, tvTime, tvLocation;
        View view;

        public SearchResultViewHolder(@NonNull View itemView, int viewType) {
            super(itemView, viewType);
            view = itemView;
            tvName = itemView.findViewById(R.id.tv_name_item_checkpoint);
            tvLocation = itemView.findViewById(R.id.tv_location_item_checkpoint);
            tvTime = itemView.findViewById(R.id.tv_time_item_checkpoint);
        }

        public void bind(final SearchResult searchResult) {
            tvName.setText(searchResult.getPlaceName());
            tvLocation.setText(searchResult.getPlaceAddress());
            tvTime.setText("");
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onSearchItemClickedListener != null) {
                        editSearch.setText(searchResult.getPlaceName());
                        handleState(STATE_COLLAPSED);
                        onSearchItemClickedListener.onSearchItemClicked(searchResult);
                    }
                }
            });
        }
    }

    public class SearchResultAdapter extends OneRecyclerView.Adapter<SearchResultViewHolder> {
        ArrayList<SearchResult> searchResults;

        public SearchResultAdapter() {
            this.searchResults = new ArrayList<>();
        }

        public void changeDataSet(ArrayList<SearchResult> newSearchResults) {
            this.searchResults = newSearchResults;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new SearchResultViewHolder(getLayoutInflater().inflate(R.layout.item_checkpoint, parent, false), viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
            holder.bind(searchResults.get(position));
        }

        @Override
        public int getItemCount() {
            return searchResults.size();
        }
    }
}
