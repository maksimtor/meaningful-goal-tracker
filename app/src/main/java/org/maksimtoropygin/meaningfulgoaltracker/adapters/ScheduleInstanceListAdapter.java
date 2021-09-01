
package org.maksimtoropygin.meaningfulgoaltracker.adapters;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

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

import org.maksimtoropygin.meaningfulgoaltracker.R;
import org.maksimtoropygin.meaningfulgoaltracker.Receiver;
import org.maksimtoropygin.meaningfulgoaltracker.adapters.data.ScheduleTaskInstance;
import org.maksimtoropygin.meaningfulgoaltracker.ui.scheduler.SchedulerAddEdit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class ScheduleInstanceListAdapter extends RecyclerView.Adapter<ScheduleInstanceListAdapter.ContactHolder> {

    private ArrayList<ScheduleTaskInstance> scheduleList;
    private Context mContext;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final String currentUser = mAuth.getCurrentUser().getEmail();
    int done = 0;
    int missed = 0;

    public ScheduleInstanceListAdapter(ArrayList<ScheduleTaskInstance> scheduleList, Context context) {
        this.scheduleList = scheduleList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ContactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.recycler_schedule_day, parent, false);
        return new ContactHolder(view);
    }

    @Override
    public int getItemCount() {
        return scheduleList == null? 0: scheduleList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactHolder holder, final int position) {
        final ScheduleTaskInstance schedule = scheduleList.get(position);

        holder.setScheduleTitle(schedule.getTitle());
        holder.scheduleDesc.setText(schedule.getDesc());
        holder.scheduleGoal.setText(schedule.getGoal());
        holder.setScheduleTime(schedule.getStart().timeToString() + " - " + schedule.getFinish().timeToString());
        db.collection("goals").addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                assert queryDocumentSnapshots != null;
                for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                    if (document.getId().equals(schedule.getGoal())){
                        holder.setScheduleGoal(Objects.requireNonNull(document.getData().get("title")).toString());
                    }
                }
            }
        });

        final Calendar calendar = Calendar.getInstance();
        int today = (calendar.get(Calendar.DAY_OF_WEEK) + 6)%7 - 1;
        int difference = scheduleList.get(position).getDay() - today;
        calendar.add(Calendar.DAY_OF_MONTH, difference);
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        db.collection("scheduleActivityEvents")
                .whereEqualTo("scheduleId", scheduleList.get(position).getScheduleTaskId())
                .whereEqualTo("day", scheduleList.get(position).getDay())
                .whereEqualTo("start", scheduleList.get(position).getStart().timeToString())
                .whereEqualTo("finish", scheduleList.get(position).getFinish().timeToString())
                .whereEqualTo("date", dateFormat.format(calendar.getTime()))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d("Hiii", dateFormat.format(calendar.getTime()));
                        String status = "undefined";
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                status = Objects.requireNonNull(document.get("status")).toString();
                                status = status.substring(0,1).toUpperCase()+status.substring(1);
                                Log.d("Hii", status);
                                holder.scheduleMissed.setVisibility(View.INVISIBLE);
                                holder.scheduleDone.setVisibility(View.INVISIBLE);
                                holder.scheduleStatus.setVisibility(View.VISIBLE);
                                holder.scheduleStatus.setText(status);
                            }
                            holder.main_recycler_todo.setVisibility(View.VISIBLE);
                        }}
                });

        holder.scheduleDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int eventId = scheduleList.get(position).getEventId();
                Intent intent = new Intent(mContext, Receiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, eventId, intent, 0);
                AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
                final DocumentReference scheduleToDelete = FirebaseFirestore.getInstance().collection("scheduleActivity").document(scheduleList.get(position).getScheduleTaskId());
                scheduleToDelete.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            List<Long> daysOfTheWeek = (List<Long>) document.get("daysOfTheWeek");
                            List<String> startTimes = (List<String>) document.get("startTimes");
                            List<String> finishTimes = (List<String>) document.get("finishTimes");
                            assert daysOfTheWeek != null;
                            for (int i = 0; i<daysOfTheWeek.size(); i++){
                                if (daysOfTheWeek.get(i) == scheduleList.get(position).getDay()){
                                    daysOfTheWeek.remove(i);
                                    assert startTimes != null;
                                    startTimes.remove(i);
                                    assert finishTimes != null;
                                    finishTimes.remove(i);
                                    break;
                                }
                            }
                            if (daysOfTheWeek.size() > 0) {
                                scheduleToDelete.update(
                                        "daysOfTheWeek", daysOfTheWeek,
                                        "startTimes", startTimes,
                                        "finishTimes", finishTimes
                                ).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("FirebaseTest2", "DocumentSnapshot successfully deleted!");
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("FirebaseTest2", "Error deleted document", e);
                                            }
                                        });
                            }
                            else {
                                scheduleToDelete.delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("FirebaseTest2", "DocumentSnapshot successfully deleted!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("FirebaseTest2", "Error deleted document", e);
                                            }
                                        });
                            }

                        }
                    }
                });

            }
        });
        holder.scheduleEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SchedulerAddEdit.class);
                intent.putExtra("action", "edit");
                intent.putExtra("id", scheduleList.get(position).getScheduleTaskId());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);
            }
        });
        holder.scheduleDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Map<String, Object> scheduleTrack = new HashMap<>();
                scheduleTrack.put("scheduleId", scheduleList.get(position).getScheduleTaskId());
                scheduleTrack.put("day", scheduleList.get(position).getDay());
                scheduleTrack.put("start", scheduleList.get(position).getStart().timeToString());
                scheduleTrack.put("finish", scheduleList.get(position).getFinish().timeToString());
                scheduleTrack.put("status", "done");
                scheduleTrack.put("date", dateFormat.format(calendar.getTime()));
                scheduleTrack.put("userId", currentUser);
                scheduleTrack.put("goalId", scheduleList.get(position).getGoal());

                db.collection("scheduleActivityEvents")
                        .add(scheduleTrack)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.w("Hi", "Nice");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("Hi", "Error adding document", e);
                            }
                        });
                final DocumentReference reloadEntry = FirebaseFirestore.getInstance().collection("scheduleActivity").document("reload");
                reloadEntry.update("reload", true);
            }
        });
        holder.scheduleMissed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Map<String, Object> scheduleTrack = new HashMap<>();
                scheduleTrack.put("scheduleId", scheduleList.get(position).getScheduleTaskId());
                scheduleTrack.put("day", scheduleList.get(position).getDay());
                scheduleTrack.put("start", scheduleList.get(position).getStart().timeToString());
                scheduleTrack.put("finish", scheduleList.get(position).getFinish().timeToString());
                scheduleTrack.put("status", "missed");
                scheduleTrack.put("date", dateFormat.format(calendar.getTime()));
                scheduleTrack.put("userId", currentUser);
                scheduleTrack.put("goalId", scheduleList.get(position).getGoal());

                db.collection("scheduleActivityEvents")
                        .add(scheduleTrack)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.w("Hi", "Nice");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("Hi", "Error adding document", e);
                            }
                        });
                final DocumentReference reloadEntry = FirebaseFirestore.getInstance().collection("scheduleActivity").document("reload");
                reloadEntry.update("reload", true);
            }
        });
        holder.expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.forExpand.getVisibility() == View.GONE){
                    holder.forExpand.setVisibility(View.VISIBLE);
                    holder.expandButton.setImageDrawable(mContext.getDrawable(R.drawable.expand_less));
                }
                else {
                    holder.forExpand.setVisibility(View.GONE);
                    holder.expandButton.setImageDrawable(mContext.getDrawable(R.drawable.expand_more));
                }
            }
        });
        db.collection("scheduleActivityEvents").addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                assert queryDocumentSnapshots != null;
                done = 0;
                missed = 0;
                for (final QueryDocumentSnapshot document: queryDocumentSnapshots){
                    if (document.getData().get("scheduleId") != null && document.getData().get("userId") != null && Objects.requireNonNull(document.getData().get("userId")).toString().equals(currentUser) && Objects.requireNonNull(document.getData().get("scheduleId")).toString().equals(schedule.getScheduleTaskId())){
                        if (document.getData().get("status").equals("done"))
                            done++;
                        else if (document.getData().get("status").equals("missed"))
                            missed++;
                        float prodRate = ((float) done/ (float) (done+missed))*100;
                        holder.scheduleRate.setText("Productivity rate is " + (int) prodRate + "%" + ". Done " + done + ", missed " + missed);

                    }
                }
            }
        });
    }

    public static class ContactHolder extends RecyclerView.ViewHolder {

        private TextView scheduleTitle;
        private TextView scheduleTime;
        private TextView scheduleStatus;
        private TextView scheduleDesc;
        private TextView scheduleGoal;
        private TextView scheduleRate;
        private ImageButton scheduleDelete;
        private ImageButton scheduleEdit;
        private ImageButton scheduleDone;
        private ImageButton scheduleMissed;
        private CardView main_recycler_todo;
        private ImageButton expandButton;
        private LinearLayout forExpand;


        public ContactHolder(View itemView) {
            super(itemView);

            scheduleTitle = itemView.findViewById(R.id.rec_schedule_title);
            scheduleTime = itemView.findViewById(R.id.rec_schedule_time);
            scheduleDelete = itemView.findViewById(R.id.rec_schedule_delete);
            scheduleEdit = itemView.findViewById(R.id.rec_schedule_edit);
            scheduleDone = itemView.findViewById(R.id.rec_schedule_done);
            scheduleMissed = itemView.findViewById(R.id.rec_schedule_missed);
            scheduleStatus = itemView.findViewById(R.id.rec_schedule_status);
            main_recycler_todo = itemView.findViewById(R.id.main_recycler_scheduler);
            scheduleDesc = itemView.findViewById(R.id.rec_schedule_desc);
            scheduleGoal = itemView.findViewById(R.id.rec_schedule_goal);
            expandButton = itemView.findViewById(R.id.rec_schedule_expand);
            forExpand = itemView.findViewById(R.id.schedule_for_expand);
            scheduleRate = itemView.findViewById(R.id.rec_schedule_rate);
        }

        public void setScheduleTitle(String title) {
            scheduleTitle.setText(title);
        }

        public void setScheduleTime(String time) {
            scheduleTime.setText(time);
        }

        public void setScheduleGoal(String goal) {
            scheduleGoal.setText(goal);
        }
    }
}