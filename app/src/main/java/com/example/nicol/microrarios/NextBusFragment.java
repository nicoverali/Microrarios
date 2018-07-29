package com.example.nicol.microrarios;


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
import bus.timetable.BusTimeTable;

/**
 * This fragment represents the main section of the app. It shows information about the current next bus.
 */
public class NextBusFragment extends Fragment {
    // Constant keys for communication
    public static final String TIMETABLE_KEY = "com.verali.apps.NextBusFragment.timetable";
    public static final String DEPARTURE_STOP_KEY = "com.verali.apps.NextBusFragment.departureStop";
    public static final String ARRIVAL_STOP_KEY = "com.verali.apps.NextBusFragment.arrivalStop";

    // Attributes
    private BusTimeTable timetable;
    private BusStop departureStop;
    private BusStop arrivalStop;

    // Prefab Layouts
    private static final int VERTICAL_STOP = R.layout.template_vertical_bs;
    private static final int VERTICAL_BLANK = R.layout.template_vertical_bs_blank;

    @Override
    /**
     * Get timetable and stops from arguments, error if no arguments given.
     */
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if(arguments == null)
            throw new UnsupportedOperationException("This fragment can't be created without timetable and stops given as arguments");
        timetable = arguments.getParcelable(TIMETABLE_KEY);
        departureStop = arguments.getParcelable(DEPARTURE_STOP_KEY);
        arrivalStop = arguments.getParcelable(ARRIVAL_STOP_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_next_bus, container, false);
        if(timetable != null){
            Bus nextBus = timetable.nextBus();
            // Make view
            setMainTime(nextBus, layout);
            setBusStops(nextBus, layout.findViewById(R.id.vertical_timeline_viewgroup));
        }
        return layout;
    }

    // This private methods fulfill the view information with the given bus

    /**
     * Sets the main time to the departure time of the next bus.
     * @param nextBus Next bus
     * @param layout Fragment layout
     */
    private void setMainTime(Bus nextBus, View layout){
        ((TextView) layout.findViewById(R.id.text_main_time)).setText(nextBus.getDepartureStop().getValue().toString());
    }

    /**
     * Sets all the stops and their arrival time within the vertical timeline view group that's given as a parameter.
     * @param nextBus Next bus
     * @param timelineLayout Vertical timeline linear layout
     */
    private void setBusStops(Bus nextBus, LinearLayout timelineLayout){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        Iterable<Map.Entry<BusStop, ScheduleTime>> stops = nextBus.getBusStops();

        LinearLayout.LayoutParams blankViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, (int) getResources().getDimension(R.dimen.vertical_regular_blank_timeline_height));
        LinearLayout.LayoutParams stopViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        blankViewParams.gravity = (stopViewParams.gravity = Gravity.END);

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
