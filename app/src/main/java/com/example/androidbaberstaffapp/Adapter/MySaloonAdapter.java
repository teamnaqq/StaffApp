package com.example.androidbaberstaffapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.example.androidbaberstaffapp.Common.CustomLoginDialog;
import com.example.androidbaberstaffapp.Interface.IDialogClickListener;
import com.example.androidbaberstaffapp.Interface.IGetBarberListener;
import com.example.androidbaberstaffapp.Interface.IRecyclerItemSelectedListener;
import com.example.androidbaberstaffapp.Interface.IUserLoginRememberListener;
import com.example.androidbaberstaffapp.Model.Barber;
import com.example.androidbaberstaffapp.Model.Saloon;
import com.example.androidbaberstaffapp.R;
import com.example.androidbaberstaffapp.StaffHomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

// Inflate layout_saloon and substitute the content (i.k.a name & addr) with the one in FireBase
public class MySaloonAdapter extends RecyclerView.Adapter<MySaloonAdapter.MyViewHolder> implements IDialogClickListener {

    Context context;
    List<Saloon> saloonList;  // This list came from Model.Saloon
    List<CardView> cardViewList;

    IUserLoginRememberListener iUserLoginRememberListener;
    IGetBarberListener iGetBarberListener;



    public MySaloonAdapter(Context context, List<Saloon> saloonList, IUserLoginRememberListener iUserLoginRememberListener, IGetBarberListener iGetBarberListener) {
        this.context = context;
        this.saloonList = saloonList;
        cardViewList = new ArrayList<>();
        this.iUserLoginRememberListener = iUserLoginRememberListener;
        this.iGetBarberListener = iGetBarberListener;


    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_saloon,viewGroup,false);
        return new MyViewHolder(itemView);
    }

    //Replace the name in myViewHolder with the one in saloonList
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, int i) {
        // getName() and getAddress() came from Model.Saloon
        myViewHolder.txt_saloon_name.setText(saloonList.get(i).getName());
        myViewHolder.txt_saloon_address.setText(saloonList.get(i).getAddress());


        if(!cardViewList.contains(myViewHolder.card_saloon))
            cardViewList.add(myViewHolder.card_saloon);

        myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelectedListener(View view, int position) {

                Common.selectedSaloon = saloonList.get(position);

                showLoginDialog();


            }
        });
    }

    private void showLoginDialog() {
        CustomLoginDialog.getInstance()
                .showLoginDialog("STAFF LOGIN",
                        "LOGIN",
                        "CANCEL",
                        context,
                        this);
    }


    @Override
    public int getItemCount() {
        return saloonList.size();
    }

    @Override
    public void onClickPositiveButton(final DialogInterface dialogInterface, final String userName, String password) {
        //Show loading dialog
        final AlertDialog loadingDialog = new SpotsDialog.Builder().setCancelable(false)
                .setContext(context).build();

        loadingDialog.show();

        // /AllSaloon/NewYork/Branch/gcn5ulYdj0IuYKRkBw8Y/Barber/MHuUCYeQeMlsslRIhXDA/
        FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selectedSaloon.getSaloonID())
                .collection("Babers")
                .whereEqualTo("username", userName) // "username" is the field in the firebase
                .whereEqualTo("password", password)
                .limit(1)
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult().size() > 0){

                                dialogInterface.dismiss();

                                loadingDialog.dismiss();

                                iUserLoginRememberListener.onUserLoginSuccess(userName);
                                // Create Barber
                                Barber barber = new Barber();
                                for(DocumentSnapshot barberSnapShot:task.getResult()){

                                    barber = barberSnapShot.toObject(Barber.class);
                                    barber.setBarberID(barberSnapShot.getId());
                                }

                                iGetBarberListener.onGetBarberSuccess(barber);

                                //Then we will navigate to home and clear all previous activity
                                Intent staffHome = new Intent(context, StaffHomeActivity.class);
                                staffHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                staffHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(staffHome);

                            }
                            else{
                                loadingDialog.dismiss();
                                Toast.makeText(context, "Sai tên người dùng / mật khẩu hoặc sai Saloon", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

    }

    @Override
    public void onClickNegativeButton(DialogInterface dialogInterface) {
        dialogInterface.dismiss();
    }

    // Find the nane and addr field in layout_saloon
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_saloon_name, txt_saloon_address;
        CardView card_saloon;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);


            txt_saloon_name = (TextView)itemView.findViewById(R.id.txt_saloon_name);
            txt_saloon_address = (TextView) itemView.findViewById(R.id.txt_saloon_address);

            card_saloon = (CardView)itemView.findViewById(R.id.card_saloon);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelectedListener(view, getAdapterPosition());
        }
    }
}
