package org.maksimtoropygin.meaningfulgoaltracker.ui.scheduler;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.maksimtoropygin.meaningfulgoaltracker.MainActivity;
import org.maksimtoropygin.meaningfulgoaltracker.R;
import org.maksimtoropygin.meaningfulgoaltracker.Receiver;
import org.maksimtoropygin.meaningfulgoaltracker.adapters.DaytimeListAdapter;
import org.maksimtoropygin.meaningfulgoaltracker.adapters.data.Daytime;
import org.maksimtoropygin.meaningfulgoaltracker.adapters.data.Goal;
import org.maksimtoropygin.meaningfulgoaltracker.adapters.data.Time;
import org.maksimtoropygin.meaningfulgoaltracker.ui.goal_keeper.GoalKeeperAddEdit;
import org.maksimtoropygin.meaningfulgoaltracker.ui.value_explorer.ValueExplorerAddEdit;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class SchedulerAddEdit extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final String currentUser = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();

    private ArrayList<Goal> goalList = new ArrayList<>();
    private String[] arraySpinner;

    private ArrayList<Daytime> daytimeList = new ArrayList<>();
    private RecyclerView daytimeRecycler;
    private DaytimeListAdapter daytimeListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_edit_schedule);

        final EditText title = findViewById(R.id.add_edit_schedule_title);
        final EditText description = findViewById(R.id.add_edit_schedule_desc);
        final Spinner goal = findViewById(R.id.add_edit_schedule_goal);
        final Button addOrEdit = findViewById(R.id.add_edit_schedule_button);
        daytimeRecycler = findViewById(R.id.recycler_schedule_daytime);
        LinearLayoutManager layoutManagerDaytime = new LinearLayoutManager(this);
        daytimeRecycler.setLayoutManager(layoutManagerDaytime);
        daytimeListAdapter = new DaytimeListAdapter(daytimeList);
        daytimeRecycler.setAdapter(daytimeListAdapter);
        Button addDay = findViewById(R.id.add_edit_schedule_new_day);
        addDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                daytimeList.add(new Daytime("Monday", new Time(0,0), new Time(0,0)));
                daytimeListAdapter = new DaytimeListAdapter(daytimeList);
                daytimeRecycler.setAdapter(daytimeListAdapter);
            }
        });

        db.collection("goals").addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                goalList = new ArrayList<>();
                assert queryDocumentSnapshots != null;
                for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                    if (Objects.equals(document.getData().get("userId"), currentUser)){
                        goalList.add(new Goal(document.getId(), Objects.requireNonNull(document.getData().get("valueId")).toString(), Objects.requireNonNull(document.getData().get("title")).toString(), Objects.requireNonNull(document.getData().get("description")).toString()));
                    }
                }
                arraySpinner = new String[goalList.size()+1];
                for (int i = 0; i<goalList.size(); i++) {
                    arraySpinner[i] = goalList.get(i).getTitle();
                }
                arraySpinner[goalList.size()] = "Create a new goal";
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                        android.R.layout.simple_spinner_item, arraySpinner);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                goal.setAdapter(adapter);
                goal.setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                                if (pos==goalList.size()){
                                    goal.setSelection(0);
                                    Intent intent = new Intent(parent.getContext(), GoalKeeperAddEdit.class);
                                    intent.putExtra("action", "add");
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }
                            }
                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });
                Intent intent = getIntent();
                String action = intent.getStringExtra("action");
                assert action != null;
                if (action.equals("add")){
                    setTitle("Add a new schedule task");
                    daytimeList = new ArrayList<>();
                    daytimeList.add(new Daytime("Monday", new Time(0,0), new Time(0,0)));
                    daytimeListAdapter = new DaytimeListAdapter(daytimeList);
                    daytimeRecycler.setAdapter(daytimeListAdapter);
                    addOrEdit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String textTitle = title.getText().toString();
                            String textDesc = description.getText().toString();
                            String textGoalId = goalList.get(goal.getSelectedItemPosition()).getId();
                            // making arrays
                            ArrayList<Integer> daysOfTheWeek = new ArrayList<>();
                            ArrayList<String> startTimes = new ArrayList<>();
                            ArrayList<String> finishTimes = new ArrayList<>();
                            ArrayList<Integer> eventIds = new ArrayList<>();
                            for (int i = 0; i<daytimeList.size(); i++){
                                // these values are not updating, they need to be updated in recycler
                                daysOfTheWeek.add(daytimeList.get(i).dayToInt());
                                startTimes.add(daytimeList.get(i).getStart().timeToString());
                                finishTimes.add(daytimeList.get(i).getFinish().timeToString());


                                // save the id each alarm in the db
                                Random rand = new Random();
                                int randInt = rand.nextInt(1000000);
                                eventIds.add(randInt);

                                // using the id, create an alarm
                                int dayOfAlarm = daytimeList.get(i).dayToInt();
                                int hourOfAlarm = daytimeList.get(i).getStart().getHour();
                                int minuteOfAlarm = daytimeList.get(i).getStart().getMinute();

                                Calendar calendar = Calendar.getInstance();
                                int today = (calendar.get(Calendar.DAY_OF_WEEK) + 6)%7 - 1;
                                int dayDiff = dayOfAlarm - today;
                                if (dayDiff<0) {
                                    dayDiff += 6;
                                }
                                Log.d("AlarmCheck", "Id should be " + randInt);
                                if (today == dayOfAlarm){
                                    if (hourOfAlarm>calendar.get(Calendar.HOUR_OF_DAY)){
                                        Log.d("AlarmCheck", "1");
                                    }
                                    else if (hourOfAlarm == calendar.get(Calendar.HOUR_OF_DAY)) {
                                        Log.d("AlarmCheck", "2");
                                        if (minuteOfAlarm>calendar.get(Calendar.MINUTE)){

                                        }
                                        else {
                                            dayDiff = 7;
                                        }
                                    }
                                    else {
                                        Log.d("AlarmCheck", "3");
                                        dayDiff = 7;
                                    }
                                }
                                calendar.add(Calendar.DATE, dayDiff);
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfAlarm);
                                calendar.set(Calendar.MINUTE, minuteOfAlarm);
                                calendar.set(Calendar.SECOND, 0);
                                Log.d("AlarmCheck", calendar.getTime() + "");


                                Intent intent = new Intent(getApplicationContext(), Receiver.class);
                                intent.putExtra("id", randInt);
                                intent.putExtra("title", textTitle);
                                intent.putExtra("desc", textDesc);
                                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), randInt, intent, 0);
                                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                                alarmManager.setRepeating(alarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmManager.INTERVAL_DAY*7, pendingIntent);
                            }
                            Map<String, Object> schedule = new HashMap<>();
                            schedule.put("title", textTitle);
                            schedule.put("goalId", textGoalId);
                            schedule.put("userId", currentUser);
                            schedule.put("description", textDesc);
                            schedule.put("daysOfTheWeek", daysOfTheWeek);
                            schedule.put("startTimes", startTimes);
                            schedule.put("finishTimes", finishTimes);
                            schedule.put("eventIds", eventIds);

                            db.collection("scheduleActivity")
                                    .add(schedule)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w("Hi", "Error adding document", e);
                                        }
                                    });

                            //

                        }
                    });
                }
                else if (action.equals("edit")){
                    setTitle("Edit a schedule task");
                    addOrEdit.setText("Edit");
                    final String id = intent.getStringExtra("id");

                    assert id != null;
                    DocumentReference scheduleDoc = db.collection("scheduleActivity").document(id);
                    scheduleDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                assert document != null;
                                if (document.exists()) {
                                    List<Long> daysOfTheWeek = (List<Long>) document.get("daysOfTheWeek");
                                    List<String> startTimes = (List<String>) document.get("startTimes");
                                    List<String> finishTimes = (List<String>) document.get("finishTimes");
                                    final List<Long> eventIds = (List<Long>) document.get("eventIds");

                                    int spinnerValueIndex = 0;

                                    for (int i = 0; i<goalList.size(); i++) {
                                        if (goalList.get(i).getId().equals(Objects.requireNonNull(Objects.requireNonNull(document.getData()).get("goalId")).toString())) {
                                            spinnerValueIndex = i;
                                        }
                                    }

                                    final DocumentReference scheduleDocument = db.collection("scheduleActivity").document(id);

                                    title.setText(Objects.requireNonNull(Objects.requireNonNull(document.getData()).get("title")).toString());
                                    description.setText(Objects.requireNonNull(document.getData().get("description")).toString());
                                    goal.setSelection(spinnerValueIndex);

                                    assert daysOfTheWeek != null;
                                    for (int i = 0; i<daysOfTheWeek.size(); i++) {
                                        Time startTime = new Time(0,0);
                                        assert startTimes != null;
                                        startTime.setTimeFromString(startTimes.get(i));
                                        Time finishTime = new Time(0,0);
                                        assert finishTimes != null;
                                        finishTime.setTimeFromString(finishTimes.get(i));
                                        long l=daysOfTheWeek.get(i);
                                        int j=(int)l;
                                        daytimeList.add(new Daytime(j, startTime, finishTime));
                                    }
                                    daytimeListAdapter = new DaytimeListAdapter(daytimeList);
                                    daytimeRecycler.setAdapter(daytimeListAdapter);


                                    addOrEdit.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            // Delete reminders
                                            for (int i=0; i<eventIds.size(); i++){
                                                int eventId = eventIds.get(i).intValue();
                                                Intent intent = new Intent(getApplicationContext(), Receiver.class);
                                                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), eventId, intent, 0);
                                                AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                                                alarmManager.cancel(pendingIntent);
                                            }
                                            String textTitle = title.getText().toString();
                                            String textDesk = description.getText().toString();
                                            String textGoalId = goalList.get(goal.getSelectedItemPosition()).getId();
                                            ArrayList<Integer> daysOfTheWeek = new ArrayList<>();
                                            ArrayList<String> startTimes = new ArrayList<>();
                                            ArrayList<String> finishTimes = new ArrayList<>();
                                            ArrayList<Integer> eventIds = new ArrayList<>();
                                            for (int i = 0; i<daytimeList.size(); i++){
                                                // these values are not updating, they need to be updated in recycler
                                                daysOfTheWeek.add(daytimeList.get(i).dayToInt());
                                                startTimes.add(daytimeList.get(i).getStart().timeToString());
                                                finishTimes.add(daytimeList.get(i).getFinish().timeToString());

                                                // add reminders
                                                Random rand = new Random();
                                                int randInt = rand.nextInt(1000000);
                                                eventIds.add(randInt);

                                                // using the id, create an alarm
                                                int dayOfAlarm = daytimeList.get(i).dayToInt();
                                                int hourOfAlarm = daytimeList.get(i).getStart().getHour();
                                                int minuteOfAlarm = daytimeList.get(i).getStart().getMinute();

                                                Calendar calendar = Calendar.getInstance();
                                                int today = (calendar.get(Calendar.DAY_OF_WEEK) + 6)%7 - 1;
                                                int dayDiff = dayOfAlarm - today;
                                                if (dayDiff<0) {
                                                    dayDiff += 6;
                                                }
                                                Log.d("AlarmCheck", "Id should be " + randInt);
                                                if (today == dayOfAlarm){
                                                    if (hourOfAlarm>calendar.get(Calendar.HOUR_OF_DAY)){
                                                        Log.d("AlarmCheck", "1");
                                                    }
                                                    else if (hourOfAlarm == calendar.get(Calendar.HOUR_OF_DAY)) {
                                                        Log.d("AlarmCheck", "2");
                                                        if (minuteOfAlarm>calendar.get(Calendar.MINUTE)){

                                                        }
                                                        else {
                                                            dayDiff = 7;
                                                        }
                                                    }
                                                    else {
                                                        Log.d("AlarmCheck", "3");
                                                        dayDiff = 7;
                                                    }
                                                }
                                                calendar.add(Calendar.DATE, dayDiff);
                                                calendar.set(Calendar.HOUR_OF_DAY, hourOfAlarm);
                                                calendar.set(Calendar.MINUTE, minuteOfAlarm);
                                                calendar.set(Calendar.SECOND, 0);
                                                Log.d("AlarmCheck", calendar.getTime() + "");


                                                Intent intent = new Intent(getApplicationContext(), Receiver.class);
                                                intent.putExtra("id", randInt);
                                                intent.putExtra("title",textTitle);
                                                intent.putExtra("desc", textDesk);
                                                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), randInt, intent, 0);
                                                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                                                alarmManager.setRepeating(alarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmManager.INTERVAL_DAY*7, pendingIntent);
                                            }
                                            scheduleDocument.update(
                                                    "title", textTitle,
                                                    "description", textDesk,
                                                    "goalId", textGoalId,
                                                    "daysOfTheWeek", daysOfTheWeek,
                                                    "startTimes", startTimes,
                                                    "finishTimes", finishTimes,
                                                    "eventIds", eventIds
                                            );
                                            finish();
                                        }
                                    });
                                } else {
                                    Log.d("TAG", "No such document");
                                }
                            } else {
                                Log.d("TAG", "get failed with ", task.getException());
                            }
                        }
                    });
                }
            }
        });
    }
}