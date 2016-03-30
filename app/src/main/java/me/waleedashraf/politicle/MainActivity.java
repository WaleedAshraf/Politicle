package me.waleedashraf.politicle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;


public class MainActivity extends AppCompatActivity {
    /**
     * Called when the activity is first created.
     */
    CheckBox blockAll_cb;//,blockcontacts_cb;
    Button upload;
    BroadcastReceiver CallBlocker;
    TelephonyManager telephonyManager;
    ITelephony telephonyService;
    EditText mNum;
    HttpURLConnection urlConn;
    Context mContext;
    ProgressDialog pDialog;

    boolean check = true;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initviews();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#006666"));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>Politcle</font>"));
        mNum = (EditText) findViewById(R.id.editText);
        blockAll_cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                CallBlocker = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {

                        // TODO Auto-generated method stub
                        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                        //Java Reflections
                        Class c = null;
                        mContext = context;
                        try {
                            c = Class.forName(telephonyManager.getClass().getName());
                        } catch (ClassNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Method m = null;
                        try {
                            m = c.getDeclaredMethod("getITelephony");
                        } catch (SecurityException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (NoSuchMethodException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        m.setAccessible(true);
                        try {
                            telephonyService = (ITelephony) m.invoke(telephonyManager);
                        } catch (IllegalArgumentException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        telephonyManager.listen(callBlockListener, PhoneStateListener.LISTEN_CALL_STATE);
                    }//onReceive()

                    PhoneStateListener callBlockListener = new PhoneStateListener() {
                        public void onCallStateChanged(int state, String incomingNumber) {

                            if (state == TelephonyManager.CALL_STATE_RINGING) {
                                if (blockAll_cb.isChecked()) {
                                    try {
                                        telephonyService.endCall();
                                        String mPhoneNumber = mNum.getText().toString();
                                        WriteBtn(mPhoneNumber,incomingNumber);

                                    } catch (RemoteException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    };
                };//BroadcastReceiver
                IntentFilter filter = new IntentFilter("android.intent.action.PHONE_STATE");
                registerReceiver(CallBlocker, filter);
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void initviews() {
        blockAll_cb = (CheckBox) findViewById(R.id.cbBlockAll);
        upload = (Button) findViewById(R.id.button);

        upload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(isConnected())
                {
                    uploadData();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Network connection not available", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    // write text to file
    public void WriteBtn(String tono, String fromno) {
        // add-write text into file

        Calendar c = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("dd/MM/yy");
        SimpleDateFormat time = new SimpleDateFormat("HH:mm aa");
        String strDate = date.format(c.getTime());
        String strTime = time.format(c.getTime());
        String separator = System.getProperty("line.separator");

        try {
            FileOutputStream fileout = openFileOutput("votes.txt", MODE_APPEND);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
            outputWriter.append(tono + "&" + fromno + "&" + strTime + "&" + strDate);
            outputWriter.append(separator);
            outputWriter.close();
            fileout.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Read text from file
    public LinkedList<String> ReadBtn() {

        LinkedList<String> list = new LinkedList<>();
        //reading text from file
        try {
            FileInputStream fileIn = openFileInput("votes.txt");
            InputStreamReader InputRead = new InputStreamReader(fileIn);
            BufferedReader bufferread = new BufferedReader(InputRead);

            String line;
            // read every line of the file into the line-variable, on line at the time
            do {
                line = bufferread.readLine();
                if(line!=null)
                {
                    list.add(line);
                }

            } while (line != null);

            bufferread.close();
            InputRead.close();
            fileIn.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public JSONArray data() {
        LinkedList<String> list = ReadBtn();
        String[] array = new String[4];
        JSONObject jObject;
        JSONArray jArray = new JSONArray();
        String [] firstArray = new String[4];
        String first = null;


        try {
            for (int i = 0; i < list.size(); i++) {
                jObject = new JSONObject();
                if(i!=0)
                {
                    firstArray = list.get(--i).split("&");
                    first = firstArray[1];
                }
                if(list.get(i)!=null)
                {
                    array = list.get(i).split("&");
                    if(array[0].equalsIgnoreCase("") || array[0].isEmpty() || array[0]==null)
                    {
                        jObject.put("tono", "empty");
                    }
                    else
                    {
                        jObject.put("tono", array[0]);
                    }
                    jObject.put("fromno", array[1]);
                    if(array[1].toString().equalsIgnoreCase(first))
                    {
                        i++;
                        continue;
                    }
                    jObject.put("time", array[2]);
                    jObject.put("date", array[3]);
                    jArray.put(jObject);
                }
            }
        } catch (Exception e) {
        }
        return jArray;
    }

    public void uploadData() {
        JSONArray jsonArray = data();
        if(jsonArray.isNull(0) || jsonArray.length()==0)
        {
            Toast.makeText(MainActivity.this, "No votes in memory!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            postData(jsonArray);
        }

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (CallBlocker != null) {
            unregisterReceiver(CallBlocker);
            CallBlocker = null;
        }
    }

    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    public void postData(JSONArray data) {
        ConnectionTask task = new ConnectionTask();
        String[] params = new String[3];
        params[0] = "http://nucleo.azurewebsites.net/api/phonevote";
        String strData = data.toString();
        Log.d("DATA",strData);
        params[1] = strData;
        task.execute(params);
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://me.waleedashraf.politicle/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://me.waleedashraf.politicle/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    class ConnectionTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Uploading votes");
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {
            int status = 0;
            String result = null;
            if (check == true) {
                check = false;
                URL url;

                try {
                    url = new URL (urls[0]);
                    urlConn = (HttpURLConnection)url.openConnection();
                    urlConn.setDoInput(true);
                    urlConn.setDoOutput(true);
                    urlConn.setUseCaches(false);
                    urlConn.setRequestProperty("Content-Type", "application/json");
                    urlConn.connect();
                    OutputStreamWriter writer = new OutputStreamWriter(urlConn.getOutputStream());
                    writer.write(urls[1]);
                    writer.flush();
                    writer.close();

                    status = urlConn.getResponseCode();
                    if(status==400)
                    {
                        String message = urlConn.getResponseMessage();
                        String body = urlConn.getContent().toString();

                        Log.d("ERROR",message+body);
                    }


                    StringBuilder sb = new StringBuilder();
                    sb.append("");
                    sb.append(status);
                    result = sb.toString();
                    return result;

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            pDialog.dismiss();
            if(result!=null && result.equalsIgnoreCase("200"))
            {
                Toast.makeText(MainActivity.this, "Votes uploaded successfully!", Toast.LENGTH_SHORT).show();
                clearFile();
            }
            else
            {
                Toast.makeText(MainActivity.this, "Upload failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void clearFile()
    {
        try{
            FileOutputStream fileout = openFileOutput("votes.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
            outputWriter.write("");
            outputWriter.flush();
            outputWriter.close();
            fileout.flush();
            fileout.close();

        }
        catch (Exception e){
            Log.d("ERROR FILE",e.toString());
        }
    }
}


