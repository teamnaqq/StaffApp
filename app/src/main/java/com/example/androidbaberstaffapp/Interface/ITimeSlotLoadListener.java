package com.example.androidbaberstaffapp.Interface;


import com.example.androidbaberstaffapp.Model.BookingInformation;

import java.util.List;

public interface ITimeSlotLoadListener {
    void onTimeSlotLoadSuccess(List<BookingInformation> timeSlotList);
    void onTimeSlotLoadFailed(String message);
    void onTimeSlotLoadEmpty();
}
