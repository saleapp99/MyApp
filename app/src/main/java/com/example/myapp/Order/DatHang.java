package com.example.myapp.Order;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.loader.content.AsyncTaskLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrinterId;
import android.util.Log;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.HandlerThread;
import android.text.Html;

import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.util.Locale;

import java.util.Properties;


import com.example.myapp.Complete;
import com.example.myapp.MainActivity;
import com.example.myapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class DatHang extends AppCompatActivity {

    //new Paypal
    public static final  String  PAYPAL_KEY = "ARngjN6PNhsNq67WQ3LWZVHJPo2YdC18WtpbWjaDIn6N60rt0eOHro9acC1c495KUAesMUJ3PqYaKnbM";
    private static final int REQUEST_CODE_PAYMENT =1;
    private static final int REQUEST_CODE_FUTURE_PAYMENT =2;
    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;
    private static PayPalConfiguration config;
    PayPalPayment thingsToBuy;
    Button btnPayPal;


    String data;
    private static final String TAG = "DatHang";

    Date date;
    String sEmail, sPassword, email;

    RecyclerView foodRecycler;
    ImageView wn1 , wn2, wn3;
    FoodOrderAdapter FoodOrderAdapter;

    ArrayList<FoodOrder> foodOrders;

    TextView tongtien, tvLocation, txtlocation;
    Button btnTT, btnCFMLocation;

    EditText editGmail, name, phone;

    public Integer finalPrice = 0;


    private GoogleMap mMap;


    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dat_hang);
        tongtien = findViewById(R.id.tongtien);
        btnTT=  findViewById(R.id.btnTT);
        btnPayPal=  findViewById(R.id.btnPaypal);
        Button btnCFMLocation=  (Button) findViewById(R.id.btnCfmLocation);

        this.btnCFMLocation = btnCFMLocation;
        tvLocation = (TextView) findViewById(R.id.tvLocation);

        txtlocation = (TextView) findViewById(R.id.txtLocation); //b??n activity_maps

//        mMap = googleMap;
//        getTapLocation(googleMap);

        editGmail=findViewById(R.id.editMail);
        name = findViewById(R.id.editName);
        phone = findViewById(R.id.editPhone);
        wn1 = findViewById(R.id.wn1);
        wn2 = findViewById(R.id.wn2);
        wn3 = findViewById(R.id.wn3);

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(signInAccount != null){
            name.setText(signInAccount.getDisplayName());
            editGmail.setText(signInAccount.getEmail());
        }
        else {
            name.setText(null);
            editGmail.setText(null);
        }

        try{

            data=MapsActivity.getActivityInstance().getData();
            tvLocation.setText(data);
            Log.d(TAG, "??a?? B????m!!!!!!!!");

        }catch (Exception e) {
            e.printStackTrace();
        }


        ShowBill(); // hien thi thong tin hoa don
        tongtien.setText(finalPrice + " ??");

        // tai khoan gmail smtp:
        sEmail = "hjhj2305@gmail.com";
        sPassword = "rlxpglgnjbqjyteo";
        btnTT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Sendmail().execute();
            }
        });

        btnPayPal.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                MakePaymet();
            }
        });
        ConfigPaypal();
    }

    private void ConfigPaypal() {
        config = new PayPalConfiguration()
                .environment(CONFIG_ENVIRONMENT)
                .clientId(PAYPAL_KEY)
                .merchantName("Paypal Login")
                .merchantPrivacyPolicyUri(Uri.parse("https://www.example.com/privacy"))
                .merchantUserAgreementUri(Uri.parse("https://www.example.com/legal"));
    }

    private void MakePaymet() {
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        startService(intent);

        thingsToBuy = new PayPalPayment(new BigDecimal(String.valueOf("10.45")), "USD", "Payment",PayPalPayment.PAYMENT_INTENT_SALE);
        Intent payment = new Intent(this, PaymentActivity.class);
        payment.putExtra(PaymentActivity.EXTRA_PAYMENT,thingsToBuy);
        payment.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        startActivityForResult(payment,REQUEST_CODE_PAYMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999 && resultCode == RESULT_OK) {

            tvLocation.setText(data.getStringExtra("message"));
        }
        if(requestCode == REQUEST_CODE_PAYMENT){
            if(resultCode == Activity.RESULT_OK){
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

                if(confirm !=null)
                {
                    try {
                        System.out.println(confirm.toJSONObject().toString(4));
                        System.out.println(confirm.getPayment().toJSONObject().toString(4));
                        Toast.makeText(this, "Thanh toa??n tha??nh c??ng",Toast.LENGTH_LONG).show();
                    }
                    catch (JSONException e)
                    {
                        Toast.makeText(this, e.toString(),Toast.LENGTH_LONG).show();
                    }
                }
            }
            else if(requestCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "??a?? hu??y thanh toa??n",Toast.LENGTH_LONG).show();
            }else if(resultCode == PaymentActivity.RESULT_EXTRAS_INVALID){
                Toast.makeText(this,"??a?? bi?? l????i",Toast.LENGTH_LONG).show();
            }
        }else if(requestCode == REQUEST_CODE_FUTURE_PAYMENT){
            if(resultCode == Activity.RESULT_OK){
                PayPalAuthorization auth = data.getParcelableExtra(PayPalFuturePaymentActivity.EXTRA_RESULT_AUTHORIZATION);
                if(auth!=null)
                {
                    try{
                        Log.i("Payment Example", auth.toJSONObject().toString(4));

                        String authorization_code = auth.getAuthorizationCode();
                        Log.d("PaymentExample", authorization_code);

                        Log.e("paypal",  "future Payment code received from Paypal: "+authorization_code);
                    } catch (JSONException e)
                    {
                        Toast.makeText(this, "??a?? bi?? l????i", Toast.LENGTH_LONG).show();
                        Log.e("PaymentExample", "L????i n????ng: ",e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "??a?? hu??y thanh toa??n",Toast.LENGTH_LONG).show();
                Log.d("PaymentExample","User canceled");
            } else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID){
                Toast.makeText(this, "error_occurred",Toast.LENGTH_LONG).show();
                Log.d("PaymentExample","l????i");
            }
        }
    }

    private class Sendmail extends AsyncTask<Message,String,String> {             //G???i mail cho kh??ch h??ng
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(DatHang.this,"Vui l??ng ?????i","??ang ?????t h??ng...",true,false);
        }

        @Override
        protected String doInBackground(Message... messages) {
            Properties properties = new Properties();
            properties.put("mail.smtp.host", "smtp.gmail.com");
            properties.put("mail.smtp.socketFactory.port", "465");
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.port", "465");
            Integer c = 0;

            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(sEmail,sPassword);
                }
            });

            try{
                Message message = new MimeMessage(session); // t???o 1 mail g???i cho ng?????i d??ng


                message.setFrom(new InternetAddress(sEmail));

                message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(editGmail.getText().toString().trim()));

                message.setSubject("Taiducfood - K??nh ch??o qu?? kh??ch");

                MimeBodyPart mimeBodyPart = new MimeBodyPart();
                Multipart multipart  = new MimeMultipart();
                String htmnlHeader = "<table style=font-family:-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;\">\n" +
                        "        <td>\n" +
                        "           <img src='https://firebasestorage.googleapis.com/v0/b/taiducfood.appspot.com/o/119126284_350226082826029_2841802760364491756_n.png?alt=media&token=c63a4718-d56e-4efe-af39-f8b2a0f40b98' alt='asdasd' width=\"250px\" style=\"border-radius: 16px;\">\n" +
                        "        </td>\n" +
                        "        <td style=\"padding-left: 16px;\">\n" +
                        "            <p style=\"font-size: 28px;\">C??ng ty TNHH Th???c Ph???m T??i ?????c</p>\n" +
                        "            <p>Website: <a href='taiducfood.com'> Taiducfood.com</a></p>" +
                        "            <p>?????a ch???: <a> 46/13-46/15 ???????ng T??n C???ng, Ph?????ng 25, Qu???n B??nh Th???nh, TP.HCM, Vi???t Nam</a></p>\n" +
                        "            <p>Email: <a> info.taiducfood@gmail.com</a></p>\n" +
                        "            <p>Phone: <a href=\"tel:0913098639\">091 309 8639</a> - <a href=\"tel:0918698639\">091 869 8639</a> - <a href=\"tel:0935588910\">093 558 8910</a></p>\n" +
                        "        </td>\n" +
                        "    </table>";
                mimeBodyPart.setContent(htmnlHeader,"text/html;charset=utf-8");
                multipart.addBodyPart(mimeBodyPart);

                mimeBodyPart = new MimeBodyPart();
                mimeBodyPart.setText("K??nh ch??o " + name.getText().toString().trim() + "!\n");
                multipart.addBodyPart(mimeBodyPart);

                mimeBodyPart = new MimeBodyPart();
                mimeBodyPart.setText("M?? ????n h??ng: " + "#123456789"+ "\n");
                multipart.addBodyPart(mimeBodyPart);
                ;

                mimeBodyPart = new MimeBodyPart();
                String htmltag = "<h3>Th??ng tin ????n h??ng:</h3>" +
                        "<table border=\"1\" width=\"820px\">\n" +
                        "        <tr>\n" +
                        "            <th>STT</th>\n" +
                        "            <th>T??n s???n ph???m</th>\n" +
                        "            <th>Gi??</th>\n" +
                        "            <th>S??? l?????ng</th>\n" +
                        "            <th>Khuy???n m??i</th>\n" +
                        "            <th>T???ng</th>\n" +
                        "        </tr>";
                mimeBodyPart.setContent(htmltag,"text/html;charset=utf-8");
                multipart.addBodyPart(mimeBodyPart);

                EasyDB easyDB = EasyDB.init(DatHang.this,"ITEM_DB")
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
                    c++;
                    String id =  res.getString(0);
                    String name =  res.getString(1);
                    String priceo = res.getString(2);
                    String price =  res.getString(3);
                    String cout =  res.getString(4);

                    mimeBodyPart = new MimeBodyPart();
                    String htmlcode ="<tr>\n" +
                            "            <th>"+c.toString().trim()+"</th>\n" +
                            "            <th>"+name+"</th>\n" +
                            "            <th>"+priceo+" vnd"+"</th>\n" +
                            "            <th>"+cout+"</th>\n" +
                            "            <th></th>\n" +
                            "            <th>"+price+" vnd"+"</th>\n" +
                            "        </tr>";
                    mimeBodyPart.setContent(htmlcode,"text/html;charset=utf-8");
                    multipart.addBodyPart(mimeBodyPart);
                }

                mimeBodyPart = new MimeBodyPart();
                String htmlfooter = " <tr>\n" +
                        "        <th></th>\n" +
                        "        <th></th>\n" +
                        "        <th></th>\n" +
                        "        <th></th>\n" +
                        "        <th>T???ng ti???n</th>\n" +
                        "        <th>"+finalPrice+" vnd"+"</th>\n" +
                        "        </tr>\n" +
                        "    </table>\n" +
                        "</body>";

                mimeBodyPart.setContent(htmlfooter,"text/html;charset=utf-8");
                multipart.addBodyPart(mimeBodyPart);

                mimeBodyPart = new MimeBodyPart();
                String htmlInfoUser = "<h3 style=\"text-align: start;\">Th??ng tin kh??ch h??ng: </h3>\n" +
                        "    <span style=\"text-align: left;\">\n" +
                        "        <p>\n" +
                        "            <span>T??n kh??ch h??ng: </span> \n" +
                        "            <span>"+name.getText().toString().trim()+"</span>\n" +
                        "        </p>\n" +
                        "        <p>\n" +
                        "            <span>?????a ch??? giao h??ng: </span> \n" +
                        "            <span>"+tvLocation.getText().toString().trim()+"</span>\n" +
                        "        </p>\n" +
                        "        <p>\n" +
                        "            <span>S??? ??i???n tho???i: </span> \n" +
                        "            <span>"+phone.getText().toString().trim()+"</span>\n" +
                        "        </p>\n" +
                        "        <p>\n" +
                        "            <span>Email: </span> \n" +
                        "            <span>"+editGmail.getText().toString().trim()+"</span>\n" +
                        "        </p>\n" +
                        "        <p style=\"font-weight: bold; font-size: 18px;\">\n" +
                        "            <span>H??nh th???c thanh to??n:</span>\n" +
                        "            <span>Thanh to??n khi nh???n h??ng</span>\n" +
                        "        </p>\n" +
                        "    </span>";

                mimeBodyPart.setContent(htmlInfoUser,"text/html;charset=utf-8");
                multipart.addBodyPart(mimeBodyPart);

                mimeBodyPart = new MimeBodyPart();
                String htmlfooter1 = "<footer style=\"text-align: center;\" >\n" +
                        "    <p>\n" +
                        "        C???m ??n qu?? kh??ch ???? s??? d???ng d???ch v??? c???a ch??ng t??i. Nh??n vi??n c???a ch??ng t??i s??? li??n l???c cho qu?? kh??ch trong th???i gian s???m nh???t.\n" +
                        "    </p>\n" +
                        "    <p>B???n quy???n c???a Taiducfood ?? 2020. B???o l??u m???i quy???n.</p>\n" +
                        "</footer>";

                mimeBodyPart.setContent(htmlfooter1,"text/html;charset=utf-8");
                multipart.addBodyPart(mimeBodyPart);

                message.setContent(multipart);

                Transport.send(message);

                return "Success";

            } catch (MessagingException e) {
                e.printStackTrace();
                return "Error";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            if(s.equals("Success"))
            {
                Pay();
                Intent intent = new Intent(DatHang.this,Complete.class);
                startActivity(intent);
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(DatHang.this);
                builder.setTitle(Html.fromHtml("<font color='#509324'>Th??ng b??o</font>"));
                builder.setMessage("?????t h??ng kh??ng th??nh c??ng! Xin vui l??ng th??? l???i");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        }
    } //g???i mail cho kh??ch h??ng


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

                FoodOrder foodOrder = new FoodOrder(""+name,""+price+" ??",""+cout,""+id,""+priceo+" ??");
                foodOrders.add(foodOrder);
                finalPrice += Integer.parseInt(price);
            }
            setFoodRecycler(foodOrders);
        }catch (Exception ex)
        {
            Toast.makeText(this,"Kh??ng c?? s???n ph???m",Toast.LENGTH_SHORT).show();
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

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        //final Intent data
//        //super.onActivityResult(requestCode, resultCode, data);
//
//    }

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
