package com.example.nicol.microrarios;

import android.app.Activity;
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

import bus.Bus;
import bus.timetable.BusTimeTable;

public class FeedFragment extends Fragment {
    // Attributes
    private BusTimeTable mTimetable;
    private int loadCardsTimes = 1;

    // Prefab layouts
    private static final int SINGLE_CARD_NEXT = R.layout.template_next_buses_card;
    private static final int SINGLE_CARD_LAST = R.layout.template_last_bus_card;
    private static final int CARD_TITLE = R.layout.template_feed_title;
    private static final int CARD_BLANK = R.layout.template_feed_blank;

    @Override
    /**
     * If the creator activity is HomeActivity, then checks if the timetables are loaded and stores the current one.
     */
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity creatorActivity = getActivity();
        if(creatorActivity instanceof HomeActivity){
            HomeActivity home = (HomeActivity) creatorActivity;
            TimetablesSingleton timetables = TimetablesSingleton.getInstance();
            if(timetables.getTimetablesState() == TimetablesSingleton.STATE_LOADED){
                mTimetable = timetables.getTimetable(home.getCurrentTableId());
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_feed, container, false);
        View firstBlank = inflater.inflate(CARD_BLANK, layout, false);
        firstBlank.getLayoutParams().height = (int) getResources().getDimension(R.dimen.feed_blank_first_height);
        View separatorBlank = inflater.inflate(CARD_BLANK, layout, false);
        separatorBlank.getLayoutParams().height = (int) getResources().getDimension(R.dimen.feed_blank_separator_height);

        if(mTimetable != null){
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
        Bus lastBus = mTimetable.lastBus();
        View lastBusCard = inflater.inflate(SINGLE_CARD_LAST, layout, false);
        ((TextView) lastBusCard.findViewById(R.id.leave_time_textview)).setText("55 min");
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
        CardArrayAdapter adapter = new CardArrayAdapter(getContext(), 0, mTimetable.nextBuses(1));
        listView.setAdapter(adapter);
        setListViewHeightBasedOnChildren(listView);

        // Add button listener
        loadMoreButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              // TODO If I don't allow a bus to be consider next if it departures now, then I would't have to add one in the parameter
              adapter.addAll(mTimetable.nextBuses(adapter.getCount()));
              setListViewHeightBasedOnChildren(listView);
              if(--loadCardsTimes == 0){
                  loadMoreButton.setEnabled(false);
              }
          }
        });
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
