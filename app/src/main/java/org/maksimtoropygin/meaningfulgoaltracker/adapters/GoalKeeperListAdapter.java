package org.maksimtoropygin.meaningfulgoaltracker.adapters;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
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
import org.maksimtoropygin.meaningfulgoaltracker.adapters.data.Goal;
import org.maksimtoropygin.meaningfulgoaltracker.adapters.data.ScheduleEvent;
import org.maksimtoropygin.meaningfulgoaltracker.ui.goal_keeper.GoalKeeperAddEdit;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;


public class GoalKeeperListAdapter extends RecyclerView.Adapter<GoalKeeperListAdapter.ContactHolder> {

    private ArrayList<Goal> goalList;
    private Context mContext;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final String currentUser = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
    int done = 0;
    int missed = 0;

    public GoalKeeperListAdapter(ArrayList<Goal> goalList, Context context) {
        this.goalList = goalList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ContactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.recycler_goal_keeper, parent, false);
        return new ContactHolder(view);
    }

    @Override
    public int getItemCount() {
        return goalList == null? 0: goalList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactHolder holder, final int position) {
        final Goal goal = goalList.get(position);

        holder.setGoalTitle(goal.getTitle());
        holder.setGoalDescription(goal.getDescription());
        db.collection("values").addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                assert queryDocumentSnapshots != null;
                for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                    if (document.getId().equals(goal.getValueId())){
                        holder.setGoalValue(Objects.requireNonNull(document.getData().get("title")).toString());
                    }
                }
            }
        });

        holder.valueDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference goalToDelete = FirebaseFirestore.getInstance().collection("goals").document(goalList.get(position).getId());
                Log.d("FirebaseTest2", goalList.get(position).getId());
                goalToDelete
                        .delete()
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

                // Delete schedules
                db.collection("scheduleActivity").addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                assert queryDocumentSnapshots != null;
                                for (final QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    if (document.getData().get("goalId") != null && document.getData().get("goalId").toString().equals(goalList.get(position).getId())){
                                        DocumentReference scheduleToDelete = FirebaseFirestore.getInstance().collection("scheduleActivity").document(document.getId());
                                        scheduleToDelete
                                                .delete()
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

                // Delete todo items
                db.collection("todos").addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        assert queryDocumentSnapshots != null;
                        for (final QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            if (document.getData().get("goalId") != null && document.getData().get("goalId").toString().equals(goalList.get(position).getId())){
                                DocumentReference todoToDelete = FirebaseFirestore.getInstance().collection("todos").document(document.getId());
                                todoToDelete
                                        .delete()
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
        holder.valueEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, GoalKeeperAddEdit.class);
                intent.putExtra("action", "edit");
                intent.putExtra("title", goalList.get(position).getTitle());
                intent.putExtra("description", goalList.get(position).getDescription());
                intent.putExtra("valueId", goalList.get(position).getValueId());
                intent.putExtra("id", goalList.get(position).getId());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);
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
                    if (document.getData().get("scheduleId") != null && document.getData().get("goalId") != null && document.getData().get("userId") != null && Objects.requireNonNull(document.getData().get("userId")).toString().equals(currentUser) && document.getData().get("goalId").toString().equals(goalList.get(position).getId())){
                        if (document.getData().get("status").equals("done"))
                            done++;
                        else if (document.getData().get("status").equals("missed"))
                            missed++;
                        float prodRate = ((float) done/ (float) (done+missed))*100;
                        holder.goalRate.setText("Productivity rate is " + (int) prodRate + "%" + ". Done " + done + ", missed " + missed);
                    }
                }
            }
        });

    }

    public static class ContactHolder extends RecyclerView.ViewHolder {

        private TextView goalTitle;
        private TextView goalDescription;
        private TextView goalValue;
        private TextView goalRate;
        private ImageButton valueDelete;
        private ImageButton valueEdit;
        private ImageButton expandButton;
        private LinearLayout forExpand;

        public ContactHolder(View itemView) {
            super(itemView);

            goalTitle = itemView.findViewById(R.id.rec_goal_title);
            goalDescription = itemView.findViewById(R.id.rec_goal_description);
            goalValue = itemView.findViewById(R.id.rec_goal_value);
            valueDelete = itemView.findViewById(R.id.rec_goal_delete);
            valueEdit = itemView.findViewById(R.id.rec_goal_edit);
            expandButton = itemView.findViewById(R.id.rec_goal_expand);
            forExpand = itemView.findViewById(R.id.goal_for_expand);
            goalRate = itemView.findViewById(R.id.rec_goal_rate);
        }

        public void setGoalTitle(String title) {
            goalTitle.setText(title);
        }

        public void setGoalDescription(String description) {
            goalDescription.setText(description);
        }

        public void setGoalValue(String value) {
            goalValue.setText(value);
        }
    }
}