package com.example.androidbaberstaffapp.Interface;


import com.example.androidbaberstaffapp.Model.ShoppingItem;

import java.util.List;

public interface IShoppingDataLoadListener {
    void onShoppingDataLoadSuccess(List<ShoppingItem> shoppingItemList);
    void onShoppingDataLoadFailed(String message);
}
