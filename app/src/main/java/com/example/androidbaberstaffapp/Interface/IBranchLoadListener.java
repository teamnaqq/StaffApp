package com.example.androidbaberstaffapp.Interface;


import com.example.androidbaberstaffapp.Model.Saloon;

import java.util.List;

public interface IBranchLoadListener {
    void onBranchLoadSuccess(List<Saloon> saloonList);
    void onBranchLoadFailed(String message);

}
