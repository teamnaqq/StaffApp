package com.example.androidbaberstaffapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import com.example.androidbaberstaffapp.Common.Common;
import com.example.androidbaberstaffapp.Fragments.ShoppingFragment;
import com.example.androidbaberstaffapp.Fragments.TotalPriceFragment;
import com.example.androidbaberstaffapp.Interface.IBarberServiceLoadListener;
import com.example.androidbaberstaffapp.Interface.IOnShoppingItemSelected;
import com.example.androidbaberstaffapp.Model.BarberServices;
import com.example.androidbaberstaffapp.Model.CartItem;
import com.example.androidbaberstaffapp.Model.EventBus.DismissFromBottomSheetEvent;
import com.example.androidbaberstaffapp.Model.ShoppingItem;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;

public class DoneServiceActivity extends AppCompatActivity implements IBarberServiceLoadListener, IOnShoppingItemSelected {

    private static final int MY_CAMERA_REQUEST_CODE = 1000;
    @BindView(R.id.txt_customer_name)
    TextView txt_customer_name;

    @BindView(R.id.txt_customer_phone)
    TextView txt_customer_phone;

    @BindView(R.id.chip_group_services)
    ChipGroup chip_group_services;

    @BindView(R.id.chip_group_shopping)
    ChipGroup chip_group_shopping;

//    @BindView(R.id.edt_services)
//    AppCompatAutoCompleteTextView edt_services;

    @BindView(R.id.img_customer_hair)
    ImageView img_customer_hair;

    @BindView(R.id.add_item)
    ImageView add_item;

    @BindView(R.id.btn_finish)
    Button btn_finish;

    @BindView(R.id.rdi_no_picture)
    RadioButton rdi_no_picture;

    @BindView(R.id.rdi_picture)
    RadioButton rdi_picture;


    AlertDialog dialog;
    IBarberServiceLoadListener iBarberServiceLoadListener;

    HashSet<BarberServices> serviceAdded = new HashSet<>();

    //Part 28:
    // List<ShoppingItem> shoppingItems = new ArrayList<>();

    LayoutInflater inflater;

    Uri fileUri;
    StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done_service);

        ButterKnife.bind(this);

        init();

        //Part 22:
        initView();
        

        // Part 20:
        setCustomerInformation();

        // Part 21:
        loadBarberServices();


    }

    private void initView() {

        // Part 25;
        rdi_picture.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    img_customer_hair.setVisibility(View.VISIBLE);
                    btn_finish.setEnabled(false);
                }
            }
        });

        rdi_no_picture.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    img_customer_hair.setVisibility(View.GONE);
                    btn_finish.setEnabled(true);
                }
            }
        });


        getSupportActionBar().setTitle("Checkout");

        // Part 23
        btn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(rdi_no_picture.isChecked())
                {
                    dialog.dismiss();

                    TotalPriceFragment fragment = TotalPriceFragment.getInstance();
                    Bundle bundle = new Bundle();
                    bundle.putString(Common.SERVICES_ADDED, new Gson().toJson(serviceAdded));
                    //bundle.putString(Common.SHOPPING_LIST, new Gson().toJson(shoppingItems));

                    fragment.setArguments(bundle);
                    fragment.show(getSupportFragmentManager(),"Price");
                }
                else
                {
                    uploadPicture(fileUri);
                }
            }
        });

        // Part 22
        img_customer_hair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());

                fileUri = getOutputMediaFileUri();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent, MY_CAMERA_REQUEST_CODE);


            }
        });


        add_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShoppingFragment shoppingFragment = ShoppingFragment.getInstance(DoneServiceActivity.this);
                shoppingFragment.show(getSupportFragmentManager(), "Shopping");

            }
        });
    }

    //part 23
    //Part 28 3530: Updated
    private void uploadPicture(Uri fileUri) {
        if(fileUri != null)
        {
            dialog.show();

            String fileName = Common.getFileName(getContentResolver(),fileUri);
            String path = new StringBuilder("Customer_Pictures/")
                    .append(fileName)
                    .toString();
            storageReference = FirebaseStorage.getInstance().getReference(path);

            UploadTask uploadTask = storageReference.putFile(fileUri);
            
            // Create task
            Task<Uri> task = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful())
                        Toast.makeText(DoneServiceActivity.this, "Failed to upload", Toast.LENGTH_SHORT).show();
                        return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful())
                    {
                        String url = task.getResult().toString()
                                .substring(0,
                                        task.getResult().toString().indexOf("&token"));
                        Log.d("Downloadable Link", url);
                        dialog.dismiss();

                        // Part 24: Create Fragment of total price
                        TotalPriceFragment fragment = TotalPriceFragment.getInstance();
                        Bundle bundle = new Bundle();
                        bundle.putString(Common.SERVICES_ADDED, new Gson().toJson(serviceAdded));
                       // bundle.putString(Common.SHOPPING_LIST, new Gson().toJson(shoppingItems));
                        bundle.putString(Common.IMAGE_DOWNLOADABLE_URL, url);
                        fragment.setArguments(bundle);
                        fragment.show(getSupportFragmentManager(),"Price");
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.dismiss();
                    Toast.makeText(DoneServiceActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
        else
        {
            Toast.makeText(this, "Image is empty", Toast.LENGTH_SHORT).show();
        }
        
    }

    //Part 23
    private Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    // Part 23
    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "BarberStaffApp" );
        if(!mediaStorageDir.exists())
        {
            if(!mediaStorageDir.mkdir())
            {
                return null;
            }
        }

        String time_stamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath()+ File.separator+"IMG_"
        +time_stamp+"_"+new Random().nextInt()+".jpg");

        return mediaFile;
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this)
                .setCancelable(false)
                .build();

        iBarberServiceLoadListener = this;
        inflater = LayoutInflater.from(this);
    }

    private void loadBarberServices() {
        dialog.show();

        FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selectedSaloon.getSaloonID())
                .collection("Services")
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                            iBarberServiceLoadListener.onBarberServicesLoadFailed(e.getMessage());
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful())
                        {
                            List<BarberServices> barberServices = new ArrayList<>();
                            for(DocumentSnapshot barberSnapShot:task.getResult())
                            {
                                BarberServices services = barberSnapShot.toObject(BarberServices.class);
                                barberServices.add(services);
                            }
//                            iBarberServiceLoadListener.onBarberServicesLoadSuccess(barberServices);
                            dialog.dismiss();
                        }
                    }
                });

    }

    private void setCustomerInformation() {
        txt_customer_name.setText(Common.currentBookingInformation.getCustomerName());
        txt_customer_phone.setText(Common.currentBookingInformation.getCustomerPhone());
    }

    //Part 28:
    @Override
    public void onBarberServicesLoadSuccess(final List<BarberServices> barberServicesList) {
        final List<String> nameServices = new ArrayList<>();
        //Sort alpha-beta
        Collections.sort(barberServicesList, new Comparator<BarberServices>() {
            @Override
            public int compare(BarberServices o1, BarberServices o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        // Add all name of services after sorting
        for(BarberServices barberServices:barberServicesList)
            nameServices.add(barberServices.getName());

        // Create Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_item, nameServices);
        //edt_services.setThreshold(1); // will start working from first character
       // edt_services.setAdapter(adapter);
        //edt_services.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
//                // Add to Chip group
//                int index = nameServices.indexOf(edt_services.getText().toString().trim());
//
//                if(!serviceAdded.contains(barberServicesList.get(index))) {
//                    // We don't want to have duplicate service in list so we use HashSet
//                    serviceAdded.add(barberServicesList.get(index));
//
//                    final Chip item = (Chip) inflater.inflate(R.layout.chip_item, null);
//                    item.setText(edt_services.getText().toString());
//                    item.setTag(i);
//                    edt_services.setText("");
//
//                    item.setOnCloseIconClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            chip_group_services.removeView(v);
//                            serviceAdded.remove((int) item.getTag());
//                        }
//                    });
//
//                    chip_group_services.addView(item);
//                }
//                else
//                {
//                    edt_services.setText("");
//                }
//            }
//        });
//
//        //part 28
//        loadExtraItems();

    }

    @Override
    public void onBarberServicesLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @Override
    public void onShoppingItemSelected(ShoppingItem shoppingItem) {
        // Here we will create an List to hold shopping Item
        // shoppingItems.add(shoppingItem);
        // Log.d("ShoppingItem", ""+shoppingItems.size());

        //Part 28:
        CartItem cartItem = new CartItem();
        cartItem.setProductId(shoppingItem.getId());
        cartItem.setProductImage(shoppingItem.getImage());
        cartItem.setProductName(shoppingItem.getName());
        cartItem.setProductPrice(shoppingItem.getPrice());
        cartItem.setProductQuantity(1);
        cartItem.setUserPhone(Common.currentBookingInformation.getCustomerPhone());

        if(Common.currentBookingInformation.getCartItemList() == null) // If user submit empty cart
            Common.currentBookingInformation.setCartItemList(new ArrayList<CartItem>());

        boolean flag = false; // We will use this flag to update cart item quantity increase by 1
                              // if alreadyhave item with same name in cart

        for(int i=0; i<Common.currentBookingInformation.getCartItemList().size(); i++)
        {
            if(Common.currentBookingInformation.getCartItemList().get(i).getProductName()
            .equals(shoppingItem.getName()))
            {
                flag =true; //Enable Flag
                CartItem itemUpdate = Common.currentBookingInformation.getCartItemList().get(i);
                itemUpdate.setProductQuantity(itemUpdate.getProductQuantity()+1);
                Common.currentBookingInformation.getCartItemList().set(i, itemUpdate); // Update the list
            }
        }

        if(!flag)// If flag = false
        {
            Common.currentBookingInformation.getCartItemList().add(cartItem);

            // Add shopping item to chip group
            final Chip item = (Chip) inflater.inflate(R.layout.chip_item, null);
            item.setText(cartItem.getProductName());
            item.setTag(Common.currentBookingInformation.getCartItemList().indexOf(cartItem));


            item.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chip_group_shopping.removeView(v);
                    Common.currentBookingInformation.getCartItemList().remove((int) item.getTag());
                }
            });
            chip_group_shopping.addView(item);

        }
        else // flag = true, item update
        {
            chip_group_shopping.removeAllViews();
            loadExtraItems();

        }

        dialog.dismiss();
    }

    private void loadExtraItems() {
        if(Common.currentBookingInformation.getCartItemList() != null)
        {
            for(CartItem cartItem:Common.currentBookingInformation.getCartItemList())
            {
                final Chip item = (Chip) inflater.inflate(R.layout.chip_item, null);
                item.setText(new StringBuilder(cartItem.getProductName())
                    .append(" x")
                    .append(cartItem.getProductQuantity()));
                item.setTag(Common.currentBookingInformation.getCartItemList().indexOf(cartItem));

                item.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        chip_group_shopping.removeView(v);
                        Common.currentBookingInformation.getCartItemList().remove((int) item.getTag());
                    }
                });
                chip_group_shopping.addView(item);
            }
        }
        dialog.dismiss();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MY_CAMERA_REQUEST_CODE)
        {
            if(resultCode == RESULT_OK)
            {
                Bitmap bitmap = null;
                ExifInterface ei = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),fileUri);
                    ei = new ExifInterface(getContentResolver().openInputStream(fileUri));

                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                    Bitmap rotateBitmap = null;

                    switch (orientation)
                    {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotateBitmap = rotateImage(bitmap, 90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotateBitmap = rotateImage(bitmap, 180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotateBitmap = rotateImage(bitmap, 270);
                            break;
                        case ExifInterface.ORIENTATION_NORMAL:
                        default:
                            rotateBitmap = bitmap;
                            break;
                    }
                    img_customer_hair.setImageBitmap(rotateBitmap);
                    btn_finish.setEnabled(true);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Bitmap rotateImage(Bitmap bitmap, int i) {
        Matrix matrix = new Matrix();
        matrix.postRotate(i);
        return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    }

    // Part 28
  //  @Override
    public void onDismissBottomsheetDialog(boolean fromButton) {
        if(fromButton){ // == trues
            finish();
        }
    }

    // Part 28: Convert to Event Bus

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void dismissDialog(DismissFromBottomSheetEvent event)
    {
        if(event.isButtonClick())
            finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
