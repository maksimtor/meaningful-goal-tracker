package org.maksimtoropygin.meaningfulgoaltracker.ui.todo_list;

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
import org.maksimtoropygin.meaningfulgoaltracker.adapters.TodoListAdapter;
import org.maksimtoropygin.meaningfulgoaltracker.adapters.data.Todo;

import java.util.ArrayList;
import java.util.Objects;

public class ToDoListFragment extends Fragment {

    private TodoListAdapter todoListAdapter;
    private TodoListAdapter todoListResolvedAdapter;
    private ArrayList<Todo> todoList = new ArrayList<>();
    private ArrayList<Todo> todoListResolved = new ArrayList<>();
    private RecyclerView todoRecycler;
    private RecyclerView todoResolvedRecycler;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final String currentUser = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_todo_list, container, false);

        FloatingActionButton addEditFab = root.findViewById(R.id.fab_new_todo);
        addEditFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ToDoAddEdit.class);
                intent.putExtra("action", "add");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        todoRecycler = root.findViewById(R.id.recycler_list_todo);
        todoResolvedRecycler = root.findViewById(R.id.recycler_list_todo_resolved);
        LinearLayoutManager layoutManagerTodo = new LinearLayoutManager(getContext());
        LinearLayoutManager layoutManagerTodoResolved = new LinearLayoutManager(getContext());
        todoRecycler.setLayoutManager(layoutManagerTodo);
        todoResolvedRecycler.setLayoutManager(layoutManagerTodoResolved);

        db.collection("todos").addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                todoList = new ArrayList<>();
                todoListResolved = new ArrayList<>();
                assert queryDocumentSnapshots != null;
                for (QueryDocumentSnapshot document: queryDocumentSnapshots){
                    if (Objects.equals(document.getData().get("userId"), currentUser)){
                        if (Objects.equals(document.getData().get("status"), "unresolved"))
                            todoList.add(new Todo(document.getId(), Objects.requireNonNull(document.getData().get("goalId")).toString(), Objects.requireNonNull(document.getData().get("title")).toString(), Objects.requireNonNull(document.getData().get("status")).toString()));
                        else if (Objects.equals(document.getData().get("status"), "resolved"))
                            todoListResolved.add(new Todo(document.getId(), Objects.requireNonNull(document.getData().get("goalId")).toString(), Objects.requireNonNull(document.getData().get("title")).toString(), Objects.requireNonNull(document.getData().get("status")).toString()));
                    }
                }
                todoListAdapter = new TodoListAdapter(todoList, getContext());
                todoListResolvedAdapter = new TodoListAdapter(todoListResolved, getContext());
                todoRecycler.setAdapter(todoListAdapter);
                todoResolvedRecycler.setAdapter(todoListResolvedAdapter);
            }
        });
        ImageButton helperButton = getActivity().findViewById(R.id.action_help);
        final CardView helper = root.findViewById(R.id.todo_helper);
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