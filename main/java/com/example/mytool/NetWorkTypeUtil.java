package com.example.mytool;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.TELEPHONY_SERVICE;

public class NetWorkTypeUtil {
    //没有网络连接
    public static final int NETWORN_NONE = 0;
    //wifi连接
    public static final int NETWORN_WIFI = 1;
    //手机网络数据连接类型
    public static final int NETWORN_2G = 2;
    public static final int NETWORN_3G = 3;
    public static final int NETWORN_4G = 4;
    public static final int NETWORN_5G = 5;
    public static final int NETWORN_MOBILE = 6;

    public static int getNetworkState(Context context) {
//获取系统的网络服务
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//如果当前没有网络
        if (null == connManager)
            return NETWORN_NONE;
//获取当前网络类型，如果为空，返回无网络
        NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();
        if (activeNetInfo == null || !activeNetInfo.isAvailable()) {
            return NETWORN_NONE;
        }
// 判断是不是连接的是不是wifi
        NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (null != wifiInfo) {
            NetworkInfo.State state = wifiInfo.getState();
            if (null != state)
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    return NETWORN_WIFI;
                }
        }
// 如果不是wifi，则判断当前连接的是运营商的哪种网络2g、3g、4g等
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (null != networkInfo) {
            NetworkInfo.State state = networkInfo.getState();
            String strSubTypeName = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                strSubTypeName = networkInfo.getSubtypeName();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                int subtype = activeNetInfo.getSubtype();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                String subtypeName = activeNetInfo.getSubtypeName();
            }
            if (null != state)
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
                        switch (activeNetInfo.getSubtype()) {
    //如果是2g类型
                            case TelephonyManager.NETWORK_TYPE_GPRS: // 联通2g
                            case TelephonyManager.NETWORK_TYPE_CDMA: // 电信2g
                            case TelephonyManager.NETWORK_TYPE_EDGE: // 移动2g
                            case TelephonyManager.NETWORK_TYPE_1xRTT:
                            case TelephonyManager.NETWORK_TYPE_IDEN:
                            case TelephonyManager.NETWORK_TYPE_GSM:
                                return NETWORN_2G;
    //如果是3g类型
                            case TelephonyManager.NETWORK_TYPE_EVDO_A: // 电信3g
                            case TelephonyManager.NETWORK_TYPE_UMTS:
                            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                            case TelephonyManager.NETWORK_TYPE_HSDPA:
                            case TelephonyManager.NETWORK_TYPE_HSUPA:
                            case TelephonyManager.NETWORK_TYPE_HSPA:
                            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                            case TelephonyManager.NETWORK_TYPE_EHRPD:
                            case TelephonyManager.NETWORK_TYPE_HSPAP:
                            case TelephonyManager.NETWORK_TYPE_IWLAN:
                                return NETWORN_3G;
    //如果是4g类型
                            case TelephonyManager.NETWORK_TYPE_LTE:
                                return NETWORN_4G;
                            //如果是5G类型
                            case TelephonyManager.NETWORK_TYPE_NR:
                                return NETWORN_5G;
                            default:
    //中国移动 联通 电信 三种3G制式
                                if (strSubTypeName.equalsIgnoreCase("TD-SCDMA") || strSubTypeName.equalsIgnoreCase("WCDMA") || strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                                    return NETWORN_3G;
                                } else {
                                    return NETWORN_MOBILE;
                                }
                        }
                    }
                }
        }
        return NETWORN_NONE;
    }

    public static String[] get_internet_address() {
        String url = "https://2021.ip138.com/";
        try {
            Document doc = (Document) Jsoup.connect(url).header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36").get();
            Element element = doc.getElementsByTag("p").first();
            String[] rs = element.text().split("\\s+|：");
            rs[1] = rs[1].substring(1, rs[1].length() - 1);
            return rs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String get_upf_info(InetAddress inetAddress) {
        String[] upf201_cmnet = {"1a80", "1c80", "1e80", "2080", "2280", "2480"};
        String[] upf202_cmnet = {"1a88", "1c88", "1e88", "2088", "2288", "2488"};
        String[] upf203_cmnet = {"1a90", "1c90", "1e90", "2090", "2290", "2490"};
        String[] upf204_cmnet = {"1a98", "1c98", "1e98", "2098", "2298", "2498"};
        String[] upf201_ims = {"1e80", "1b80", "1d00", "1f00", "2080", "2280", "2480"};
        String[] upf202_ims = {"1ea0", "1b90", "1d10", "1f10", "2090", "2290", "2490"};
        String[] upf203_ims = {"1c80", "1bc0", "1d40", "1f40", "20c0", "22c0", "24c0"};
        String[] upf204_ims = {"1ca0", "1bd0", "1d50", "1f50", "20d0", "22d0", "24d0"};

        String[] addr_seg = inetAddress.getHostAddress().split(":");
        String net_add = Integer.toHexString(Integer.parseInt(addr_seg[2], 16) & 0xfff8);
        if (addr_seg[1].equals("8934")) {
            if (Arrays.asList(upf201_cmnet).contains(net_add))
                return "cmnet:upf201";
            if (Arrays.asList(upf202_cmnet).contains(net_add))
                return "cmnet:upf202";
            if (Arrays.asList(upf203_cmnet).contains(net_add))
                return "cmnet:upf203";
            if (Arrays.asList(upf204_cmnet).contains(net_add))
                return "cmnet:upf204";
        }
        if (addr_seg[1].equals("8134")) {
            net_add = Integer.toHexString(Integer.parseInt(addr_seg[2], 16) & 0xffe0);
            if (Arrays.asList(upf201_ims).contains(net_add))
                return "ims:upf201";
            if (Arrays.asList(upf202_ims).contains(net_add))
                return "ims:upf202";
            if (Arrays.asList(upf203_ims).contains(net_add))
                return "ims:upf203";
            if (Arrays.asList(upf204_ims).contains(net_add))
                return "ims:upf204";
            net_add = Integer.toHexString(Integer.parseInt(addr_seg[2], 16) & 0xfff0);
            if (Arrays.asList(upf201_ims).contains(net_add))
                return "ims:upf201";
            if (Arrays.asList(upf202_ims).contains(net_add))
                return "ims:upf202";
            if (Arrays.asList(upf203_ims).contains(net_add))
                return "ims:upf203";
            if (Arrays.asList(upf204_ims).contains(net_add))
                return "ims:upf204";
        }
        return null;
    }

    public static ArrayList<String[]> get_network_info(Context context){
        ArrayList<String[]> net_data = new ArrayList<String[]>();
        String[] info;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != connManager){
            NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo.State state = networkInfo.getState();
            if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING){
                info = new String[2];
                info[0] = "网络类型";
                info[1] = networkInfo.getTypeName();
                net_data.add(info);
            }
            networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            state = networkInfo.getState();
            if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING){
                info = new String[2];
                info[0] = "网络类型";
                info[1] = networkInfo.getSubtypeName();
                net_data.add(info);
            }
        }
        try {
            for (Enumeration<NetworkInterface> enNetI = NetworkInterface
                    .getNetworkInterfaces(); enNetI.hasMoreElements(); ) {
                NetworkInterface netI = enNetI.nextElement();
                if (netI.getName().contains("wlan")) {
                    for (Enumeration<InetAddress> enumIpAddr = netI
                            .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (inetAddress instanceof Inet4Address) {
                            info = new String[2];
                            info[0] = netI.getName();
                            info[1] = inetAddress.getHostAddress();
                            net_data.add(info);
                        }
                    }
                }
                if (netI.getName().contains("rmnet")){
                    for (Enumeration<InetAddress> enumIpAddr = netI
                            .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.getHostAddress().startsWith("fe80")){
                            info = new String[2];
                            info[0] = netI.getName();
                            info[1] = inetAddress.getHostAddress();
                            net_data.add(info);
                            if(inetAddress instanceof Inet6Address){
                                String upf = get_upf_info(inetAddress);
                                if(upf != null){
                                    info = new String[2];
                                    info[0] = upf.split(":")[0];
                                    info[1] = upf.split(":")[1];
                                    net_data.add(info);
                                }
                            }
                        }
                    }
                }
            }
        } catch(SocketException e){
            e.printStackTrace();
        }
        String [] internet_info = get_internet_address();
        info = new String[2];
        info[0] = "PUB_IP";
        info[1] = internet_info[1];
        net_data.add(info);
        info = new String[2];
        info[0] = "区域";
        info[1] = internet_info[3];
        net_data.add(info);
        info = new String[2];
        info[0] = "运营商";
        info[1] = internet_info[4];
        net_data.add(info);
        return net_data;
    }


    public static ArrayList<String[]> get_cell_info(Context context) {
        ArrayList<String[]> cellsinfo = new ArrayList<String[]>();
        TelephonyManager telephonymanager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        List<CellInfo> cellInfoList = telephonymanager.getAllCellInfo();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           return null;
        }
        CellLocation location = telephonymanager.getCellLocation();
        for(CellInfo cellInfo:cellInfoList){

            if (cellInfo.isRegistered()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (cellInfo instanceof CellInfoNr) {
                        String[] cli = new String[2];
                        cli[0] = "NR_CI";
                        String ciinfo =((CellInfoNr) cellInfo).getCellIdentity().toString();
                        Pattern pattern = Pattern.compile("mPci = \\d+|mNrArfcn = \\d+");
                        Matcher matcher = pattern.matcher(ciinfo);
                        String citmp = "";
                        while(matcher.find())
                            citmp = citmp+matcher.group()+"\n";
                        cli[1] = citmp.trim();
                        cellsinfo.add(cli);
                        cli = new String[2];
                        cli[0] = "信号强度";
                        ciinfo =((CellInfoNr) cellInfo).getCellSignalStrength().toString();
                        pattern = Pattern.compile("ssRsrp = \\d+|ssRsrq = \\d+|ssSinr = \\d+");
                        matcher = pattern.matcher(ciinfo);
                        citmp = "";
                        while(matcher.find())
                            citmp = citmp+matcher.group()+"\n";
                        cli[1] = citmp.trim();
                        cellsinfo.add(cli);
                        cli = new String[2];
                        cli[0] = "LAC";
                        cli[1] = ((GsmCellLocation) location).getLac() + "";
                        cellsinfo.add(cli);
                    }
                }
                if (cellInfo instanceof CellInfoLte) {
                    String[] cli = new String[2];
                    cli[0] = "LTE_CI";
                    String ciinfo =((CellInfoLte) cellInfo).getCellIdentity().toString();
                    //Log.i(MainActivity.TAG,ciinfo);
                    Pattern pattern = Pattern.compile("mPci=\\d+|mEarfcn=\\d+");
                    Matcher matcher = pattern.matcher(ciinfo);
                    String citmp = "";
                    while(matcher.find())
                        citmp = citmp+matcher.group()+"\n";
                    cli[1] = citmp.trim();
                    cellsinfo.add(cli);
                    cli = new String[2];
                    cli[0] = "信号强度";
                    ciinfo =((CellInfoLte) cellInfo).getCellSignalStrength().toString();
                    //Log.i(MainActivity.TAG,ciinfo);
                    pattern = Pattern.compile("rsrp=-\\d+|rsrq=-\\d+|rssi=-\\d+");
                    matcher = pattern.matcher(ciinfo);
                    citmp = "";
                    while(matcher.find())
                        citmp = citmp+matcher.group()+"\n";
                    cli[1] = citmp.trim();
                    cellsinfo.add(cli);
                    cli = new String[2];
                    cli[0] = "LAC";
                    cli[1] = ((GsmCellLocation) location).getLac() + "";
                    cellsinfo.add(cli);
                }
            }
        }
        if(cellsinfo.isEmpty())
            return null;
        else
            return cellsinfo;

    }
    public static String[] getDnsFromCmd() {
        LinkedList<String> dnsServers = new LinkedList<>();
        try {
            //Process process = Runtime.getRuntime().exec("getprop");
            Process process = Runtime.getRuntime().exec("ping -c 3 www.bsidussd.com");
            InputStream inputStream = process.getInputStream();
            LineNumberReader lnr = new LineNumberReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = lnr.readLine()) != null) {
                Log.i(MainActivity.TAG,line);
                int split = line.indexOf("]: [");
                if (split == -1) continue;
                String property = line.substring(1, split);
                String value = line.substring(split + 4, line.length() - 1);
                if (property.endsWith(".dns")
                        || property.endsWith(".dns1")
                        || property.endsWith(".dns2")
                        || property.endsWith(".dns3")
                        || property.endsWith(".dns4")) {
                    InetAddress ip = InetAddress.getByName(value);
                    if (ip == null) continue;
                    value = ip.getHostAddress();
                    if (value == null) continue;
                    if (value.length() == 0) continue;
                    dnsServers.add(value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dnsServers.isEmpty() ? new String[0] : dnsServers.toArray(new String[dnsServers.size()]);
    }
    public static ArrayList<String[]> getDnsFromConnectionManager(Context context) {
        ArrayList<String[]> dnsv4Servers = new ArrayList<String[]>();
        ArrayList<String[]> dnsv6Servers = new ArrayList<String[]>();
        ArrayList<String[]> dnsServers = new ArrayList<String[]>();
        int i=0,j=0;
        if (Build.VERSION.SDK_INT >= 21 && context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                for (Network network : connectivityManager.getAllNetworks()) {
                    NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
                    if(networkInfo.getType() == 0){
                        LinkProperties lp = connectivityManager.getLinkProperties(network);
                        for (InetAddress addr : lp.getDnsServers()) {
                            String [] dnsinfo = new String[2];
                            if (addr instanceof Inet4Address){
                                dnsinfo[0] = "DNS"+(++i);
                                dnsinfo[1] = addr.getHostAddress();
                                dnsv4Servers.add(dnsinfo);
                            }
                            if (addr instanceof Inet6Address){
                                dnsinfo[0] = "DNS"+(++j);
                                dnsinfo[1] = addr.getHostAddress();
                                dnsv6Servers.add(dnsinfo);
                            }
                            Log.i(MainActivity.TAG,addr.getHostAddress());
                        }
                    }
                }
                int minLen = (i <= j ? i:j);
                if (minLen > 0){
                    for(int k = 0;k<minLen;k++){
                        dnsv4Servers.get(k)[1] = dnsv4Servers.get(k)[1] + "\n"+dnsv6Servers.get(k)[1];
                        dnsServers.add(dnsv4Servers.get(k));
                    }
                }

            }
        }
        return dnsServers.isEmpty() ? null : dnsServers;
    }
}
