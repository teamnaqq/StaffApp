package com.example.androidbaberstaffapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.example.androidbaberstaffapp.Common.Common;
import com.example.androidbaberstaffapp.DoneServiceActivity;
import com.example.androidbaberstaffapp.Interface.IRecyclerItemSelectedListener;
import com.example.androidbaberstaffapp.Model.BookingInformation;
import com.example.androidbaberstaffapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyTimeSlotAdapter extends RecyclerView.Adapter<MyTimeSlotAdapter.MyViewHolder> {

    Context context;
    List<BookingInformation> timeSlotList;

    // Create cardview list for user to choose
    List<CardView> cardViewList;



    public MyTimeSlotAdapter(Context context) {
        this.context = context;
        this.timeSlotList= new ArrayList<>();

        cardViewList = new ArrayList<>();

    }

    public MyTimeSlotAdapter(Context context, List<BookingInformation> timeSlotList) {
        this.context = context;
        this.timeSlotList = timeSlotList;
        cardViewList = new ArrayList<>();

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_time_slot,viewGroup,false);

        return new MyViewHolder(itemView);
    }

    // Set the text in ca rdview of time slot
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, final int i) {

        myViewHolder.txt_time_slot.setText(new StringBuilder(Common.convertTimeSlotToString(i)).toString());

        if(timeSlotList.size() == 0){ // If all the position is available

            myViewHolder.txt_time_slot_description.setText("Trống");
            myViewHolder.txt_time_slot_description.setTextColor(context.getResources()
                    .getColor(android.R.color.black));
            myViewHolder.txt_time_slot.setTextColor(context.getResources()
                    .getColor(android.R.color.black));
            myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources()
                    .getColor(android.R.color.white));

            // Part20: Add Event nothing
            myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                @Override
                public void onItemSelectedListener(View view, int pos) {
                    //Fix crash if we not add this function;
                }
            });
        }
        else{ // if the position is fully booked
            for(final BookingInformation slotValue:timeSlotList){
                //Loop all time slot form server to  different color
                final int slot = Integer.parseInt(slotValue.getSlot().toString());
                if(slot == i){ // If slot == position

                    if(!slotValue.isDone()) {

                        // We will set tag for all the time slot that's full. So based on tag, we can set
                        // all the remaining card background without changing full time slot.

                        myViewHolder.card_time_slot.setTag(Common.DISABLE_TAG);
                        myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources()
                                .getColor(android.R.color.darker_gray));
                        myViewHolder.txt_time_slot_description.setText("Đã đặt");
                        myViewHolder.txt_time_slot_description.setTextColor(context.getResources()
                                .getColor(android.R.color.white));
                        myViewHolder.txt_time_slot.setTextColor(context.getResources()
                                .getColor(android.R.color.white));

                        // myViewHolder.card_time_slot.setEnabled(false);

                        // Part20:
                        myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                            @Override
                            public void onItemSelectedListener(View view, int pos) {
                                // Only add for gray time slot.
                                // Here we will get BookingInformation and store in Common.currentBookingInformation.
                                // After that, start DoneServiceActivity.
                                FirebaseFirestore.getInstance()
                                        .collection("AllSalon")
                                        .document(Common.state_name)
                                        .collection("Branch")
                                        .document(Common.selectedSaloon.getSaloonID())
                                        .collection("Babers")
                                        .document(Common.currentBarber.getBarberID())
                                        .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                                        .document(slotValue.getSlot().toString())
                                        .get()
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    if (task.getResult().exists()) {
                                                        Common.currentBookingInformation = task.getResult().toObject(BookingInformation.class);
                                                        // Part 25: Added line below
                                                        Common.currentBookingInformation.setBookingId(task.getResult().getId());
                                                        context.startActivity(new Intent(context, DoneServiceActivity.class));

                                                    }
                                                }
                                            }
                                        });
                            }
                        });
                    }
                    else  // PArt 25: if service is done
                    {
                        myViewHolder.card_time_slot.setTag(Common.DISABLE_TAG);
                        myViewHolder.card_time_slot.setCardBackgroundColor(context.getResources()
                                .getColor(android.R.color.holo_orange_dark));
                        myViewHolder.txt_time_slot_description.setText("Hoàn thành");
                        myViewHolder.txt_time_slot_description.setTextColor(context.getResources()
                                .getColor(android.R.color.white));
                        myViewHolder.txt_time_slot.setTextColor(context.getResources()
                                .getColor(android.R.color.white));

                        myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                            @Override
                            public void onItemSelectedListener(View view, int pos) {
                                // Add here to fix crash
                            }
                        });
                    }
                }
                else
                {
                    // Fix crash when barber click on white time slot
                    if(myViewHolder.getiRecyclerItemSelectedListener() == null)
                    {
                        // We only add event for view holder which is not implemented click.
                        //Because if we dont put this if condition
                        // All time slot with slot value higher than current time slot will be override by event
                        myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                            @Override
                            public void onItemSelectedListener(View view, int pos) {

                            }
                        });
                    }
                }
            }
        }

        // Add all 20 card to list
        if(!cardViewList.contains(myViewHolder.card_time_slot))
            cardViewList.add(myViewHolder.card_time_slot);

    }

    @Override
    public int getItemCount() {
        return Common.TIME_SLOT_TOTAL;

    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_time_slot, txt_time_slot_description;
        CardView card_time_slot;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public IRecyclerItemSelectedListener getiRecyclerItemSelectedListener() {
            return iRecyclerItemSelectedListener;
        }

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            card_time_slot = (CardView)itemView.findViewById(R.id.card_time_slot);
            txt_time_slot = (TextView)itemView.findViewById(R.id.txt_time_slot);
            txt_time_slot_description = (TextView)itemView.findViewById(R.id.txt_time_slot_description1);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iRecyclerItemSelectedListener.onItemSelectedListener(v,getAdapterPosition());
        }
    }
}
