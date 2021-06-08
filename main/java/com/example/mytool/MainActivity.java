package com.example.mytool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.example.mytool.NetWorkTypeUtil.getDnsFromConnectionManager;
import static com.example.mytool.NetWorkTypeUtil.get_cell_info;
import static com.example.mytool.NetWorkTypeUtil.get_network_info;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "MyTool";
    public class NetInfoAdapter extends ArrayAdapter<String[]>{
        private int resourceid;
        public NetInfoAdapter(Context context, int viewid, List<String[]> objects){
            super(context,viewid,objects);
            resourceid = viewid;
        }
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            String[] netdata = getItem(position);
            View view;
            if(convertView == null){
                view = LayoutInflater.from(getContext()).inflate(resourceid,parent,false);
            }else {
                view = convertView;
            }
            TextView keyname = (TextView)view.findViewById(R.id.key_name);
            keyname.setText(netdata[0]);
            TextView keyvalue = (TextView)view.findViewById(R.id.key_value);
            keyvalue.setText(netdata[1]);
            return view;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = (ListView)findViewById((R.id.listview));
        Button button = (Button)findViewById(R.id.button);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(MainActivity.TAG,"1未授权");
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(MainActivity.TAG,"2未授权");
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        TelephonyManager manager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);

        manager.listen(new SignalStrengthListener(),PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getDnsFromCmd();
                    }
                }).start();
                 */
                try{
                    new Thread(new Runnable(){
                        @Override
                        public void run() {
                            ArrayList<String[]> netdata = get_network_info(MainActivity.this.getBaseContext());
                            ArrayList<String[]> cidata = get_cell_info(MainActivity.this.getBaseContext());
                            ArrayList<String[]> dnsdata = getDnsFromConnectionManager(MainActivity.this.getBaseContext());
                            if (cidata != null)
                                netdata.addAll(cidata);
                            if (dnsdata != null)
                                netdata.addAll(dnsdata);
                            NetInfoAdapter netInfoAdapter = new NetInfoAdapter(MainActivity.this,R.layout.item_layout,netdata);
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    listView.setAdapter(netInfoAdapter);
                                }
                            });
                        }
                    }).start();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });


    }
    private class SignalStrengthListener extends PhoneStateListener {
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            int lte_sinr;
            int lte_rsrp;
            int lte_rsrq;
            int lte_rssnr;
            int lte_cqi;
            try {
                lte_sinr = (Integer) signalStrength.getClass().getMethod("getLteSignalStrength").invoke(signalStrength);
                /*lte_rsrp = (Integer) signalStrength.getClass().getMethod("getLteRsrp").invoke(signalStrength);
                lte_rsrq = (Integer) signalStrength.getClass().getMethod("getLteRsrq").invoke(signalStrength);
                lte_rssnr = (Integer) signalStrength.getClass().getMethod("getLteRssnr").invoke(signalStrength);
                lte_cqi = (Integer) signalStrength.getClass().getMethod("getLteCqi").invoke(signalStrength);*/
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    Log.d(TAG, "lte_rsrp:" + lte_sinr);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "error:"+e.toString());
                return;
            }
        }
    }
}