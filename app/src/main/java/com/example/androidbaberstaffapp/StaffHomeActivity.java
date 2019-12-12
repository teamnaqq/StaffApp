package com.example.androidbaberstaffapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidbaberstaffapp.Adapter.MyTimeSlotAdapter;
import com.example.androidbaberstaffapp.Common.Common;
import com.example.androidbaberstaffapp.Common.SpacesItemDecoration;
import com.example.androidbaberstaffapp.Interface.INotificationCountListener;
import com.example.androidbaberstaffapp.Interface.ITimeSlotLoadListener;
import com.example.androidbaberstaffapp.Model.BookingInformation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class StaffHomeActivity extends AppCompatActivity implements ITimeSlotLoadListener, INotificationCountListener {

    @BindView(R.id.activity_main)
    DrawerLayout drawerLayout;
    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    ActionBarDrawerToggle actionBarDrawerToggle;

    // I created this line just to have getSupportActionBar in line 103
    // ActionBar actionBar;


    //================Copy from DocBooking app (Client app)===================================//
    DocumentReference barberDoc;
    ITimeSlotLoadListener iTimeSlotLoadListener;

    AlertDialog dialog;

    @BindView(R.id.recycler_time_slot)
    RecyclerView recycler_time_slot;
    @BindView(R.id.calendarView)
    HorizontalCalendarView calendarView;
    SimpleDateFormat simpleDateFormat;
    // ================================= End of copy ===========================================//


    //Part 16:
    TextView txt_notification_badge;
    TextView txt_barber_name;

    CollectionReference notificationCollection;
    CollectionReference currentBookDateCollection;

    EventListener<QuerySnapshot> notificationEvent;
    EventListener<QuerySnapshot> bookingEvent;

    ListenerRegistration notificationListener;
    ListenerRegistration bookingRealtimeListener;

    INotificationCountListener iNotificationCountListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_home);

        ButterKnife.bind(this);

        init();
        initView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        //Part 19:
        if (item.getItemId() == R.id.action_new_notification) {
            startActivity(new Intent(StaffHomeActivity.this, NotificationActivity.class));
            txt_notification_badge.setText("");
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.open,
                R.string.close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        // getActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_exit)
                    logOut();
                dialog.dismiss();

                return true;
            }
        });

        View headerView = navigationView.getHeaderView(0);
        txt_barber_name = (TextView) headerView.findViewById(R.id.txt_barber_name);
        txt_barber_name.setText(Common.currentBarber.getName());


        //================Copy from DocBooking app (Client app)===================================//
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        // Calendar
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0); // Add current date
        loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberID(),
                Common.simpleDateFormat.format(date.getTime()));

        recycler_time_slot.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recycler_time_slot.setLayoutManager(gridLayoutManager);
        recycler_time_slot.addItemDecoration(new SpacesItemDecoration(8));

        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE, 0);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE, 2); // 2 day left

        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(this, R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(1)
                .mode(HorizontalCalendar.Mode.DAYS)
                .defaultSelectedDate(startDate)
                .build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                if (Common.bookingDate.getTimeInMillis() != date.getTimeInMillis()) {
                    Common.bookingDate = date; // This code will not load again if you selected new day same with day selected
                    loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberID(),
                            simpleDateFormat.format(date.getTime()));

                }
            }
        });
        // ================================= End of copy ===========================================//
    }

    // Part 15: Just delete all remember key and start MainActivity
    private void logOut() {
        Paper.init(this);
        Paper.book().delete(Common.SALOON_KEY);
        Paper.book().delete(Common.BARBER_KEY);
        Paper.book().delete(Common.STATE_KEY);
        Paper.book().delete(Common.LOGGED_KEY);

        new AlertDialog.Builder(this)
                .setMessage("Bạn có chắc chắn bạn muốn thoát?")
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(StaffHomeActivity.this, "Fake func exit", Toast.LENGTH_SHORT).show();

                        Intent mainActivity = new Intent(StaffHomeActivity.this, MainActivity.class);
                        mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(mainActivity);
                        finish();
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialogInterface.dismiss();

                    }
                }).show();
    }

    private void loadAvailableTimeSlotOfBarber(final String barberID, final String bookDate) {
        //================Copy from DocBooking app (Client app)===================================//
        dialog.show();

        // Get info of this barber
        barberDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) { // if barber available
                        // Get information of booking
                        // If not created, return empty
                        CollectionReference date = FirebaseFirestore.getInstance()
                                .collection("AllSalon")
                                .document(Common.state_name)
                                .collection("Branch")
                                .document(Common.selectedSaloon.getSaloonID())
                                .collection("Babers")
                                .document(barberID)
                                .collection(bookDate); // bookDate is date simple formate with dd_MM_yyy
                        date.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot querySnapshot = task.getResult();
                                    if (querySnapshot.isEmpty())  // If no appointment

                                        iTimeSlotLoadListener.onTimeSlotLoadEmpty();

                                    else {
                                        // If appointment available
                                        List<BookingInformation> timeSlots = new ArrayList<>();
                                        for (QueryDocumentSnapshot document : task.getResult())
                                            timeSlots.add(document.toObject(BookingInformation.class));

                                        iTimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlots);
                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                iTimeSlotLoadListener.onTimeSlotLoadFailed(e.getMessage());
                            }
                        });
                    }
                }
            }
        });
        // ================================= End of copy ===========================================//
    }

    private void init() {
        iTimeSlotLoadListener = this;

        iNotificationCountListener = this;
        initNotificationRealtimeUpdate();
        initBookingRealtimeUpdate();
    }

    // Part 16
    private void initBookingRealtimeUpdate() {

        barberDoc = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selectedSaloon.getSaloonID())
                .collection("Babers")
                .document(Common.currentBarber.getBarberID());


        // Get current date
        final Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0);
        bookingEvent = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                // If there's any new booking, update adapter.
                loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberID(),
                        Common.simpleDateFormat.format(date.getTime()));
            }
        };

        currentBookDateCollection = barberDoc.collection(Common.simpleDateFormat.format(date.getTime()));

        bookingRealtimeListener = currentBookDateCollection.addSnapshotListener(bookingEvent);

    }

    // Part 16
    private void initNotificationRealtimeUpdate() {
        notificationCollection = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selectedSaloon.getSaloonID())
                .collection("Babers")
                .document(Common.currentBarber.getBarberID())
                .collection("Notifications");

        notificationEvent = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots.size() > 0)
                    loadNotification();
            }
        };

        notificationListener = notificationCollection.whereEqualTo("read", false) // Only listen and count all the unread notification
                .addSnapshotListener(notificationEvent);


    }


    // Part 14
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
//        new AlertDialog.Builder(this)
//                .setMessage("Are you sure you want to exit?")
//                .setCancelable(false)
//                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(StaffHomeActivity.this, "Fake func exit", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int which) {
//                        dialogInterface.dismiss();
//
//                    }
//                }).show();
    }

    @Override
    public void onTimeSlotLoadSuccess(List<BookingInformation> timeSlotList) {
        //================Copy from DocBooking app (Client app)===================================//
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this, timeSlotList);
        recycler_time_slot.setAdapter(adapter);

        dialog.dismiss();

        // ================================= End of copy ===========================================//

    }

    @Override
    public void onTimeSlotLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadEmpty() {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this);
        recycler_time_slot.setAdapter(adapter);

        dialog.dismiss();
    }


    // Part 16
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.staff_home_menu, menu);
        final MenuItem menuItem = menu.findItem(R.id.action_new_notification);

        txt_notification_badge = (TextView) menuItem.getActionView()
                .findViewById(R.id.notification_badge);

        loadNotification();

        menuItem.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(menuItem);
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void loadNotification() {
        notificationCollection.whereEqualTo("read", false)
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(StaffHomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            iNotificationCountListener.onNotificationCountSuccess(task.getResult().size());
                        }
                    }
                });

    }

    @Override
    public void onNotificationCountSuccess(int count) {

        if (count == 0) {
            txt_notification_badge.setVisibility(View.INVISIBLE);
        } else {
            txt_notification_badge.setVisibility(View.VISIBLE);
            if (count <= 9)
                txt_notification_badge.setText(String.valueOf(count));
            else
                txt_notification_badge.setText("9+");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        initBookingRealtimeUpdate();
        initNotificationRealtimeUpdate();
    }

    @Override
    protected void onStop() {
        if (notificationListener != null)
            notificationListener.remove();
        if (bookingRealtimeListener != null)
            bookingRealtimeListener.remove();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (notificationListener != null)
            notificationListener.remove();
        if (bookingRealtimeListener != null)
            bookingRealtimeListener.remove();
        super.onDestroy();
    }
}
