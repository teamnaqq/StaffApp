package com.example.androidbaberstaffapp.Model;

import java.util.List;

public class Invoice {
    private String saloonId, saloonName, saloonAddress;
    private String barberId, barberName;
    private String customerName, customerPhone;
    private String imageUrl;
    private List<CartItem> shoppingItemList;
    private List<BarberServices> barberServicesList;
    private double finalPrice;

    public Invoice() {
    }

    public String getSaloonId() {
        return saloonId;
    }

    public void setSaloonId(String saloonId) {
        this.saloonId = saloonId;
    }

    public String getSaloonName() {
        return saloonName;
    }

    public void setSaloonName(String saloonName) {
        this.saloonName = saloonName;
    }

    public String getSaloonAddress() {
        return saloonAddress;
    }

    public void setSaloonAddress(String saloonAddress) {
        this.saloonAddress = saloonAddress;
    }

    public String getBarberId() {
        return barberId;
    }

    public void setBarberId(String barberId) {
        this.barberId = barberId;
    }

    public String getBarberName() {
        return barberName;
    }

    public void setBarberName(String barberName) {
        this.barberName = barberName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<CartItem> getShoppingItemList() {
        return shoppingItemList;
    }

    public void setShoppingItemList(List<CartItem> shoppingItemList) {
        this.shoppingItemList = shoppingItemList;
    }

    public List<BarberServices> getBarberServicesList() {
        return barberServicesList;
    }

    public void setBarberServicesList(List<BarberServices> barberServicesList) {
        this.barberServicesList = barberServicesList;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }
}



