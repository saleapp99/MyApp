package com.example.myapp.Order;

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.myapp.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class DatHang extends AppCompatActivity {

    String data;
    private static final String TAG = "DatHang";
    RecyclerView foodRecycler;
    FoodOrderAdapter FoodOrderAdapter;

    ArrayList<FoodOrder> foodOrders;
    TextView tongtien, tvLocation, txtlocation;
    Button btnTT, btnCFMLocation;
    public Integer finalPrice = 0;


    private GoogleMap mMap;


    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dat_hang);
        tongtien = findViewById(R.id.tongtien);
        btnTT=  findViewById(R.id.btnTT);
        Button btnCFMLocation=  (Button) findViewById(R.id.btnCfmLocation);
        this.btnCFMLocation = btnCFMLocation;
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        txtlocation = (TextView) findViewById(R.id.txtLocation);

//        mMap = googleMap;
//        getTapLocation(googleMap);
        try{

                data=MapsActivity.getActivityInstance().getData();
                tvLocation.setText(data);
                Log.d(TAG, "đã Bấm!!!!!!!!");

        }catch (Exception e) {
            e.printStackTrace();
        }

        ShowBill();
        tongtien.setText(finalPrice + " đ");
        btnTT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Pay();
                Toast.makeText(DatHang.this, "Đặt hàng thành công!",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setFoodRecycler(List<FoodOrder> foodorder_spList)
    {
        foodRecycler = findViewById(R.id.bill_recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
        foodRecycler.setLayoutManager(layoutManager);
        FoodOrderAdapter = new FoodOrderAdapter(this,foodorder_spList);
        foodRecycler.setAdapter(FoodOrderAdapter);
    }


    void ShowBill()
    {
        foodOrders = new ArrayList<>();

        try {
            EasyDB easyDB = EasyDB.init(this,"ITEM_DB")
                    .setTableName("ITEMS_TABLE")
                    .addColumn(new Column("ID", new String[]{"text","unique"}))
                    .addColumn(new Column("itemName", new String[]{"text","not null"}))
                    .addColumn(new Column("itemPrice", new String[]{"text","not null"}))
                    .addColumn(new Column("itemFinal", new String[]{"text","not null"}))
                    .addColumn(new Column("itemNumber", new String[]{"text","not null"}))
                    .doneTableColumn();
            Cursor res = easyDB.getAllData();
            while (res.moveToNext())
            {
                String id =  res.getString(0);
                String name =  res.getString(1);
                String priceo = res.getString(2);
                String price =  res.getString(3);
                String cout =  res.getString(4);

                FoodOrder foodOrder = new FoodOrder(""+name,""+price+" đ",""+cout,""+id,""+priceo+" đ");

                foodOrders.add(foodOrder);
                finalPrice += Integer.parseInt(price);
            }
            setFoodRecycler(foodOrders);
        }catch (Exception ex)
        {
            Toast.makeText(this,"Không có sản phẩm",Toast.LENGTH_SHORT).show();
        }
    }

    void Pay()
    {
        EasyDB easyDB = EasyDB.init(this,"ITEM_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("ID", new String[]{"text","unique"}))
                .addColumn(new Column("itemName", new String[]{"text","not null"}))
                .addColumn(new Column("itemPrice", new String[]{"text","not null"}))
                .addColumn(new Column("itemFinal", new String[]{"text","not null"}))
                .addColumn(new Column("itemNumber", new String[]{"text","not null"}))
                .doneTableColumn();
        easyDB.deleteAllDataFromTable();


    }

    public void onBackPressed(){
        super.onBackPressed();
    }
    public void btnBack(View view) {
        super.onBackPressed();
    }

    public void LocationBtnOnClick(View view) {
        startActivityForResult(new Intent(getApplicationContext(), MapsActivity.class),999);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data)
    {
        if(requestCode == 999 && resultCode == RESULT_OK)
        {
            tvLocation.setText(data.getStringExtra("message"));
        }
    }

//
//    public String getAddress(double lat, double lon){
//
//        String address = "";
//
//        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//
//        List<Address> addresses;
//
//        try{
//
//            addresses = geocoder.getFromLocation(lat,lon,1);
//            if(addresses.size() > 0)
//            {
//                address = addresses.get(0).getAddressLine(0);
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return address;
//    }
//
//    private void getTapLocation(final GoogleMap googleMap) {
//
//        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
//            @Override
//            public void onCameraIdle() {
//                LatLng tapLocation = googleMap.getCameraPosition().target;
//                tvLocation.setText(getAddress(tapLocation.latitude,tapLocation.longitude));
//            }
//        });
//    }
//    btnCFMLocation.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//
//        }
//    });
}
