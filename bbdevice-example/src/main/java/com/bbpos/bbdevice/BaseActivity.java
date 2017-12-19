package com.bbpos.bbdevice;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bbpos.bbdevice.BBDeviceController.AccountSelectionResult;
import com.bbpos.bbdevice.BBDeviceController.AmountInputType;
import com.bbpos.bbdevice.BBDeviceController.AudioAutoConfigError;
import com.bbpos.bbdevice.BBDeviceController.BBDeviceControllerListener;
import com.bbpos.bbdevice.BBDeviceController.BatteryStatus;
import com.bbpos.bbdevice.BBDeviceController.CheckCardMode;
import com.bbpos.bbdevice.BBDeviceController.CheckCardResult;
import com.bbpos.bbdevice.BBDeviceController.ConnectionMode;
import com.bbpos.bbdevice.BBDeviceController.ContactlessStatus;
import com.bbpos.bbdevice.BBDeviceController.ContactlessStatusTone;
import com.bbpos.bbdevice.BBDeviceController.CurrencyCharacter;
import com.bbpos.bbdevice.BBDeviceController.DisplayText;
import com.bbpos.bbdevice.BBDeviceController.EmvOption;
import com.bbpos.bbdevice.BBDeviceController.EncryptionKeySource;
import com.bbpos.bbdevice.BBDeviceController.EncryptionKeyUsage;
import com.bbpos.bbdevice.BBDeviceController.EncryptionMethod;
import com.bbpos.bbdevice.BBDeviceController.EncryptionPaddingMethod;
import com.bbpos.bbdevice.BBDeviceController.Error;
import com.bbpos.bbdevice.BBDeviceController.LEDMode;
import com.bbpos.bbdevice.BBDeviceController.NfcDetectCardResult;
import com.bbpos.bbdevice.BBDeviceController.PhoneEntryResult;
import com.bbpos.bbdevice.BBDeviceController.PinEntryResult;
import com.bbpos.bbdevice.BBDeviceController.PinEntrySource;
import com.bbpos.bbdevice.BBDeviceController.PrintResult;
import com.bbpos.bbdevice.BBDeviceController.ReadNdefRecord;
import com.bbpos.bbdevice.BBDeviceController.SessionError;
import com.bbpos.bbdevice.BBDeviceController.TerminalSettingStatus;
import com.bbpos.bbdevice.BBDeviceController.TransactionResult;
import com.bbpos.bbdevice.BBDeviceController.TransactionType;
import com.bbpos.bbdevice.BBDeviceController.VASResult;
import com.bbpos.bbdevice.util.TripleDES;
import com.bbpos.bbdevice.util.Utils;
import com.bbpos.bbdevice.util.log.HLog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class BaseActivity extends Activity {
    protected static String TAG = "MainActivity";
    protected enum State {
        GETTING_PSE, READING_RECORD, READING_AID, GETTING_PROCESS_OPTION, READING_DATA
    }

    protected static final String[] DEVICE_NAMES = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    protected static BBDeviceController bbDeviceController;
    protected static MyBBDeviceControllerListener listener;
    protected static BaseActivity currentActivity;

    protected static String masterKey = "B93EEC40D5A2F45885BC831AA4DC131F";

    protected static String pinSessionKey = "A1223344556677889900AABBCCDDEEFF";
    protected static String encryptedPinSessionKey = "";
    protected static String pinKcv = "";

    protected static String dataSessionKey = "A2223344556677889900AABBCCDDEEFF";
    protected static String encryptedDataSessionKey = "";
    protected static String dataKcv = "";

    protected static String trackSessionKey = "A4223344556677889900AABBCCDDEEFF";
    protected static String encryptedTrackSessionKey = "";
    protected static String trackKcv = "";

    protected static String macSessionKey = "A6223344556677889900AABBCCDDEEFF";
    protected static String encryptedMacSessionKey = "";
    protected static String macKcv = "";

    protected static String fid65WorkingKey = "A1223344556677889900AABBCCDDEEFF";
    protected static String fid65MasterKey = "0123456789ABCDEFFEDCBA9876543210";

    protected static Spinner fidSpinner;
    protected static Button startButton;
    protected static EditText amountEditText;
    protected static EditText statusEditText;
    protected static ListView appListView;
    protected static Dialog dialog;
    protected static ProgressDialog progressDialog;

    protected static Button clearLogButton;
    protected static Button powerOnIccButton;
    protected static Button powerOffIccButton;
    protected static Button apduButton;
    protected static Button getCAPKListButton;
    protected static Button getCAPKDetailButton;
    protected static Button findCAPKButton;
    protected static Button updateCAPKButton;
    protected static Button getEmvReportListButton;
    protected static Button getEmvReportButton;

    protected static Button updateGprsSettingButton;
    protected static Button updateWifiSettingButton;
    protected static Button readGprsSettingButton;
    protected static Button readWifiSettingButton;

    protected static Button startNfcDetectionButton;
    protected static Button stopNfcDetectionButton;
    protected static Button nfcDataExchangeWriteButton;
    protected static Button nfcDataExchangeRead1StButton;
    protected static Button nfcDataExchangeReadNextButton;

    protected static ArrayAdapter<String> arrayAdapter;
    protected static String amount = "";
    protected static String cashbackAmount = "";
    protected static boolean isPinCanceled = false;

    protected static List<BluetoothDevice> foundDevices;

    protected static ArrayList<byte[]> receipts;

    protected static String cardholderName;
    protected static String cardNumber;
    protected static String expiryDate;
    protected static String track2 = "";
    protected static String pan = "";
    protected static String aid;
    protected static String appLabel;
    protected static String tc;
    protected static String batchNum;
    protected static String tid;
    protected static String mid;
    protected static String transactionDateTime;
    protected static boolean signatureRequired;

    protected static String ksn = "";
    protected static boolean isApduEncrypted = true;
    protected static final String DATA_KEY = "Data Key";
    protected static final String DATA_KEY_VAR = "Data Key Var";
    protected static final String PIN_KEY_VAR = "PIN Key Var";

    protected static final String ECB = "ECB";
    protected static final String CBC = "CBC";
    protected static String keyMode = DATA_KEY;
    protected static String encryptionMode = CBC;
    protected static State state = null;

    private static int aidCounter = 0;
    private static String[] afls = null;
    private static int aflCounter = 0;
    private static String sfi = "";
    private static int readingFileIndex = 0;
    private static int total = 0;

    protected static long startTime;
    protected static boolean isPKCS7 = false;
    protected boolean isSwitchingActivity = false;

    private static String[] aids = new String[]{"A0000000031010", "A0000000041010", "A00000002501"};

    private static CheckCardMode checkCardMode;

    private static final String BDK = "0123456789ABCDEFFEDCBA9876543210";
    private String uid = null;

    private static final boolean DEBUG_MODE = true;

    private final static String LOG_TAG = BaseActivity.class.getName();

    private void log(String msg) {
        if (DEBUG_MODE) {
            Log.d(LOG_TAG, "[BaseActivity] " + msg);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (bbDeviceController == null) {
            listener = new MyBBDeviceControllerListener();
            bbDeviceController = BBDeviceController.getInstance(getApplicationContext(), listener);
            BBDeviceController.setDebugLogEnabled(true);
            bbDeviceController.setDetectAudioDevicePlugged(true);//设置音频设备开启
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        HLog.w(TAG,"");
        if (isSwitchingActivity) {
            isSwitchingActivity = false;
        } else {
            bbDeviceController.disconnectBT();
        }
    }

    public void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    public String encrypt(String data, String key) {
        HLog.w(TAG,"encrypt__data="+data+"\nkey="+key);
        if (key.length() == 16) {
            key += key.substring(0, 8);
        }
        byte[] d = hexToByteArray(data);
        byte[] k = hexToByteArray(key);

        SecretKey sk = new SecretKeySpec(k, "DESede");
        try {
            Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, sk);
            byte[] enc = cipher.doFinal(d);
            return toHexString(enc);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        statusEditText.setText("BBDevice API : " + BBDeviceController.getApiVersion());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void injectNextSessionKey() {
        HLog.w(TAG,"injectNextSessionKey");
        if (!encryptedPinSessionKey.equals("")) {
            Hashtable<String, String> data = new Hashtable<String, String>();
            data.put("index", "1");
            data.put("encSK", encryptedPinSessionKey);
            data.put("kcv", pinKcv);
            HLog.w(TAG,"encryptedPinSessionKey:" + data.toString());

            statusEditText.setText(getString(R.string.sending_encrypted_pin_session_key));
            encryptedPinSessionKey = "";
            bbDeviceController.injectSessionKey(data);
            return;
        }

        if (!encryptedDataSessionKey.equals("")) {
            Hashtable<String, String> data = new Hashtable<String, String>();
            data.put("index", "2");
            data.put("encSK", encryptedDataSessionKey);
            data.put("kcv", dataKcv);
            HLog.w(TAG,"encryptedDataSessionKey:" + data.toString());
            statusEditText.setText(getString(R.string.sending_encrypted_data_session_key));
            encryptedDataSessionKey = "";
            bbDeviceController.injectSessionKey(data);
            return;
        }

        if (!encryptedTrackSessionKey.equals("")) {
            Hashtable<String, String> data = new Hashtable<String, String>();
            data.put("index", "3");
            data.put("encSK", encryptedTrackSessionKey);
            data.put("kcv", trackKcv);
            HLog.w(TAG,"encryptedTrackSessionKey:" + data.toString());
            statusEditText.setText(getString(R.string.sending_encrypted_track_session_key));
            encryptedTrackSessionKey = "";
            bbDeviceController.injectSessionKey(data);
            return;
        }

        if (!encryptedMacSessionKey.equals("")) {
            Hashtable<String, String> data = new Hashtable<String, String>();
            data.put("index", "4");
            data.put("encSK", encryptedMacSessionKey);
            data.put("kcv", macKcv);
            HLog.w(TAG,"encryptedMacSessionKey:" + data.toString());
            statusEditText.setText(getString(R.string.sending_encrypted_mac_session_key));
            encryptedMacSessionKey = "";
            bbDeviceController.injectSessionKey(data);
            return;
        }
    }

    public void stopConnection() {
        ConnectionMode connectionMode = bbDeviceController.getConnectionMode();
        HLog.w(TAG,"stopConnection__connectionMode=" + connectionMode);
        if (connectionMode == ConnectionMode.BLUETOOTH) {
            bbDeviceController.disconnectBT();
        } else if (connectionMode == ConnectionMode.AUDIO) {
            bbDeviceController.stopAudio();
        } else if (connectionMode == ConnectionMode.SERIAL) {
            bbDeviceController.stopSerial();
        } else if (connectionMode == ConnectionMode.USB) {
            bbDeviceController.stopUsb();
        }
    }

    public void promptForConnection() {
        HLog.w(TAG,"promptForConnection");
        dismissDialog();
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.connection_dialog);
        dialog.setTitle(getString(R.string.connection));
        dialog.setCanceledOnTouchOutside(false);

        String[] connections = new String[4];
        connections[0] = "Bluetooth";
        connections[1] = "Audio";
        connections[2] = "Serial";
        connections[3] = "USB";

        ListView listView = (ListView) dialog.findViewById(R.id.connectionList);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, connections));
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dismissDialog();
                if (position == 0) {
                    Object[] pairedObjects = BluetoothAdapter.getDefaultAdapter().getBondedDevices().toArray();
                    final BluetoothDevice[] pairedDevices = new BluetoothDevice[pairedObjects.length];
                    for (int i = 0; i < pairedObjects.length; ++i) {
                        pairedDevices[i] = (BluetoothDevice) pairedObjects[i];
                    }

                    final ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(currentActivity, android.R.layout.simple_list_item_1);
                    for (int i = 0; i < pairedDevices.length; ++i) {
                        mArrayAdapter.add(pairedDevices[i].getName());
                        HLog.w(TAG,"可用的蓝牙设备：" + pairedDevices[i].getName());
                    }

                    dismissDialog();

                    dialog = new Dialog(currentActivity);
                    dialog.setContentView(R.layout.bluetooth_2_device_list_dialog);
                    dialog.setTitle(R.string.bluetooth_devices);

                    ListView listView1 = (ListView) dialog.findViewById(R.id.pairedDeviceList);
                    listView1.setAdapter(mArrayAdapter);
                    listView1.setOnItemClickListener(new OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            statusEditText.setText(getString(R.string.connecting_bluetooth));
                            bbDeviceController.connectBT(pairedDevices[position]);
                            dismissDialog();
                        }

                    });

                    arrayAdapter = new ArrayAdapter<String>(currentActivity, android.R.layout.simple_list_item_1);
                    ListView listView2 = (ListView) dialog.findViewById(R.id.discoveredDeviceList);
                    listView2.setAdapter(arrayAdapter);
                    listView2.setOnItemClickListener(new OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            statusEditText.setText(getString(R.string.connecting_bluetooth));
                            bbDeviceController.connectBT(foundDevices.get(position));
                            dismissDialog();
                        }

                    });

                    dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            bbDeviceController.stopBTScan();
                            dismissDialog();
                        }
                    });
                    dialog.setCancelable(false);
                    dialog.show();
                    HLog.w(TAG,"startBTScan");
                    bbDeviceController.startBTScan(DEVICE_NAMES, 120);
                } else if (position == 1) {
                    bbDeviceController.startAudio();
                    HLog.w(TAG,"startAudio");
                } else if (position == 2) {
                    HLog.w(TAG,"startSerial");
                    bbDeviceController.startSerial();
                } else if (position == 3) {
                    HLog.w(TAG,"startUsb");
                    bbDeviceController.startUsb();
                }
            }

        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismissDialog();
            }
        });

        dialog.show();
    }

    public void promptForAmount() {
        HLog.w(TAG,"promptForAmount");
        dismissDialog();
        dialog = new Dialog(currentActivity);
        dialog.setContentView(R.layout.amount_dialog);
        dialog.setTitle(getString(R.string.set_amount));
        dialog.setCanceledOnTouchOutside(false);

        String[] symbols = new String[]{"DOLLAR", "RUPEE", "YEN", "POUND", "EURO", "WON", "DIRHAM", "RIYAL", "AED", "BS.", "YUAN", "NEW_SHEKEL", "NULL"};
        ((Spinner) dialog.findViewById(R.id.symbolSpinner)).setAdapter(new ArrayAdapter<String>(currentActivity, android.R.layout.simple_spinner_item, symbols));

        String[] transactionTypes = new String[]{"GOODS", "SERVICES", "CASHBACK", "INQUIRY", "TRANSFER", "PAYMENT", "REFUND", "VOID", "REVERSAL"};
        ((Spinner) dialog.findViewById(R.id.transactionTypeSpinner)).setAdapter(new ArrayAdapter<String>(currentActivity, android.R.layout.simple_spinner_item, transactionTypes));

        dialog.findViewById(R.id.setButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String amount = ((EditText) (dialog.findViewById(R.id.amountEditText))).getText().toString();
                String cashbackAmount = ((EditText) (dialog.findViewById(R.id.cashbackAmountEditText))).getText().toString();
                String transactionTypeString = (String) ((Spinner) dialog.findViewById(R.id.transactionTypeSpinner)).getSelectedItem();
                String symbolString = (String) ((Spinner) dialog.findViewById(R.id.symbolSpinner)).getSelectedItem();

                TransactionType transactionType = TransactionType.GOODS;
                if (transactionTypeString.equals("GOODS")) {
                    transactionType = TransactionType.GOODS;
                } else if (transactionTypeString.equals("SERVICES")) {
                    transactionType = TransactionType.SERVICES;
                } else if (transactionTypeString.equals("CASHBACK")) {
                    transactionType = TransactionType.CASHBACK;
                } else if (transactionTypeString.equals("INQUIRY")) {
                    transactionType = TransactionType.INQUIRY;
                } else if (transactionTypeString.equals("TRANSFER")) {
                    transactionType = TransactionType.TRANSFER;
                } else if (transactionTypeString.equals("PAYMENT")) {
                    transactionType = TransactionType.PAYMENT;
                } else if (transactionTypeString.equals("REFUND")) {
                    transactionType = TransactionType.REFUND;
                } else if (transactionTypeString.equals("VOID")) {
                    transactionType = TransactionType.VOID;
                } else if (transactionTypeString.equals("REVERSAL")) {
                    transactionType = TransactionType.REVERSAL;
                }

                CurrencyCharacter[] currencyCharacters = new CurrencyCharacter[]{CurrencyCharacter.A, CurrencyCharacter.B, CurrencyCharacter.C};
                if (symbolString.equals("DOLLAR")) {
                    currencyCharacters = new CurrencyCharacter[]{CurrencyCharacter.DOLLAR};
                } else if (symbolString.equals("RUPEE")) {
                    currencyCharacters = new CurrencyCharacter[]{CurrencyCharacter.RUPEE};
                } else if (symbolString.equals("YEN")) {
                    currencyCharacters = new CurrencyCharacter[]{CurrencyCharacter.YEN};
                } else if (symbolString.equals("POUND")) {
                    currencyCharacters = new CurrencyCharacter[]{CurrencyCharacter.POUND};
                } else if (symbolString.equals("EURO")) {
                    currencyCharacters = new CurrencyCharacter[]{CurrencyCharacter.EURO};
                } else if (symbolString.equals("WON")) {
                    currencyCharacters = new CurrencyCharacter[]{CurrencyCharacter.WON};
                } else if (symbolString.equals("DIRHAM")) {
                    currencyCharacters = new CurrencyCharacter[]{CurrencyCharacter.DIRHAM};
                } else if (symbolString.equals("RIYAL")) {
                    currencyCharacters = new CurrencyCharacter[]{CurrencyCharacter.RIYAL, CurrencyCharacter.RIYAL_2};
                } else if (symbolString.equals("AED")) {
                    currencyCharacters = new CurrencyCharacter[]{CurrencyCharacter.A, CurrencyCharacter.E, CurrencyCharacter.D};
                } else if (symbolString.equals("BS.")) {
                    currencyCharacters = new CurrencyCharacter[]{CurrencyCharacter.B, CurrencyCharacter.S, CurrencyCharacter.DOT};
                } else if (symbolString.equals("YUAN")) {
                    currencyCharacters = new CurrencyCharacter[]{CurrencyCharacter.YUAN};
                } else if (symbolString.equals("NEW_SHEKEL")) {
                    currencyCharacters = new CurrencyCharacter[]{CurrencyCharacter.NEW_SHEKEL};
                } else if (symbolString.equals("NULL")) {
                    currencyCharacters = null;
                }
//                currencyCharacters = new CurrencyCharacter[]{CurrencyCharacter.YUAN};
                String currencyCode;
                if (Locale.getDefault().getCountry().equalsIgnoreCase("CN")) {
                    currencyCode = "156";
                    currencyCharacters = new CurrencyCharacter[]{CurrencyCharacter.YUAN};
                } else {
                    currencyCode = "840";
                }

                if (bbDeviceController.setAmount(amount, cashbackAmount, currencyCode, transactionType, currencyCharacters)) {
                    amountEditText.setText("$" + amount);
                    currentActivity.amount = amount;
                    currentActivity.cashbackAmount = cashbackAmount;
                    statusEditText.setText(getString(R.string.please_confirm_amount));

                    dismissDialog();
                } else {
                    promptForAmount();
                }
            }

        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                HLog.w(TAG,"cancelSetAmount");
                bbDeviceController.cancelSetAmount();
                dialog.dismiss();
            }

        });

        dialog.show();
    }

    public void promptForCheckCard() {
        HLog.w(TAG,"promptForCheckCard");
        dismissDialog();
        dialog = new Dialog(currentActivity);
        dialog.setContentView(R.layout.check_card_mode_dialog);
        dialog.setTitle(getString(R.string.select_mode));
        dialog.setCanceledOnTouchOutside(false);

        String[] swipeInsertTap = new String[7];
        swipeInsertTap[0] = getString(R.string.swipe_or_insert);
        swipeInsertTap[1] = getString(R.string.swipe);
        swipeInsertTap[2] = getString(R.string.insert);
        swipeInsertTap[3] = getString(R.string.tap);
        swipeInsertTap[4] = getString(R.string.swipe_or_tap);
        swipeInsertTap[5] = getString(R.string.insert_or_tap);
        swipeInsertTap[6] = getString(R.string.swipe_or_insert_or_tap);

        ListView listView = (ListView) dialog.findViewById(R.id.swipeInsertTapList);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, swipeInsertTap));
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dismissDialog();
                if (position == 0) {
                checkCardMode = CheckCardMode.SWIPE_OR_INSERT;
                HLog.w(TAG,"checkCardMode:" + checkCardMode);
                checkCard();
                HLog.w(TAG,"checkCard");
                } else if (position == 1) {
                    checkCardMode = CheckCardMode.SWIPE;
                    checkCard();
                } else if (position == 2) {
                    checkCardMode = CheckCardMode.INSERT;
                    checkCard();
                } else if (position == 3) {
                    checkCardMode = CheckCardMode.TAP;
                    startEmv();
                } else if (position == 4) {
                    checkCardMode = CheckCardMode.SWIPE_OR_TAP;
                    startEmv();
                } else if (position == 5) {
                    checkCardMode = CheckCardMode.INSERT_OR_TAP;
                    startEmv();
                } else if (position == 6) {
                    checkCardMode = CheckCardMode.SWIPE_OR_INSERT_OR_TAP;
                    startEmv();
                }

            }

        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismissDialog();
                statusEditText.setText("");
            }
        });

        dialog.show();
    }

    public void promptForStartEmv() {
        HLog.w(TAG,"promptForStartEmv");
        dismissDialog();
        dialog = new Dialog(currentActivity);
        dialog.setContentView(R.layout.check_card_mode_dialog);
        dialog.setTitle(getString(R.string.select_mode));
        dialog.setCanceledOnTouchOutside(false);

        String[] swipeInsertTap = new String[8];
        swipeInsertTap[0] = getString(R.string.swipe_or_insert);
        swipeInsertTap[1] = getString(R.string.swipe);
        swipeInsertTap[2] = getString(R.string.insert);
        swipeInsertTap[3] = getString(R.string.tap);
        swipeInsertTap[4] = getString(R.string.swipe_or_tap);
        swipeInsertTap[5] = getString(R.string.insert_or_tap);
        swipeInsertTap[6] = getString(R.string.swipe_or_insert_or_tap);
        swipeInsertTap[7] = getString(R.string.manual_pan_entry);

        ListView listView = (ListView) dialog.findViewById(R.id.swipeInsertTapList);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, swipeInsertTap));

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dismissDialog();
                if (position == 0) {
                    checkCardMode = CheckCardMode.SWIPE_OR_INSERT;
                } else if (position == 1) {
                    checkCardMode = CheckCardMode.SWIPE;
                } else if (position == 2) {
                    checkCardMode = CheckCardMode.INSERT;
                } else if (position == 3) {
                    checkCardMode = CheckCardMode.TAP;
                } else if (position == 4) {
                    checkCardMode = CheckCardMode.SWIPE_OR_TAP;
                } else if (position == 5) {
                    checkCardMode = CheckCardMode.INSERT_OR_TAP;
                } else if (position == 6) {
                    checkCardMode = CheckCardMode.SWIPE_OR_INSERT_OR_TAP;
                } else if (position == 7) {
                    checkCardMode = CheckCardMode.MANUAL_PAN_ENTRY;
                }
                checkCardMode = CheckCardMode.SWIPE_OR_INSERT;
                HLog.w(TAG,"startEmv:"+checkCardMode);
                startEmv();
            }

        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismissDialog();
                statusEditText.setText("");
            }
        });

        dialog.show();
    }

    public void promptForGprs() {
        HLog.w(TAG,"promptForGprs");
        dismissDialog();
        dialog = new Dialog(currentActivity);
        dialog.setContentView(R.layout.gprs_dialog);
        dialog.setTitle(getString(R.string.gprs));
        dialog.setCanceledOnTouchOutside(false);

        ((EditText) (dialog.findViewById(R.id.gprsOperatorEditText))).setText("CSL");
        ((EditText) (dialog.findViewById(R.id.gprsAPNEditText))).setText("hkcsl");
        ((EditText) (dialog.findViewById(R.id.gprsUsernameEditText))).setText("guest");
        ((EditText) (dialog.findViewById(R.id.gprsPasswordEditText))).setText("guest");

        dialog.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String gprsOperator = ((EditText) (dialog.findViewById(R.id.gprsOperatorEditText))).getText().toString();
                String gprsAPN = ((EditText) (dialog.findViewById(R.id.gprsAPNEditText))).getText().toString();
                String gprsUsername = ((EditText) (dialog.findViewById(R.id.gprsUsernameEditText))).getText().toString();
                String gprsPassword = ((EditText) (dialog.findViewById(R.id.gprsPasswordEditText))).getText().toString();

                Hashtable<String, String> gprsData = new Hashtable<String, String>();
                gprsData.put("operator", gprsOperator);
                gprsData.put("apn", gprsAPN);
                gprsData.put("username", gprsUsername);
                gprsData.put("password", gprsPassword);

                bbDeviceController.updateGprsSettings(gprsData);

                dialog.dismiss();
            }

        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }

        });

        dialog.show();
    }

    public void promptForWifi() {
        HLog.w(TAG,"promptForWifi");
        dismissDialog();
        dialog = new Dialog(currentActivity);
        dialog.setContentView(R.layout.wifi_dialog);
        dialog.setTitle(getString(R.string.wifi));
        dialog.setCanceledOnTouchOutside(false);

        ((EditText) (dialog.findViewById(R.id.wifiSSIDEditText))).setText("BBPOS_AP");
        ((EditText) (dialog.findViewById(R.id.wifiPasswordEditText))).setText("bb1904@AP");
        ((EditText) (dialog.findViewById(R.id.wifiUrlEditText))).setText("ws://chip.mswipetech.com/mswipeGWG2/TxHandlerG2.ashx");
        ((EditText) (dialog.findViewById(R.id.wifiPortNumberEditText))).setText("8080");

        dialog.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String wifiSSID = ((EditText) (dialog.findViewById(R.id.wifiSSIDEditText))).getText().toString();
                String wifiPassword = ((EditText) (dialog.findViewById(R.id.wifiPasswordEditText))).getText().toString();
                String wifiUrl = ((EditText) (dialog.findViewById(R.id.wifiUrlEditText))).getText().toString();
                String wifiPortNumber = ((EditText) (dialog.findViewById(R.id.wifiPortNumberEditText))).getText().toString();

                Hashtable<String, String> wifiData = new Hashtable<String, String>();
                if (((CheckBox) (dialog.findViewById(R.id.enableWifiSSIDCheckBox))).isChecked()) {
                    wifiData.put("ssid", wifiSSID);
                }

                if (((CheckBox) (dialog.findViewById(R.id.enableWifiPasswordCheckBox))).isChecked()) {
                    wifiData.put("password", wifiPassword);
                }

                if (((CheckBox) (dialog.findViewById(R.id.enableWifiUrlCheckBox))).isChecked()) {
                    wifiData.put("url", wifiUrl);
                }

                if (((CheckBox) (dialog.findViewById(R.id.enableWifiPortNumberCheckBox))).isChecked()) {
                    wifiData.put("portNumber", wifiPortNumber);
                }

                bbDeviceController.updateWiFiSettings(wifiData);

                dialog.dismiss();
            }

        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }

        });

        dialog.show();
    }

    public void promptForInitSession() {
        HLog.w(TAG,"promptForInitSession");
        dismissDialog();
        dialog = new Dialog(currentActivity);
        dialog.setContentView(R.layout.general_string_input_dialog);
        dialog.setTitle(getString(R.string.init_session));
        dialog.setCanceledOnTouchOutside(false);

        ((TextView) (dialog.findViewById(R.id.general1TextView))).setText("Vendor Token");
        ((EditText) (dialog.findViewById(R.id.general1EditText))).setText("2BC1EF345F564C7C");

        ((TextView) (dialog.findViewById(R.id.general2TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general2EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general3TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general3EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general4TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general4EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general5TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general5EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general6TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general6EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general7TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general7EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general8TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general8EditText))).setVisibility(View.GONE);

        dialog.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                bbDeviceController.initSession(((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString());

                dialog.dismiss();
            }

        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }

        });

        dialog.show();
    }

    public void promptForStartNfcDetection() {
        HLog.w(TAG,"promptForStartNfcDetection");
        dismissDialog();
        dialog = new Dialog(currentActivity);
        dialog.setContentView(R.layout.general_string_input_dialog);
        dialog.setTitle(getString(R.string.start_nfc_detection));
        dialog.setCanceledOnTouchOutside(false);

        ((TextView) (dialog.findViewById(R.id.general1TextView))).setText("NFC card detection timeout");
        ((TextView) (dialog.findViewById(R.id.general1TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general1EditText))).setText("15");

        ((TextView) (dialog.findViewById(R.id.general2TextView))).setText("NFC Operation Mode");
        ((TextView) (dialog.findViewById(R.id.general2TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general2EditText))).setText("3");

        ((TextView) (dialog.findViewById(R.id.general3TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general3EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general4TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general4EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general5TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general5EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general6TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general6EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general7TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general7EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general8TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general8EditText))).setVisibility(View.GONE);

        dialog.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Hashtable<String, Object> data = new Hashtable<String, Object>();
                String nfcCardDetectionTimeout = ((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString();
                if ((nfcCardDetectionTimeout != null) && (!nfcCardDetectionTimeout.equalsIgnoreCase(""))) {
                    data.put("nfcCardDetectionTimeout", nfcCardDetectionTimeout);
                }
                String nfcOperationMode = ((EditText) (dialog.findViewById(R.id.general2EditText))).getText().toString();
                if ((nfcOperationMode != null) && (!nfcOperationMode.equalsIgnoreCase(""))) {
                    data.put("nfcOperationMode", nfcOperationMode);
                }
                bbDeviceController.startNfcDetection(data);

                dialog.dismiss();
            }

        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }

        });

        dialog.show();
    }

    public void promptForStopNfcDetection() {
        HLog.w(TAG,"promptForStopNfcDetection");
        dismissDialog();
        dialog = new Dialog(currentActivity);
        dialog.setContentView(R.layout.general_string_input_dialog);
        dialog.setTitle(getString(R.string.stop_nfc_detection));
        dialog.setCanceledOnTouchOutside(false);

        ((TextView) (dialog.findViewById(R.id.general1TextView))).setText("NFC card removal timeout");
        ((TextView) (dialog.findViewById(R.id.general1TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general1EditText))).setText("15");

        ((TextView) (dialog.findViewById(R.id.general2TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general2EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general3TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general3EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general4TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general4EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general5TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general5EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general6TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general6EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general7TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general7EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general8TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general8EditText))).setVisibility(View.GONE);

        dialog.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Hashtable<String, Object> data = new Hashtable<String, Object>();
                String nfcCardRemovalTimeout = ((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString();
                if ((nfcCardRemovalTimeout != null) && (!nfcCardRemovalTimeout.equalsIgnoreCase(""))) {
                    data.put("nfcCardRemovalTimeout", ((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString());
                }
                bbDeviceController.stopNfcDetection(data);

                dialog.dismiss();
            }

        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }

        });

        dialog.show();
    }

    public void promptForNfcDataExchangeWrite() {
        HLog.w(TAG,"promptForNfcDataExchangeWrite");
        dismissDialog();
        dialog = new Dialog(currentActivity);
        dialog.setContentView(R.layout.general_string_input_dialog);
        dialog.setTitle(getString(R.string.nfc_data_exchange_write));
        dialog.setCanceledOnTouchOutside(false);

        ((TextView) (dialog.findViewById(R.id.general1TextView))).setText("Write NDEF Record");
        ((TextView) (dialog.findViewById(R.id.general1TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general1EditText))).setText("1234567890");

        ((TextView) (dialog.findViewById(R.id.general2TextView))).setText("Mifare Card Auth Type");
        ((TextView) (dialog.findViewById(R.id.general2TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general2EditText))).setText("0");

        ((TextView) (dialog.findViewById(R.id.general3TextView))).setText("Mifare Card Key");
        ((TextView) (dialog.findViewById(R.id.general3TextView))).getLayoutParams().width = 500;
        //((EditText) (dialog.findViewById(R.id.general3EditText))).setText("FFFFFFFFFFFF");
        ((EditText) (dialog.findViewById(R.id.general3EditText))).setText("0000000000000000");

        ((TextView) (dialog.findViewById(R.id.general4TextView))).setText("Mifare Card Key Number");
        ((TextView) (dialog.findViewById(R.id.general4TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general4EditText))).setText("0");

        ((TextView) (dialog.findViewById(R.id.general5TextView))).setText("Mifare Card Block Number");
        ((TextView) (dialog.findViewById(R.id.general5TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general5EditText))).setText("2");

        ((TextView) (dialog.findViewById(R.id.general6TextView))).setText("Mifare Desfire Card AID");
        ((TextView) (dialog.findViewById(R.id.general6TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general6EditText))).setText("1");

        ((TextView) (dialog.findViewById(R.id.general7TextView))).setText("Mifare Desfire Card FID");
        ((TextView) (dialog.findViewById(R.id.general7TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general7EditText))).setText("0");

        ((TextView) (dialog.findViewById(R.id.general8TextView))).setText("Mifare Desfire Card File Offset");
        ((TextView) (dialog.findViewById(R.id.general8TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general8EditText))).setText("0");

        ((TextView) (dialog.findViewById(R.id.general9TextView))).setVisibility(View.VISIBLE);
        ((EditText) (dialog.findViewById(R.id.general9EditText))).setVisibility(View.VISIBLE);
        ((TextView) (dialog.findViewById(R.id.general9TextView))).setText("Mifare Desfire Card Read Data Length");
        ((TextView) (dialog.findViewById(R.id.general9TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general9EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general10TextView))).setVisibility(View.VISIBLE);
        ((EditText) (dialog.findViewById(R.id.general10EditText))).setVisibility(View.VISIBLE);
        ((TextView) (dialog.findViewById(R.id.general10TextView))).setText("Mifare Desfire Card Special Operation Command");
        ((TextView) (dialog.findViewById(R.id.general10TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general10EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general11TextView))).setVisibility(View.VISIBLE);
        ((EditText) (dialog.findViewById(R.id.general11EditText))).setVisibility(View.VISIBLE);
        ((TextView) (dialog.findViewById(R.id.general11TextView))).setText("Mifare Desfire Card Special Operation Parameter");
        ((TextView) (dialog.findViewById(R.id.general11TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general11EditText))).setText("");

        dialog.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Hashtable<String, Object> data = new Hashtable<String, Object>();
                String writeNdefRecord = ((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString();
                if ((writeNdefRecord != null) && (!writeNdefRecord.equalsIgnoreCase(""))) {
                    data.put("writeNdefRecord", Utils.encodeNdefFormat(writeNdefRecord));
                }
                String mifareCardAuthType = ((EditText) (dialog.findViewById(R.id.general2EditText))).getText().toString();
                if ((mifareCardAuthType != null) && (!mifareCardAuthType.equalsIgnoreCase(""))) {
                    data.put("mifareCardAuthType", mifareCardAuthType);
                }
                String mifareCardKey = ((EditText) (dialog.findViewById(R.id.general3EditText))).getText().toString();
                if ((mifareCardKey != null) && (!mifareCardKey.equalsIgnoreCase(""))) {
                    data.put("mifareCardKey", mifareCardKey);
                }
                String mifareCardKeyNumber = ((EditText) (dialog.findViewById(R.id.general4EditText))).getText().toString();
                if ((mifareCardKeyNumber != null) && (!mifareCardKeyNumber.equalsIgnoreCase(""))) {
                    data.put("mifareCardKeyNumber", mifareCardKeyNumber);
                }
                String mifareCardBlockNumber = ((EditText) (dialog.findViewById(R.id.general5EditText))).getText().toString();
                if ((mifareCardBlockNumber != null) && (!mifareCardBlockNumber.equalsIgnoreCase(""))) {
                    data.put("mifareCardBlockNumber", mifareCardBlockNumber);
                }
                String mifareDesFireCardAID = ((EditText) (dialog.findViewById(R.id.general6EditText))).getText().toString();
                if ((mifareDesFireCardAID != null) && (!mifareDesFireCardAID.equalsIgnoreCase(""))) {
                    data.put("mifareDesFireCardAID", mifareDesFireCardAID);
                }
                String mifareDesFireCardFID = ((EditText) (dialog.findViewById(R.id.general7EditText))).getText().toString();
                if ((mifareDesFireCardFID != null) && (!mifareDesFireCardFID.equalsIgnoreCase(""))) {
                    data.put("mifareDesFireCardFID", mifareDesFireCardFID);
                }
                String mifareDesFireCardFileOffset = ((EditText) (dialog.findViewById(R.id.general8EditText))).getText().toString();
                if ((mifareDesFireCardFileOffset != null) && (!mifareDesFireCardFileOffset.equalsIgnoreCase(""))) {
                    data.put("mifareDesFireCardFileOffset", mifareDesFireCardFileOffset);
                }
                String mifareDesFireCardDataLength = ((EditText) (dialog.findViewById(R.id.general9EditText))).getText().toString();
                if ((mifareDesFireCardDataLength != null) && (!mifareDesFireCardDataLength.equalsIgnoreCase(""))) {
                    data.put("mifareDesFireCardDataLength", mifareDesFireCardDataLength);
                }
                String mifareDesFireCardCommand = ((EditText) (dialog.findViewById(R.id.general10EditText))).getText().toString();
                if ((mifareDesFireCardCommand != null) && (!mifareDesFireCardCommand.equalsIgnoreCase(""))) {
                    data.put("mifareDesFireCardCommand", mifareDesFireCardCommand);
                }
                String mifareDesFireCardCommandData = ((EditText) (dialog.findViewById(R.id.general11EditText))).getText().toString();
                if ((mifareDesFireCardCommandData != null) && (!mifareDesFireCardCommandData.equalsIgnoreCase(""))) {
                    data.put("mifareDesFireCardCommandData", mifareDesFireCardCommandData);
                }
                bbDeviceController.nfcDataExchange(data);

                dialog.dismiss();
            }

        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }

        });

        dialog.show();
    }

    public void promptForNfcDataExchangeRead1St() {
        HLog.w(TAG,"promptForNfcDataExchangeRead1St");
        dismissDialog();
        dialog = new Dialog(currentActivity);
        dialog.setContentView(R.layout.general_string_input_dialog);
        dialog.setTitle(getString(R.string.nfc_data_exchange_read_1st));
        dialog.setCanceledOnTouchOutside(false);

        ((TextView) (dialog.findViewById(R.id.general1TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general1EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general2TextView))).setText("Mifare Card Auth Type");
        ((TextView) (dialog.findViewById(R.id.general2TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general2EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general3TextView))).setText("Mifare Card Key");
        ((TextView) (dialog.findViewById(R.id.general3TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general3EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general4TextView))).setText("Mifare Card Key Number");
        ((TextView) (dialog.findViewById(R.id.general4TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general4EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general5TextView))).setText("Mifare Card Block Number");
        ((TextView) (dialog.findViewById(R.id.general5TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general5EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general6TextView))).setText("Mifare Desfire Card AID");
        ((TextView) (dialog.findViewById(R.id.general6TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general6EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general7TextView))).setText("Mifare Desfire Card FID");
        ((TextView) (dialog.findViewById(R.id.general7TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general7EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general8TextView))).setText("Mifare Desfire Card File Offset");
        ((TextView) (dialog.findViewById(R.id.general8TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general8EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general9TextView))).setVisibility(View.VISIBLE);
        ((EditText) (dialog.findViewById(R.id.general9EditText))).setVisibility(View.VISIBLE);
        ((TextView) (dialog.findViewById(R.id.general9TextView))).setText("Mifare Desfire Card Read Data Length");
        ((TextView) (dialog.findViewById(R.id.general9TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general9EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general10TextView))).setVisibility(View.VISIBLE);
        ((EditText) (dialog.findViewById(R.id.general10EditText))).setVisibility(View.VISIBLE);
        ((TextView) (dialog.findViewById(R.id.general10TextView))).setText("Mifare Desfire Card Special Operation Command");
        ((TextView) (dialog.findViewById(R.id.general10TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general10EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general11TextView))).setVisibility(View.VISIBLE);
        ((EditText) (dialog.findViewById(R.id.general11EditText))).setVisibility(View.VISIBLE);
        ((TextView) (dialog.findViewById(R.id.general11TextView))).setText("Mifare Desfire Card Special Operation Parameter");
        ((TextView) (dialog.findViewById(R.id.general11TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general11EditText))).setText("");

        dialog.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Hashtable<String, Object> data = new Hashtable<String, Object>();
                data.put("readNdefRecord", ReadNdefRecord.READ_1ST);
                String mifareCardAuthType = ((EditText) (dialog.findViewById(R.id.general2EditText))).getText().toString();
                if ((mifareCardAuthType != null) && (!mifareCardAuthType.equalsIgnoreCase(""))) {
                    data.put("mifareCardAuthType", mifareCardAuthType);
                }
                String mifareCardKey = ((EditText) (dialog.findViewById(R.id.general3EditText))).getText().toString();
                if ((mifareCardKey != null) && (!mifareCardKey.equalsIgnoreCase(""))) {
                    data.put("mifareCardKey", mifareCardKey);
                }
                String mifareCardKeyNumber = ((EditText) (dialog.findViewById(R.id.general4EditText))).getText().toString();
                if ((mifareCardKeyNumber != null) && (!mifareCardKeyNumber.equalsIgnoreCase(""))) {
                    data.put("mifareCardKeyNumber", mifareCardKeyNumber);
                }
                String mifareCardBlockNumber = ((EditText) (dialog.findViewById(R.id.general5EditText))).getText().toString();
                if ((mifareCardBlockNumber != null) && (!mifareCardBlockNumber.equalsIgnoreCase(""))) {
                    data.put("mifareCardBlockNumber", mifareCardBlockNumber);
                }
                String mifareDesFireCardAID = ((EditText) (dialog.findViewById(R.id.general6EditText))).getText().toString();
                if ((mifareDesFireCardAID != null) && (!mifareDesFireCardAID.equalsIgnoreCase(""))) {
                    data.put("mifareDesFireCardAID", mifareDesFireCardAID);
                }
                String mifareDesFireCardFID = ((EditText) (dialog.findViewById(R.id.general7EditText))).getText().toString();
                if ((mifareDesFireCardFID != null) && (!mifareDesFireCardFID.equalsIgnoreCase(""))) {
                    data.put("mifareDesFireCardFID", mifareDesFireCardFID);
                }
                String mifareDesFireCardFileOffset = ((EditText) (dialog.findViewById(R.id.general8EditText))).getText().toString();
                if ((mifareDesFireCardFileOffset != null) && (!mifareDesFireCardFileOffset.equalsIgnoreCase(""))) {
                    data.put("mifareDesFireCardFileOffset", mifareDesFireCardFileOffset);
                }
                String mifareDesFireCardDataLength = ((EditText) (dialog.findViewById(R.id.general9EditText))).getText().toString();
                if ((mifareDesFireCardDataLength != null) && (!mifareDesFireCardDataLength.equalsIgnoreCase(""))) {
                    data.put("mifareDesFireCardDataLength", mifareDesFireCardDataLength);
                }
                String mifareDesFireCardCommand = ((EditText) (dialog.findViewById(R.id.general10EditText))).getText().toString();
                if ((mifareDesFireCardCommand != null) && (!mifareDesFireCardCommand.equalsIgnoreCase(""))) {
                    data.put("mifareDesFireCardCommand", mifareDesFireCardCommand);
                }
                String mifareDesFireCardCommandData = ((EditText) (dialog.findViewById(R.id.general11EditText))).getText().toString();
                if ((mifareDesFireCardCommandData != null) && (!mifareDesFireCardCommandData.equalsIgnoreCase(""))) {
                    data.put("mifareDesFireCardCommandData", mifareDesFireCardCommandData);
                }
                bbDeviceController.nfcDataExchange(data);

                dialog.dismiss();
            }

        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }

        });

        dialog.show();
    }

    public void promptForNfcDataExchangeReadNext() {
        HLog.w(TAG,"promptForNfcDataExchangeReadNext");
        dismissDialog();
        dialog = new Dialog(currentActivity);
        dialog.setContentView(R.layout.general_string_input_dialog);
        dialog.setTitle(getString(R.string.nfc_data_exchange_read_next));
        dialog.setCanceledOnTouchOutside(false);

        ((TextView) (dialog.findViewById(R.id.general1TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general1EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general2TextView))).setText("Mifare Card Auth Type");
        ((TextView) (dialog.findViewById(R.id.general2TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general2EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general3TextView))).setText("Mifare Card Key");
        ((TextView) (dialog.findViewById(R.id.general3TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general3EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general4TextView))).setText("Mifare Card Key Number");
        ((TextView) (dialog.findViewById(R.id.general4TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general4EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general5TextView))).setText("Mifare Card Block Number");
        ((TextView) (dialog.findViewById(R.id.general5TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general5EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general6TextView))).setText("Mifare Desfire Card AID");
        ((TextView) (dialog.findViewById(R.id.general6TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general6EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general7TextView))).setText("Mifare Desfire Card FID");
        ((TextView) (dialog.findViewById(R.id.general7TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general7EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general8TextView))).setText("Mifare Desfire Card File Offset");
        ((TextView) (dialog.findViewById(R.id.general8TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general8EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general9TextView))).setVisibility(View.VISIBLE);
        ((EditText) (dialog.findViewById(R.id.general9EditText))).setVisibility(View.VISIBLE);
        ((TextView) (dialog.findViewById(R.id.general9TextView))).setText("Mifare Desfire Card Read Data Length");
        ((TextView) (dialog.findViewById(R.id.general9TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general9EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general10TextView))).setVisibility(View.VISIBLE);
        ((EditText) (dialog.findViewById(R.id.general10EditText))).setVisibility(View.VISIBLE);
        ((TextView) (dialog.findViewById(R.id.general10TextView))).setText("Mifare Desfire Card Special Operation Command");
        ((TextView) (dialog.findViewById(R.id.general10TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general10EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general11TextView))).setVisibility(View.VISIBLE);
        ((EditText) (dialog.findViewById(R.id.general11EditText))).setVisibility(View.VISIBLE);
        ((TextView) (dialog.findViewById(R.id.general11TextView))).setText("Mifare Desfire Card Special Operation Parameter");
        ((TextView) (dialog.findViewById(R.id.general11TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general11EditText))).setText("");

        dialog.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Hashtable<String, Object> data = new Hashtable<String, Object>();
                data.put("readNdefRecord", ReadNdefRecord.READ_NEXT);
                String mifareCardAuthType = ((EditText) (dialog.findViewById(R.id.general2EditText))).getText().toString();
                if ((mifareCardAuthType != null) && (!mifareCardAuthType.equalsIgnoreCase(""))) {
                    data.put("mifareCardAuthType", mifareCardAuthType);
                }
                String mifareCardKey = ((EditText) (dialog.findViewById(R.id.general3EditText))).getText().toString();
                if ((mifareCardKey != null) && (!mifareCardKey.equalsIgnoreCase(""))) {
                    data.put("mifareCardKey", mifareCardKey);
                }
                String mifareCardKeyNumber = ((EditText) (dialog.findViewById(R.id.general4EditText))).getText().toString();
                if ((mifareCardKeyNumber != null) && (!mifareCardKeyNumber.equalsIgnoreCase(""))) {
                    data.put("mifareCardKeyNumber", mifareCardKeyNumber);
                }
                String mifareCardBlockNumber = ((EditText) (dialog.findViewById(R.id.general5EditText))).getText().toString();
                if ((mifareCardBlockNumber != null) && (!mifareCardBlockNumber.equalsIgnoreCase(""))) {
                    data.put("mifareCardBlockNumber", mifareCardBlockNumber);
                }
                String mifareDesFireCardAID = ((EditText) (dialog.findViewById(R.id.general6EditText))).getText().toString();
                if ((mifareDesFireCardAID != null) && (!mifareDesFireCardAID.equalsIgnoreCase(""))) {
                    data.put("mifareDesFireCardAID", mifareDesFireCardAID);
                }
                String mifareDesFireCardFID = ((EditText) (dialog.findViewById(R.id.general7EditText))).getText().toString();
                if ((mifareDesFireCardFID != null) && (!mifareDesFireCardFID.equalsIgnoreCase(""))) {
                    data.put("mifareDesFireCardFID", mifareDesFireCardFID);
                }
                String mifareDesFireCardFileOffset = ((EditText) (dialog.findViewById(R.id.general8EditText))).getText().toString();
                if ((mifareDesFireCardFileOffset != null) && (!mifareDesFireCardFileOffset.equalsIgnoreCase(""))) {
                    data.put("mifareDesFireCardFileOffset", mifareDesFireCardFileOffset);
                }
                String mifareDesFireCardDataLength = ((EditText) (dialog.findViewById(R.id.general9EditText))).getText().toString();
                if ((mifareDesFireCardDataLength != null) && (!mifareDesFireCardDataLength.equalsIgnoreCase(""))) {
                    data.put("mifareDesFireCardDataLength", mifareDesFireCardDataLength);
                }
                String mifareDesFireCardCommand = ((EditText) (dialog.findViewById(R.id.general10EditText))).getText().toString();
                if ((mifareDesFireCardCommand != null) && (!mifareDesFireCardCommand.equalsIgnoreCase(""))) {
                    data.put("mifareDesFireCardCommand", mifareDesFireCardCommand);
                }
                String mifareDesFireCardCommandData = ((EditText) (dialog.findViewById(R.id.general11EditText))).getText().toString();
                if ((mifareDesFireCardCommandData != null) && (!mifareDesFireCardCommandData.equalsIgnoreCase(""))) {
                    data.put("mifareDesFireCardCommandData", mifareDesFireCardCommandData);
                }
                bbDeviceController.nfcDataExchange(data);

                dialog.dismiss();
            }

        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }

        });

        dialog.show();
    }


    public void promptForReadAID() {
        HLog.w(TAG,"promptForReadAID");
        dismissDialog();
        dialog = new Dialog(currentActivity);
        dialog.setContentView(R.layout.general_string_input_dialog);
        dialog.setTitle(getString(R.string.read_aid));
        dialog.setCanceledOnTouchOutside(false);

        ((TextView) (dialog.findViewById(R.id.general1TextView))).setText("appIndex");
        ((TextView) (dialog.findViewById(R.id.general1TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general1EditText))).setText("00000001");

        ((TextView) (dialog.findViewById(R.id.general2TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general2EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general3TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general3EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general4TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general4EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general5TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general5EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general6TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general6EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general7TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general7EditText))).setVisibility(View.GONE);

        ((TextView) (dialog.findViewById(R.id.general8TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general8EditText))).setVisibility(View.GONE);

        dialog.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                bbDeviceController.readAID(((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString());
                dialog.dismiss();
            }

        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }

        });

        dialog.show();
    }

    public void promptForUpdateAID() {
        HLog.w(TAG,"promptForUpdateAID");
        dismissDialog();
        dialog = new Dialog(currentActivity);
        dialog.setContentView(R.layout.general_string_input_dialog);
        dialog.setTitle(getString(R.string.update_aid));
        dialog.setCanceledOnTouchOutside(false);

        ((TextView) (dialog.findViewById(R.id.general1TextView))).setText("appIndex");
        ((TextView) (dialog.findViewById(R.id.general1TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general1EditText))).setText("00000001");

        ((TextView) (dialog.findViewById(R.id.general2TextView))).setText("aid");
        ((TextView) (dialog.findViewById(R.id.general2TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general2EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general3TextView))).setText("appVersion");
        ((TextView) (dialog.findViewById(R.id.general3TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general3EditText))).setText("008C");

        ((TextView) (dialog.findViewById(R.id.general4TextView))).setText("terminalFloorLimit");
        ((TextView) (dialog.findViewById(R.id.general4TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general4EditText))).setText("00000000");

        ((TextView) (dialog.findViewById(R.id.general5TextView))).setText("contactTACDefault");
        ((TextView) (dialog.findViewById(R.id.general5TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general5EditText))).setText("DC4000A800");

        ((TextView) (dialog.findViewById(R.id.general6TextView))).setText("contactTACDenial");
        ((TextView) (dialog.findViewById(R.id.general6TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general6EditText))).setText("0010000000");

        ((TextView) (dialog.findViewById(R.id.general7TextView))).setText("contactTACOnline");
        ((TextView) (dialog.findViewById(R.id.general7TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general7EditText))).setText("DC4004F800");

        ((TextView) (dialog.findViewById(R.id.general8TextView))).setText("defaultTDOL");
        ((TextView) (dialog.findViewById(R.id.general8TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general8EditText))).setText("5F2403");

        ((TextView) (dialog.findViewById(R.id.general9TextView))).setVisibility(View.VISIBLE);
        ((EditText) (dialog.findViewById(R.id.general9EditText))).setVisibility(View.VISIBLE);
        ((TextView) (dialog.findViewById(R.id.general9TextView))).setText("defaultDDOL");
        ((TextView) (dialog.findViewById(R.id.general9TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general9EditText))).setText("9F37045A085F34019A03");

        ((TextView) (dialog.findViewById(R.id.general10TextView))).setVisibility(View.VISIBLE);
        ((EditText) (dialog.findViewById(R.id.general10EditText))).setVisibility(View.VISIBLE);
        ((TextView) (dialog.findViewById(R.id.general10TextView))).setText("contactlessTransactionLimit");
        ((TextView) (dialog.findViewById(R.id.general10TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general10EditText))).setText("999999999999");

        ((TextView) (dialog.findViewById(R.id.general11TextView))).setVisibility(View.VISIBLE);
        ((EditText) (dialog.findViewById(R.id.general11EditText))).setVisibility(View.VISIBLE);
        ((TextView) (dialog.findViewById(R.id.general11TextView))).setText("contactlessCVMRequiredLimit");
        ((TextView) (dialog.findViewById(R.id.general11TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general11EditText))).setText("999999999999");

        ((TextView) (dialog.findViewById(R.id.general12TextView))).setVisibility(View.VISIBLE);
        ((EditText) (dialog.findViewById(R.id.general12EditText))).setVisibility(View.VISIBLE);
        ((TextView) (dialog.findViewById(R.id.general12TextView))).setText("contactlessFloorLimit");
        ((TextView) (dialog.findViewById(R.id.general12TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general12EditText))).setText("000000000000");

        ((TextView) (dialog.findViewById(R.id.general13TextView))).setVisibility(View.VISIBLE);
        ((EditText) (dialog.findViewById(R.id.general13EditText))).setVisibility(View.VISIBLE);
        ((TextView) (dialog.findViewById(R.id.general13TextView))).setText("contactlessTACDefault");
        ((TextView) (dialog.findViewById(R.id.general13TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general13EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general14TextView))).setVisibility(View.VISIBLE);
        ((EditText) (dialog.findViewById(R.id.general14EditText))).setVisibility(View.VISIBLE);
        ((TextView) (dialog.findViewById(R.id.general14TextView))).setText("contactlessTACDenial");
        ((TextView) (dialog.findViewById(R.id.general14TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general14EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general15TextView))).setVisibility(View.VISIBLE);
        ((EditText) (dialog.findViewById(R.id.general15EditText))).setVisibility(View.VISIBLE);
        ((TextView) (dialog.findViewById(R.id.general15TextView))).setText("contactlessTACOnline");
        ((TextView) (dialog.findViewById(R.id.general15TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general15EditText))).setText("");

        ((TextView) (dialog.findViewById(R.id.general16TextView))).setVisibility(View.VISIBLE);
        ((EditText) (dialog.findViewById(R.id.general16EditText))).setVisibility(View.VISIBLE);
        ((TextView) (dialog.findViewById(R.id.general16TextView))).setText("contactlessTransactionLimitODCV");
        ((TextView) (dialog.findViewById(R.id.general16TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general16EditText))).setText("999999999999");

        ((LinearLayout) (dialog.findViewById(R.id.general1LinearLayout))).setOrientation(LinearLayout.VERTICAL);
        ((LinearLayout) (dialog.findViewById(R.id.general2LinearLayout))).setOrientation(LinearLayout.VERTICAL);
        ((LinearLayout) (dialog.findViewById(R.id.general3LinearLayout))).setOrientation(LinearLayout.VERTICAL);
        ((LinearLayout) (dialog.findViewById(R.id.general4LinearLayout))).setOrientation(LinearLayout.VERTICAL);
        ((LinearLayout) (dialog.findViewById(R.id.general5LinearLayout))).setOrientation(LinearLayout.VERTICAL);
        ((LinearLayout) (dialog.findViewById(R.id.general6LinearLayout))).setOrientation(LinearLayout.VERTICAL);
        ((LinearLayout) (dialog.findViewById(R.id.general7LinearLayout))).setOrientation(LinearLayout.VERTICAL);
        ((LinearLayout) (dialog.findViewById(R.id.general8LinearLayout))).setOrientation(LinearLayout.VERTICAL);
        ((LinearLayout) (dialog.findViewById(R.id.general9LinearLayout))).setOrientation(LinearLayout.VERTICAL);
        ((LinearLayout) (dialog.findViewById(R.id.general10LinearLayout))).setOrientation(LinearLayout.VERTICAL);
        ((LinearLayout) (dialog.findViewById(R.id.general11LinearLayout))).setOrientation(LinearLayout.VERTICAL);
        ((LinearLayout) (dialog.findViewById(R.id.general12LinearLayout))).setOrientation(LinearLayout.VERTICAL);
        ((LinearLayout) (dialog.findViewById(R.id.general13LinearLayout))).setOrientation(LinearLayout.VERTICAL);
        ((LinearLayout) (dialog.findViewById(R.id.general14LinearLayout))).setOrientation(LinearLayout.VERTICAL);
        ((LinearLayout) (dialog.findViewById(R.id.general15LinearLayout))).setOrientation(LinearLayout.VERTICAL);
        ((LinearLayout) (dialog.findViewById(R.id.general16LinearLayout))).setOrientation(LinearLayout.VERTICAL);

        dialog.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Hashtable<String, String> data = new Hashtable<String, String>();

                String appIndex = ((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString();
                if ((appIndex != null) && (!appIndex.equalsIgnoreCase(""))) {
                    data.put("appIndex", appIndex);
                }
                String aid = ((EditText) (dialog.findViewById(R.id.general2EditText))).getText().toString();
                if ((aid != null) && (!aid.equalsIgnoreCase(""))) {
                    data.put("aid", aid);
                }
                String appVersion = ((EditText) (dialog.findViewById(R.id.general3EditText))).getText().toString();
                if ((appVersion != null) && (!appVersion.equalsIgnoreCase(""))) {
                    data.put("appVersion", appVersion);
                }
                String terminalFloorLimit = ((EditText) (dialog.findViewById(R.id.general4EditText))).getText().toString();
                if ((terminalFloorLimit != null) && (!terminalFloorLimit.equalsIgnoreCase(""))) {
                    data.put("terminalFloorLimit", terminalFloorLimit);
                }
                String contactTACDefault = ((EditText) (dialog.findViewById(R.id.general5EditText))).getText().toString();
                if ((contactTACDefault != null) && (!contactTACDefault.equalsIgnoreCase(""))) {
                    data.put("contactTACDefault", contactTACDefault);
                }
                String contactTACDenial = ((EditText) (dialog.findViewById(R.id.general6EditText))).getText().toString();
                if ((contactTACDenial != null) && (!contactTACDenial.equalsIgnoreCase(""))) {
                    data.put("contactTACDenial", contactTACDenial);
                }
                String contactTACOnline = ((EditText) (dialog.findViewById(R.id.general7EditText))).getText().toString();
                if ((contactTACOnline != null) && (!contactTACOnline.equalsIgnoreCase(""))) {
                    data.put("contactTACOnline", contactTACOnline);
                }
                String defaultTDOL = ((EditText) (dialog.findViewById(R.id.general8EditText))).getText().toString();
                if ((defaultTDOL != null) && (!defaultTDOL.equalsIgnoreCase(""))) {
                    data.put("defaultTDOL", defaultTDOL);
                }
                String defaultDDOL = ((EditText) (dialog.findViewById(R.id.general9EditText))).getText().toString();
                if ((defaultDDOL != null) && (!defaultDDOL.equalsIgnoreCase(""))) {
                    data.put("defaultDDOL", defaultDDOL);
                }
                String contactlessTransactionLimit = ((EditText) (dialog.findViewById(R.id.general10EditText))).getText().toString();
                if ((contactlessTransactionLimit != null) && (!contactlessTransactionLimit.equalsIgnoreCase(""))) {
                    data.put("contactlessTransactionLimit", contactlessTransactionLimit);
                }
                String contactlessCVMRequiredLimit = ((EditText) (dialog.findViewById(R.id.general11EditText))).getText().toString();
                if ((contactlessCVMRequiredLimit != null) && (!contactlessCVMRequiredLimit.equalsIgnoreCase(""))) {
                    data.put("contactlessCVMRequiredLimit", contactlessCVMRequiredLimit);
                }
                String contactlessFloorLimit = ((EditText) (dialog.findViewById(R.id.general12EditText))).getText().toString();
                if ((contactlessFloorLimit != null) && (!contactlessFloorLimit.equalsIgnoreCase(""))) {
                    data.put("contactlessFloorLimit", contactlessFloorLimit);
                }
                String contactlessTACDefault = ((EditText) (dialog.findViewById(R.id.general13EditText))).getText().toString();
                if ((contactlessTACDefault != null) && (!contactlessTACDefault.equalsIgnoreCase(""))) {
                    data.put("contactlessTACDefault", contactlessTACDefault);
                }
                String contactlessTACDenial = ((EditText) (dialog.findViewById(R.id.general14EditText))).getText().toString();
                if ((contactlessTACDenial != null) && (!contactlessTACDenial.equalsIgnoreCase(""))) {
                    data.put("contactlessTACDenial", contactlessTACDenial);
                }
                String contactlessTACOnline = ((EditText) (dialog.findViewById(R.id.general15EditText))).getText().toString();
                if ((contactlessTACOnline != null) && (!contactlessTACOnline.equalsIgnoreCase(""))) {
                    data.put("contactlessTACOnline", contactlessTACOnline);
                }
                String contactlessTransactionLimitODCV = ((EditText) (dialog.findViewById(R.id.general16EditText))).getText().toString();
                if ((contactlessTransactionLimitODCV != null) && (!contactlessTransactionLimitODCV.equalsIgnoreCase(""))) {
                    data.put("contactlessTransactionLimitODCV", contactlessTransactionLimitODCV);
                }
                bbDeviceController.updateAID(data);

                dialog.dismiss();
            }

        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }

        });

        dialog.show();
    }

    public void promptForControlLED() {
        HLog.w(TAG,"promptForControlLED");
        dismissDialog();
        dialog = new Dialog(currentActivity);
        dialog.setContentView(R.layout.control_led_dialog);
        dialog.setTitle(getString(R.string.control_led));
        dialog.setCanceledOnTouchOutside(false);

        ((TextView) (dialog.findViewById(R.id.general1TextView))).setText("duration (HEX)");
        ((TextView) (dialog.findViewById(R.id.general1TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general1EditText))).setText("10");

        ((CheckBox) (dialog.findViewById(R.id.general2CheckBox))).setText("ledIndex1");

        ((TextView) (dialog.findViewById(R.id.general3TextView))).setText("flashOnInterval (HEX)");
        ((TextView) (dialog.findViewById(R.id.general3TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general3EditText))).setText("50");

        ((TextView) (dialog.findViewById(R.id.general4TextView))).setText("flashOffInterval (HEX)");
        ((TextView) (dialog.findViewById(R.id.general4TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general4EditText))).setText("20");

        ((TextView) (dialog.findViewById(R.id.general5TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general5EditText))).setVisibility(View.GONE);

        ((CheckBox) (dialog.findViewById(R.id.general7CheckBox))).setText("ledIndex2");

        ((TextView) (dialog.findViewById(R.id.general8TextView))).setText("flashOnInterval (HEX)");
        ((TextView) (dialog.findViewById(R.id.general8TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general8EditText))).setText("50");

        ((TextView) (dialog.findViewById(R.id.general9TextView))).setText("flashOffInterval (HEX)");
        ((TextView) (dialog.findViewById(R.id.general9TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general9EditText))).setText("20");

        ((TextView) (dialog.findViewById(R.id.general10TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general10EditText))).setVisibility(View.GONE);

        ((CheckBox) (dialog.findViewById(R.id.general12CheckBox))).setText("ledIndex3");

        ((TextView) (dialog.findViewById(R.id.general13TextView))).setText("flashOnInterval (HEX)");
        ((TextView) (dialog.findViewById(R.id.general13TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general13EditText))).setText("50");

        ((TextView) (dialog.findViewById(R.id.general14TextView))).setText("flashOffInterval (HEX)");
        ((TextView) (dialog.findViewById(R.id.general14TextView))).getLayoutParams().width = 500;
        ((EditText) (dialog.findViewById(R.id.general14EditText))).setText("20");

        ((TextView) (dialog.findViewById(R.id.general15TextView))).setVisibility(View.GONE);
        ((EditText) (dialog.findViewById(R.id.general15EditText))).setVisibility(View.GONE);

        ((RadioButton) (dialog.findViewById(R.id.radio1Button))).setText("DEFAULT");
        ((RadioButton) (dialog.findViewById(R.id.radio2Button))).setText("ON");
        ((RadioButton) (dialog.findViewById(R.id.radio3Button))).setText("OFF");
        ((RadioButton) (dialog.findViewById(R.id.radio4Button))).setText("FLASH");

        ((RadioButton) (dialog.findViewById(R.id.radio5Button))).setText("DEFAULT");
        ((RadioButton) (dialog.findViewById(R.id.radio6Button))).setText("ON");
        ((RadioButton) (dialog.findViewById(R.id.radio7Button))).setText("OFF");
        ((RadioButton) (dialog.findViewById(R.id.radio8Button))).setText("FLASH");

        ((RadioButton) (dialog.findViewById(R.id.radio9Button))).setText("DEFAULT");
        ((RadioButton) (dialog.findViewById(R.id.radio10Button))).setText("ON");
        ((RadioButton) (dialog.findViewById(R.id.radio11Button))).setText("OFF");
        ((RadioButton) (dialog.findViewById(R.id.radio12Button))).setText("FLASH");

        dialog.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Hashtable<String, Object> data = new Hashtable<String, Object>();
                data.put("duration", ((EditText) (dialog.findViewById(R.id.general1EditText))).getText().toString());
                if (((CheckBox) (dialog.findViewById(R.id.general2CheckBox))).isChecked()) {
                    Hashtable<String, Object> led1Data = new Hashtable<String, Object>();
                    String led1FlashOnInterval = ((EditText) (dialog.findViewById(R.id.general3EditText))).getText().toString();
                    if ((led1FlashOnInterval != null) && (!led1FlashOnInterval.equalsIgnoreCase(""))) {
                        led1Data.put("flashOnInterval", led1FlashOnInterval);
                    }
                    String led1FlashOffInterval = ((EditText) (dialog.findViewById(R.id.general4EditText))).getText().toString();
                    if ((led1FlashOffInterval != null) && (!led1FlashOffInterval.equalsIgnoreCase(""))) {
                        led1Data.put("flashOffInterval", led1FlashOffInterval);
                    }
                    if (((RadioButton) (dialog.findViewById(R.id.radio1Button))).isChecked()) {
                        led1Data.put("mode", LEDMode.DEFAULT);
                    } else if (((RadioButton) (dialog.findViewById(R.id.radio2Button))).isChecked()) {
                        led1Data.put("mode", LEDMode.ON);
                    } else if (((RadioButton) (dialog.findViewById(R.id.radio3Button))).isChecked()) {
                        led1Data.put("mode", LEDMode.OFF);
                    } else if (((RadioButton) (dialog.findViewById(R.id.radio4Button))).isChecked()) {
                        led1Data.put("mode", LEDMode.FLASH);
                    }
                    data.put("ledIndex1", led1Data);
                }

                if (((CheckBox) (dialog.findViewById(R.id.general7CheckBox))).isChecked()) {
                    Hashtable<String, Object> led2Data = new Hashtable<String, Object>();
                    String led2FlashOnInterval = ((EditText) (dialog.findViewById(R.id.general8EditText))).getText().toString();
                    if ((led2FlashOnInterval != null) && (!led2FlashOnInterval.equalsIgnoreCase(""))) {
                        led2Data.put("flashOnInterval", led2FlashOnInterval);
                    }
                    String led2FlashOffInterval = ((EditText) (dialog.findViewById(R.id.general9EditText))).getText().toString();
                    if ((led2FlashOffInterval != null) && (!led2FlashOffInterval.equalsIgnoreCase(""))) {
                        led2Data.put("flashOffInterval", led2FlashOffInterval);
                    }
                    if (((RadioButton) (dialog.findViewById(R.id.radio5Button))).isChecked()) {
                        led2Data.put("mode", LEDMode.DEFAULT);
                    } else if (((RadioButton) (dialog.findViewById(R.id.radio6Button))).isChecked()) {
                        led2Data.put("mode", LEDMode.ON);
                    } else if (((RadioButton) (dialog.findViewById(R.id.radio7Button))).isChecked()) {
                        led2Data.put("mode", LEDMode.OFF);
                    } else if (((RadioButton) (dialog.findViewById(R.id.radio8Button))).isChecked()) {
                        led2Data.put("mode", LEDMode.FLASH);
                    }
                    data.put("ledIndex2", led2Data);
                }

                if (((CheckBox) (dialog.findViewById(R.id.general12CheckBox))).isChecked()) {
                    Hashtable<String, Object> led3Data = new Hashtable<String, Object>();
                    String led3FlashOnInterval = ((EditText) (dialog.findViewById(R.id.general13EditText))).getText().toString();
                    if ((led3FlashOnInterval != null) && (!led3FlashOnInterval.equalsIgnoreCase(""))) {
                        led3Data.put("flashOnInterval", led3FlashOnInterval);
                    }
                    String led3FlashOffInterval = ((EditText) (dialog.findViewById(R.id.general14EditText))).getText().toString();
                    if ((led3FlashOffInterval != null) && (!led3FlashOffInterval.equalsIgnoreCase(""))) {
                        led3Data.put("flashOffInterval", led3FlashOffInterval);
                    }
                    if (((RadioButton) (dialog.findViewById(R.id.radio9Button))).isChecked()) {
                        led3Data.put("mode", LEDMode.DEFAULT);
                    } else if (((RadioButton) (dialog.findViewById(R.id.radio10Button))).isChecked()) {
                        led3Data.put("mode", LEDMode.ON);
                    } else if (((RadioButton) (dialog.findViewById(R.id.radio11Button))).isChecked()) {
                        led3Data.put("mode", LEDMode.OFF);
                    } else if (((RadioButton) (dialog.findViewById(R.id.radio12Button))).isChecked()) {
                        led3Data.put("mode", LEDMode.FLASH);
                    }
                    data.put("ledIndex3", led3Data);
                }
                bbDeviceController.controlLED(data);

                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }

        });

        dialog.show();
    }


    public void checkCard() {
        HLog.w(TAG,"checkCard");
        Hashtable<String, Object> data = new Hashtable<String, Object>();
        if (checkCardMode != null) {
            data.put("checkCardMode", checkCardMode);
        }
        data.put("checkCardMode", checkCardMode);
        data.put("checkCardTimeout", "120");
        if (fidSpinner.getSelectedItem().equals("FID61_orderID_randomNumber")) {
            data.put("orderID", "0123456789ABCDEF0123456789ABCDEF");
            data.put("randomNumber", "012345");
            Toast.makeText(this,"orderID__randomNumber",Toast.LENGTH_LONG).show();
        } else if (fidSpinner.getSelectedItem().equals("FID46_randomNumber")) {
            data.put("randomNumber", "0123456789ABCDEF");
            Toast.makeText(this,"only__randomNumber",Toast.LENGTH_LONG).show();
        } else if (fidSpinner.getSelectedItem().equals("FID65_pin_data_mac")) {
            String encWorkingKey = TripleDES.encrypt(fid65WorkingKey, fid65MasterKey);
            String workingKeyKcv = TripleDES.encrypt("0000000000000000", fid65WorkingKey);

            data.put("encPinKey", encWorkingKey + workingKeyKcv);
            data.put("encDataKey", encWorkingKey + workingKeyKcv);
            data.put("encMacKey", encWorkingKey + workingKeyKcv);
            Toast.makeText(this,"encPinKey__encDataKey__encMacKey",Toast.LENGTH_LONG).show();
        }
        HLog.w(TAG,data.toString());
        bbDeviceController.checkCard(data);
    }

    public void startEmv() {
        HLog.w(TAG,"startEmv");
        Hashtable<String, Object> data = new Hashtable<String, Object>();
        data.put("emvOption", EmvOption.START);
        if (fidSpinner.getSelectedItem().equals("FID61")) {
            data.put("orderID", "0123456789ABCDEF0123456789ABCDEF");
            data.put("randomNumber", "012345");
        } else if (fidSpinner.getSelectedItem().equals("FID46")) {
            data.put("randomNumber", "0123456789ABCDEF");
        } else if (fidSpinner.getSelectedItem().equals("FID65")) {
            String encWorkingKey = TripleDES.encrypt(fid65WorkingKey, fid65MasterKey);
            String workingKeyKcv = TripleDES.encrypt("0000000000000000", fid65WorkingKey);

            data.put("encPinKey", encWorkingKey + workingKeyKcv);
            data.put("encDataKey", encWorkingKey + workingKeyKcv);
            data.put("encMacKey", encWorkingKey + workingKeyKcv);
        }
        if (checkCardMode != null) {
            data.put("checkCardMode", checkCardMode);
        }

        String terminalTime = new SimpleDateFormat("yyMMddHHmmss").format(Calendar.getInstance().getTime());
        data.put("terminalTime", terminalTime);
        HLog.w(TAG,"startEmv:"+data.toString());

        bbDeviceController.startEmv(data);
    }

    private static String hexString2AsciiString(String hexString) {
        HLog.w(TAG,"hexString2AsciiString__hexString="+hexString);
        if (hexString == null)
            return "";
        hexString = hexString.replaceAll(" ", "");
        if (hexString.length() % 2 != 0) {
            return "";
        }
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hexString.length(); i += 2) {
            String str = hexString.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    private static byte[] hexToByteArray(String s) {
        HLog.w(TAG,"hexToByteArray="+s);
        if (s == null) {
            s = "";
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        for (int i = 0; i < s.length() - 1; i += 2) {
            String data = s.substring(i, i + 2);
            bout.write(Integer.parseInt(data, 16));
        }
        return bout.toByteArray();
    }

    private static String toHexString(byte[] b) {
        if (b == null) {
            return "null";
        }
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xFF) + 0x100, 16).substring(1);
        }
        return result;
    }

    public static void setStatus(String message) {
        HLog.w(TAG,"setStatus="+message+"\nstatusEditText=" + statusEditText.getText());
        String tmp = message + "\n" + statusEditText.getText();
        int maxLength = 20000;
        if (tmp.length() >= maxLength) {
            int index = tmp.indexOf("\n", maxLength);
            if (index >= maxLength) {
                statusEditText.setText(tmp.substring(0, index));
                return;
            }
        }
        statusEditText.setText(tmp);
    }

    protected static String toHexString(byte b) {
        return Integer.toString((b & 0xFF) + 0x100, 16).substring(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_start_connection) {
            promptForConnection();
            return true;
        } else if (item.getItemId() == R.id.menu_stop_connection) {
            stopConnection();
            return true;
        } else if (item.getItemId() == R.id.menu_init_session) {
            statusEditText.setText(R.string.initializing_session);
            promptForInitSession();
        } else if (item.getItemId() == R.id.menu_reset_session) {
            statusEditText.setText(R.string.reset_session);
            bbDeviceController.resetSession();
        } else if (item.getItemId() == R.id.menu_get_deivce_info) {
            statusEditText.setText(R.string.getting_info);
            bbDeviceController.getDeviceInfo();
        } else if (item.getItemId() == R.id.menu_unpair_all) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Handler handler = new Handler(Looper.getMainLooper());
                    try {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(BaseActivity.this, getString(R.string.unpair_all_start), Toast.LENGTH_SHORT).show();
                            }
                        });

                        Object[] pairedObjects = BluetoothAdapter.getDefaultAdapter().getBondedDevices().toArray();
                        BluetoothDevice pairedDevices;
                        for (int i = 0; i < pairedObjects.length; ++i) {
                            pairedDevices = (BluetoothDevice) pairedObjects[i];

                            try {
                                Method m = pairedDevices.getClass().getMethod("removeBond", (Class[]) null);
                                m.invoke(pairedDevices, (Object[]) null);
                                Thread.sleep(3000);
                            } catch (Exception e) {
                            }
                        }

                    } catch (Exception e) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(BaseActivity.this, getString(R.string.unpair_all_fail), Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(BaseActivity.this, getString(R.string.unpair_all_end), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }).start();

            return true;
        } else if (item.getItemId() == R.id.menu_cancel_check_card) {
            statusEditText.setText(R.string.cancel_check_card);
            bbDeviceController.cancelCheckCard();
        } else if (item.getItemId() == R.id.menu_auto_config) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setTitle(R.string.auto_configuring);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(100);
            progressDialog.setIndeterminate(false);
            progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    statusEditText.setText(getString(R.string.canceling_auto_config));
                    bbDeviceController.cancelAudioAutoConfig();
                }
            });
            progressDialog.show();
            bbDeviceController.startAudioAutoConfig();
        } else if (item.getItemId() == R.id.menu_enable_input_amount) {
            Hashtable<String, Object> data = new Hashtable<String, Object>();
            if (Locale.getDefault().getCountry().equalsIgnoreCase("CN")) {
                data.put("currencyCode", "156");
                data.put("currencyCharacters", new CurrencyCharacter[]{CurrencyCharacter.YEN});
            } else {
                data.put("currencyCode", "840");
                data.put("currencyCharacters", new CurrencyCharacter[]{CurrencyCharacter.DOLLAR});
            }
            data.put("amountInputType", AmountInputType.AMOUNT_AND_CASHBACK);
            bbDeviceController.enableInputAmount(data);
        } else if (item.getItemId() == R.id.menu_encrypt_pin) {
            String encWorkingKey = encrypt(fid65WorkingKey, fid65MasterKey);
            String workingKeyKcv = encrypt("0000000000000000", fid65WorkingKey);

            Hashtable<String, Object> data = new Hashtable<String, Object>();
            data.put("pin", "123456");
            data.put("pan", "123456789012345678");
            data.put("encPinKey", encWorkingKey + workingKeyKcv);

            bbDeviceController.encryptPin(data);
        } else if (item.getItemId() == R.id.menu_encrypt_data) {
            String encWorkingKey = encrypt(fid65WorkingKey, fid65MasterKey);
            String workingKeyKcv = encrypt("0000000000000000", fid65WorkingKey);

            Hashtable<String, Object> data = new Hashtable<String, Object>();
            data.put("data", "0123456789ABCDEF0123456789ABCDEF");
            data.put("encWorkingKey", encWorkingKey + workingKeyKcv);
            data.put("encryptionMethod", EncryptionMethod.MAC_METHOD_1);
            data.put("encryptionKeySource", EncryptionKeySource.BY_SERVER_16_BYTES_WORKING_KEY);
            data.put("encryptionPaddingMethod", EncryptionPaddingMethod.ZERO_PADDING);
            data.put("macLength", "8");
            data.put("randomNumber", "0123456789ABCDEF");
            data.put("keyUsage", EncryptionKeyUsage.TAK);
            data.put("initialVector", "0000000000000000");
            bbDeviceController.encryptDataWithSettings(data);
        } else if (item.getItemId() == R.id.menu_print_sample) {
            receipts = new ArrayList<byte[]>();
            if (Locale.getDefault().getCountry().equalsIgnoreCase("CN")) {
                receipts.add(ReceiptUtility.genReceipt2(this));
            } else {
                receipts.add(ReceiptUtility.genReceipt(this));
            }
            bbDeviceController.startPrint(receipts.size(), 60);
        } else if (item.getItemId() == R.id.menu_main) {
            isSwitchingActivity = true;
            finish();
            Intent in = new Intent(this, MainActivity.class);
            startActivity(in);
        } else if (item.getItemId() == R.id.menu_apdu) {
            isSwitchingActivity = true;
            finish();
            Intent in = new Intent(this, ApduActivity.class);
            startActivity(in);
        } else if (item.getItemId() == R.id.capk_activity) {
            isSwitchingActivity = true;
            finish();
            Intent in = new Intent(this, CAPKActivity.class);
            startActivity(in);
        } else if (item.getItemId() == R.id.menu_gprs_wifi) {
            isSwitchingActivity = true;
            finish();
            Intent in = new Intent(this, GprsWifiActivity.class);
            startActivity(in);
        } else if (item.getItemId() == R.id.menu_nfc) {
            isSwitchingActivity = true;
            finish();
            Intent in = new Intent(this, NfcActivity.class);
            startActivity(in);
        } else if (item.getItemId() == R.id.menu_inject_session_key) {
            BaseActivity.encryptedPinSessionKey = TripleDES.encrypt(BaseActivity.pinSessionKey, BaseActivity.masterKey);
            BaseActivity.pinKcv = TripleDES.encrypt("0000000000000000", BaseActivity.pinSessionKey);
            BaseActivity.pinKcv = BaseActivity.pinKcv.substring(0, 6);

            BaseActivity.encryptedDataSessionKey = TripleDES.encrypt(BaseActivity.dataSessionKey, BaseActivity.masterKey);
            BaseActivity.dataKcv = TripleDES.encrypt("0000000000000000", BaseActivity.dataSessionKey);
            BaseActivity.dataKcv = BaseActivity.dataKcv.substring(0, 6);

            BaseActivity.encryptedTrackSessionKey = TripleDES.encrypt(BaseActivity.trackSessionKey, BaseActivity.masterKey);
            BaseActivity.trackKcv = TripleDES.encrypt("0000000000000000", BaseActivity.trackSessionKey);
            BaseActivity.trackKcv = BaseActivity.trackKcv.substring(0, 6);

            BaseActivity.encryptedMacSessionKey = TripleDES.encrypt(BaseActivity.macSessionKey, BaseActivity.masterKey);
            BaseActivity.macKcv = TripleDES.encrypt("0000000000000000", BaseActivity.macSessionKey);
            BaseActivity.macKcv = BaseActivity.macKcv.substring(0, 6);

            injectNextSessionKey();
        } else if (item.getItemId() == R.id.menu_powerDown) {
            statusEditText.setText(R.string.power_down);
            bbDeviceController.powerDown();
        } else if (item.getItemId() == R.id.menu_enterStandby) {
            statusEditText.setText(R.string.standby);
            bbDeviceController.enterStandbyMode();
        } else if (item.getItemId() == R.id.menu_control_led) {
            statusEditText.setText(R.string.led);
            promptForControlLED();
        } else if (item.getItemId() == R.id.menu_readAID) {
            statusEditText.setText(R.string.reading_aid);
            promptForReadAID();
        } else if (item.getItemId() == R.id.menu_updateAID) {
            statusEditText.setText(R.string.updating_aid_please_wait);
            promptForUpdateAID();
        }
        return true;
    }

    private void handleApduResult(boolean isSuccess, String apdu, int apduLength) {
        HLog.w(TAG,"isSuccess="+isSuccess+"\napdu="+apdu+"\napduLength="+apduLength);
        dismissDialog();

        try {
            if (isSuccess) {

                if (isApduEncrypted) {
                    String key;
                    if (keyMode.equals(DATA_KEY)) {
                        key = DUKPTServer.GetDataKey(ksn, "0123456789ABCDEFFEDCBA9876543210");
                    } else if (keyMode.equals(DATA_KEY_VAR)) {
                        key = DUKPTServer.GetDataKeyVar(ksn, "0123456789ABCDEFFEDCBA9876543210");
                    } else {
                        key = DUKPTServer.GetPinKeyVar(ksn, "0123456789ABCDEFFEDCBA9876543210");
                    }
                    HLog.w(TAG,"ksn="+ksn+"\nkey="+key);

                    if (encryptionMode.equals(CBC)) {
                        apdu = TripleDES.decrypt_CBC(apdu, key);
                    } else {
                        apdu = TripleDES.decrypt(apdu, key);
                    }

                    if (apduLength == 0) {
                        int padding = Integer.parseInt(apdu.substring(apdu.length() - 2));
                        apdu = apdu.substring(0, apdu.length() - padding * 2);
                    } else {
                        apdu = apdu.substring(0, apduLength * 2);
                    }
                }

                setStatus(getString(R.string.apdu_result) + apdu);

                if (apdu.startsWith("61") && apdu.length() == 4) {
                    sendApdu("00C00000" + apdu.substring(2));
                    HLog.w(TAG,"apdu="+apdu+"\nvalue="+"00C00000" + apdu.substring(2));
                    return;
                }

                if (state == State.GETTING_PSE) {
                    if (apdu.endsWith("9000")) {
                        List<TLV> tlvList = TLVParser.parse(apdu.substring(0, apdu.length() - 4));
                        TLV tlv = TLVParser.searchTLV(tlvList, "88");
                        if (tlv != null && tlv.value.equals("01")) {
                            state = State.READING_RECORD;
                            sendApdu("00B2010C00");
                            HLog.w(TAG,"apdu="+apdu+"\nsendApdu="+"00B2010C00");
                            // setStatus("Reading record...");
                        }
                    } else if (apdu.equalsIgnoreCase("6A82")) {
                        aidCounter = 0;
                        state = State.READING_AID;
                        sendApdu("00A40400" + toHexString((byte) (aids[aidCounter].length() / 2)) + aids[aidCounter]);
                        HLog.w(TAG,"apdu="+apdu+"\nsendApdu="+"00A40400" + toHexString((byte) (aids[aidCounter].length() / 2)) + aids[aidCounter]);
                    }
                } else if (state == State.READING_RECORD) {
                    if (apdu.endsWith("9000")) {
                        List<TLV> tlvList = TLVParser.parse(apdu.substring(0, apdu.length() - 4));
                        TLV tlv = TLVParser.searchTLV(tlvList, "4F");
                        if (tlv != null) {
                            state = State.READING_AID;
                            sendApdu("00A40400" + tlv.length + tlv.value);
                            HLog.w(TAG,"apdu="+apdu+"\nsendApdu="+"00A40400" + tlv.length + tlv.value);
                        }
                    }
                } else if (state == State.READING_AID) {
                    if (apdu.endsWith("9000")) {
                        List<TLV> tlvList = TLVParser.parse(apdu.substring(0, apdu.length() - 4));
                        TLV tlv = TLVParser.searchTLV(tlvList, "9F38");
                        state = State.GETTING_PROCESS_OPTION;
                        String command = "80A800000283";
                        if (tlv != null) {
                            int len = 0;
                            List<TLV> challenges = TLVParser.parseWithoutValue(tlv.value);
                            for (int i = 0; i < challenges.size(); ++i) {
                                len += Integer.parseInt(challenges.get(i).length);
                            }
                            command = "80A80000" + toHexString((byte) (len + 2)) + "83" + toHexString((byte) len);
                            for (int i = 0; i < len; ++i) {
                                command += "00";
                            }
                        } else {
                            command += "00";
                        }

                        sendApdu(command);
                        // setStatus("Getting Process Option...");
                    } else if (apdu.equalsIgnoreCase("6A82")) {
                        ++aidCounter;
                        if (aidCounter < aids.length) {
                            sendApdu("00A40400" + toHexString((byte) (aids[aidCounter].length() / 2)) + aids[aidCounter]);
                        } else {
                            setStatus(getString(R.string.no_aid_matched));
                        }
                        HLog.w(TAG,"apdu="+apdu+"\nsendApdu="+"00A40400" + toHexString((byte) (aids[aidCounter].length() / 2)) + aids[aidCounter]);
                        // setStatus("Read AID failed");
                        // setStatus("Trying to read AID " + aids[aidCounter] +
                        // "...");
                    }
                } else if (state == State.GETTING_PROCESS_OPTION) {
                    if (apdu.endsWith("9000")) {
                        List<TLV> tlvList = TLVParser.parse(apdu.substring(0, apdu.length() - 4));
                        TLV tlv = TLVParser.searchTLV(tlvList, "94");
                        if (tlv != null) {
                            aflCounter = 0;
                            afls = new String[tlv.value.length() / 8];
                            for (int i = 0; i < afls.length; ++i) {
                                afls[i] = tlv.value.substring(i * 8, i * 8 + 8);
                            }
                            readingFileIndex = Integer.parseInt(afls[aflCounter].substring(2, 4), 16);
                            total = Integer.parseInt(afls[aflCounter].substring(4, 6), 16);
                            sfi = toHexString((byte) (((Integer.parseInt(afls[aflCounter].substring(0, 2), 16) & 0xF8) | 0x04)));

                            state = State.READING_DATA;

                            sendApdu("00B2" + toHexString((byte) readingFileIndex) + sfi + "00");
                            HLog.w(TAG,"apdu="+apdu+"\nsendApdu="+"00B2" + toHexString((byte) readingFileIndex) + sfi + "00");
                            // setStatus("Reading record...");
                        } else if (apdu.startsWith("80")) {
                            afls = new String[(apdu.length() - 12) / 8];
                            for (int i = 0; i < afls.length; ++i) {
                                afls[i] = apdu.substring(i * 8 + 8, i * 8 + 16);
                            }

                            aflCounter = 0;
                            readingFileIndex = Integer.parseInt(afls[aflCounter].substring(2, 4), 16);
                            total = Integer.parseInt(afls[aflCounter].substring(4, 6), 16);
                            sfi = toHexString((byte) (((Integer.parseInt(afls[aflCounter].substring(0, 2), 16) & 0xF8) | 0x04)));

                            state = State.READING_DATA;

                            sendApdu("00B2" + toHexString((byte) readingFileIndex) + sfi + "00");
                            HLog.w(TAG,"apdu="+apdu+"\nsendApdu="+"00B2" + toHexString((byte) readingFileIndex) + sfi + "00");
                            // setStatus("Reading record...");
                        }
                    }
                } else if (state == State.READING_DATA) {
                    if (apdu.endsWith("9000")) {
                        List<TLV> tlvList = TLVParser.parse(apdu.substring(0, apdu.length() - 4));
                        TLV tlv;
                        tlv = TLVParser.searchTLV(tlvList, "5F20");
                        if (tlv != null) {
                            cardholderName = new String(hexToByteArray(tlv.value));
                        }

                        tlv = TLVParser.searchTLV(tlvList, "5F24");
                        if (tlv != null) {
                            expiryDate = tlv.value;
                        }

                        tlv = TLVParser.searchTLV(tlvList, "57");
                        if (tlv != null) {
                            track2 = tlv.value;
                        }

                        tlv = TLVParser.searchTLV(tlvList, "5A");
                        if (tlv != null) {
                            pan = tlv.value;
                        }

                        if (!cardholderName.equals("") && !expiryDate.equals("") && !track2.equals("") && !pan.equals("")) {
                            setStatus("");
                            setStatus("Cardholder Name: " + cardholderName);
                            setStatus("Expire Date: " + expiryDate);
                            setStatus("Track 2: " + track2);
                            setStatus("PAN: " + pan);
                            if (startTime != 0) {
                                setStatus((System.currentTimeMillis() - startTime) + "ms");
                                startTime = 0;
                            }
                            return;
                        }

                        ++readingFileIndex;
                        if (readingFileIndex <= total) {
                            sendApdu("00B2" + toHexString((byte) readingFileIndex) + sfi + "00");
                        } else if (aflCounter < afls.length - 1) {
                            ++aflCounter;
                            readingFileIndex = Integer.parseInt(afls[aflCounter].substring(2, 4), 16);
                            total = Integer.parseInt(afls[aflCounter].substring(4, 6), 16);
                            sfi = toHexString((byte) (((Integer.parseInt(afls[aflCounter].substring(0, 2), 16) & 0xF8) | 0x04)));

                            state = State.READING_DATA;

                            sendApdu("00B2" + toHexString((byte) readingFileIndex) + sfi + "00");
                            HLog.w(TAG,"apdu="+apdu+"\nsendApdu="+"00B2" + toHexString((byte) readingFileIndex) + sfi + "00");
                            // setStatus("Reading record...");
                        }
                    }
                }
                /*
                 * ++count; if(count < apduCommands.length) {
				 *
				 * //setStatus(getString(R.string.sending) + apduCommands[count]); //emvSwipeController.sendApdu(apduCommands[count], apduCommands[count].length() / 2);
				 *
				 * setStatus(getString(R.string.sending) + apduCommands[count]);
				 *
				 * String command = apduCommands[count]; while((command.length() / 2) % 8 != 0) { command = command + "00"; } String encryptedCommand = TripleDES.encrypt_CBC(command, key); emvSwipeController.sendApdu(encryptedCommand, apduCommands[count].length() / 2); }
				 */
            } else {
                setStatus(getString(R.string.apdu_failed));
            }
        } catch (Exception e) {
            setStatus(e.getMessage());
            StackTraceElement[] elements = e.getStackTrace();
            for (int i = 0; i < elements.length; ++i) {
                setStatus(elements[i].toString());
            }
        }
    }

    protected void sendApdu(String command) {
        HLog.w(TAG,"sendApdu___command="+command);
        try {
            if (isApduEncrypted) {
                String key;
                if (keyMode.equals(DATA_KEY)) {
                    key = DUKPTServer.GetDataKey(ksn, "0123456789ABCDEFFEDCBA9876543210");
                } else if (keyMode.equals(DATA_KEY_VAR)) {
                    key = DUKPTServer.GetDataKeyVar(ksn, "0123456789ABCDEFFEDCBA9876543210");
                } else {
                    key = DUKPTServer.GetPinKeyVar(ksn, "0123456789ABCDEFFEDCBA9876543210");
                }

                HLog.w(TAG,"key="+key);
                String temp = command;
                if (isPKCS7) {
                    int padding = 8 - (temp.length() / 2) % 8;
                    for (int i = 0; i < padding; ++i) {
                        temp += "0" + padding;
                    }
                } else {
                    while ((temp.length() / 2) % 8 != 0) {
                        temp += "00";
                    }
                }

                String encryptedCommand;

                if (encryptionMode.equals(CBC)) {
                    encryptedCommand = TripleDES.encrypt_CBC(temp, key);
                } else {
                    encryptedCommand = TripleDES.encrypt(temp, key);
                }

                Hashtable<String, Object> apduInput = new Hashtable<String, Object>();
                apduInput.put("apdu", encryptedCommand);
                if (isPKCS7) {
                    bbDeviceController.sendApdu(apduInput);
                } else {
                    apduInput.put("apduLength", command.length() / 2);
                    bbDeviceController.sendApdu(apduInput);
                }

                setStatus(getString(R.string.sending) + command);
            } else {
                Hashtable<String, Object> apduInput = new Hashtable<String, Object>();
                apduInput.put("apdu", command);
                apduInput.put("apduLength", command.length() / 2);
                bbDeviceController.sendApdu(apduInput);
                HLog.w(TAG,"sendApdu="+apduInput.toString());
            }
        } catch (Exception e) {
            setStatus(e.getMessage());
            StackTraceElement[] elements = e.getStackTrace();
            for (int i = 0; i < elements.length; ++i) {
                setStatus(elements[i].toString());
            }
        }
    }

    class MyBBDeviceControllerListener implements BBDeviceControllerListener {

        @Override
        public void onWaitingForCard(CheckCardMode checkCardMode) {
            HLog.w(TAG,"onWaitingForCard" + checkCardMode);
            dismissDialog();
            switch (checkCardMode) {
                case INSERT:
                    statusEditText.setText(getString(R.string.please_insert_card));
                    break;
                case SWIPE:
                    statusEditText.setText(getString(R.string.please_swipe_card));
                    break;
                case SWIPE_OR_INSERT:
                    statusEditText.setText(getString(R.string.please_swipe_insert_card));
                    break;
                case TAP:
                    statusEditText.setText(getString(R.string.please_tap_card));
                    break;
                case SWIPE_OR_TAP:
                    statusEditText.setText(getString(R.string.please_swipe_tap_card));
                    break;
                case INSERT_OR_TAP:
                    statusEditText.setText(getString(R.string.please_insert_tap_card));
                    break;
                case SWIPE_OR_INSERT_OR_TAP:
                    statusEditText.setText(getString(R.string.please_swipe_insert_tap_card));
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onWaitingReprintOrPrintNext() {
            HLog.w(TAG,"onWaitingReprintOrPrintNext" + "");
            statusEditText.setText(statusEditText.getText() + "\n" + getString(R.string.please_press_reprint_or_print_next));
        }

        @Override
        public void onBTConnected(BluetoothDevice bluetoothDevice) {
            HLog.w(TAG,"onBTConnected-设备已连接" + bluetoothDevice.getName());
            statusEditText.setText(getString(R.string.bluetooth_connected) + ": " + bluetoothDevice.getAddress());
        }

        @Override
        public void onBTDisconnected() {
            HLog.w(TAG,"onBTDisconnected");
            statusEditText.setText(getString(R.string.bluetooth_disconnected));
        }

        @Override
        public void onBTReturnScanResults(List<BluetoothDevice> foundDevices) {
            HLog.w(TAG,"onBTReturnScanResults"+foundDevices.toString());
            currentActivity.foundDevices = foundDevices;
            if (arrayAdapter != null) {
                arrayAdapter.clear();
                for (int i = 0; i < foundDevices.size(); ++i) {
                    arrayAdapter.add(foundDevices.get(i).getName());
                }
                arrayAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onBTScanStopped() {
            HLog.w(TAG,"onBTScanStopped");
            statusEditText.setText(getString(R.string.bluetooth_scan_stopped));
        }

        @Override
        public void onBTScanTimeout() {
            HLog.w(TAG,"onBTScanTimeout");
            statusEditText.setText(getString(R.string.bluetooth_scan_timeout));
        }

        @Override
        public void onReturnCheckCardResult(CheckCardResult checkCardResult, Hashtable<String, String> decodeData) {
            HLog.w(TAG,"onReturnCheckCardResult-"+checkCardResult.toString()+"\ndecodeData="+decodeData.toString());
            dismissDialog();
            statusEditText.setText("" + checkCardResult + "\n");
            if (checkCardResult == CheckCardResult.NO_CARD) {
            } else if (checkCardResult == CheckCardResult.INSERTED_CARD) {

            } else if (checkCardResult == CheckCardResult.NOT_ICC) {
            } else if (checkCardResult == CheckCardResult.BAD_SWIPE) {
            } else if (checkCardResult == CheckCardResult.MSR) {
                String formatID = decodeData.get("formatID");
                final String maskedPAN = decodeData.get("maskedPAN");
                String PAN = decodeData.get("PAN");
                final String expiryDate = decodeData.get("expiryDate");
                final String cardHolderName = decodeData.get("cardholderName");
                String ksn = decodeData.get("ksn");
                String serviceCode = decodeData.get("serviceCode");
                String track1Length = decodeData.get("track1Length");
                String track2Length = decodeData.get("track2Length");
                String track3Length = decodeData.get("track3Length");
                String encTracks = decodeData.get("encTracks");
                String encTrack1 = decodeData.get("encTrack1");
                String encTrack2 = decodeData.get("encTrack2");
                String encTrack3 = decodeData.get("encTrack3");
                String track1Status = decodeData.get("track1Status");
                String track2Status = decodeData.get("track2Status");
                String track3Status = decodeData.get("track3Status");
                String partialTrack = decodeData.get("partialTrack");
                String productType = decodeData.get("productType");
                String trackEncoding = decodeData.get("trackEncoding");
                String randomNumber = decodeData.get("randomNumber");
                String finalMessage = decodeData.get("finalMessage");
                String encWorkingKey = decodeData.get("encWorkingKey");
                String mac = decodeData.get("mac");
                String serialNumber = decodeData.get("serialNumber");
                String bID = decodeData.get("bID");
                String posEntryMode = decodeData.get("posEntryMode");

                String content = "" + checkCardResult + "\n";
                content += getString(R.string.format_id) + " " + formatID + "\n";
                content += getString(R.string.masked_pan) + " " + maskedPAN + "\n";
                content += getString(R.string.pan) + " " + PAN + "\n";
                content += getString(R.string.expiry_date) + " " + expiryDate + "\n";
                content += getString(R.string.cardholder_name) + " " + cardHolderName + "\n";
                content += getString(R.string.ksn) + " " + ksn + "\n";
                content += getString(R.string.service_code) + " " + serviceCode + "\n";
                content += getString(R.string.track_1_length) + " " + track1Length + "\n";
                content += getString(R.string.track_2_length) + " " + track2Length + "\n";
                content += getString(R.string.track_3_length) + " " + track3Length + "\n";
                content += getString(R.string.encrypted_tracks) + " " + encTracks + "\n";
                content += getString(R.string.encrypted_track_1) + " " + encTrack1 + "\n";
                content += getString(R.string.encrypted_track_2) + " " + encTrack2 + "\n";
                content += getString(R.string.encrypted_track_3) + " " + encTrack3 + "\n";
                content += getString(R.string.track_1_status) + " " + track1Status + "\n";
                content += getString(R.string.track_2_status) + " " + track2Status + "\n";
                content += getString(R.string.track_3_status) + " " + track3Status + "\n";
                content += getString(R.string.partial_track) + " " + partialTrack + "\n";
                content += getString(R.string.product_type) + " " + productType + "\n";
                content += getString(R.string.track_encoding) + " " + trackEncoding + "\n";
                content += getString(R.string.random_number) + " " + randomNumber + "\n";
                content += getString(R.string.final_message) + " " + finalMessage + "\n";
                content += getString(R.string.encrypted_working_key) + " " + encWorkingKey + "\n";
                content += getString(R.string.mac) + " " + mac + "\n";
                if ((decodeData != null) && (decodeData.containsKey("data"))) {
                    content += getString(R.string.data) + decodeData.get("data");
                }

                if ((serialNumber != null) && (!serialNumber.equals(""))) {
                    content += getString(R.string.serial_number) + serialNumber + "\n";
                }

                if ((bID != null) && (!bID.equals(""))) {
                    content += getString(R.string.b_id) + "  :" + bID + "\n";
                }

                if ((posEntryMode != null) && (!posEntryMode.equals(""))) {
                    content += getString(R.string.pos_entry_mode) + "  :" + posEntryMode + "\n";
                }
                isPinCanceled=false;
                promptForStartEmv();
                statusEditText.setText(content);
            } else if (checkCardResult == CheckCardResult.MAG_HEAD_FAIL) {
            } else if (checkCardResult == CheckCardResult.USE_ICC_CARD) {
                String content = "" + checkCardResult + "\n";
                if (decodeData != null) {
                    String formatID = decodeData.get("formatID");
                    final String maskedPAN = decodeData.get("maskedPAN");
                    String PAN = decodeData.get("PAN");
                    final String expiryDate = decodeData.get("expiryDate");
                    final String cardHolderName = decodeData.get("cardholderName");
                    String ksn = decodeData.get("ksn");
                    String serviceCode = decodeData.get("serviceCode");
                    String track1Length = decodeData.get("track1Length");
                    String track2Length = decodeData.get("track2Length");
                    String track3Length = decodeData.get("track3Length");
                    String encTracks = decodeData.get("encTracks");
                    String encTrack1 = decodeData.get("encTrack1");
                    String encTrack2 = decodeData.get("encTrack2");
                    String encTrack3 = decodeData.get("encTrack3");
                    String track1Status = decodeData.get("track1Status");
                    String track2Status = decodeData.get("track2Status");
                    String track3Status = decodeData.get("track3Status");
                    String partialTrack = decodeData.get("partialTrack");
                    String productType = decodeData.get("productType");
                    String trackEncoding = decodeData.get("trackEncoding");
                    String randomNumber = decodeData.get("randomNumber");
                    String encWorkingKey = decodeData.get("encWorkingKey");
                    String mac = decodeData.get("mac");

                    content += getString(R.string.format_id) + " " + formatID + "\n";
                    content += getString(R.string.masked_pan) + " " + maskedPAN + "\n";
                    content += getString(R.string.pan) + " " + PAN + "\n";
                    content += getString(R.string.expiry_date) + " " + expiryDate + "\n";
                    content += getString(R.string.cardholder_name) + " " + cardHolderName + "\n";
                    content += getString(R.string.ksn) + " " + ksn + "\n";
                    content += getString(R.string.service_code) + " " + serviceCode + "\n";
                    content += getString(R.string.track_1_length) + " " + track1Length + "\n";
                    content += getString(R.string.track_2_length) + " " + track2Length + "\n";
                    content += getString(R.string.track_3_length) + " " + track3Length + "\n";
                    content += getString(R.string.encrypted_tracks) + " " + encTracks + "\n";
                    content += getString(R.string.encrypted_track_1) + " " + encTrack1 + "\n";
                    content += getString(R.string.encrypted_track_2) + " " + encTrack2 + "\n";
                    content += getString(R.string.encrypted_track_3) + " " + encTrack3 + "\n";
                    content += getString(R.string.track_1_status) + " " + track1Status + "\n";
                    content += getString(R.string.track_2_status) + " " + track2Status + "\n";
                    content += getString(R.string.track_3_status) + " " + track3Status + "\n";
                    content += getString(R.string.partial_track) + " " + partialTrack + "\n";
                    content += getString(R.string.product_type) + " " + productType + "\n";
                    content += getString(R.string.track_encoding) + " " + trackEncoding + "\n";
                    content += getString(R.string.random_number) + " " + randomNumber + "\n";
                    content += getString(R.string.encrypted_working_key) + " " + encWorkingKey + "\n";
                    content += getString(R.string.mac) + " " + mac + "\n";
                }
                statusEditText.setText(content);
            } else if (checkCardResult == CheckCardResult.TAP_CARD_DETECTED) {
            } else if (checkCardResult == CheckCardResult.MANUAL_PAN_ENTRY) {
                String content = "" + checkCardResult + "\n";
                if (decodeData != null) {
                    Object[] keys = decodeData.keySet().toArray();
                    Arrays.sort(keys);
                    for (Object key : keys) {
                        content += "\n" + (String) key + " : ";
                        Object obj = decodeData.get(key);
                        if (obj instanceof String) {
                            content += (String) obj;
                        } else if (obj instanceof Boolean) {
                            content += ((Boolean) obj) + "";
                        }
                    }
                    statusEditText.setText(content);
                }
            }
        }

        @Override
        public void onReturnCancelCheckCardResult(boolean isSuccess) {
            HLog.w(TAG,"onReturnCancelCheckCardResult--"+isSuccess);

            if (isSuccess) {
                statusEditText.setText(R.string.cancel_check_card_success);
            } else {
                statusEditText.setText(R.string.cancel_check_card_fail);
            }
        }

        @Override
        public void onReturnDeviceInfo(Hashtable<String, String> deviceInfoData) {
            HLog.w(TAG,"onReturnDeviceInfo-"+deviceInfoData.toString());
            dismissDialog();
            uid = deviceInfoData.get("uid");
            String isSupportedTrack1 = deviceInfoData.get("isSupportedTrack1");
            String isSupportedTrack2 = deviceInfoData.get("isSupportedTrack2");
            String isSupportedTrack3 = deviceInfoData.get("isSupportedTrack3");
            String bootloaderVersion = deviceInfoData.get("bootloaderVersion");
            String firmwareVersion = deviceInfoData.get("firmwareVersion");
            String mainProcessorVersion = deviceInfoData.get("mainProcessorVersion");
            String coprocessorVersion = deviceInfoData.get("coprocessorVersion");
            String coprocessorBootloaderVersion = deviceInfoData.get("coprocessorBootloaderVersion");
            String isUsbConnected = deviceInfoData.get("isUsbConnected");
            String isCharging = deviceInfoData.get("isCharging");
            String batteryLevel = deviceInfoData.get("batteryLevel");
            String batteryPercentage = deviceInfoData.get("batteryPercentage");
            String hardwareVersion = deviceInfoData.get("hardwareVersion");
            String productId = deviceInfoData.get("productId");
            String pinKsn = deviceInfoData.get("pinKsn");
            String emvKsn = deviceInfoData.get("emvKsn");
            String trackKsn = deviceInfoData.get("trackKsn");
            String terminalSettingVersion = deviceInfoData.get("terminalSettingVersion");
            String deviceSettingVersion = deviceInfoData.get("deviceSettingVersion");
            String formatID = deviceInfoData.get("formatID");
            String vendorID = deviceInfoData.get("vendorID");
            String csn = deviceInfoData.get("csn");
            String uid = deviceInfoData.get("uid");
            String serialNumber = deviceInfoData.get("serialNumber");
            String modelName = deviceInfoData.get("modelName");
            String macKsn = deviceInfoData.get("macKsn");
            String nfcKsn = deviceInfoData.get("nfcKsn");
            String messageKsn = deviceInfoData.get("messageKsn");
            String bID = deviceInfoData.get("bID");
            String publicKeyVersion = deviceInfoData.get("publicKeyVersion");

            String vendorIDAscii = "";
            if ((vendorID != null) && (!vendorID.equals(""))) {
                if (!vendorID.substring(0, 2).equalsIgnoreCase("00")) {
                    vendorIDAscii = hexString2AsciiString(vendorID);
                }
            }

            String content = "";
            content += getString(R.string.format_id) + formatID + "\n";
            content += getString(R.string.vendor_id_hex) + vendorID + "\n";
            content += getString(R.string.vendor_id_ascii) + vendorIDAscii + "\n";
            content += getString(R.string.bootloader_version) + bootloaderVersion + "\n";
            content += getString(R.string.firmware_version) + firmwareVersion + "\n";
            content += getString(R.string.main_processor_version) + mainProcessorVersion + "\n";
            content += getString(R.string.coprocessor_version) + coprocessorVersion + "\n";
            content += getString(R.string.coprocessor_bootloader_version) + coprocessorBootloaderVersion + "\n";
            content += getString(R.string.usb) + isUsbConnected + "\n";
            content += getString(R.string.charge) + isCharging + "\n";
            content += getString(R.string.battery_level) + batteryLevel + "\n";
            content += getString(R.string.battery_percentage) + batteryPercentage + "\n";
            content += getString(R.string.hardware_version) + hardwareVersion + "\n";
            content += getString(R.string.track_1_supported) + isSupportedTrack1 + "\n";
            content += getString(R.string.track_2_supported) + isSupportedTrack2 + "\n";
            content += getString(R.string.track_3_supported) + isSupportedTrack3 + "\n";
            content += getString(R.string.product_id) + productId + "\n";
            content += getString(R.string.pin_ksn) + pinKsn + "\n";
            content += getString(R.string.emv_ksn) + emvKsn + "\n";
            content += getString(R.string.track_ksn) + trackKsn + "\n";
            content += getString(R.string.terminal_setting_version) + terminalSettingVersion + "\n";
            content += getString(R.string.device_setting_version) + deviceSettingVersion + "\n";
            content += getString(R.string.csn) + csn + "\n";
            content += getString(R.string.uid) + uid + "\n";
            content += getString(R.string.serial_number) + serialNumber + "\n";
            if ((modelName != null) && (!modelName.equals(""))) {
                content += getString(R.string.model_name) + modelName + "\n";
            }

            if ((macKsn != null) && (!macKsn.equals(""))) {
                content += getString(R.string.mac_ksn) + macKsn + "\n";
            }
            if ((nfcKsn != null) && (!nfcKsn.equals(""))) {
                content += getString(R.string.nfc_ksn) + nfcKsn + "\n";
            }
            if ((messageKsn != null) && (!messageKsn.equals(""))) {
                content += getString(R.string.message_ksn) + messageKsn + "\n";
            }
            if ((bID != null) && (!bID.equals(""))) {
                content += getString(R.string.b_id) + "  :" + bID + "\n";
            }
            if ((publicKeyVersion != null) && (!publicKeyVersion.equals(""))) {
                content += getString(R.string.public_key_version) + "  :" + publicKeyVersion + "\n";
            }

            statusEditText.setText(content);

            if (formatID.equals("46")) {
                fidSpinner.setSelection(2);
            } else if (formatID.equals("61")) {
                fidSpinner.setSelection(6);
            } else if (formatID.equals("65")) {
                fidSpinner.setSelection(8);
            } else {
                fidSpinner.setSelection(5);
            }
        }

        @Override
        public void onReturnTransactionResult(TransactionResult transactionResult) {
            HLog.w(TAG,"onReturnTransactionResult="+transactionResult);
            dismissDialog();
            dialog = new Dialog(currentActivity);
            dialog.setContentView(R.layout.alert_dialog);
            dialog.setTitle(R.string.transaction_result);
            TextView messageTextView = (TextView) dialog.findViewById(R.id.messageTextView);

            String message = "" + transactionResult + "\n";
            if (transactionResult == TransactionResult.APPROVED) {
                message = getString(R.string.amount) + ": $" + amount + "\n";
                if (!cashbackAmount.equals("")) {
                    message += getString(R.string.cashback_amount) + ": $" + cashbackAmount;
                }
            }

            messageTextView.setText(message);

            amount = "";
            cashbackAmount = "";
            amountEditText.setText("");

            dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismissDialog();
                }
            });

            dialog.show();
        }

        @Override
        public void onReturnBatchData(String tlv) {
            HLog.w(TAG,"onReturnBatchData-"+tlv);
            dismissDialog();
            String content = getString(R.string.batch_data) + "\n";
            Hashtable<String, String> decodeData = BBDeviceController.decodeTlv(tlv);
            Object[] keys = decodeData.keySet().toArray();
            Arrays.sort(keys);
            for (Object key : keys) {
                String value = decodeData.get(key);
                content += key + ": " + value + "\n";
            }
            statusEditText.setText(content);
        }

        @Override
        public void onReturnReversalData(String tlv) {
            HLog.w(TAG,"onReturnReversalData-"+tlv);
            dismissDialog();
            String content = getString(R.string.reversal_data);
            content += tlv;
            statusEditText.setText(content);
        }

        @Override
        public void onReturnAmountConfirmResult(boolean isSuccess) {
            HLog.w(TAG,"onReturnAmountConfirmResult-"+isSuccess);

            if (isSuccess) {
                statusEditText.setText(getString(R.string.amount_confirmed));
            } else {
                statusEditText.setText(getString(R.string.amount_canceled));
            }
        }

        @Override
        public void onReturnPinEntryResult(PinEntryResult pinEntryResult, Hashtable<String, String> data) {
            HLog.w(TAG,"onReturnPinEntryResult-"+pinEntryResult+"\ndata="+data);
            if (pinEntryResult == PinEntryResult.ENTERED) {
                String content = getString(R.string.pin_entered);
                if (data.containsKey("epb")) {
                    content += "\n" + getString(R.string.epb) + data.get("epb");
                }
                if (data.containsKey("ksn")) {
                    content += "\n" + getString(R.string.ksn) + data.get("ksn");
                }
                if (data.containsKey("randomNumber")) {
                    content += "\n" + getString(R.string.random_number) + data.get("randomNumber");
                }
                if (data.containsKey("encWorkingKey")) {
                    content += "\n" + getString(R.string.encrypted_working_key) + data.get("encWorkingKey");
                }

                statusEditText.setText(content);
            } else if (pinEntryResult == PinEntryResult.BYPASS) {
                statusEditText.setText(getString(R.string.pin_bypassed));
            } else if (pinEntryResult == PinEntryResult.CANCEL) {
                statusEditText.setText(getString(R.string.pin_canceled));
            } else if (pinEntryResult == PinEntryResult.TIMEOUT) {
                statusEditText.setText(getString(R.string.pin_timeout));
            }
        }

        @Override
        public void onReturnPrintResult(PrintResult printResult) {
            HLog.w(TAG,"onReturnPrintResult-"+printResult);
            statusEditText.setText("" + printResult);
        }

        @Override
        public void onReturnAmount(Hashtable<String, String> data) {
            HLog.w(TAG,"onReturnAmount-"+data.toString());
            String amount = data.get("amount");
            String cashbackAmount = data.get("cashbackAmount");
            String currencyCode = data.get("currencyCode");

            String text = "";
            text += getString(R.string.amount_with_colon) + amount + "\n";
            text += getString(R.string.cashback_with_colon) + cashbackAmount + "\n";
            text += getString(R.string.currency_with_colon) + currencyCode + "\n";

            statusEditText.setText(text);
        }

        @Override
        public void onReturnUpdateTerminalSettingResult(TerminalSettingStatus terminalSettingStatus) {
            HLog.w(TAG,"onReturnUpdateTerminalSettingResult-"+terminalSettingStatus);
            dismissDialog();
            if (terminalSettingStatus == TerminalSettingStatus.SUCCESS) {
                statusEditText.setText(getString(R.string.update_terminal_setting_success));
            } else if (terminalSettingStatus == TerminalSettingStatus.TAG_NOT_FOUND) {
                statusEditText.setText(getString(R.string.update_terminal_setting_tag_not_found));
            } else if (terminalSettingStatus == TerminalSettingStatus.LENGTH_INCORRECT) {
                statusEditText.setText(getString(R.string.update_terminal_setting_length_incorrect));
            } else if (terminalSettingStatus == TerminalSettingStatus.TLV_INCORRECT) {
                statusEditText.setText(getString(R.string.update_terminal_setting_tlv_incorrect));
            } else if (terminalSettingStatus == TerminalSettingStatus.BOOTLOADER_NOT_SUPPORT) {
                statusEditText.setText(getString(R.string.update_terminal_setting_bootloader_not_support));
            } else if (terminalSettingStatus == TerminalSettingStatus.TAG_NOT_ALLOWED_TO_ACCESS) {
                statusEditText.setText(getString(R.string.update_terminal_setting_tag_not_allowed_to_change));
            } else if (terminalSettingStatus == TerminalSettingStatus.USER_DEFINED_DATA_NOT_ENALBLED) {
                statusEditText.setText(getString(R.string.update_terminal_setting_user_defined_data_not_allowed_to_change));
            } else if (terminalSettingStatus == TerminalSettingStatus.TAG_NOT_WRITTEN_CORRECTLY) {
                statusEditText.setText(getString(R.string.update_terminal_setting_tag_not_written_correctly));
            }
        }

        @Override
        public void onReturnReadTerminalSettingResult(TerminalSettingStatus terminalSettingStatus, String value) {
            HLog.w(TAG,"onReturnReadTerminalSettingResult-"+terminalSettingStatus+"\n"+value);
            dismissDialog();
            if (terminalSettingStatus == TerminalSettingStatus.SUCCESS) {
                statusEditText.setText(getString(R.string.read_terminal_setting_success) + "\n" + getString(R.string.value) + " " + value);
            } else if (terminalSettingStatus == TerminalSettingStatus.TAG_NOT_FOUND) {
                statusEditText.setText(getString(R.string.read_terminal_setting_tag_not_found));
            } else if (terminalSettingStatus == TerminalSettingStatus.LENGTH_INCORRECT) {
                statusEditText.setText(getString(R.string.read_terminal_setting_length_incorrect));
            } else if (terminalSettingStatus == TerminalSettingStatus.TLV_INCORRECT) {
                statusEditText.setText(getString(R.string.read_terminal_setting_tlv_incorrect));
            } else if (terminalSettingStatus == TerminalSettingStatus.BOOTLOADER_NOT_SUPPORT) {
                statusEditText.setText(getString(R.string.read_terminal_setting_bootloader_not_support));
            } else if (terminalSettingStatus == TerminalSettingStatus.TAG_NOT_ALLOWED_TO_ACCESS) {
                statusEditText.setText(getString(R.string.read_terminal_setting_tag_not_allowed_to_access));
            } else if (terminalSettingStatus == TerminalSettingStatus.USER_DEFINED_DATA_NOT_ENALBLED) {
                statusEditText.setText(getString(R.string.read_terminal_setting_user_defined_data_not_allowed_to_change));
            } else if (terminalSettingStatus == TerminalSettingStatus.TAG_NOT_WRITTEN_CORRECTLY) {
                statusEditText.setText(getString(R.string.read_terminal_setting_tag_not_written_correctly));
            }
        }

        @Override
        public void onReturnEnableInputAmountResult(boolean isSuccess) {
            HLog.w(TAG,"onReturnEnableInputAmountResult-"+isSuccess);
            if (isSuccess) {
                statusEditText.setText(getString(R.string.enable_input_amount_success));
            } else {
                statusEditText.setText(getString(R.string.enable_input_amount_fail));
            }
        }

        @Override
        public void onReturnDisableInputAmountResult(boolean isSuccess) {
            HLog.w(TAG,"onReturnDisableInputAmountResult-"+isSuccess);
            if (isSuccess) {
                statusEditText.setText(getString(R.string.disable_input_amount_success));
            } else {
                statusEditText.setText(getString(R.string.disable_input_amount_fail));
            }
        }

        @Override
        public void onReturnPhoneNumber(PhoneEntryResult phoneEntryResult, String phoneNumber) {
            HLog.w(TAG,"onReturnPhoneNumber-"+phoneEntryResult+"\nphoneNumber="+phoneNumber);
            if (phoneEntryResult == PhoneEntryResult.ENTERED) {
                statusEditText.setText(getString(R.string.phone_number) + " " + phoneNumber);
            } else if (phoneEntryResult == PhoneEntryResult.TIMEOUT) {
                statusEditText.setText(getString(R.string.timeout));
            } else if (phoneEntryResult == PhoneEntryResult.CANCEL) {
                statusEditText.setText(getString(R.string.canceled));
            } else if (phoneEntryResult == PhoneEntryResult.WRONG_LENGTH) {
                statusEditText.setText(getString(R.string.wrong_length));
            } else if (phoneEntryResult == PhoneEntryResult.BYPASS) {
                statusEditText.setText(getString(R.string.bypass));
            }
        }

        @Override
        public void onReturnEmvCardDataResult(boolean isSuccess, String tlv) {
            HLog.w(TAG,"onReturnEmvCardDataResult-"+isSuccess+"\n"+tlv);
            if (isSuccess) {
                statusEditText.setText(getString(R.string.emv_card_data_result) + tlv);
            } else {
                statusEditText.setText(getString(R.string.emv_card_data_failed));
            }
        }

        @Override
        public void onReturnEmvCardNumber(boolean isSuccess, String cardNumber) {
            HLog.w(TAG,"onReturnEmvCardNumber-"+isSuccess+"\n"+cardNumber);
            statusEditText.setText(getString(R.string.pan) + cardNumber);
        }

        @Override
        public void onReturnEncryptPinResult(boolean isSuccess, Hashtable<String, String> data) {
            HLog.w(TAG,"onReturnEncryptPinResult-"+isSuccess+"\n"+data.toString());
            String ksn = data.get("ksn");
            String epb = data.get("epb");
            String randomNumber = data.get("randomNumber");
            String encWorkingKey = data.get("encWorkingKey");
            String errorMessage = data.get("errorMessage");
            String content = getString(R.string.ksn) + ksn + "\n";
            content += getString(R.string.epb) + epb + "\n";
            content += getString(R.string.random_number) + randomNumber + "\n";
            content += getString(R.string.encrypted_working_key) + encWorkingKey + "\n";
            content += getString(R.string.error_message) + errorMessage;
            statusEditText.setText(content);
        }

        @Override
        public void onReturnEncryptDataResult(boolean isSuccess, Hashtable<String, String> data) {
            HLog.w(TAG,"onReturnEncryptDataResult-"+isSuccess+"\n"+data.toString());
            if (isSuccess) {
                String content = "";
                if (data.containsKey("ksn")) {
                    content += getString(R.string.ksn) + data.get("ksn") + "\n";
                }
                if (data.containsKey("randomNumber")) {
                    content += getString(R.string.random_number) + data.get("randomNumber") + "\n";
                }
                if (data.containsKey("encData")) {
                    content += getString(R.string.encrypted_data) + data.get("encData") + "\n";
                }
                if (data.containsKey("mac")) {
                    content += getString(R.string.mac) + data.get("mac") + "\n";
                }
                statusEditText.setText(content);
            } else {
                statusEditText.setText(getString(R.string.encrypt_data_failed));
            }
        }

        @Override
        public void onReturnInjectSessionKeyResult(boolean isSuccess, Hashtable<String, String> data) {
            HLog.w(TAG,"onReturnInjectSessionKeyResult-"+isSuccess+"\n"+data.toString());

            String content;
            if (isSuccess) {
                content = getString(R.string.inject_session_key_success);
                if (data.size() == 0) {
                    injectNextSessionKey();
                }
            } else {
                content = getString(R.string.inject_session_key_failed);
                content += "\n" + getString(R.string.error_message) + data.get("errorMessage");
            }
            setStatus(content);
        }

        @Override
        public void onReturnApduResult(boolean isSuccess, Hashtable<String, Object> data) {
            HLog.w(TAG,"onReturnApduResult-"+isSuccess+"\n"+data.toString());
            try {
                String apdu = "";
                int apduLength = 0;

                if ((data != null) && (data.containsKey("apduLength")) && (data.get("apduLength") instanceof String)) {
                    apduLength = Integer.parseInt((String) data.get("apduLength"));
                } else if ((data != null) && (data.containsKey("apduLength")) && (data.get("apduLength") instanceof Integer)) {
                    apduLength = (Integer) data.get("apduLength");
                }

                if ((data != null) && (data.containsKey("apdu"))) {
                    apdu = (String) data.get("apdu");
                    handleApduResult(isSuccess, apdu, apduLength);
                }
            } catch (Exception e) {

            }
        }

        @Override
        public void onReturnPowerOffIccResult(boolean isSuccess) {
            HLog.w(TAG,"onReturnPowerOffIccResult-"+isSuccess);
            dismissDialog();
            if (isSuccess) {
                setStatus(getString(R.string.power_off_icc_success));
            } else {
                setStatus(getString(R.string.power_off_icc_failed));
            }
        }

        @Override
        public void onReturnPowerOnIccResult(boolean isSuccess, String ksn, String atr, int atrLength) {
            HLog.w(TAG,"onReturnPowerOnIccResult-"+isSuccess+"\nksn="+ksn
            +"\natr="+atr+"\natrLength="+atrLength);
            dismissDialog();
            if (isSuccess) {
                BaseActivity.ksn = ksn;

                setStatus(getString(R.string.power_on_icc_success));
                setStatus(getString(R.string.ksn) + ksn);
                setStatus(getString(R.string.atr) + atr);
                setStatus(getString(R.string.atr_length) + atrLength);
            } else {
                setStatus(getString(R.string.power_on_icc_failed));
            }
        }

        @Override
        public void onRequestSelectApplication(ArrayList<String> appList) {
            HLog.w(TAG,"onRequestSelectApplication-"+appList.toString());
            dismissDialog();

            dialog = new Dialog(currentActivity);
            dialog.setContentView(R.layout.application_dialog);
            dialog.setTitle(R.string.please_select_app);
            dialog.setCanceledOnTouchOutside(false);

            String[] appNameList = new String[appList.size()];
            for (int i = 0; i < appNameList.length; ++i) {
                appNameList[i] = appList.get(i);
            }

            appListView = (ListView) dialog.findViewById(R.id.appList);
            appListView.setAdapter(new ArrayAdapter<String>(currentActivity, android.R.layout.simple_list_item_1, appNameList));
            appListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    bbDeviceController.selectApplication(position);
                    dismissDialog();
                }

            });

            dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    bbDeviceController.cancelSelectApplication();
                    dismissDialog();
                }
            });
            dialog.show();
        }

        @Override
        public void onRequestSetAmount() {
            HLog.w(TAG,"onRequestSetAmount-");
            promptForAmount();

        }

        @Override
        public void onRequestPinEntry(PinEntrySource pinEntrySource) {
            HLog.w(TAG,"onRequestPinEntry-"+pinEntrySource);
            dismissDialog();
            if (pinEntrySource == PinEntrySource.KEYPAD) {
                statusEditText.setText(getString(R.string.enter_pin_on_keypad));
            } else {
                dismissDialog();

                dialog = new Dialog(currentActivity);
                dialog.setContentView(R.layout.pin_dialog);
                dialog.setTitle(getString(R.string.enter_pin));
                dialog.setCanceledOnTouchOutside(false);

                dialog.findViewById(R.id.confirmButton).setOnClickListener(
                        new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                String pin = ((EditText) dialog.findViewById(R.id.pinEditText)).getText().toString();
                                bbDeviceController.sendPinEntryResult(pin);
                                dismissDialog();
                            }
                        });

                dialog.findViewById(R.id.bypassButton).setOnClickListener(
                        new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                bbDeviceController.bypassPinEntry();
                                dismissDialog();
                            }
                        });

                dialog.findViewById(R.id.cancelButton).setOnClickListener(
                        new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                isPinCanceled = true;
                                bbDeviceController.cancelPinEntry();
                                dismissDialog();
                            }
                        });

                dialog.show();
            }
        }

        @Override
        public void onRequestOnlineProcess(String tlv) {
            HLog.w(TAG,"onRequestOnlineProcess-"+tlv);
            String content = getString(R.string.request_data_to_server) + "\n";
            Hashtable<String, String> decodeData = BBDeviceController.decodeTlv(tlv);
            Object[] keys = decodeData.keySet().toArray();
            Arrays.sort(keys);
            for (Object key : keys) {
                String value = decodeData.get(key);
                content += key + ": " + value + "\n";
            }
            statusEditText.setText(content);

            dismissDialog();
            dialog = new Dialog(currentActivity);
            dialog.setContentView(R.layout.alert_dialog);
            dialog.setTitle(R.string.request_data_to_server);
            dialog.setCanceledOnTouchOutside(false);

            if (isPinCanceled) {
                ((TextView) dialog.findViewById(R.id.messageTextView)).setText(R.string.replied_failed);
            } else {
                ((TextView) dialog.findViewById(R.id.messageTextView)).setText(R.string.replied_success);
            }

            dialog.findViewById(R.id.confirmButton).setOnClickListener(
                    new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (isPinCanceled) {
                                bbDeviceController.sendOnlineProcessResult(null);
                            } else {
                                bbDeviceController.sendOnlineProcessResult("8A023030");
                            }
                            dismissDialog();
                        }
                    });

            dialog.show();
        }

        @Override
        public void onRequestTerminalTime() {
            HLog.w(TAG,"onRequestTerminalTime-");
            dismissDialog();
            String terminalTime = new SimpleDateFormat("yyMMddHHmmss").format(Calendar.getInstance().getTime());
            bbDeviceController.sendTerminalTime(terminalTime);
            statusEditText.setText(getString(R.string.request_terminal_time) + " " + terminalTime);
        }

        @Override
        public void onRequestDisplayText(DisplayText displayText) {
            HLog.w(TAG,"onRequestDisplayText-"+displayText);
            dismissDialog();
            statusEditText.setText("" + displayText);
        }

        @Override
        public void onRequestClearDisplay() {
            HLog.w(TAG,"onRequestDisplayText-");
            dismissDialog();
            statusEditText.setText("");
        }

        @Override
        public void onRequestFinalConfirm() {

            HLog.w(TAG,"onRequestFinalConfirm");
            dismissDialog();
            if (!isPinCanceled) {
                dialog = new Dialog(currentActivity);
                dialog.setContentView(R.layout.confirm_dialog);
                dialog.setTitle(getString(R.string.confirm_amount));
                dialog.setCanceledOnTouchOutside(false);

                String message = getString(R.string.amount) + ": $" + amount;
                if (!cashbackAmount.equals("")) {
                    message += "\n" + getString(R.string.cashback_amount) + ": $" + cashbackAmount;
                }

                ((TextView) dialog.findViewById(R.id.messageTextView)).setText(message);

                dialog.findViewById(R.id.confirmButton).setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                bbDeviceController.sendFinalConfirmResult(true);
                                dialog.dismiss();
                            }
                        });

                dialog.findViewById(R.id.cancelButton).setOnClickListener(
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                bbDeviceController.sendFinalConfirmResult(false);
                                dialog.dismiss();
                            }
                        });

                dialog.show();
            } else {
                bbDeviceController.sendFinalConfirmResult(false);
            }
        }

        @Override
        public void onRequestPrintData(int index, boolean isReprint) {
            HLog.w(TAG,"onRequestPrintData-index="+index+"\nisReprint="+isReprint);
            bbDeviceController.sendPrintData(receipts.get(index));
            if (isReprint) {
                statusEditText.setText(getString(R.string.request_reprint_data) + index);
            } else {
                statusEditText.setText(getString(R.string.request_printer_data) + index);
            }
        }

        @Override
        public void onPrintDataCancelled() {
            HLog.w(TAG,"onPrintDataCancelled-");
            statusEditText.setText(getString(R.string.printer_operation_cancelled));
        }

        @Override
        public void onPrintDataEnd() {
            HLog.w(TAG,"onPrintDataEnd-");
            statusEditText.setText(getString(R.string.printer_operation_end));
        }

        @Override
        public void onBatteryLow(BatteryStatus batteryStatus) {
            HLog.w(TAG,"onBatteryLow-"+batteryStatus);
            if (batteryStatus == BatteryStatus.LOW) {
                statusEditText.setText(getString(R.string.battery_low));
            } else if (batteryStatus == BatteryStatus.CRITICALLY_LOW) {
                statusEditText.setText(getString(R.string.battery_critically_low));
            }
        }

        @Override
        public void onAudioDevicePlugged() {
            HLog.w(TAG,"onAudioDevicePlugged-");

            statusEditText.setText(getString(R.string.device_plugged));
        }

        @Override
        public void onAudioDeviceUnplugged() {

            statusEditText.setText(getString(R.string.device_unplugged));
        }

        @Override
        public void onError(Error errorState, String errorMessage) {
            HLog.w(TAG,"onError-errorState="+errorState+"\nerrorMessage="+errorMessage);
            dismissDialog();
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            amountEditText.setText("");

            String content = "";
            if (errorState == Error.CMD_NOT_AVAILABLE) {
                content = getString(R.string.command_not_available);
            } else if (errorState == Error.TIMEOUT) {
                content = getString(R.string.device_no_response);
            } else if (errorState == Error.UNKNOWN) {
                content = getString(R.string.unknown_error);
            } else if (errorState == Error.DEVICE_BUSY) {
                content = getString(R.string.device_busy);
            } else if (errorState == Error.INPUT_OUT_OF_RANGE) {
                content = getString(R.string.out_of_range);
            } else if (errorState == Error.INPUT_INVALID_FORMAT) {
                content = getString(R.string.invalid_format);
                Toast.makeText(currentActivity, getString(R.string.invalid_format), Toast.LENGTH_LONG).show();
            } else if (errorState == Error.INPUT_INVALID) {
                content = getString(R.string.input_invalid);
                Toast.makeText(currentActivity, getString(R.string.input_invalid), Toast.LENGTH_LONG).show();
            } else if (errorState == Error.CASHBACK_NOT_SUPPORTED) {
                content = getString(R.string.cashback_not_supported);
                Toast.makeText(currentActivity, getString(R.string.cashback_not_supported), Toast.LENGTH_LONG).show();
            } else if (errorState == Error.CRC_ERROR) {
                content = getString(R.string.crc_error);
            } else if (errorState == Error.COMM_ERROR) {
                content = getString(R.string.comm_error);
            } else if (errorState == Error.FAIL_TO_START_BT) {
                content = getString(R.string.fail_to_start_bluetooth);
            } else if (errorState == Error.FAIL_TO_START_AUDIO) {
                content = getString(R.string.fail_to_start_audio);
            } else if (errorState == Error.INVALID_FUNCTION_IN_CURRENT_CONNECTION_MODE) {
                content = getString(R.string.invalid_function);
            } else if (errorState == Error.COMM_LINK_UNINITIALIZED) {
                content = getString(R.string.comm_link_uninitialized);
            }
            else if (errorState == Error.BTV4_NOT_SUPPORTED) {
                content = getString(R.string.bluetooth_4_not_supported);
                Toast.makeText(currentActivity, getString(R.string.bluetooth_4_not_supported), Toast.LENGTH_LONG).show();
            }
            else if (errorState == Error.CHANNEL_BUFFER_FULL) {
                content = getString(R.string.channel_buffer_full);
            } else if (errorState == Error.BLUETOOTH_PERMISSION_DENIED) {
                content = getString(R.string.bluetooth_permission_denied);
            } else if (errorState == Error.VOLUME_WARNING_NOT_ACCEPTED) {
                content = getString(R.string.volume_warning_not_accepted);
            } else if (errorState == Error.FAIL_TO_START_SERIAL) {
                content = getString(R.string.fail_to_start_serial);
            } else if (errorState == Error.USB_DEVICE_NOT_FOUND) {
                content = getString(R.string.usb_device_not_found);
            } else if (errorState == Error.USB_DEVICE_PERMISSION_DENIED) {
                content = getString(R.string.usb_device_permission_denied);
            } else if (errorState == Error.USB_NOT_SUPPORTED) {
                content = getString(R.string.usb_not_supported);
            }

            if (errorMessage != null && !errorMessage.equals("")) {
                content += "\n" + getString(R.string.error_message) + errorMessage;
            }

            statusEditText.setText(content);
        }

        @Override
        public void onReturnCAPKList(List<CAPK> capkList) {
            HLog.w(TAG,"onReturnCAPKList-"+capkList.toString());
            String content = getString(R.string.capk);
            for (int i = 0; i < capkList.size(); ++i) {
                CAPK capk = capkList.get(i);
                content += "\n" + i + ": ";
                content += "\n" + getString(R.string.location) + capk.location;
                content += "\n" + getString(R.string.rid) + capk.rid;
                content += "\n" + getString(R.string.index) + capk.index;
                content += "\n";
            }
            setStatus(content);
        }

        @Override
        public void onReturnCAPKDetail(CAPK capk) {
            HLog.w(TAG,"onReturnCAPKDetail-"+capk.toString());
            String content = getString(R.string.capk);
            if (capk != null) {
                content += "\n" + getString(R.string.location) + capk.location;
                content += "\n" + getString(R.string.rid) + capk.rid;
                content += "\n" + getString(R.string.index) + capk.index;
                content += "\n" + getString(R.string.exponent) + capk.exponent;
                content += "\n" + getString(R.string.modulus) + capk.modulus;
                content += "\n" + getString(R.string.checksum) + capk.checksum;
                content += "\n" + getString(R.string.size) + capk.size;
                content += "\n";
            } else {
                content += "\nnull \n";
            }
            setStatus(content);
        }

        @Override
        public void onReturnCAPKLocation(String location) {
            HLog.w(TAG,"onReturnCAPKLocation-"+location);
            setStatus(getString(R.string.location) + location);
        }

        @Override
        public void onReturnUpdateCAPKResult(boolean isSuccess) {
            HLog.w(TAG,"onReturnUpdateCAPKResult-"+isSuccess);
            if (isSuccess) {
                setStatus(getString(R.string.update_capk_success));
            } else {
                setStatus(getString(R.string.update_capk_fail));
            }
        }

        @Override
        public void onReturnEmvReport(String tlv) {
            HLog.w(TAG,"onReturnEmvReport-"+tlv);
            String content = getString(R.string.emv_report) + "\n";

            Hashtable<String, String> decodeData = BBDeviceController.decodeTlv(tlv);
            Object[] keys = decodeData.keySet().toArray();
            Arrays.sort(keys);
            for (Object key : keys) {
                if (((String) key).matches(".*[a-z].*") && decodeData.containsKey(((String) key).toUpperCase(Locale.ENGLISH))) {
                    continue;
                }
                String value = decodeData.get(key);
                content += key + ": " + value + "\n";

                if (((String) key).toUpperCase(Locale.ENGLISH).equalsIgnoreCase(TagList.EMV_REPORT_TEMPLATE)) {
                    Hashtable<String, String> innerDecodeData = BBDeviceController.decodeTlv(value);
                    Object[] innerKeys = innerDecodeData.keySet().toArray();
                    Arrays.sort(innerKeys);
                    for (Object innerKey : innerKeys) {
                        if (((String) innerKey).matches(".*[a-z].*") && innerDecodeData.containsKey(((String) innerKey).toUpperCase(Locale.ENGLISH))) {
                            continue;
                        }
                        String innerValue = innerDecodeData.get(innerKey);
                        content += "\n" + innerKey + ": " + innerValue;
                    }
                }
            }

            setStatus(content);
        }

        @Override
        public void onReturnEmvReportList(Hashtable<String, String> data) {
            HLog.w(TAG,"onReturnEmvReportList-"+data.toString());
            String content = getString(R.string.emv_report_list) + "\n";
            Object[] keys = data.keySet().toArray();
            Arrays.sort(keys);
            for (Object key : keys) {
                String value = data.get(key);
                content += key + ": " + value + "\n";
            }

            setStatus(content);
        }

        @Override
        public void onSessionInitialized() {
            HLog.w(TAG,"onSessionInitialized-");
            setStatus(getString(R.string.session_initialized));
        }

        @Override
        public void onSessionError(SessionError sessionError, String errorMessage) {
            HLog.w(TAG,"onSessionError-sessionError="+sessionError
            +"\nerrorMessage="+errorMessage);
            if (sessionError == SessionError.FIRMWARE_NOT_SUPPORTED) {
                setStatus(getString(R.string.session_error_firmware_not_supported));
            } else if (sessionError == SessionError.INVALID_SESSION) {
                setStatus(getString(R.string.session_error_invalid_session));
            } else if (sessionError == SessionError.INVALID_VENDOR_TOKEN) {
                setStatus(getString(R.string.session_error_invalid_vendor_token));
            } else if (sessionError == SessionError.SESSION_NOT_INITIALIZED) {
                setStatus(getString(R.string.session_error_session_not_initialized));
            }
            setStatus(getString(R.string.error_message) + errorMessage);
        }

        @Override
        public void onReturnReadGprsSettingsResult(boolean isSuccess, Hashtable<String, Object> data) {
            HLog.w(TAG,"onReturnReadGprsSettingsResult-isSuccess="+isSuccess
                    +"\ndata="+data.toString());

            if (isSuccess) {
                String text = getString(R.string.read_gprs_setting_success);
                text += "\n" + getString(R.string.operator) + (String) data.get("operator");
                text += "\n" + getString(R.string.apn) + (String) data.get("apn");
                text += "\n" + getString(R.string.username) + (String) data.get("username");
                text += "\n" + getString(R.string.password) + (String) data.get("password");
                setStatus(text);
            } else {
                String text = getString(R.string.read_gprs_setting_fail);
                TerminalSettingStatus terminalSettingStatus = (TerminalSettingStatus) data.get("gprs");
                switch (terminalSettingStatus) {
                    case SUCCESS:
                        setStatus(getString(R.string.read_terminal_setting_success));
                        break;
                    case LENGTH_INCORRECT:
                        setStatus(getString(R.string.length_incorrect));
                        break;
                    case TLV_INCORRECT:
                        setStatus(getString(R.string.tlv_incorrect));
                        break;
                    case TAG_NOT_FOUND:
                        setStatus(getString(R.string.tag_not_found));
                        break;
                    case BOOTLOADER_NOT_SUPPORT:
                        setStatus(getString(R.string.bootloader_not_support));
                        break;
                    case TAG_NOT_ALLOWED_TO_ACCESS:
                        setStatus(getString(R.string.tag_not_allowed_to_access));
                        break;
                    case USER_DEFINED_DATA_NOT_ENALBLED:
                        setStatus(getString(R.string.user_defined_data_not_allowed_to_change));
                        break;
                    case TAG_NOT_WRITTEN_CORRECTLY:
                        setStatus(getString(R.string.tag_not_written_correctly));
                        break;
                    default:
                        break;
                }
                setStatus(text);
            }
        }

        @Override
        public void onReturnReadWiFiSettingsResult(boolean isSuccess, Hashtable<String, Object> data) {
            HLog.w(TAG,"onReturnReadWiFiSettingsResult-isSuccess="+isSuccess
                    +"\ndata="+data.toString());
            if (isSuccess) {
                String text = getString(R.string.read_wifi_setting_success);
                text += "\n" + getString(R.string.ssid) + data.get("ssid");
                text += "\n" + getString(R.string.password) + data.get("password");
                text += "\n" + getString(R.string.url) + data.get("url");
                text += "\n" + getString(R.string.portNumber) + data.get("portNumber");
                setStatus(text);
            } else {
                String text = getString(R.string.read_wifi_setting_fail);
                Object[] keys = data.keySet().toArray();
                Arrays.sort(keys);
                for (Object key : keys) {
                    text += "\n" + (String) key + " : ";
                    TerminalSettingStatus terminalSettingStatus = (TerminalSettingStatus) data.get(key);
                    switch (terminalSettingStatus) {
                        case SUCCESS:
                            text += getString(R.string.read_terminal_setting_success);
                            break;
                        case LENGTH_INCORRECT:
                            text += getString(R.string.length_incorrect);
                            break;
                        case TLV_INCORRECT:
                            text += getString(R.string.tlv_incorrect);
                            break;
                        case TAG_NOT_FOUND:
                            text += getString(R.string.tag_not_found);
                            break;
                        case BOOTLOADER_NOT_SUPPORT:
                            text += getString(R.string.bootloader_not_support);
                            break;
                        case TAG_NOT_ALLOWED_TO_ACCESS:
                            text += getString(R.string.tag_not_allowed_to_access);
                            break;
                        case USER_DEFINED_DATA_NOT_ENALBLED:
                            text += getString(R.string.user_defined_data_not_allowed_to_change);
                            break;
                        case TAG_NOT_WRITTEN_CORRECTLY:
                            text += getString(R.string.tag_not_written_correctly);
                            break;
                        default:
                            break;
                    }
                }
                setStatus(text);
            }
        }

        @Override
        public void onReturnUpdateGprsSettingsResult(boolean isSuccess, Hashtable<String, TerminalSettingStatus> data) {
            HLog.w(TAG,"onReturnUpdateGprsSettingsResult-isSuccess="+isSuccess
                    +"\ndata="+data.toString());
            if (isSuccess) {
                String text = getString(R.string.update_gprs_setting_success);
                setStatus(text);
            } else {
                String text = getString(R.string.update_gprs_setting_fail);
                text += "\n" + getString(R.string.terminal_setting_status) + data.get("gprs");
                setStatus(text);
            }
        }

        @Override
        public void onReturnUpdateWiFiSettingsResult(boolean isSuccess, Hashtable<String, TerminalSettingStatus> data) {
            HLog.w(TAG,"onReturnUpdateWiFiSettingsResult-isSuccess="+isSuccess
                    +"\ndata="+data.toString());
            if (isSuccess) {
                String text = getString(R.string.update_wifi_setting_success);
                setStatus(text);
            } else {
                String text = getString(R.string.update_wifi_setting_fail);
                Object[] keys = data.keySet().toArray();
                Arrays.sort(keys);
                for (Object key : keys) {
                    text += "\n" + (String) key + " : ";
                    TerminalSettingStatus terminalSettingStatus = (TerminalSettingStatus) data.get(key);
                    switch (terminalSettingStatus) {
                        case SUCCESS:
                            text += getString(R.string.read_terminal_setting_success);
                            break;
                        case LENGTH_INCORRECT:
                            text += getString(R.string.length_incorrect);
                            break;
                        case TLV_INCORRECT:
                            text += getString(R.string.tlv_incorrect);
                            break;
                        case TAG_NOT_FOUND:
                            text += getString(R.string.tag_not_found);
                            break;
                        case BOOTLOADER_NOT_SUPPORT:
                            text += getString(R.string.bootloader_not_support);
                            break;
                        case TAG_NOT_ALLOWED_TO_ACCESS:
                            text += getString(R.string.tag_not_allowed_to_access);
                            break;
                        case USER_DEFINED_DATA_NOT_ENALBLED:
                            text += getString(R.string.user_defined_data_not_allowed_to_change);
                            break;
                        case TAG_NOT_WRITTEN_CORRECTLY:
                            text += getString(R.string.tag_not_written_correctly);
                            break;
                        default:
                            break;
                    }
                }
                setStatus(text);
            }
        }

        @Override
        public void onAudioAutoConfigCompleted(boolean isDefaultSettings, String autoConfigSettings) {
            HLog.w(TAG,"onAudioAutoConfigCompleted-isDefaultSettings="+isDefaultSettings
                    +"\nautoConfigSettings="+autoConfigSettings);
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            String outputDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.bbpos.bbdevice/";
            String filename = "settings.txt";
            String content = getString(R.string.auto_config_completed);
            if (isDefaultSettings) {
                content += "\n" + getString(R.string.default_settings);
                new File(outputDirectory + filename).delete();
            } else {
                content += "\n" + getString(R.string.settings) + autoConfigSettings;

                try {
                    File directory = new File(outputDirectory);
                    if (!directory.isDirectory()) {
                        directory.mkdirs();
                    }
                    FileOutputStream fos = new FileOutputStream(outputDirectory + filename, false);
                    fos.write(autoConfigSettings.getBytes());
                    fos.flush();
                    fos.close();

                    content += "\n" + getString(R.string.settings_written_to_external_storage);
                } catch (Exception e) {
                }
            }
            setStatus(content);
        }

        @Override
        public void onAudioAutoConfigError(AudioAutoConfigError autoConfigError) {
            HLog.w(TAG,"onAudioAutoConfigError-autoConfigError="+autoConfigError);
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            if (autoConfigError == AudioAutoConfigError.PHONE_NOT_SUPPORTED) {
                statusEditText.setText(getString(R.string.auto_config_error_phone_not_supported));
            } else if (autoConfigError == AudioAutoConfigError.INTERRUPTED) {
                statusEditText.setText(getString(R.string.auto_config_error_interrupted));
            }
        }

        @Override
        public void onAudioAutoConfigProgressUpdate(double percentage) {
            HLog.w(TAG,"onAudioAutoConfigProgressUpdate-percentage="+percentage);

            if (progressDialog != null) {
                progressDialog.setProgress((int) percentage);
            }
        }

        @Override
        public void onDeviceHere(boolean arg0) {
            HLog.w(TAG,"onDeviceHere-arg0="+arg0);

        }

        @Override
        public void onNoAudioDeviceDetected() {

            HLog.w(TAG,"onNoAudioDeviceDetected-");
            dismissDialog();
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            statusEditText.setText(getString(R.string.no_device_detected));
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(currentActivity, getString(R.string.no_device_detected), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onReturnNfcDataExchangeResult(boolean isSuccess, Hashtable<String, String> data) {
            HLog.w(TAG,"onReturnNfcDataExchangeResult-isSuccess="+isSuccess
                    +"\ndata="+data.toString());
            if (isSuccess) {
                String text = getString(R.string.nfc_data_exchange_success);
                text += "\n" + getString(R.string.ndef_record) + data.get("ndefRecord");
                setStatus(text);
            } else {
                String text = getString(R.string.nfc_data_exchange_fail);
                text += "\n" + getString(R.string.error_message) + data.get("errorMessage");
                setStatus(text);
            }
        }

        @Override
        public void onReturnNfcDetectCardResult(NfcDetectCardResult nfcDetectCardResult, Hashtable<String, Object> data) {
            HLog.w(TAG,"onReturnNfcDetectCardResult-nfcDetectCardResult="+nfcDetectCardResult.toString()
                    +"\ndata="+data.toString());
            String text = "";
            text += getString(R.string.nfc_card_detection_result) + nfcDetectCardResult;
            text += "\n" + getString(R.string.nfc_tag_information) + data.get("nfcTagInfo");
            text += "\n" + getString(R.string.nfc_card_uid) + data.get("nfcCardUID");
            if (data.containsKey("errorMessage")) {
                text += "\n" + getString(R.string.error_message) + data.get("errorMessage");
            }
            setStatus(text);
        }

        @Override
        public void onUsbConnected() {
            HLog.w(TAG,"onUsbConnected-");
            setStatus(getString(R.string.usb_connected));
        }

        @Override
        public void onUsbDisconnected() {
            HLog.w(TAG,"onUsbDisconnected-");
            setStatus(getString(R.string.usb_disconnected));
        }

        @Override
        public void onRequestDisplayAsterisk(int arg0) {
            HLog.w(TAG,"onRequestDisplayAsterisk-arg0="+arg0);
        }

        @Override
        public void onSerialConnected() {
            HLog.w(TAG,"onSerialConnected-");
            final ProgressDialog progressDialog = ProgressDialog.show(BaseActivity.this, getString(R.string.please_wait), getString(R.string.initializing));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(6000);
                    } catch (InterruptedException e) {
                    }
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            statusEditText.setText(getString(R.string.serial_connected));
                        }
                    });
                }
            }).start();
        }

        @Override
        public void onSerialDisconnected() {
            HLog.w(TAG,"onSerialDisconnected-");
            setStatus(getString(R.string.serial_disconnected));
        }

        @Override
        public void onBarcodeReaderConnected() {
            HLog.w(TAG,"onBarcodeReaderConnected-");
        }

        @Override
        public void onBarcodeReaderDisconnected() {
            HLog.w(TAG,"onBarcodeReaderDisconnected-");
        }

        @Override
        public void onReturnBarcode(String arg0) {
            HLog.w(TAG,"onReturnBarcode-arg0="+arg0);
        }

        @Override
        public void onRequestDisplayLEDIndicator(ContactlessStatus contactlessStatus) {
            HLog.w(TAG,"onRequestDisplayLEDIndicator-contactlessStatus="+contactlessStatus);
        }

        @Override
        public void onRequestProduceAudioTone(ContactlessStatusTone contactlessStatusTone) {
            HLog.w(TAG,"onRequestProduceAudioTone-contactlessStatusTone="+contactlessStatusTone);
        }

        @Override
        public void onReturnReadAIDResult(Hashtable<String, Object> data) {
            HLog.w(TAG,"onReturnReadAIDResult-data="+data.toString());

            String text = getString(R.string.read_aid_success);

            Object[] keys = data.keySet().toArray();
            Arrays.sort(keys);
            for (Object key : keys) {
                text += "\n" + (String) key + " : ";
                Object obj = data.get(key);
                if (obj instanceof String) {
                    text += (String) obj;
                } else if (obj instanceof Boolean) {
                    text += ((Boolean) obj) + "";
                } else if (obj instanceof TerminalSettingStatus) {
                    text += ((TerminalSettingStatus) obj) + "";
                }
            }
            HLog.w(TAG,"onReturnReadAIDResult-text="+text);
            setStatus(text);
        }

        @Override
        public void onReturnUpdateAIDResult(Hashtable<String, TerminalSettingStatus> data) {
            HLog.w(TAG,"onReturnUpdateAIDResult-data="+data.toString());
            String text = getString(R.string.update_aid_success);

            Object[] keys = data.keySet().toArray();
            Arrays.sort(keys);
            for (Object key : keys) {
                text += "\n" + (String) key + " : ";
                Object obj = data.get(key);
                if (obj instanceof String) {
                    text += (String) obj;
                } else if (obj instanceof Boolean) {
                    text += ((Boolean) obj) + "";
                } else if (obj instanceof TerminalSettingStatus) {
                    text += ((TerminalSettingStatus) obj) + "";
                }
            }

            setStatus(text);
        }

        @Override
        public void onDeviceReset() {
            HLog.w(TAG,"onDeviceReset-");
            setStatus(getString(R.string.device_reset));
        }

        @Override
        public void onPowerButtonPressed() {
            HLog.w(TAG,"onPowerButtonPressed-");
            setStatus(getString(R.string.power_button_pressed));
        }

        @Override
        public void onPowerDown() {
            HLog.w(TAG,"onPowerDown-");
            setStatus(getString(R.string.power_down));
        }

        @Override
        public void onEnterStandbyMode() {
            HLog.w(TAG,"onEnterStandbyMode-");
            setStatus(getString(R.string.enter_standby_mode));
        }

        @Override
        public void onReturnAccountSelectionResult(AccountSelectionResult accountSelectionResult, int selectedAccountType) {
            HLog.w(TAG,"onReturnAccountSelectionResult-accountSelectionResult="+accountSelectionResult.toString()
                    +"\nselectedAccountType="+selectedAccountType);
        }

        @Override
        public void onReturnDisableAccountSelectionResult(boolean isSuccess) {
            HLog.w(TAG,"onReturnDisableAccountSelectionResult-isSuccess"+isSuccess);
        }

        @Override
        public void onReturnEnableAccountSelectionResult(boolean isSuccess) {
            HLog.w(TAG,"onReturnEnableAccountSelectionResult-isSuccess"+isSuccess);
        }

        @Override
        public void onRequestStartEmv() {
            HLog.w(TAG,"onRequestStartEmv-");
        }

        @Override
        public void onReturnControlLEDResult(boolean isSuccess, String errorMessage) {
            HLog.w(TAG,"onReturnControlLEDResult-isSuccess="+isSuccess
                    +"\nerrorMessage="+errorMessage);
            if (isSuccess) {
                setStatus(getString(R.string.control_led_success));
            } else {
                setStatus(getString(R.string.control_led_fail) + "\n" + getString(R.string.error_message) + errorMessage);
            }
        }

        @Override
        public void onReturnVasResult(VASResult vasResult, Hashtable<String, Object> data) {
            HLog.w(TAG,"onReturnVasResult-vasResult="+vasResult
                    +"\ndata="+data.toString());

        }
    }
}
