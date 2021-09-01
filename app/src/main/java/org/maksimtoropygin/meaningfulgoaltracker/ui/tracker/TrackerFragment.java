package org.maksimtoropygin.meaningfulgoaltracker.ui.tracker;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.maksimtoropygin.meaningfulgoaltracker.R;
import org.maksimtoropygin.meaningfulgoaltracker.adapters.data.ScheduleEvent;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class TrackerFragment extends Fragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final String currentUser = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
    private ArrayList<ScheduleEvent> listOfScheduleEvents = new ArrayList<>();
    PieChart pieChart;
    PieData pieData;
    PieDataSet pieDataSet;
    ArrayList<PieEntry> pieEntries;

    PieChart pieChartMonth;
    PieData pieDataMonth;
    PieDataSet pieDataSetMonth;
    ArrayList<PieEntry> pieEntriesMonth;

    PieChart pieChartYear;
    PieData pieDataYear;
    PieDataSet pieDataSetYear;
    ArrayList<PieEntry> pieEntriesYear;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_tracker, container, false);
        pieChart = root.findViewById(R.id.tracker_pie_week);
        pieChartMonth = root.findViewById(R.id.tracker_pie_month);
        pieChartYear = root.findViewById(R.id.tracker_pie_year);

        db.collection("scheduleActivityEvents").addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                listOfScheduleEvents = new ArrayList<>();
                assert queryDocumentSnapshots != null;
                for (final QueryDocumentSnapshot document: queryDocumentSnapshots){
                    if (document.getData().get("scheduleId") != null && document.getData().get("userId") != null && Objects.requireNonNull(document.getData().get("userId")).toString().equals(currentUser)){
                        String stringDate = Objects.requireNonNull(document.getData().get("date")).toString();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = null;
                        try {
                            date = dateFormat.parse(stringDate);
                            listOfScheduleEvents.add(new ScheduleEvent(date, Objects.requireNonNull(document.getData().get("status")).toString()));

                        } catch (ParseException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                Calendar calendarWeek = Calendar.getInstance(), calendarMonth = Calendar.getInstance(), calendarYear = Calendar.getInstance();

                calendarWeek.add(Calendar.DAY_OF_MONTH, -7);
                Date weekAgo = calendarWeek.getTime();

                calendarMonth.add(Calendar.MONTH, -1);
                Date monthAgo = calendarMonth.getTime();

                calendarYear.add(Calendar.YEAR, -1);
                Date yearAgo = calendarYear.getTime();

                int weekDone = 0, weekMissed = 0, monthDone = 0, monthMissed = 0, yearDone = 0, yearMissed = 0;
                for (int i = 0; i<listOfScheduleEvents.size(); i++) {
                    if (listOfScheduleEvents.get(i).getDate().after(weekAgo)){
                        if (listOfScheduleEvents.get(i).getStatus().equals("done")){
                            weekDone++;
                            monthDone++;
                            yearDone++;
                        }
                        else {
                            weekMissed++;
                            monthMissed++;
                            yearMissed++;
                        }
                    }
                    else if (listOfScheduleEvents.get(i).getDate().after(monthAgo)){
                        if (listOfScheduleEvents.get(i).getStatus().equals("done")){
                            monthDone++;
                            yearDone++;
                        }
                        else {
                            monthMissed++;
                            yearMissed++;
                        }
                    }
                    else if (listOfScheduleEvents.get(i).getDate().after(yearAgo)){
                        if (listOfScheduleEvents.get(i).getStatus().equals("done")){
                            yearDone++;
                        }
                        else {
                            yearMissed++;
                        }
                    }
                }
                if (weekDone == 0 && weekMissed ==0){
                    pieChart.setVisibility(View.GONE);
                    TextView weekError = root.findViewById(R.id.tracker_schedule_week_error);
                    weekError.setVisibility(View.VISIBLE);
                }
                else {
                    pieEntries = new ArrayList<>();
                    pieEntries.add(new PieEntry(weekDone, "Done"));
                    pieEntries.add(new PieEntry(weekMissed, "Missed"));
                    pieDataSet = new PieDataSet(pieEntries, "");
                    pieDataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
                    pieDataSet.setSliceSpace(2f);
                    pieDataSet.setValueTextColor(Color.BLACK);
                    pieDataSet.setValueTextSize(15f);
                    pieDataSet.setSliceSpace(5f);
                    pieDataSet.setValueLineColor(Color.BLACK);
                    pieData = new PieData(pieDataSet);
                    pieChart.setData(pieData);
                    pieChart.setActivated(true);
                    pieChart.setEntryLabelColor(Color.BLACK);
                    pieChart.setDrawMarkers(false);
                    pieChart.getDescription().setEnabled(false);
                }

                if (monthDone == 0 && monthMissed ==0){
                    pieChartMonth.setVisibility(View.GONE);
                    TextView monthError = root.findViewById(R.id.tracker_schedule_month_error);
                    monthError.setVisibility(View.VISIBLE);
                }
                else {
                    pieEntriesMonth = new ArrayList<>();
                    pieEntriesMonth.add(new PieEntry(monthDone, "Done"));
                    pieEntriesMonth.add(new PieEntry(monthMissed, "Missed"));
                    pieDataSetMonth = new PieDataSet(pieEntriesMonth, "");
                    pieDataSetMonth.setColors(ColorTemplate.VORDIPLOM_COLORS);
                    pieDataSetMonth.setSliceSpace(2f);
                    pieDataSetMonth.setValueTextColor(Color.BLACK);
                    pieDataSetMonth.setValueTextSize(15f);
                    pieDataSetMonth.setSliceSpace(5f);
                    pieDataSetMonth.setValueLineColor(Color.BLACK);
                    pieDataMonth = new PieData(pieDataSetMonth);
                    pieChartMonth.setData(pieDataMonth);
                    pieChartMonth.setActivated(true);
                    pieChartMonth.setEntryLabelColor(Color.BLACK);
                    pieChartMonth.setDrawMarkers(false);
                    pieChartMonth.getDescription().setEnabled(false);
                }

                if (yearDone == 0 && yearMissed ==0){
                    pieChartYear.setVisibility(View.GONE);
                    TextView yearError = root.findViewById(R.id.tracker_schedule_year_error);
                    yearError.setVisibility(View.VISIBLE);
                }
                pieEntriesYear = new ArrayList<>();
                pieEntriesYear.add(new PieEntry(yearDone, "Done"));
                pieEntriesYear.add(new PieEntry(yearMissed, "Missed"));
                pieDataSetYear = new PieDataSet(pieEntriesYear, "");
                pieDataSetYear.setColors(ColorTemplate.VORDIPLOM_COLORS);
                pieDataSetYear.setSliceSpace(2f);
                pieDataSetYear.setValueTextColor(Color.BLACK);
                pieDataSetYear.setValueTextSize(15f);
                pieDataSetYear.setSliceSpace(5f);
                pieDataSetYear.setValueLineColor(Color.BLACK);
                pieDataYear = new PieData(pieDataSetYear);
                pieChartYear.setData(pieDataYear);
                pieChartYear.setActivated(true);
                pieChartYear.setEntryLabelColor(Color.BLACK);
                pieChartYear.setDrawMarkers(false);
                pieChartYear.getDescription().setEnabled(false);
            }
        });
        ImageButton helperButton = getActivity().findViewById(R.id.action_help);
        final CardView helper = root.findViewById(R.id.tracker_helper);
        helperButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (helper.getVisibility() == View.GONE)
                    helper.setVisibility(View.VISIBLE);
                else
                    helper.setVisibility(View.GONE);
            }
        });
        return root;
    }
}