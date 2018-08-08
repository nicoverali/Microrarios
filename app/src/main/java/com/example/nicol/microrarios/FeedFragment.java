package com.example.nicol.microrarios;

import android.arch.lifecycle.ViewModelProviders;
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

import java.util.List;

import bus.Bus;
import bus.BusStop;
import bus.helper.ScheduleTime;

public class FeedFragment extends Fragment {
    // Attributes
    private int loadCardsTimes = 1;
    private TimetableViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(TimetableViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_feed, container, false);
        ViewGroup lastBusCard = layout.findViewById(R.id.last_bus_card);
        ListView nextBusesListView = layout.findViewById(R.id.next_buses_listview);
        setLastBus(lastBusCard);
        setNextBuses(nextBusesListView, layout);

        // Set titles
        ((TextView)layout.findViewById(R.id.last_bus_title).findViewById(R.id.title_textview)).setText("Último en salir");
        ((TextView)layout.findViewById(R.id.next_buses_title).findViewById(R.id.title_textview)).setText("Próximos colectivos");

        // Subscribe to ViewModel changes
        viewModel.observeAnyStopChange(this, busStop -> {
            setLastBus(lastBusCard);
            setNextBuses(nextBusesListView, layout);
        });
        return layout;
    }

    // Private view creator methods
    private void setLastBus(ViewGroup lastBusView){
        BusStop departureStop = viewModel.getDepartureStop();
        BusStop arrivalStop = viewModel.getArrivalStop();
        Bus lastBus = viewModel.getTimetable().lastBus(departureStop);
        ((TextView) lastBusView.findViewById(R.id.leave_time_textview)).setText(getFormattedDifference(lastBus.getBusTimeAt(departureStop), new ScheduleTime(DateTime.now())));
        ((TextView) lastBusView.findViewById(R.id.departure_time_textview)).setText(getResources().getString(R.string.time_template, lastBus.getBusTimeAt(departureStop).getHour(), lastBus.getBusTimeAt(departureStop).getMinute()));
        ((TextView) lastBusView.findViewById(R.id.arrival_time_textview)).setText(getResources().getString(R.string.time_template, lastBus.getBusTimeAt(arrivalStop).getHour(), lastBus.getBusTimeAt(arrivalStop).getMinute()));
    }
    private void setNextBuses(ListView listView, ViewGroup layout){
        // Set listview
        List<Bus> buses = viewModel.getTimetable().nextBuses(viewModel.getDepartureStop(), 1);
        CardArrayAdapter adapter = new CardArrayAdapter(getContext(), 0, buses, viewModel.getDepartureStop(), viewModel.getArrivalStop());
        listView.setAdapter(adapter);
        setListViewHeightBasedOnChildren(listView);

        // Add button listener
        Button loadMoreButton = layout.findViewById(R.id.load_more_button);
        loadMoreButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              adapter.addAll(viewModel.getTimetable().nextBuses(viewModel.getDepartureStop(), adapter.getCount() + 1));
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
