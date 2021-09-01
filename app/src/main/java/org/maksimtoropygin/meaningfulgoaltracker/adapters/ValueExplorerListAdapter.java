package org.maksimtoropygin.meaningfulgoaltracker.adapters;


import android.content.Context;
import android.content.Intent;
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
import org.maksimtoropygin.meaningfulgoaltracker.adapters.data.Value;
import org.maksimtoropygin.meaningfulgoaltracker.ui.value_explorer.ValueExplorerAddEdit;

import java.util.ArrayList;
import java.util.Objects;


public class ValueExplorerListAdapter extends RecyclerView.Adapter<ValueExplorerListAdapter.ContactHolder> {

    private ArrayList<Value> valueList;
    private Context mContext;
    int done = 0;
    int missed = 0;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public ValueExplorerListAdapter(ArrayList<Value> valueList, Context context) {
        this.valueList = valueList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ContactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.recycler_value_explorer, parent, false);
        return new ContactHolder(view);
    }

    @Override
    public int getItemCount() {
        return valueList == null? 0: valueList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactHolder holder, final int position) {
        final Value value = valueList.get(position);
        holder.setValueTitle(value.getTitle());
        holder.setValueDescription(value.getDescription());
        holder.valueDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference valueToDelete = FirebaseFirestore.getInstance().collection("values").document(valueList.get(position).getId());
                valueToDelete
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

                db.collection("goals").addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        assert queryDocumentSnapshots != null;
                        for (final QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            if (document.getData().get("valueId") != null && document.getData().get("valueId").toString().equals(valueList.get(position).getId())){
                                DocumentReference goalToDelete = FirebaseFirestore.getInstance().collection("goals").document(document.getId());
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

                                final String goalId = document.getId();
                                db.collection("scheduleActivity").addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                        assert queryDocumentSnapshots != null;
                                        for (final QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                            if (document.getData().get("goalId") != null && document.getData().get("goalId").toString().equals(goalId)){
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
                                            if (document.getData().get("goalId") != null && document.getData().get("goalId").toString().equals(goalId)){
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
                        }

                    }
                });
            }
        });
        holder.valueEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ValueExplorerAddEdit.class);
                intent.putExtra("action", "edit");
                intent.putExtra("title", valueList.get(position).getTitle());
                intent.putExtra("description", valueList.get(position).getDescription());
                intent.putExtra("id", valueList.get(position).getId());
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

//        db.collection("scheduleActivityEvents").addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//                assert queryDocumentSnapshots != null;
//                done = 0;
//                missed = 0;
//                for (final QueryDocumentSnapshot document: queryDocumentSnapshots){
//                    String currentUser = "";
//                    if (mAuth.getCurrentUser() != null) {
//                        currentUser = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
//                    }
//                    if (document.getData().get("scheduleId") != null && document.getData().get("userId") != null && Objects.requireNonNull(document.getData().get("userId")).toString().equals(currentUser)){
//                        String scheduleId = document.getData().get("scheduleId").toString();
//                        DocumentReference scheduleToCheck = FirebaseFirestore.getInstance().collection("scheduleActivity").document(scheduleId);
//                        scheduleToCheck.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                            @Override
//                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                if (task.isSuccessful()) {
//                                    DocumentSnapshot scheduleToCheckResult = task.getResult();
//                                    if (scheduleToCheckResult.get("goalId") != null){
//                                        DocumentReference goalToCheck = FirebaseFirestore.getInstance().collection("goals").document(scheduleToCheckResult.get("goalId").toString());
//                                        goalToCheck.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                                if (task.isSuccessful()) {
//                                                    DocumentSnapshot goalToCheckResult = task.getResult();
//                                                    if (goalToCheckResult.get("valueId") != null && goalToCheckResult.get("valueId").toString().equals(valueList.get(position).getId())){
//                                                        if (document.getData().get("status").equals("done"))
//                                                            done++;
//                                                        else if (document.getData().get("status").equals("missed"))
//                                                            missed++;
//                                                        float prodRate = ((float) done/ (float) (done+missed))*100;
//                                                        holder.valueRate.setText("Productivity rate is " + (int) prodRate + "%" + ". Done " + done + ", missed " + missed);
//                                                    }
//                                                }
//                                            }});
//                                    }
//                                }
//                            }
//                        });
//                    }
//                }
//            }
//        });

    }

    public static class ContactHolder extends RecyclerView.ViewHolder {

        private TextView valueTitle;
        private TextView valueDescription;
        // private TextView valueRate;
        private ImageButton valueDelete;
        private ImageButton valueEdit;
        private ImageButton expandButton;
        private LinearLayout forExpand;

        public ContactHolder(View itemView) {
            super(itemView);

            valueTitle = itemView.findViewById(R.id.rec_val_title);
            valueDescription = itemView.findViewById(R.id.rec_val_description);
            valueDelete = itemView.findViewById(R.id.rec_val_delete);
            valueEdit = itemView.findViewById(R.id.rec_val_edit);
            expandButton = itemView.findViewById(R.id.rec_val_expand);
            forExpand = itemView.findViewById(R.id.value_for_expand);
            // valueRate = itemView.findViewById(R.id.rec_val_rate);
        }

        public void setValueTitle(String title) {
            valueTitle.setText(title);
        }

        public void setValueDescription(String description) {
            valueDescription.setText(description);
        }
    }
}