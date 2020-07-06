package cf.bautroixa.maptest.ui.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import cf.bautroixa.maptest.R;
import cf.bautroixa.maptest.interfaces.NavigationInterface;
import cf.bautroixa.maptest.interfaces.NavigationInterfaceOwner;
import cf.bautroixa.maptest.model.firestore.ModelManager;
import cf.bautroixa.maptest.model.types.GeocodingResult;
import cf.bautroixa.maptest.model.types.SearchResult;
import cf.bautroixa.maptest.ui.adapter.pager_adapter.MainActivityPagerAdapter;
import cf.bautroixa.maptest.ui.adapter.viewholder.PlaceVH;
import cf.bautroixa.maptest.ui.map.TabMapFragment;
import cf.bautroixa.maptest.ui.theme.FullScreenDialogFragment;
import cf.bautroixa.maptest.ui.theme.OneRecyclerView;
import cf.bautroixa.maptest.ui.theme.ViewAnim;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SearchDialogFragment extends FullScreenDialogFragment implements NavigationInterfaceOwner {
    private static final String TAG = "SearchFragment";

    private ModelManager manager;
    private NavigationInterface navigationInterface;

    private String avatarUrl = "";
    private boolean showToolbar = true;

    private ConstraintLayout root;
    private Toolbar toolbar;
    private EditText editSearch;
    private ImageButton btnBack, btnClear;
    private RecyclerView rvSearchResult;
    private SearchResultAdapter searchResultAdapter;

    public SearchDialogFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = ModelManager.getInstance(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        root = view.findViewById(R.id.root_frag_search);
        toolbar = view.findViewById(R.id.toolbar_frag_search);
        btnBack = view.findViewById(R.id.btn_back_frag_search);
        btnClear = view.findViewById(R.id.btn_clear_frag_search);
        editSearch = view.findViewById(R.id.edit_search_location_frag_search);

        editSearch.requestFocus();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editSearch.setText("");
                dismiss();
            }
        });

//        editSearch.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                editSearch.requestFocus();
//            }
//        });
//        editSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                }
//            }
//        });
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editSearch.setText("");
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
                            if (response.body() != null) {
                                List<CarmenFeature> results = response.body().features();
                                if (results.size() > 0) {
                                    ArrayList<SearchResult> newSearchResults = new ArrayList<>();
                                    for (CarmenFeature feature : results) {
                                        if (feature.placeName() == null || feature.center() == null)
                                            continue;
                                        GeocodingResult geocodingResult = new GeocodingResult(new LatLng(feature.center().latitude(), feature.center().longitude()), feature.placeName());
                                        newSearchResults.add(new SearchResult(geocodingResult.getPlaceName(), geocodingResult.getPlaceAddress(), feature.center().latitude(), feature.center().longitude()));
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

        rvSearchResult = view.findViewById(R.id.rv_search_results);
        searchResultAdapter = new SearchResultAdapter();
        rvSearchResult.setAdapter(searchResultAdapter);
        rvSearchResult.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.navigationInterface = null;
    }

    public void showHideCompletely(boolean isShown) {
        ViewAnim.toggleHideShow(root, isShown, ViewAnim.HIDE_DIRECTION_UP);
    }

    public void showHideToolbar(boolean isShown) {
        showToolbar = isShown;
        ViewAnim.toggleHideShow(toolbar, isShown, ViewAnim.HIDE_DIRECTION_UP);
    }

    @Override
    public void setNavigationInterface(NavigationInterface navigationInterface) {
        this.navigationInterface = navigationInterface;
    }

    public class SearchResultAdapter extends OneRecyclerView.Adapter<PlaceVH> {
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
        public PlaceVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new PlaceVH(getLayoutInflater().inflate(R.layout.item_place, parent, false), viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull PlaceVH holder, int position) {
            SearchResult searchResult = searchResults.get(position);
            holder.bind(searchResult, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editSearch.setText(searchResult.getPlaceName());
                    navigationInterface.navigate(MainActivityPagerAdapter.Tabs.TAB_MAP, TabMapFragment.STATE_SEARCH_RESULT, searchResult);
                    dismiss();
                }
            });
        }

        @Override
        public int getItemCount() {
            return searchResults.size();
        }
    }
}
