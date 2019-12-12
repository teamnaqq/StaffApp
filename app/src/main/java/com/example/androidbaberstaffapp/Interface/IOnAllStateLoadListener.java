package com.example.androidbaberstaffapp.Interface;


import com.example.androidbaberstaffapp.Model.City;

import java.util.List;

public interface IOnAllStateLoadListener {
    void onAllStateLoadSuccess(List<City> cityList);
    void onAllStateLoadFailed(String message);

}
