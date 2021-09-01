package org.maksimtoropygin.meaningfulgoaltracker.ui.scheduler;

        import android.content.Intent;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.ImageButton;
        import android.widget.Spinner;

        import androidx.annotation.NonNull;
        import androidx.annotation.Nullable;
        import androidx.cardview.widget.CardView;
        import androidx.fragment.app.Fragment;
        import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

        import com.google.android.material.floatingactionbutton.FloatingActionButton;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.firestore.EventListener;
        import com.google.firebase.firestore.FirebaseFirestore;
        import com.google.firebase.firestore.FirebaseFirestoreException;
        import com.google.firebase.firestore.MetadataChanges;
        import com.google.firebase.firestore.QueryDocumentSnapshot;
        import com.google.firebase.firestore.QuerySnapshot;

        import org.maksimtoropygin.meaningfulgoaltracker.R;
        import org.maksimtoropygin.meaningfulgoaltracker.adapters.ScheduleInstanceListAdapter;
        import org.maksimtoropygin.meaningfulgoaltracker.adapters.data.ScheduleTaskInstance;
        import org.maksimtoropygin.meaningfulgoaltracker.adapters.data.Time;

        import java.util.ArrayList;
        import java.util.Calendar;
        import java.util.Collections;
        import java.util.List;
        import java.util.Objects;

public class SchedulerFragment extends Fragment {

    private ScheduleInstanceListAdapter schedulePastListAdapter;
    private ScheduleInstanceListAdapter scheduleUpcomingListAdapter;
    private ArrayList<ScheduleTaskInstance> schedulePastList = new ArrayList<>();
    private ArrayList<ScheduleTaskInstance> scheduleUpcomingList = new ArrayList<>();
    private RecyclerView schedulePastRecycler;
    private RecyclerView scheduleUpcomingRecycler;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final String currentUser = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();

    private Spinner day;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_scheduler, container, false);

        FloatingActionButton addEditFab = root.findViewById(R.id.fab_new_schedule);
        addEditFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SchedulerAddEdit.class);
                intent.putExtra("action", "add");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        schedulePastRecycler = root.findViewById(R.id.recycler_list_schedule_past);
        scheduleUpcomingRecycler = root.findViewById(R.id.recycler_list_schedule_upcoming);
        LinearLayoutManager layoutManagerPast = new LinearLayoutManager(getContext());
        LinearLayoutManager layoutManagerUpcoming = new LinearLayoutManager(getContext());
        schedulePastRecycler.setLayoutManager(layoutManagerPast);
        scheduleUpcomingRecycler.setLayoutManager(layoutManagerUpcoming);

        String[] arraySpinner = new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        day = root.findViewById(R.id.scheduler_day);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()),
                android.R.layout.simple_spinner_item, arraySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        day.setAdapter(adapter);
        Calendar calendar = Calendar.getInstance();
        final int today = (calendar.get(Calendar.DAY_OF_WEEK) + 6)%7 - 1;
        day.setSelection(today);
        day.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        db.collection("scheduleActivity").addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                Calendar calendar = Calendar.getInstance();
                                schedulePastList = new ArrayList<>();
                                scheduleUpcomingList = new ArrayList<>();
                                assert queryDocumentSnapshots != null;
                                for (final QueryDocumentSnapshot document: queryDocumentSnapshots){
                                    if (document.getData().get("userId") != null && Objects.equals(document.getData().get("userId"), currentUser)){
                                        final List<Long> daysOfTheWeek = (List<Long>) document.get("daysOfTheWeek");
                                        List<String> startTimes = (List<String>) document.get("startTimes");
                                        List<String> finishTimes = (List<String>) document.get("finishTimes");
                                        List<Long> eventIds = (List<Long>) document.get("eventIds");
                                        int today = (calendar.get(Calendar.DAY_OF_WEEK) + 6)%7 - 1;
                                        assert daysOfTheWeek != null;
                                        for (int i = 0; i<daysOfTheWeek.size(); i++){
                                            if (daysOfTheWeek.get(i) == day.getSelectedItemPosition()){
                                                final Time startTime = new Time(0,0);
                                                assert startTimes != null;
                                                startTime.setTimeFromString(startTimes.get(i));
                                                final Time finishTime = new Time(0,0);
                                                assert finishTimes != null;
                                                finishTime.setTimeFromString(finishTimes.get(i));
                                                final int eventId = eventIds.get(i).intValue();

                                                if (daysOfTheWeek.get(i)>today){
                                                    scheduleUpcomingList.add(new ScheduleTaskInstance(document.getId(), Objects.requireNonNull(document.getData().get("title")).toString(), startTime, finishTime, day.getSelectedItemPosition(), document.getData().get("description").toString(), document.getData().get("goalId").toString(), eventId));
                                                }
                                                else if (daysOfTheWeek.get(i)<today){
                                                    schedulePastList.add(new ScheduleTaskInstance(document.getId(), Objects.requireNonNull(document.getData().get("title")).toString(), startTime, finishTime, day.getSelectedItemPosition(), document.getData().get("description").toString(), document.getData().get("goalId").toString(), eventId));
                                                }
                                                else {
                                                    final Time currentTime = new Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
                                                    if (currentTime.greaterThan(finishTime))
                                                        schedulePastList.add(new ScheduleTaskInstance(document.getId(), Objects.requireNonNull(document.getData().get("title")).toString(), startTime, finishTime, day.getSelectedItemPosition(), document.getData().get("description").toString(), document.getData().get("goalId").toString(), eventId));
                                                    else
                                                        scheduleUpcomingList.add(new ScheduleTaskInstance(document.getId(), Objects.requireNonNull(document.getData().get("title")).toString(), startTime, finishTime, day.getSelectedItemPosition(), document.getData().get("description").toString(), document.getData().get("goalId").toString(), eventId));
                                                }
                                                Log.d("Sorting", "Start to sort");
                                                Collections.sort(scheduleUpcomingList);
                                                Collections.sort(schedulePastList);
                                            }
                                        }
                                    }
                                }
                                if (mAuth.getCurrentUser() != null) {
                                    Collections.sort(scheduleUpcomingList);
                                    Collections.sort(schedulePastList);
                                    schedulePastListAdapter = new ScheduleInstanceListAdapter(schedulePastList, getContext());
                                    scheduleUpcomingListAdapter = new ScheduleInstanceListAdapter(scheduleUpcomingList, getContext());
                                    schedulePastRecycler.setAdapter(schedulePastListAdapter);
                                    scheduleUpcomingRecycler.setAdapter(scheduleUpcomingListAdapter);
                                }
                            }
                        });
                    }
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
        ImageButton helperButton = getActivity().findViewById(R.id.action_help);
        final CardView helper = root.findViewById(R.id.scheduler_helper);
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