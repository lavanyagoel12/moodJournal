package com.example.nu_mad_sp2023_final_project_10;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link JournalHomePageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JournalHomePageFragment extends Fragment {

    private static final String USER = "param1";
    private static FirebaseFirestore db;

    private Button buttonLogout;
    private ArrayList<CalendarDay> days = new ArrayList<>();
    private ArrayList<CalendarDay> daysJournal = new ArrayList<>();

    private User user;
    private BottomNavigationView bottomMenu;
    private RecyclerView calendarRecycler;
    private RecyclerView.LayoutManager calendarRecyclerLayoutManager;
    private CalendarAdapter calendarAdapter;
    private int calYear = LocalDate.now().getYear();
    private int calMonth = LocalDate.now().getMonthValue();
    private Button prev;
    private Button next;
    private TextView monthYear;
    private IfromFragmentToActivity listener;
    private Spinner spinnerMoods;
    private Spinner spinnerTags;
    private ArrayAdapter<String> MoodsAdapter;
    private ArrayAdapter<String> TagsAdapter;
    private String curMoodFilter = "None";
    private String curTagFilter = "None";

    public JournalHomePageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param user Parameter 1.
     * @return A new instance of fragment JournalHomePageFragment.
     */
    public static JournalHomePageFragment newInstance(User user) {
        JournalHomePageFragment fragment = new JournalHomePageFragment();
        Bundle args = new Bundle();
        args.putSerializable(USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof IfromFragmentToActivity) {
            listener = (IfromFragmentToActivity) context;
        } else {
            throw new RuntimeException(context + "must Implement IconnectToFragment");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_journal_home_page, container, false);

        getActivity().setTitle("Calendar");
        db = FirebaseFirestore.getInstance();
        prev = view.findViewById(R.id.buttonPrev);
        next = view.findViewById(R.id.buttonNext);

        monthYear = view.findViewById(R.id.textViewMonthYear);
        calendarRecycler = view.findViewById(R.id.recyclerViewCalendar);
        calendarRecyclerLayoutManager = new GridLayoutManager(getContext(), 7);
        monthYear.setText(Month.of(calMonth).name().charAt(0) + Month.of(calMonth).name().substring(1).toLowerCase() + ", " + Year.of(calYear));
        calendarAdapter = new CalendarAdapter(getContext());
        calendarRecycler.setAdapter(calendarAdapter);
        calendarRecycler.setLayoutManager(calendarRecyclerLayoutManager);
        this.getCalendarDays(calYear, calMonth);

        spinnerMoods = view.findViewById(R.id.spinnerMoods);
        spinnerTags = view.findViewById(R.id.spinnerTags);
        MoodsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
        this.getMoodOptions();
        TagsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
        this.getTagOptons();
        MoodsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        MoodsAdapter.add("None");
        TagsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMoods.setAdapter(MoodsAdapter);
        spinnerTags.setAdapter(TagsAdapter);

        spinnerMoods.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                curMoodFilter = (String) spinnerMoods.getAdapter().getItem(i);
                resetCalendar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerTags.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                curTagFilter = (String) spinnerTags.getAdapter().getItem(i);
                resetCalendar();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        bottomMenu = view.findViewById(R.id.bottomNavigationView);
        bottomMenu.getMenu().findItem(R.id.Calendar).setChecked(true);
        bottomMenu.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.memoryBox) {
                    getActivity().setTitle("Memory Box");
                    listener.onMemoryBox();
                    return true;
                }
                return true;
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(calMonth == 1) {
                    calMonth = 12;
                    calYear--;
                } else {
                    calMonth--;
                }
                resetCalendar();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(calMonth == 12) {
                    calMonth = 1;
                    calYear++;
                } else {
                    calMonth++;
                }
                resetCalendar();
            }
        });
        buttonLogout = view.findViewById(R.id.button_logout);

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onLogout();
            }
        });

        return view;
    }

    private void getTagOptons() {
        ArrayList<String> tags = new ArrayList<>();
        tags.add("None");
        db.collection("users")
                .document(user.getEmail())
                .collection("tags")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot doc: queryDocumentSnapshots.getDocuments()) {
                            tags.add((String) doc.get("tag"));
                        }
                        TagsAdapter.addAll(tags);
                        TagsAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void getMoodOptions() {
        ArrayList<String> moodsGen = new ArrayList<>();
        ArrayList<String> moodsCustom = new ArrayList<>();

        db.collection("moods")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot doc: queryDocumentSnapshots.getDocuments()) {
                            moodsGen.add((String) doc.get("name"));
                        }
                        MoodsAdapter.addAll(moodsGen);
                        MoodsAdapter.notifyDataSetChanged();
                    }
                });

        db.collection("users")
                .document(user.getEmail())
                .collection("moods")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(DocumentSnapshot doc: queryDocumentSnapshots.getDocuments()) {
                            moodsCustom.add((String) doc.get("name"));
                        }
                        MoodsAdapter.addAll(moodsCustom);
                        MoodsAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void resetCalendar() {
        this.getCalendarDays(calYear, calMonth);
        monthYear.setText(Month.of(calMonth).name().charAt(0) +
                Month.of(calMonth).name().substring(1).toLowerCase() + ", " +
                Year.of(calYear));
    }

    private void getCalendarDays(int year, int month) {
        days = new ArrayList<>();
        daysJournal = new ArrayList<>();
        YearMonth yearMonthObject = YearMonth.of(year, month);
        HashMap<Integer, DocumentReference> moodDocs = new HashMap<>();

        int daysInMonth = yearMonthObject.lengthOfMonth();
        for(int i = 1; i <= daysInMonth; i++) {
            days.add(new CalendarDay(year, month, i));
        }

        db.collection("users")
                .document(this.user.getEmail())
                .collection("year")
                .document(String.valueOf(year))
                .collection("month")
                .document(String.valueOf(month))
                .collection("day")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot doc: queryDocumentSnapshots.getDocuments()) {
                            JournalEntry journalEntry = doc.toObject(JournalEntry.class);
                            moodDocs.put(Integer.parseInt(doc.getId())-1, journalEntry.getMoodRef());
                        }

                        for(Integer day: moodDocs.keySet()) {
                            final Color[] color = new Color[1];
                            moodDocs.get(day).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    color[0] = Color.valueOf((int) (long) documentSnapshot.get("color"));
                                    days.get(day).setColor(color[0]);
                                    daysJournal.add(days.get(day));
                                    filterDays(documentSnapshot, days.get(day), curTagFilter, curMoodFilter);
                                    calendarAdapter.notifyDataSetChanged();
                                }
                            });
                        }

                        calendarAdapter.setDays(days);
                        calendarAdapter.notifyDataSetChanged();
                    }
                });


    }

    private void filterDays(DocumentSnapshot documentSnapshot, CalendarDay day, String curTagFilter, String curMoodFilter) {
        ArrayList<String> tags = new ArrayList<>();

        db.collection("users")
                .document(this.user.getEmail())
                .collection("year")
                .document(String.valueOf(day.getYear()))
                .collection("month")
                .document(String.valueOf(day.getMonth()))
                .collection("day")
                .document(String.valueOf(day.getDay()))
                .collection("tags")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot doc: queryDocumentSnapshots.getDocuments()) {
                            tags.add(doc.getId());
                        }
                        if((!curTagFilter.equals("None") && !tags.contains(curTagFilter)) || (!curMoodFilter.equals("None") && !documentSnapshot.get("name").equals(curMoodFilter))) {
                            day.setColor(null);
                            calendarAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}