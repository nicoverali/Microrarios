package com.example.nicol.microrarios;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.joda.time.DateTime;

import bus.Bus;
import bus.BusStop;
import bus.helper.ScheduleTime;
import bus.timetable.BusTimeTable;

public class FeedFragment extends Fragment {
    // Constant keys for communication
    public static final String TIMETABLE_KEY = "com.verali.apps.FeedFragment.timetable";
    public static final String DEPARTURE_STOP_KEY = "com.verali.apps.FeedFragment.departureStop";
    public static final String ARRIVAL_STOP_KEY = "com.verali.apps.FeedFragment.arrivalStop";

    // Attributes
    private BusTimeTable timetable;
    private BusStop departureStop;
    private BusStop arrivalStop;
    private int loadCardsTimes = 1;

    // Prefab layouts
    private static final int SINGLE_CARD_NEXT = R.layout.template_next_buses_card;
    private static final int SINGLE_CARD_LAST = R.layout.template_last_bus_card;
    private static final int CARD_TITLE = R.layout.template_feed_title;
    private static final int CARD_BLANK = R.layout.template_feed_blank;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_feed, container, false);
        View firstBlank = inflater.inflate(CARD_BLANK, layout, false);
        firstBlank.getLayoutParams().height = (int) getResources().getDimension(R.dimen.feed_blank_first_height);
        View separatorBlank = inflater.inflate(CARD_BLANK, layout, false);
        separatorBlank.getLayoutParams().height = (int) getResources().getDimension(R.dimen.feed_blank_separator_height);

        if(timetable != null){
            ListView listView = layout.findViewById(R.id.next_buses_listview);
            setLastBus(layout, inflater);
            // Add blank space between cards
            layout.addView(separatorBlank, layout.indexOfChild(listView));

            setNextBuses(listView, layout, inflater);

            // Add blank space at the beginning
            layout.addView(firstBlank, 0);
        }
        else{
            ((Button)layout.findViewById(R.id.load_more_button)).setEnabled(false);
        }
        return layout;
    }

    // Private view creator methods
    private void setLastBus(ViewGroup layout, LayoutInflater inflater){
        // Set last bus card
        Bus lastBus = timetable.lastBus();
        View lastBusCard = inflater.inflate(SINGLE_CARD_LAST, layout, false);
        ((TextView) lastBusCard.findViewById(R.id.leave_time_textview)).setText(getFormattedDifference(lastBus.getDepartureStop().getValue(), new ScheduleTime(DateTime.now())));
        ((TextView) lastBusCard.findViewById(R.id.departure_time_textview)).setText(getResources().getString(R.string.time_template, lastBus.getDepartureStop().getValue().getHour(), lastBus.getDepartureStop().getValue().getMinute()));
        ((TextView) lastBusCard.findViewById(R.id.arrival_time_textview)).setText(getResources().getString(R.string.time_template, lastBus.getArrivalStop().getValue().getHour(), lastBus.getArrivalStop().getValue().getMinute()));
        layout.addView(lastBusCard, 0);

        // Set title
        View titleView = inflater.inflate(CARD_TITLE, layout, false);
        ((TextView) titleView.findViewById(R.id.title_textview)).setText("Último en salir");
        layout.addView(titleView, 0);
    }
    private void setNextBuses(ListView listView, ViewGroup layout, LayoutInflater inflater){
        Button loadMoreButton = (Button) layout.findViewById(R.id.load_more_button);

        // Set title
        int listViewIndex = layout.indexOfChild(listView);
        View titleView = inflater.inflate(CARD_TITLE, layout, false);
        ((TextView) titleView.findViewById(R.id.title_textview)).setText("Próximos colectivos");
        layout.addView(titleView, listViewIndex);

        // Set listview
        CardArrayAdapter adapter = new CardArrayAdapter(getContext(), 0, timetable.nextBuses(1));
        listView.setAdapter(adapter);
        setListViewHeightBasedOnChildren(listView);

        // Add button listener
        loadMoreButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              adapter.addAll(timetable.nextBuses(adapter.getCount() + 1));
              setListViewHeightBasedOnChildren(listView);
              if(--loadCardsTimes == 0){
                  loadMoreButton.setEnabled(false);
              }
          }
        });
    }


    // Private helper methods
    private String getFormattedDifference(ScheduleTime fromTime, ScheduleTime toTime){
        int difference = fromTime.differenceInMinutes(toTime);
        String formattedDiff = difference >= 60 ? (difference/60) + " hr" : difference + " min";
        return difference >= 60 ? formattedDiff + " " + (difference % 60) + "'" : formattedDiff;
    }

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}
