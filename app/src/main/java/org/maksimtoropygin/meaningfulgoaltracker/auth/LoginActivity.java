package org.maksimtoropygin.meaningfulgoaltracker.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.maksimtoropygin.meaningfulgoaltracker.MainActivity;
import org.maksimtoropygin.meaningfulgoaltracker.R;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setTitle("Sign in");

        emailEditText = findViewById(R.id.emailEditTextinLogIn);
        passwordEditText = findViewById(R.id.pwEditTextinLogin);
        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        String reason = intent.getStringExtra("reason");
        if (reason != null){
            FirebaseAuth.getInstance().signOut();
        }
        if (mAuth.getCurrentUser() != null && reason == null) {
            final DocumentReference user = FirebaseFirestore.getInstance().collection("users").document(Objects.requireNonNull(mAuth.getCurrentUser().getEmail()));
            user.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        startMainActivity();
                        finish();
                    }
                }
            });
        }
    }

    public void signInClicked(View view) {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please, enter email and password", Toast.LENGTH_SHORT).show();
        }else{
            //Sign in the user
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Sign in successful", Toast.LENGTH_SHORT).show();
                        startMainActivity();
                    } else {
                        Toast.makeText(LoginActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    public void signUpTextViewClicked(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    public void startMainActivity(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}