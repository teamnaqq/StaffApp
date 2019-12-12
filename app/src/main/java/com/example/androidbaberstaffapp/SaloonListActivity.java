package com.example.androidbaberstaffapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.androidbaberstaffapp.Adapter.MySaloonAdapter;
import com.example.androidbaberstaffapp.Common.Common;
import com.example.androidbaberstaffapp.Common.SpacesItemDecoration;
import com.example.androidbaberstaffapp.Interface.IBranchLoadListener;
import com.example.androidbaberstaffapp.Interface.IGetBarberListener;
import com.example.androidbaberstaffapp.Interface.IOnLoadCountSaloon;
import com.example.androidbaberstaffapp.Interface.IUserLoginRememberListener;
import com.example.androidbaberstaffapp.Model.Barber;
import com.example.androidbaberstaffapp.Model.Saloon;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class SaloonListActivity extends AppCompatActivity implements IOnLoadCountSaloon, IBranchLoadListener, IGetBarberListener, IUserLoginRememberListener {


    @BindView(R.id.txt_saloon_count)
    TextView txt_saloon_count;

    @BindView(R.id.recycler_notification)
    RecyclerView recycler_saloon;

    IOnLoadCountSaloon iOnLoadCountSaloon;
    IBranchLoadListener iBranchLoadListener;

    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saloon_list);

        ButterKnife.bind(this);
        
        initView();
        init();
        loadSaloonBasedOnCity(Common.state_name);
    }

    private void loadSaloonBasedOnCity(String name) {
        dialog.show();
        FirebaseFirestore.getInstance().collection("AllSalon")
                .document(name)
                .collection("Branch")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            iOnLoadCountSaloon.onLoadCountSaloonSuccess(task.getResult().size());

                            List<Saloon> saloonList = new ArrayList<>();
                            for(DocumentSnapshot saloonSnapShot:task.getResult()){
                                Saloon saloon = saloonSnapShot.toObject(Saloon.class);
                                saloon.setSaloonID(saloonSnapShot.getId());
                                saloonList.add(saloon);
                            }
                            iBranchLoadListener.onBranchLoadSuccess(saloonList);

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        iBranchLoadListener.onBranchLoadFailed(e.getMessage());
                    }
                });
    }

    private void init() {

        dialog = new SpotsDialog.Builder().setContext(this)
                .setCancelable(false)
                .build();
        iOnLoadCountSaloon=this;
        iBranchLoadListener=this;
    }

    private void initView() {
        recycler_saloon.setHasFixedSize(true);
        recycler_saloon.setLayoutManager(new GridLayoutManager(this,2));
        recycler_saloon.addItemDecoration(new SpacesItemDecoration(8));
    }

    @Override
    public void onLoadCountSaloonSuccess(int count) {
        txt_saloon_count.setText(new StringBuilder("AllSalon(")
        .append(count)
        .append(")"));

    }

    @Override
    public void onBranchLoadSuccess(List<Saloon> branchList) {
        MySaloonAdapter saloonAdapter = new MySaloonAdapter(this,branchList,this,this);
        recycler_saloon.setAdapter(saloonAdapter);

        dialog.dismiss();
    }

    @Override
    public void onBranchLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        dialog.dismiss();
    }

    @Override
    public void onGetBarberSuccess(Barber barber) {
        Common.currentBarber = barber;
        Paper.book().write(Common.BARBER_KEY, new Gson().toJson(barber));

    }

    @Override
    public void onUserLoginSuccess(String user) {
        // Save user
        Paper.init(this);
        Paper.book().write(Common.LOGGED_KEY, user);
        Paper.book().write(Common.STATE_KEY, Common.state_name);
        Paper.book().write(Common.SALOON_KEY, new Gson().toJson(Common.selectedSaloon));

    }
}
