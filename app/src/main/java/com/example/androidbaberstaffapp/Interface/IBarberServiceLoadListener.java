package com.example.androidbaberstaffapp.Interface;


import com.example.androidbaberstaffapp.Model.BarberServices;

import java.util.List;

public interface IBarberServiceLoadListener {
    void onBarberServicesLoadSuccess(List<BarberServices> barberServicesList);
    void onBarberServicesLoadFailed(String message);
}
