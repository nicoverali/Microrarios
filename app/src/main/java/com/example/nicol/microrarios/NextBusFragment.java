package com.example.nicol.microrarios;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Map;

import bus.Bus;
import bus.BusStop;
import bus.helper.ScheduleTime;

/**
 * This fragment represents the main section of the app. It shows information about the current next bus.
 */
public class NextBusFragment extends Fragment {
    // Attributes
    private TimetableViewModel viewModel;

    // Prefab Layouts
    private static final int VERTICAL_STOP = R.layout.template_vertical_bs;
    private static final int VERTICAL_BLANK = R.layout.template_vertical_bs_blank;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(TimetableViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_next_bus, container, false);
        // Subscribe to ViewModel
        viewModel.observeDepartureStop(this, departureStop -> {
            setMainTime(departureStop, layout);
            setBusStops(viewModel.getTimetable().nextBus(departureStop), layout.findViewById(R.id.vertical_timeline_viewgroup));
        });

        return layout;
    }

    // This private methods fulfill the view information with the given bus

    /**
     * Sets the main time to the departure time of the next bus.
     * @param layout Fragment layout
     */
    private void setMainTime(BusStop departureStop, View layout){
        Iterable<Map.Entry<BusStop, ScheduleTime>> stopsEntries = viewModel.getTimetable().nextBus(departureStop).getBusStops();
        int hour = -1;
        int minute = -1;
        for(Map.Entry<BusStop, ScheduleTime> entry : stopsEntries) {
            if (entry.getKey().equals(departureStop)){
                hour = entry.getValue().getHour();
                minute = entry.getValue().getMinute();
            }
        }
        ((TextView) layout.findViewById(R.id.text_main_time)).setText(getResources().getString(R.string.time_template, hour, minute));
    }

    /**
     * Sets all the stops and their arrival time within the vertical timeline view group that's given as a parameter.
     * @param nextBus Next bus
     * @param timelineLayout Vertical timeline linear layout
     */
    private void setBusStops(Bus nextBus, LinearLayout timelineLayout){
        // Clean ViewGroup first
        timelineLayout.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getContext());
        Iterable<Map.Entry<BusStop, ScheduleTime>> stops = nextBus.getBusStops();

        // Add first blank space
        LinearLayout.LayoutParams firstBlankViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,(int) getResources().getDimension(R.dimen.vertical_first_blank_timeline_height));
        firstBlankViewParams.gravity = Gravity.END;
        timelineLayout.addView(inflater.inflate(VERTICAL_BLANK, timelineLayout, false), firstBlankViewParams);

        // Set stop and blank view parameters (Height, Width and Gravity)
        LinearLayout.LayoutParams blankViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, (int) getResources().getDimension(R.dimen.vertical_regular_blank_timeline_height));
        LinearLayout.LayoutParams stopViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        blankViewParams.gravity = (stopViewParams.gravity = Gravity.END);

        // For every stop add a stop view and a blank view
        View tempBusStopView, tempBlankView;
        for(Map.Entry<BusStop, ScheduleTime> stop : stops){
            tempBusStopView = inflater.inflate(VERTICAL_STOP, timelineLayout, false);
            tempBlankView = inflater.inflate(VERTICAL_BLANK, timelineLayout, false);
            // Change stop info
            ((TextView)tempBusStopView.findViewById(R.id.stop_name_textview)).setText(stop.getKey().getName());
            ((TextView)tempBusStopView.findViewById(R.id.stop_time_textview)).setText(getResources().getString(R.string.time_template, stop.getValue().getHour(), stop.getValue().getMinute()));
            // Add to layout
            timelineLayout.addView(tempBusStopView, stopViewParams);
            timelineLayout.addView(tempBlankView, blankViewParams);
        }
        // Remove last blank view
        timelineLayout.removeViewAt(timelineLayout.getChildCount() - 1);
    }
}
