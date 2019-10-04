package com.proyek.rahmanjai.eatit;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class checksum extends AppCompatActivity implements PaytmPaymentTransactionCallback {

    String custid="",amt="";
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checksum);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Intent intent_ = getIntent();
        //orderId = intent.getExtras().getString("orderid");
        custid = intent_.getExtras().getString("custid");
        amt = intent_.getExtras().getString("amount");
        intent = new Intent();

        //mid = "Khdxef18236530146319";
        sendUserDetailTOServerdd dl = new sendUserDetailTOServerdd();
        dl.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public class sendUserDetailTOServerdd extends AsyncTask<ArrayList<String>, Void, String> {

        private ProgressDialog dialog = new ProgressDialog(checksum.this);


        String url ="https://us-central1-fffa-e130c.cloudfunctions.net/generate_checksum";//TODO your server's url here (www.xyz/checksumGenerate.php)
        String CHECKSUMHASH ="";
        JSONObject jsonObject;

        InputStream is = null;
        JSONObject jObj = null;

        HttpURLConnection urlConnection = null;


        private JSONObject makeHttpRequest(String url, String method, String params) {


            try {
                String retSrc="";

                URL url1 = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) url1.openConnection();
                if (method == "POST") {
                    // request method is POST
                    Log.i("Params" , params);
                    urlConnection.setDoOutput(true);
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setFixedLengthStreamingMode(params.getBytes().length);
                    urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                    out.print(params);
                    out.close();
                }
                InputStream in = urlConnection.getInputStream();

                InputStreamReader isw = new InputStreamReader(in);

                byte[] bytes = new byte[10000];
                StringBuilder x = new StringBuilder();
                int numRead = 0;
                while ((numRead = in.read(bytes)) >= 0) {
                    x.append(new String(bytes, 0, numRead));
                }
                retSrc=x.toString();
                Log.i("resFromServer" , retSrc);

                jObj = new JSONObject(retSrc);
            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText( checksum.this , "Connectivity issue. Please try again later.", Toast.LENGTH_LONG).show();
                    }
                });
                return null;
            }finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return jObj;
        }

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Please wait");
            this.dialog.show();
        }

        protected String doInBackground(ArrayList<String>... alldata) {
            String param=
                    "&CUST_ID="+custid+
                            "&TXN_AMOUNT="+amt;

            jsonObject = makeHttpRequest(url,"POST",param);
            Log.e("CheckSum result >>",jsonObject.toString());
            if(jsonObject != null){
                Log.e("CheckSum result >>",jsonObject.toString());
                try {

                    CHECKSUMHASH=jsonObject.has("CHECKSUMHASH")?jsonObject.getString("CHECKSUMHASH"):"";
                    Log.e("CheckSum result >>",CHECKSUMHASH);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return CHECKSUMHASH;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(" setup acc ","  signup result  " + result);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            PaytmPGService Service = PaytmPGService.getStagingService();
            HashMap<String, String> paramMap = new HashMap<String, String>();
            try {
                paramMap.put("MID", jsonObject.get("MID").toString());
                paramMap.put("ORDER_ID", jsonObject.get("ORDER_ID").toString());
                paramMap.put("CUST_ID", jsonObject.get("CUST_ID").toString());
                paramMap.put("CHANNEL_ID", jsonObject.get("CHANNEL_ID").toString());
                paramMap.put("TXN_AMOUNT", jsonObject.get("TXN_AMOUNT").toString());
                paramMap.put("WEBSITE", jsonObject.get("WEBSITE").toString());
                paramMap.put("CALLBACK_URL" ,jsonObject.get("CALLBACK_URL").toString());
                paramMap.put("CHECKSUMHASH" ,jsonObject.get("CHECKSUMHASH").toString());
                paramMap.put("INDUSTRY_TYPE_ID", jsonObject.get("INDUSTRY_TYPE_ID").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            PaytmOrder Order = new PaytmOrder(paramMap);
            Log.e("checksum ", "param "+ paramMap.toString());
            Service.initialize(Order,null);

            Service.startPaymentTransaction(checksum.this, true, true,
                    checksum.this  );
        }

    }


    @Override
    public void onTransactionResponse(Bundle bundle) {
        Log.e("paytmResponce ", " respon true " + bundle.toString());
        //Toast.makeText(this, "Payment successful", Toast.LENGTH_LONG).show();
        if(bundle.getString("STATUS").equals("TXN_SUCCESS")){
            Log.i("BundleResp" ,"Working" );
            Log.i("BundleResp ",bundle.getString("ORDERID"));
            intent.putExtra("TXNID", bundle.getString("TXNID"));
            intent.putExtra("ORDER_ID", bundle.getString("ORDERID"));
            intent.putExtra("TXN",bundle);
            setResult(RESULT_OK, intent);
        }
        else{
            intent.putExtra("ERROR" , bundle);
            setResult(RESULT_CANCELED, intent);
        }
        finish();
    }

    @Override
    public void networkNotAvailable() {
        intent.putExtra("ERROR", " Network Error ");
        setResult(RESULT_CANCELED, intent);
        finish();
        Log.e("checksum ", " Network Error ");
    }

    @Override
    public void clientAuthenticationFailed(String s) {
        intent.putExtra("ERROR", " Client Authentication Failed. ");
        setResult(RESULT_CANCELED, intent);
        finish();
        Log.e("checksum ", " clientAuthenticationFailed "+ s );
    }

    @Override
    public void someUIErrorOccurred(String s) {
        intent.putExtra("ERROR", "UI failed to load.");
        setResult(RESULT_CANCELED, intent);
        finish();
        Log.e("checksum ", " ui fail respon  "+ s );
    }

    @Override
    public void onErrorLoadingWebPage(int i, String s, String s1) {
        intent.putExtra("ERROR", "Failed to Load web page.");
        setResult(RESULT_CANCELED, intent);
        finish();
        Log.e("checksum ", " error loading pagerespon true "+ s + "  s1 " + s1);
    }

    @Override
    public void onBackPressedCancelTransaction() {
        intent.putExtra("ERROR", "Transaction cancelled.");
        setResult(RESULT_CANCELED, intent);
        finish();
        Log.e("checksum ", " cancel call back respon  " );
    }

    @Override
    public void onTransactionCancel(String s, Bundle bundle) {
        intent.putExtra("ERROR", "Transaction cancelled.");
        setResult(RESULT_CANCELED, intent);
        finish();
        Log.e("checksum ", "  transaction cancel " );
    }
}
