package org.maksimtoropygin.meaningfulgoaltracker.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.maksimtoropygin.meaningfulgoaltracker.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class SettingsFragment extends Fragment {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final String currentUser = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        final EditText name = root.findViewById(R.id.settings_name);
        final EditText family = root.findViewById(R.id.settings_family);
        final TextView regDate = root.findViewById(R.id.profile_regdate);
        final TextView email = root.findViewById(R.id.profile_email);
        Button save = root.findViewById(R.id.settings_save);
        assert currentUser != null;
        final DocumentReference userSettings = FirebaseFirestore.getInstance().collection("users").document(currentUser);
        userSettings.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            assert document != null;
                            String pattern = "yyyy-MM-dd";
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                            Date regDateValue = (Date) document.get("regDate");
                            assert regDateValue != null;
                            String date = simpleDateFormat.format(regDateValue);
                            regDate.setText("Registered on " + date);
                            email.setText("Email is " + Objects.requireNonNull(document.getId()));
                            name.setText(Objects.requireNonNull(document.get("name")).toString());
                            family.setText(Objects.requireNonNull(document.get("familyName")).toString());
                        }
                    }
                });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userSettings.get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    userSettings.update(
                                            "name", name.getText().toString(),
                                            "familyName", family.getText().toString()
                                    );
                                }
                            }
                        });
            }});
        ImageButton helperButton = getActivity().findViewById(R.id.action_help);
        helperButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast = Toast.makeText(getContext(), "You better not click on that!", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        return root;
    }
}