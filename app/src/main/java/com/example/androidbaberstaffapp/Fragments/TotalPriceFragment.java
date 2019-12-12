package com.example.androidbaberstaffapp.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.androidbaberstaffapp.Adapter.MyConfirmShoppingItemAdapter;
import com.example.androidbaberstaffapp.Common.Common;
import com.example.androidbaberstaffapp.Model.BarberServices;
import com.example.androidbaberstaffapp.Model.CartItem;
import com.example.androidbaberstaffapp.Model.EventBus.DismissFromBottomSheetEvent;
import com.example.androidbaberstaffapp.Model.FCMResponse;
import com.example.androidbaberstaffapp.Model.FCMSendData;
import com.example.androidbaberstaffapp.Model.Invoice;
import com.example.androidbaberstaffapp.Model.MyToken;
import com.example.androidbaberstaffapp.Model.ShoppingItem;
import com.example.androidbaberstaffapp.R;
import com.example.androidbaberstaffapp.Retrofit.IFCMService;
import com.example.androidbaberstaffapp.Retrofit.RetrofitClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class TotalPriceFragment extends BottomSheetDialogFragment {
    Unbinder unbinder;

    @BindView(R.id.chip_group_services)
    ChipGroup chip_group_services;

    @BindView(R.id.recycler_view_shopping)
    RecyclerView recycler_view_shopping;

    @BindView(R.id.txt_saloon_name)
    TextView txt_saloon_name;

    @BindView(R.id.txt_barber_name)
    TextView txt_barber_name;

    @BindView(R.id.txt_time)
    TextView txt_time;

    @BindView(R.id.txt_customer_name)
    TextView txt_customer_name;

    @BindView(R.id.txt_customer_phone)
    TextView txt_customer_phone;

    @BindView(R.id.txt_total_price)
    TextView txt_total_price;

    @BindView(R.id.btn_confirm)
    TextView btn_confirm;

    HashSet<BarberServices> servicesAdded;
    // List<ShoppingItem> shoppingItemList;

    IFCMService ifcmService;
    //IBottomSheetDialogOnDismissListener iBottomSheetDialogOnDismissListener;

    AlertDialog dialog;

    String image_url;


    private static TotalPriceFragment instance;

    //Part 28
//    public TotalPriceFragment(IBottomSheetDialogOnDismissListener iBottomSheetDialogOnDismissListener) {
//        this.iBottomSheetDialogOnDismissListener = iBottomSheetDialogOnDismissListener;
//    }

    public static TotalPriceFragment getInstance(){
        return instance == null?new TotalPriceFragment():instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_total_price, container,false);

        unbinder = ButterKnife.bind(this, itemView);

        init();

        initView();

        getBundle(getArguments());

        setInformation();
        

        return itemView;
    }

    private void setInformation() {
        txt_saloon_name.setText(Common.selectedSaloon.getName());
        txt_barber_name.setText(Common.currentBarber.getName());
        txt_time.setText(Common.convertTimeSlotToString(Common.currentBookingInformation.getSlot().intValue()));
        txt_customer_name.setText(Common.currentBookingInformation.getCustomerName());
        txt_customer_phone.setText(Common.currentBookingInformation.getCustomerPhone());

        if(servicesAdded.size() > 0)
        {
            // Add to Chip Group
            int i = 0;
            for (BarberServices services:servicesAdded)
            {
                final Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_item, null);
                chip.setText(services.getName());
                chip.setTag(i);
                chip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        servicesAdded.remove((int)v.getTag());
                        chip_group_services.removeView(v);

                        calculatePrice();
                    }
                });
                chip_group_services.addView(chip);

                i++;
            }
        }

        if(Common.currentBookingInformation.getCartItemList() != null)
        {
            if(Common.currentBookingInformation.getCartItemList().size() > 0)
            {
                MyConfirmShoppingItemAdapter adapter = new MyConfirmShoppingItemAdapter(getContext(),Common.currentBookingInformation.getCartItemList());
                recycler_view_shopping.setAdapter(adapter);
            }

            calculatePrice();

        }

    }

    // Updated in part 28
    private double calculatePrice() {
        double price = Common.DEFAULT_PRICE;
        for (BarberServices services:servicesAdded)
            price+=services.getPrice();

        //Part 28
        if(Common.currentBookingInformation.getCartItemList() != null)
        {
            for (CartItem cartItem:Common.currentBookingInformation.getCartItemList())
                price+=(cartItem.getProductPrice())*(cartItem.getProductQuantity());
        }

        txt_total_price.setText(new StringBuilder(Common.MONEY_SIGN)
        .append(price));

        return price;
    }

    private void getBundle(Bundle arguments) {
        this.servicesAdded = new Gson()
                .fromJson(arguments.getString(Common.SERVICES_ADDED),
                        new TypeToken<HashSet<BarberServices>>(){}.getType());

        // part 28
//        this.shoppingItemList = new Gson()
//                .fromJson(arguments.getString(Common.SHOPPING_LIST),
//                        new TypeToken<List<ShoppingItem>>(){}.getType());

        image_url = arguments.getString(Common.IMAGE_DOWNLOADABLE_URL);
    }

    private void initView() {
        recycler_view_shopping.setHasFixedSize(true);
        recycler_view_shopping.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false));

        //Part 25:
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();

                // Update BookingInformation , set done =true
                final DocumentReference bookingSet = FirebaseFirestore.getInstance()
                        .collection("AllSalon")
                        .document(Common.state_name)
                        .collection("Branch")
                        .document(Common.selectedSaloon.getSaloonID())
                        .collection("Babers")
                        .document(Common.currentBarber.getBarberID())
                        .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                        .document(Common.currentBookingInformation.getBookingId());

                bookingSet.get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                if(task.isSuccessful())
                                {
                                    if(task.getResult().exists())
                                    {
                                        // Update
                                        Map<String, Object> dataUpdate = new HashMap<>();
                                        dataUpdate.put("done", true);
                                        bookingSet.update(dataUpdate)
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        dialog.dismiss();
                                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                        {
                                                            // If update is done, create invoice
                                                            createInvoice();

                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    // Part 25:
    private void createInvoice() {

        CollectionReference invoiceRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selectedSaloon.getSaloonID())
                .collection("Invoices");

        Invoice invoice = new Invoice();

        invoice.setBarberId(Common.currentBarber.getBarberID());
        invoice.setBarberName(Common.currentBarber.getName());

        invoice.setSaloonId(Common.selectedSaloon.getSaloonID());
        invoice.setSaloonName(Common.selectedSaloon.getName());
        invoice.setSaloonAddress(Common.selectedSaloon.getAddress());

        invoice.setCustomerName(Common.currentBookingInformation.getCustomerName());
        invoice.setCustomerPhone(Common.currentBookingInformation.getCustomerPhone());

        invoice.setImageUrl(image_url);

        invoice.setBarberServicesList(new ArrayList<BarberServices>(servicesAdded));
        invoice.setShoppingItemList(Common.currentBookingInformation.getCartItemList());
        invoice.setFinalPrice(calculatePrice());

        invoiceRef.document()
                .set(invoice)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            sendNotificationUpdateToUser(Common.currentBookingInformation.getCustomerPhone());
                        }
                    }
                });
    }

    // PArt 25:, Part 26:, Part 28
    private void sendNotificationUpdateToUser(String customerPhone) {

        //Get Token of user first
        FirebaseFirestore.getInstance()
                .collection("Tokens")
                .whereEqualTo("userPhone", customerPhone)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.getResult().size()>0)
                        {
                            MyToken myToken = new MyToken();
                            for (DocumentSnapshot tokenSnapshot:task.getResult())
                                myToken = tokenSnapshot.toObject(MyToken.class);

                            //Create notification to send
                            FCMSendData fcmSendData = new FCMSendData();
                            final Map<String, String> dataSend = new HashMap<>();
                            dataSend.put("update_done", "true");

                            // Part 26: Information needed for Rating
                            dataSend.put(Common.RATING_STATE_KEY, Common.state_name);
                            dataSend.put(Common.RATING_SALOON_ID, Common.selectedSaloon.getSaloonID());
                            dataSend.put(Common.RATING_SALOON_NAME, Common.selectedSaloon.getName());
                            dataSend.put(Common.RATING_BARBER_ID, Common.currentBarber.getBarberID());

                            fcmSendData.setTo(myToken.getToken());
                            fcmSendData.setData(dataSend);

                            ifcmService.sendNotification(fcmSendData)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(Schedulers.newThread())
                                    .subscribe(new Consumer<FCMResponse>() {
                                        @Override
                                        public void accept(FCMResponse fcmResponse) throws Exception {
                                            dialog.dismiss();
                                            dismiss();
                                            //Part 28
                                            //iBottomSheetDialogOnDismissListener.onDismissBottomsheetDialog(true);
                                            EventBus.getDefault()
                                                    .postSticky(new DismissFromBottomSheetEvent(true));
                                        }
                                    }, new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) throws Exception {
                                            Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });


                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(getContext())
                .setCancelable(false)
                .build();

        ifcmService = RetrofitClient.getInstance().create(IFCMService.class);
    }
}
