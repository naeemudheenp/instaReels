package macbeth.ReelsDownloader;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Bundle;
import android.os.StrictMode;
import android.app.ProgressDialog;
import android.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.net.ssl.HttpsURLConnection;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class MainActivity extends AppCompatActivity {
    public String instaurl="empty";

    private ProgressDialog progressBar2;
    public ClipboardManager clipboardManager;
    public String temp="nothing";
    public String urll=null;
    public boolean network=false;
    public String search="instagram";
    public AlertDialog alertDialog;
    public AlertDialog alertDialog1;
    public  AlertDialog alertDialog3;
    public Integer usage;
    public AdView adView;
    public InterstitialAd mInterstitialAd;
    public Integer runtime;
    public AlertDialog alertDialog4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AlertDialog.Builder dialog3=new AlertDialog.Builder(this);
        dialog3.setMessage("Please turn on data.").setCancelable(true);
        dialog3.setTitle("No internet.");


        dialog3.setPositiveButton("Exit",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.super.onBackPressed();
                    }
                });
        network=isNetworkavailable();//checks internet connectivity
        if(!network){
            alertDialog3=dialog3.create();
            alertDialog3.show();

        }


        //Call the function to initialize AdMob SDK
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        //get the reference to your FrameLayout
        adView= findViewById(R.id.adView);

        //Create an AdView and put it into your FrameLayout


        AdRequest adRequest = new AdRequest.Builder().build();


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6480505866670634/3719683813");

        adView.loadAd(adRequest);
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        if (Build.VERSION.SDK_INT >= 23) {

            //request permission to access storage
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }







        clipboardManager=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        ClipData data = clipboardManager.getPrimaryClip();

        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setMessage("Video downloading please wait!").setCancelable(false);
        dialog.setTitle("Video downloading");

        alertDialog=dialog.create();

        AlertDialog.Builder dialog1=new AlertDialog.Builder(this);
        dialog1.setMessage("Download completed.").setCancelable(true);
        dialog1.setTitle("Download completed");

        dialog1.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog1.dismiss();
                    }
                });
               alertDialog1=dialog1.create();

               //get incoming data
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent); // Handle multiple images being sent
            }
        }





        final AlertDialog.Builder dialog4=new AlertDialog.Builder(this);
        dialog4.setMessage("How to use ?\n1.Copy the link of instagram video you want to download." +
                "\n2.Open Insta Reels app." +
                "\n3.Your video will download automatically." +
                "\nOR" +
                "\n1.Select instagram video you want to download." +
                "\n2.Click on more option." +
                "\n3.Select Share to option." +
                "\n4.Select Insta Reels app from list." +
                "\n5.Your video will download automatically." +
                "\nPermission required." +
                "\nStorage:To download and save video to device." +
                "\nYou can download instagram video,reels and igtv.").setCancelable(true);
        dialog4.setTitle("Hello!");


        dialog4.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog4.dismiss();
                    }
                });
        alertDialog4=dialog4.create();
        runtimechecker();
    }




    public void runtimechecker(){
        SharedPreferences runtime_database = getSharedPreferences("instareels", MODE_PRIVATE);
        runtime = runtime_database.getInt("run", 0);//"No name defined" is the default value.
        if(runtime.intValue()==0){
            SharedPreferences.Editor editordb = getSharedPreferences("instareels", MODE_PRIVATE).edit();
            editordb.putInt("run", 1);
            editordb.apply();
            alertDialog4.show();

        }
    }

    public void checkusage(){
        SharedPreferences prefs = getSharedPreferences("instareels", MODE_PRIVATE);
        usage = prefs.getInt("usage", 0);//"No name defined" is the default value.

        if(usage.intValue()==5){
            usage=0;
            SharedPreferences.Editor editor = getSharedPreferences("instareels", MODE_PRIVATE).edit();
            editor.putInt("usage", usage);
            editor.apply();
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            } else {

            }
        }else{
            usage=usage+1;
            SharedPreferences.Editor editor = getSharedPreferences("instareels", MODE_PRIVATE).edit();
            editor.putInt("usage", usage);
            editor.apply();
        }

    }

    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            instaurl=sharedText;
            temp=instaurl;
            ClipboardManager clipboard2 = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("instagram url",instaurl);
            clipboard2.setPrimaryClip(clip);

            findcookie();
        }
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            // Update UI to reflect image being shared
        }
    }

    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            // Update UI to reflect multiple images being shared
        }




    }




public  void startdownload(){


    Toast.makeText(getApplicationContext(),"Download started.",Toast.LENGTH_LONG).show();
    Date currentTime = Calendar.getInstance().getTime();



        DownloadManager.Request request=new DownloadManager.Request(Uri.parse(urll));

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle("INSTA REELS");
        request.setDescription("Your instagram video is downloading");
        request.allowScanningByMediaScanner();
        request.setVisibleInDownloadsUi(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES,"instareelsvideo"+currentTime+".mp4");
        DownloadManager manager=(DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);

        manager.enqueue(request);

        progressBar2 = ProgressDialog.show(MainActivity.this, "INSTA REELS", "Downloading please wait.This may take time based on your internet speed.Check notification to know about download progress.");

        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));









}

    BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            if (progressBar2.isShowing()) {
                progressBar2.dismiss();
            }
            checkusage();
            Toast toastt = Toast.makeText(getApplicationContext(), "Download completed", Toast. LENGTH_LONG);
            toastt.show();
            alertDialog1.show();


        }
    };








    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(hasFocus) {

            if (!(clipboardManager.hasPrimaryClip())) {


            } else if (!(clipboardManager.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {

                Toast toastt = Toast. makeText(getApplicationContext(), "Please copy instagram link of the video.Error2.", Toast. LENGTH_SHORT);
                toastt.show();

            } else {

                //since the clipboard contains plain text.

                ClipData.Item item = clipboardManager.getPrimaryClip().getItemAt(0);
                temp=instaurl;
                // Gets the clipboard as text.
                instaurl = item.coerceToText(getBaseContext()).toString();
                boolean correct = instaurl.equals(temp);
                if(!correct){

                    if ( instaurl.toLowerCase().indexOf(search.toLowerCase()) != -1 ) {


                        findcookie();
                    } else {

                        Toast toastt = Toast. makeText(getApplicationContext(), "Please copy instagram link of the video.Error4.", Toast. LENGTH_SHORT);
                        toastt.show();

                    }}


            }

        }

    }

    public void findcookie() {
                    Toast toastt = Toast. makeText(getApplicationContext(), "Please wait", Toast. LENGTH_SHORT);
                    toastt.show();
                    urll = instaurl + "/?__a=1";

                    String json = getJSON(urll);
                    JSONObject obj;

                    try {
                        obj = new JSONObject(json);
                        JSONArray results_arr = obj.getJSONArray("results");
                        final int n = results_arr.length();
                        for (int i = 0; i < n; ++i) {
                            // get the place id of each object in JSON (Google Search API)
                           String video_url = results_arr.getJSONObject(i).getString("video_url");
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    urll=json;
                    try {
                        String[] temp2=urll.split("\"og:video\" content=\"");
                        urll=temp2[1];
                        String[] temp3=urll.split("\" />");
                        urll=temp3[0];
                        startdownload();
                    }
                    catch (Exception ex){
                        Toast.makeText(getApplicationContext(),"Use proper instagram url.You cannot download from Private profile or image.",Toast.LENGTH_LONG).show();
                        Log.d("nod",ex.toString());
                    }



}

    public static String getJSON(String url) {
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        HttpsURLConnection con = null;
        try {
            URL u = new URL(url);
            con = (HttpsURLConnection) u.openConnection();

            con.connect();


            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            return sb.toString();


        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (con != null) {
                try {
                    con.disconnect();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }








    public boolean isNetworkavailable(){
        ConnectivityManager  connection=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activenetworkinfo=connection.getActiveNetworkInfo();
        return activenetworkinfo!=null && activenetworkinfo.isConnected();
    }





    public void onBackPressed() {
        super.onBackPressed();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission to download instagram contents.Please allow permission manually or open again", Toast.LENGTH_SHORT).show();
                    MainActivity.super.onBackPressed();
                }
                return;
            }



            // other 'case' lines to check for other
            // permissions this app might request
        }
    }






}




