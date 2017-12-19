package com.bbpos.bbdevice;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bbpos.bbdevice.util.log.HLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;

public class MainActivity extends BaseActivity {
    protected static String TAG = "MainActivity";

    protected static String webAutoConfigString = "";
    protected static boolean isLoadedLocalSettingFile = false;
    protected static boolean isLoadedWebServiceAutoConfig = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        ((TextView) findViewById(R.id.modelTextView)).setText(Build.MANUFACTURER.toUpperCase(Locale.ENGLISH) + " - " + Build.MODEL + " (Android " + Build.VERSION.RELEASE + ")");

        fidSpinner = (Spinner) findViewById(R.id.fidSpinner);
//        startButton = (Button) findViewById(R.id.startButton);
        amountEditText = (EditText) findViewById(R.id.amountEditText);
        statusEditText = (EditText) findViewById(R.id.statusEditText);

        MyOnClickListener myOnClickListener = new MyOnClickListener();
//        startButton.setOnClickListener(myOnClickListener);

        findViewById(R.id.connect).setOnClickListener(myOnClickListener);
        findViewById(R.id.stop_connect).setOnClickListener(myOnClickListener);
        findViewById(R.id.for_amount).setOnClickListener(myOnClickListener);
        findViewById(R.id.checkcard).setOnClickListener(myOnClickListener);
        findViewById(R.id.startEmv).setOnClickListener(myOnClickListener);
        findViewById(R.id.for_session).setOnClickListener(myOnClickListener);
        findViewById(R.id.controll_led).setOnClickListener(myOnClickListener);
        findViewById(R.id.readAID).setOnClickListener(myOnClickListener);
        findViewById(R.id.updateAID).setOnClickListener(myOnClickListener);
        findViewById(R.id.print).setOnClickListener(myOnClickListener);

        String[] fids = new String[]{"FID22", "FID36", "FID46_randomNumber", "FID54", "FID55", "FID60", "FID61_orderID_randomNumber", "FID64", "FID65_pin_data_mac",};
        fidSpinner.setAdapter(new ArrayAdapter<String>(this, R.layout.my_spinner_item, fids));
        fidSpinner.setSelection(5);

        currentActivity = this;

        try {
            String filename = "settings.txt";
            String inputDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.bbpos.bbdevice.ui/";
            HLog.w(TAG,inputDirectory + filename);

            FileInputStream fis = new FileInputStream(inputDirectory + filename);
            byte[] temp = new byte[fis.available()];
            fis.read(temp);
            fis.close();

            isLoadedLocalSettingFile = true;
            HLog.w(TAG,new String(temp));
            bbDeviceController.setAudioAutoConfig(new String(temp));

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(currentActivity, getString(R.string.setting_config), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
        }

        //Create instance for AsyncCallWS
        AsyncCallWS task = new AsyncCallWS();
        //Call execute 
        task.execute();
    }

    class MyOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            statusEditText.setText("");
            switch (v.getId()){
                case R.id.connect:
                    isPinCanceled = false;
                    amountEditText.setText("");
                    statusEditText.setText("connect");
                    promptForConnection();
                    break;
                case R.id.stop_connect:
                    isPinCanceled = false;
                    amountEditText.setText("");
                    statusEditText.setText("stop_connect");
                    stopConnection();
                    break;
                case R.id.for_amount:
                    isPinCanceled = false;
                    amountEditText.setText("");
                    statusEditText.setText("for_amount");
                    promptForAmount();

                    Hashtable<String, Object> data = new Hashtable<String, Object>();
                    if (Locale.getDefault().getCountry().equalsIgnoreCase("CN")) {
                        data.put("currencyCode", "156");
                        data.put("currencyCharacters", new BBDeviceController.CurrencyCharacter[]{BBDeviceController.CurrencyCharacter.YEN});
                    } else {
                        data.put("currencyCode", "840");
                        data.put("currencyCharacters", new BBDeviceController.CurrencyCharacter[]{BBDeviceController.CurrencyCharacter.DOLLAR});
                    }
                    data.put("amountInputType", BBDeviceController.AmountInputType.AMOUNT_AND_CASHBACK);
                    bbDeviceController.enableInputAmount(data);

                    break;
                case R.id.checkcard:
                    isPinCanceled = false;
                    amountEditText.setText("");
                    statusEditText.setText("checkcard");
                    promptForCheckCard();
                    break;
                case R.id.startEmv:
                    isPinCanceled = false;
                    amountEditText.setText("");
                    statusEditText.setText("startEmv");
                    promptForStartEmv();
                    break;
                case R.id.for_session:
                    isPinCanceled = false;
                    amountEditText.setText("");
                    statusEditText.setText("checkcard");
                    promptForInitSession();
                    break;
                case R.id.controll_led:
                    isPinCanceled = false;
                    amountEditText.setText("");
                    statusEditText.setText("controll_led");
                    promptForControlLED();
                    break;

                case R.id.readAID:
                    isPinCanceled = false;
                    amountEditText.setText("");
                    statusEditText.setText("readAID");
                    promptForReadAID();
                    break;
                case R.id.updateAID:
                    isPinCanceled = false;
                    amountEditText.setText("");
                    statusEditText.setText("updateAID");
                    promptForUpdateAID();
                    break;
                case R.id.print:
                    isPinCanceled = false;
                    amountEditText.setText("");
                    statusEditText.setText("print");
                    receipts = new ArrayList<byte[]>();
                    if (Locale.getDefault().getCountry().equalsIgnoreCase("CN")) {
                        receipts.add(ReceiptUtility.genReceipt2(MainActivity.this));
                    } else {
                        receipts.add(ReceiptUtility.genReceipt(MainActivity.this));
                    }
                    bbDeviceController.startPrint(receipts.size(), 60);
                    break;

            }

        }
    }

    private class AsyncCallWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            if (isLoadedWebServiceAutoConfig == false) {
                webAutoConfigString = WebService.invokeGetAutoConfigString(Build.MANUFACTURER.toUpperCase(Locale.US),
                        Build.MODEL.toUpperCase(Locale.US), BBDeviceController.getApiVersion(), "getAutoConfigString");
                HLog.w(TAG,"getAutoConfigString---" + Build.MANUFACTURER.toUpperCase(Locale.US) + "\n" + Build.MODEL.toUpperCase(Locale.US) +
                        "\n" + BBDeviceController.getApiVersion());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (isLoadedWebServiceAutoConfig == false) {
                isLoadedWebServiceAutoConfig = true;
                if (isLoadedLocalSettingFile == false) {
                    if (!webAutoConfigString.equalsIgnoreCase("Error occured") && !webAutoConfigString.equalsIgnoreCase("")) {
                        HLog.w(TAG,webAutoConfigString);
                        bbDeviceController.setAudioAutoConfig(webAutoConfigString);

                        try {
                            String filename = "settings.txt";
                            String outputDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.bbpos.emvswipe.ui/";
                            File directory = new File(outputDirectory);
                            HLog.w(TAG,"outputDirectory="+outputDirectory);
                            if (!directory.isDirectory()) {
                                directory.mkdirs();
                            }
                            FileOutputStream fos = new FileOutputStream(outputDirectory + filename, true);
                            fos.write(webAutoConfigString.getBytes());
                            HLog.w(TAG,webAutoConfigString.getBytes().toString());
                            fos.flush();
                            fos.close();
                        } catch (Exception e) {
                        }

                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(currentActivity, getString(R.string.setting_config_from_web_service), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

    }
}
