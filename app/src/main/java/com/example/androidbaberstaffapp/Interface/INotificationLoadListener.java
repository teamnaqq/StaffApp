package com.example.androidbaberstaffapp.Interface;


import com.example.androidbaberstaffapp.Model.MyNotification;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;


public interface INotificationLoadListener {

    void onNotificationLoadSuccess(List<MyNotification> myNotificationList, DocumentSnapshot lastDocument);
    void onNotificationLoadFailed(String message);

}
