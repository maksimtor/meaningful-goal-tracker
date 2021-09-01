package org.maksimtoropygin.meaningfulgoaltracker.ui.value_explorer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

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
import org.maksimtoropygin.meaningfulgoaltracker.adapters.ValueExplorerListAdapter;
import org.maksimtoropygin.meaningfulgoaltracker.adapters.data.Value;

import java.util.ArrayList;
import java.util.Objects;

public class ValueExplorerFragment extends Fragment {
    private ValueExplorerListAdapter valueExplorerListAdapter;
    private ArrayList<Value> valueList = new ArrayList<>();
    private RecyclerView valueExplorerRecycler;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final String currentUser = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_value_explorer, container, false);
        FloatingActionButton addEditFab = root.findViewById(R.id.fab_new_value);
        addEditFab.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {
                                              Intent intent = new Intent(getActivity(), ValueExplorerAddEdit.class);
                                              intent.putExtra("action", "add");
                                              intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                              startActivity(intent);
                                          }
                                      });


        valueExplorerRecycler = root.findViewById(R.id.my_recycler_view);
        LinearLayoutManager layoutManagerValueExplorer = new LinearLayoutManager(getContext());
        valueExplorerRecycler.setLayoutManager(layoutManagerValueExplorer);

        db.collection("values").addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (currentUser != null) {
                    valueList = new ArrayList<>();
                    if (queryDocumentSnapshots != null)
                        for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                            if (Objects.equals(document.getData().get("userId"), currentUser)){
                                valueList.add(new Value(document.getId(), Objects.requireNonNull(document.getData().get("userId")).toString(), Objects.requireNonNull(document.getData().get("title")).toString(), Objects.requireNonNull(document.getData().get("description")).toString()));
                            }
                        }
                    if (currentUser != null ){
                        valueExplorerListAdapter = new ValueExplorerListAdapter(valueList, getContext());
                        valueExplorerRecycler.setAdapter(valueExplorerListAdapter);
                    }
                }

            }
        });

        ImageButton helperButton = getActivity().findViewById(R.id.action_help);
        final CardView helper = root.findViewById(R.id.value_helper);
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