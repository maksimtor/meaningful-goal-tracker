package org.maksimtoropygin.meaningfulgoaltracker.ui.value_explorer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.maksimtoropygin.meaningfulgoaltracker.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ValueExplorerAddEdit extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final String currentUser = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_edit_value);

        final EditText title = findViewById(R.id.add_edit_value_title);
        final EditText description = findViewById(R.id.add_edit_value_description);
        final Button addOrEdit = findViewById(R.id.add_edit_value_button);

        Intent intent = getIntent();
        String action = intent.getStringExtra("action");
        assert action != null;
        if (action.equals("add")){
            setTitle("Add a new value");
            addOrEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String textTitle = title.getText().toString();
                    String textDescription = description.getText().toString();
                    Map<String, Object> value = new HashMap<>();
                    value.put("title", textTitle);
                    value.put("description", textDescription);
                    value.put("userId", currentUser);
                    db.collection("values")
                            .add(value)
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
            setTitle("Edit a value");
            addOrEdit.setText("Edit");
            String oldTitle = intent.getStringExtra("title");
            String oldDescription = intent.getStringExtra("description");
            String id = intent.getStringExtra("id");

            assert id != null;
            final DocumentReference valueDocument = db.collection("values").document(id);

            title.setText(oldTitle);
            description.setText(oldDescription);

            addOrEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String textTitle = title.getText().toString();
                    String textDescription = description.getText().toString();
                    valueDocument.update(
                            "title", textTitle,
                            "description", textDescription
                    );
                    finish();
                }
            });
        }
    }
}