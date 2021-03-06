package com.example.nicol.microrarios;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bus.BusStop;
import bus.timetable.BusTimeTable;

public class HomeActivity extends AppCompatActivity {
    // Attributes
    private BusTimeTable puntaAltaTimetable;
    private BusTimeTable bahiaBlancaTimetable;
    private Map<BusStop, Integer> stopsHierarchy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Create tables
        if(getIntent().hasExtra(TimetableAsyncTask.PUNTA_ALTA_KEY)){
            puntaAltaTimetable = getIntent().getParcelableExtra(TimetableAsyncTask.PUNTA_ALTA_KEY);
            bahiaBlancaTimetable = getIntent().getParcelableExtra(TimetableAsyncTask.BAHIA_BLANCA_KEY);
        }
        else if(savedInstanceState != null && savedInstanceState.containsKey(TimetableAsyncTask.PUNTA_ALTA_KEY)){
            puntaAltaTimetable = savedInstanceState.getParcelable(TimetableAsyncTask.PUNTA_ALTA_KEY);
            bahiaBlancaTimetable = savedInstanceState.getParcelable(TimetableAsyncTask.BAHIA_BLANCA_KEY);
        }
        else{
            throw new UnsupportedOperationException("Activity can not be created without providing the timetables.");
        }

        // Set bus stops hierarchy
        stopsHierarchy = new HashMap<>();
        List<BusStop> stops = puntaAltaTimetable.getBusStops();
        for(int i = 0; i < stops.size(); i++){
            stopsHierarchy.put(stops.get(i), i);
        }

        // Set template_toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_settings_white_24dp);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Setup selector
        SlidingUpPanelLayout slidingPanel = findViewById(R.id.slidinguppanel_layout);
        FrameLayout expandedView = findViewById(R.id.selector_expanded_view);
        LinearLayout collapsedView = findViewById(R.id.selector_collapsed_view);
        Spinner originSpinner = expandedView.findViewById(R.id.origin_spinner);
        Spinner arriveSpinner = expandedView.findViewById(R.id.arrival_spinner);
        initSelectorPanel(originSpinner, slidingPanel.findViewById(R.id.origin_textview), arriveSpinner,
                slidingPanel.findViewById(R.id.arrival_textview), slidingPanel.findViewById(R.id.invert_imagebutton));
        slidingPanel.addPanelSlideListener(new SelectorListener(expandedView, collapsedView, slidingPanel));

        // Create fragments only if this activity is not being restored
        if(savedInstanceState == null){
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction newTransaction = fragmentManager.beginTransaction();
            // Add NextBusFragment
            Bundle nextBusBundle = new Bundle();
            nextBusBundle.putParcelable(NextBusFragment.TIMETABLE_KEY, getCurrentTimetable());
            nextBusBundle.putParcelable(NextBusFragment.DEPARTURE_STOP_KEY, getCurrentTimetable().getBusStops().get(0));
            nextBusBundle.putParcelable(NextBusFragment.ARRIVAL_STOP_KEY, getCurrentTimetable().getBusStops().get(2));
            NextBusFragment nextBusFragment = new NextBusFragment();
            nextBusFragment.setArguments(nextBusBundle);
            newTransaction.add(R.id.next_bus_fragment_container, nextBusFragment);
            // Add FeedFragment
            Bundle feedBundle = new Bundle();
            feedBundle.putParcelable(FeedFragment.TIMETABLE_KEY, getCurrentTimetable());
            feedBundle.putParcelable(FeedFragment.DEPARTURE_STOP_KEY, getCurrentTimetable().getBusStops().get(0));
            feedBundle.putParcelable(FeedFragment.ARRIVAL_STOP_KEY, getCurrentTimetable().getBusStops().get(1));
            FeedFragment feedFragment = new FeedFragment();
            feedFragment.setArguments(feedBundle);
            newTransaction.add(R.id.feed_fragment_container, feedFragment);
            // Commit transaction
            newTransaction.commit();
        }
    }

    @Override
    // Just for debugging
    protected void onDestroy() {
        super.onDestroy();
        Log.v("TEST", "App destroyed");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    /**
     * Returns the timetable that shows the information asked for the user at this moment
     * @return Current timetable id
     */
    public BusTimeTable getCurrentTimetable(){
        // TODO
        return puntaAltaTimetable;
    }

    /**
     * Information loaders
     */

    private void initSelectorPanel(Spinner originSpinner, TextView originTextView, Spinner arrivalSpinner, TextView arrivalTextView, ImageButton invert){
        List<BusStop> busStops = getCurrentTimetable().getBusStops();

        // Set values
        ArrayAdapter<BusStop> spinnersAdapter = new ArrayAdapter<BusStop>(this, R.layout.spinner_selector_item, busStops);
        spinnersAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        int originSpinnerInitialItem = 0;
        int arriveSpinnerInitialItem = spinnersAdapter.getCount()-1;
        SpinnersOnSelectedListener spinnersListener = new SpinnersOnSelectedListener(originSpinner, originSpinnerInitialItem, originTextView, arrivalSpinner, arriveSpinnerInitialItem, arrivalTextView);

        // Attach things
        originSpinner.setAdapter(spinnersAdapter);
        originSpinner.setOnItemSelectedListener(spinnersListener);
        arrivalSpinner.setAdapter(spinnersAdapter);
        arrivalSpinner.setOnItemSelectedListener(spinnersListener);
        invert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                originSpinner.setSelection(arrivalSpinner.getSelectedItemPosition());
            }
        });

        // Set initial values
        originSpinner.setSelection(originSpinnerInitialItem);
        arrivalSpinner.setSelection(arriveSpinnerInitialItem);
        originTextView.setText(spinnersAdapter.getItem(originSpinnerInitialItem).getName());
        arrivalTextView.setText(spinnersAdapter.getItem(arriveSpinnerInitialItem).getName());
    }

    /**
     * Listeners
     */
    private class SelectorListener extends SlidingUpPanelLayout.SimplePanelSlideListener{
        // Attributes
        private ViewGroup expandedView;
        private ViewGroup collapsedView;
        private SlidingUpPanelLayout slidingPanel;
        private float lastSlideOffset = 0f;

        public SelectorListener(ViewGroup expandedView, ViewGroup collapsedView, SlidingUpPanelLayout slidingPanel) {
            this.expandedView = expandedView;
            this.collapsedView = collapsedView;
            this.slidingPanel = slidingPanel;
        }

        @Override
        public void onPanelSlide(View panel, float slideOffset) {
            if(slideOffset > 0)
                expandedView.setVisibility(View.VISIBLE);
            expandedView.setAlpha(slideOffset);
            collapsedView.setAlpha((-2.5f*slideOffset) + 1);
            lastSlideOffset = slideOffset;
        }

        @Override
        public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
            if(newState.equals(SlidingUpPanelLayout.PanelState.COLLAPSED)){
                expandedView.setVisibility(View.GONE);
            }
            else if(newState.equals(SlidingUpPanelLayout.PanelState.ANCHORED)){
                if(lastSlideOffset < 0.5f)
                    slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                else
                    slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        }
    }

    private class SpinnersOnSelectedListener implements Spinner.OnItemSelectedListener{
        // Attributes
        private Spinner originSpinner;
        private Spinner arrivalSpinner;
        private TextView originText;
        private TextView arriveText;
        private int originSpinnerCurrentItem;
        private int arriveSpinnerCurrentItem;

        // Constructor
        public SpinnersOnSelectedListener(Spinner originSpinner, int  oSpinnerInitialItem, TextView originTextView, Spinner arrivalSpinner, int aSpinnerInitialItem, TextView arrivalTextView){
            this.originSpinner = originSpinner;
            this.arrivalSpinner = arrivalSpinner;
            this.originText = originTextView;
            this.arriveText = arrivalTextView;
            this.originSpinnerCurrentItem = oSpinnerInitialItem;
            this.arriveSpinnerCurrentItem = aSpinnerInitialItem;
        }

        // Methods
        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            // Do nothing
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int adapterPosition, long id) {
            if(adapterView.equals(originSpinner)){
                if(adapterPosition == arriveSpinnerCurrentItem)
                    arrivalSpinner.setSelection((arriveSpinnerCurrentItem = originSpinnerCurrentItem));
                originSpinnerCurrentItem = adapterPosition;
                originText.setText(((BusStop) originSpinner.getSelectedItem()).getName());
            }
            else if(adapterView.equals(arrivalSpinner)){
                    if(adapterPosition == originSpinnerCurrentItem)
                        originSpinner.setSelection((originSpinnerCurrentItem = arriveSpinnerCurrentItem));
                    arriveSpinnerCurrentItem = adapterPosition;
                    arriveText.setText(((BusStop) arrivalSpinner.getSelectedItem()).getName());
            }
        }
    }
}
