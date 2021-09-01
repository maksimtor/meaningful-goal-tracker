package org.maksimtoropygin.meaningfulgoaltracker.ui.goal_keeper;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

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
import org.maksimtoropygin.meaningfulgoaltracker.adapters.GoalKeeperListAdapter;
import org.maksimtoropygin.meaningfulgoaltracker.adapters.data.Goal;

import java.util.ArrayList;
import java.util.Objects;

public class GoalKeeperFragment extends Fragment {

    private GoalKeeperListAdapter goalKeeperListAdapter;
    private ArrayList<Goal> goalList = new ArrayList<>();
    private RecyclerView goalKeeperRecycler;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final String currentUser = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_goal_keeper, container, false);

        FloatingActionButton addEditFab = root.findViewById(R.id.fab_new_goal);
        addEditFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GoalKeeperAddEdit.class);
                intent.putExtra("action", "add");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        goalKeeperRecycler = root.findViewById(R.id.recycler_list_goal_keeper);
        LinearLayoutManager layoutManagerGoalKeeper = new LinearLayoutManager(getContext());
        goalKeeperRecycler.setLayoutManager(layoutManagerGoalKeeper);

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
                goalKeeperListAdapter = new GoalKeeperListAdapter(goalList, getContext());
                goalKeeperRecycler.setAdapter(goalKeeperListAdapter);
            }
        });
        ImageButton helperButton = getActivity().findViewById(R.id.action_help);
        final CardView helper = root.findViewById(R.id.goal_helper);
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