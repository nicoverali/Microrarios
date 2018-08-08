package com.example.nicol.microrarios;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.List;

import bus.Bus;
import bus.BusStop;
import bus.helper.ScheduleTime;

class CardArrayAdapter extends ArrayAdapter<Bus> {
    // TODO Update time left every minute, see Handler https://stackoverflow.com/questions/12916084/android-update-listview-items-every-1-minute

    // Attributes
    private Context context;
    private List<Bus> buses;
    private BusStop departureStop;
    private BusStop arrivalStop;

    // Constructor
    public CardArrayAdapter(@NonNull Context context, int resource, @NonNull List<Bus> buses, BusStop departureStop, BusStop arrivalStop) {
        super(context, resource, buses);
        this.context = context;
        this.buses = buses;
        this.departureStop = departureStop;
        this.arrivalStop = arrivalStop;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View viewToReturn = convertView != null && convertView instanceof LinearLayout ? convertView : LayoutInflater.from(context).inflate(R.layout.template_next_buses_card, null);
        Bus busToShow = buses.get(position);
        // Set time left
        ((TextView)viewToReturn.findViewById(R.id.time_left_textview)).setText(getTimeLeft(busToShow.getBusTimeAt(departureStop)));
        // Set departure and arrival times
        ScheduleTime departureTime = busToShow.getBusTimeAt(departureStop);
        ScheduleTime arrivalTime = busToShow.getBusTimeAt(arrivalStop);
        ((TextView)viewToReturn.findViewById(R.id.departure_time_textview)).setText(context.getResources().getString(R.string.time_template, departureTime.getHour(), departureTime.getMinute()));
        ((TextView)viewToReturn.findViewById(R.id.arrival_time_textview)).setText(context.getResources().getString(R.string.time_template, arrivalTime.getHour(), arrivalTime.getMinute()));

        return viewToReturn;
    }

    // Private helper methods
    /**
     * Given a ScheduleTime instance, returns the String representation of the time left from now to that ScheduleTime instance.
     * @param toTime ScheduleTime instance to compare
     * @return String representation of the time left from now to the ScheduleTime instance
     */
    private String getTimeLeft(ScheduleTime toTime){
        // Set times in minutes
        final int minutesInADay = 1440;
        int startTime = DateTime.now().getMinuteOfDay();
        int endTime = toTime.getHour()*60 + toTime.getMinute();
        int difference = endTime < startTime ? minutesInADay - startTime + endTime : endTime - startTime;
        StringBuilder timeLeft = new StringBuilder();
        if(difference >= 60){
            timeLeft.append(difference/60);
            timeLeft.append(" hr");
            if(difference % 60 > 0){
                timeLeft.append(" ");
                timeLeft.append(difference % 60);
                timeLeft.append("'");
            }
        }
        else{
            timeLeft.append(difference);
            timeLeft.append(" min");
        }
        return timeLeft.toString();
    }
}
