package org.maksimtoropygin.meaningfulgoaltracker.ui.goal_keeper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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
import org.maksimtoropygin.meaningfulgoaltracker.adapters.data.Value;
import org.maksimtoropygin.meaningfulgoaltracker.ui.scheduler.SchedulerAddEdit;
import org.maksimtoropygin.meaningfulgoaltracker.ui.value_explorer.ValueExplorerAddEdit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GoalKeeperAddEdit extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final String currentUser = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
    private ArrayList<Value> valueList = new ArrayList<>();
    private String[] arraySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_edit_goal);

        final EditText title = findViewById(R.id.add_edit_goal_title);
        final EditText description = findViewById(R.id.add_edit_goal_description);
        final Spinner value = findViewById(R.id.add_edit_goal_value);
        final Button addOrEdit = findViewById(R.id.add_edit_goal_button);

        db.collection("values").addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                valueList = new ArrayList<>();
                assert queryDocumentSnapshots != null;
                for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                    if (Objects.equals(document.getData().get("userId"), currentUser)){
                        valueList.add(new Value(document.getId(), Objects.requireNonNull(document.getData().get("userId")).toString(), Objects.requireNonNull(document.getData().get("title")).toString(), Objects.requireNonNull(document.getData().get("description")).toString()));
                    }
                }
                arraySpinner = new String[valueList.size()+1];
                for (int i = 0; i<valueList.size(); i++) {
                    arraySpinner[i] = valueList.get(i).getTitle();
                }
                arraySpinner[valueList.size()] = "Create a new value";
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                        android.R.layout.simple_spinner_item, arraySpinner);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                value.setAdapter(adapter);
                value.setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                                if (pos==valueList.size()){
                                    value.setSelection(0);
                                    Intent intent = new Intent(parent.getContext(), ValueExplorerAddEdit.class);
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
                    setTitle("Add a new goal");
                    addOrEdit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String textTitle = title.getText().toString();
                            String textDescription = description.getText().toString();
                            String textValueId = valueList.get(value.getSelectedItemPosition()).getId();
                            Map<String, Object> goal = new HashMap<>();
                            goal.put("title", textTitle);
                            goal.put("description", textDescription);
                            goal.put("valueId", textValueId);
                            goal.put("userId", currentUser);
                            db.collection("goals")
                                    .add(goal)
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
                    setTitle("Edit a goal");
                    addOrEdit.setText("Edit");
                    String oldTitle = intent.getStringExtra("title");
                    String oldDescription = intent.getStringExtra("description");
                    String oldValueId = intent.getStringExtra("valueId");
                    String id = intent.getStringExtra("id");

                    int spinnerValueIndex = 0;

                    for (int i = 0; i<valueList.size(); i++) {
                        if (valueList.get(i).getId().equals(oldValueId)) {
                            spinnerValueIndex = i;
                        }
                    }

                    assert id != null;
                    final DocumentReference goalDocument = db.collection("goals").document(id);

                    title.setText(oldTitle);
                    description.setText(oldDescription);
                    value.setSelection(spinnerValueIndex);

                    addOrEdit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String textTitle = title.getText().toString();
                            String textDescription = description.getText().toString();
                            String textValueId = valueList.get(value.getSelectedItemPosition()).getId();
                            goalDocument.update(
                                    "title", textTitle,
                                    "description", textDescription,
                                    "valueId", textValueId
                            );
                            finish();
                        }
                    });
                }
            }
        });
    }
}