package com.example.nicol.microrarios;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import bus.BusStop;

public class HomeActivity extends AppCompatActivity {
    // Attributes
    private TimetableViewModel viewModel;
    private SlidingUpPanelLayout slidingPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Get ViewModel instance
        viewModel = ViewModelProviders.of(this).get(TimetableViewModel.class);

        // Set template_toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_settings_white_24dp);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Setup selector
        this.slidingPanel = findViewById(R.id.slidinguppanel_layout);
        FrameLayout expandedView = findViewById(R.id.selector_expanded_view);
        ViewGroup collapsedView = findViewById(R.id.selector_collapsed_view);
        Spinner originSpinner = expandedView.findViewById(R.id.origin_spinner);
        Spinner arriveSpinner = expandedView.findViewById(R.id.arrival_spinner);
        initSelectorPanel(originSpinner, slidingPanel.findViewById(R.id.origin_textview), arriveSpinner, slidingPanel.findViewById(R.id.arrival_textview), slidingPanel.findViewById(R.id.invert_imagebutton));
        slidingPanel.addPanelSlideListener(new SelectorListener(expandedView, collapsedView, slidingPanel));

        // Make sliding panel close when touching outside
        View outsidePanel = findViewById(R.id.umano_main_view);
        outsidePanel.setOnTouchListener((view, motionEvent) -> {
            if(slidingPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED){
                slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                return true;
            }
            return false;
        });

        // Create fragments only if this activity is not being restored
        if(savedInstanceState == null){
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction newTransaction = fragmentManager.beginTransaction();
            // Add NextBusFragment
            newTransaction.add(R.id.next_bus_fragment_container, new NextBusFragment());
            // Add FeedFragment
            newTransaction.add(R.id.feed_fragment_container, new FeedFragment());
            // Commit transaction
            newTransaction.commit();
        }
    }

    /**
     * Checks if the Umano Sliding Panel is open, if it is and the key press is "back" then close the panel, if not do the
     * default back action
     * @param keyCode Pressed key code
     * @param event Event
     * @return {@inheritDoc}
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && this.slidingPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED){
            slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
     * Information loaders
     */

    private void initSelectorPanel(Spinner departureSpinner, TextView originTextView, Spinner arrivalSpinner, TextView arrivalTextView, ImageButton invert){
        // Set values
        SpinnersOnSelectedListener spinnersListener = new SpinnersOnSelectedListener(departureSpinner, arrivalSpinner);

        // Attach adapters
        departureSpinner.setAdapter(viewModel.getDepartureAdapter());
        arrivalSpinner.setAdapter(viewModel.getArrivalAdapter());

        // Attach listener
        invert.setOnClickListener(param -> viewModel.invertStops());
        departureSpinner.setOnItemSelectedListener(spinnersListener);
        arrivalSpinner.setOnItemSelectedListener(spinnersListener);

        // Subscribe to ViewModel changes
        viewModel.observeDepartureStop(this, busStop -> {
            originTextView.setText(busStop.getName());
            departureSpinner.setSelection(viewModel.getDepartureAdapter().getPosition(busStop));
        });
        viewModel.observeArrivalStop(this, busStop -> {
            arrivalTextView.setText(busStop.getName());
            arrivalSpinner.setSelection(viewModel.getArrivalAdapter().getPosition(busStop));
        });
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
        private Spinner departureSpinner;
        private Spinner arrivalSpinner;

        // Constructor
        public SpinnersOnSelectedListener(Spinner departureSpinner, Spinner arrivalSpinner){
            this.departureSpinner = departureSpinner;
            this.arrivalSpinner = arrivalSpinner;
        }

        // Methods
        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            // Do nothing
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int adapterPosition, long id) {
            if(adapterView.equals(departureSpinner)){
                viewModel.setDepartureStop((BusStop) adapterView.getAdapter().getItem(adapterPosition));
            }
            else if(adapterView.equals(arrivalSpinner)){
                viewModel.setArrivalStop((BusStop) adapterView.getAdapter().getItem(adapterPosition));
            }
        }
    }
}
