package org.maksimtoropygin.meaningfulgoaltracker.ui.todo_list;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.maksimtoropygin.meaningfulgoaltracker.R;
import org.maksimtoropygin.meaningfulgoaltracker.adapters.data.Goal;
import org.maksimtoropygin.meaningfulgoaltracker.ui.goal_keeper.GoalKeeperAddEdit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ToDoAddEdit extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final String currentUser = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
    private ArrayList<Goal> goalList = new ArrayList<>();
    private String[] arraySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_edit_todo);

        final EditText title = findViewById(R.id.add_edit_todo_title);
        final Spinner goal = findViewById(R.id.add_edit_todo_goal);
        final Button addOrEdit = findViewById(R.id.add_edit_todo_button);

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
                    setTitle("Add a new todo item");
                    addOrEdit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String textTitle = title.getText().toString();
                            String textGoalId = goalList.get(goal.getSelectedItemPosition()).getId();
                            Map<String, Object> todo = new HashMap<>();
                            todo.put("title", textTitle);
                            todo.put("goalId", textGoalId);
                            todo.put("userId", currentUser);
                            todo.put("status", "unresolved");
                            db.collection("todos")
                                    .add(todo)
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

                        }
                    });
                }
                else if (action.equals("edit")){
                    setTitle("Edit a todo item");
                    addOrEdit.setText("Edit");
                    String oldTitle = intent.getStringExtra("title");
                    String oldGoalId = intent.getStringExtra("goalId");
                    String id = intent.getStringExtra("id");

                    int spinnerValueIndex = 0;

                    for (int i = 0; i<goalList.size(); i++) {
                        if (goalList.get(i).getId().equals(oldGoalId)) {
                            spinnerValueIndex = i;
                        }
                    }

                    assert id != null;
                    final DocumentReference goalDocument = db.collection("todos").document(id);

                    title.setText(oldTitle);
                    goal.setSelection(spinnerValueIndex);

                    addOrEdit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String textTitle = title.getText().toString();
                            String textGoalId = goalList.get(goal.getSelectedItemPosition()).getId();
                            goalDocument.update(
                                    "title", textTitle,
                                    "goalId", textGoalId
                            );
                            finish();
                        }
                    });
                }
            }
        });
    }
}