package com.example.androidbaberstaffapp.Retrofit;


import com.example.androidbaberstaffapp.Model.FCMResponse;
import com.example.androidbaberstaffapp.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAA3T_JUY:APA91bFGtpRCKUrpUoKUXkgP4efsr71DtZc-VjiOYnQk-h9xaEgL0Dz688IbP__GnaNIfmVEooo_xc0aT6cdvmclmteiBgbKWA9vymOOnXNQkmInikyIkwJkzq464ptTsXiHaDk-rE3Z"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
