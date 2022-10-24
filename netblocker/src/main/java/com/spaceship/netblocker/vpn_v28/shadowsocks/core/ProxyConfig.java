package com.spaceship.netblocker.vpn_v28.shadowsocks.core;

import android.annotation.SuppressLint;

import com.spaceship.netblocker.vpn_v28.shadowsocks.tcpip.CommonMethods;
import com.spaceship.netblocker.vpn_v28.shadowsocks.tunnel.Config;
import com.spaceship.netblocker.vpn_v28.shadowsocks.tunnel.httpconnect.HttpConnectConfig;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ProxyConfig {
    public static final ProxyConfig INSTANCE = new ProxyConfig();
    public final static boolean IS_DEBUG = true;
    public final static int FAKE_NETWORK_IP = CommonMethods.ipStringToInt("172.25.0.0");
    private final static int FAKE_NETWORK_MASK = CommonMethods.ipStringToInt("255.255.0.0");
    public static String AppInstallID;
    static String AppVersion;
    public boolean globalMode = false;
    public ArrayList<Config> proxyList;
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            refreshProxyServer();//定时更新dns缓存
        }

        //定时更新dns缓存
        void refreshProxyServer() {
            try {
                for (int i = 0; i < proxyList.size(); i++) {
                    try {
                        Config config = proxyList.get(0);
                        InetAddress address = InetAddress.getByName(config.serverAddress.getHostName());
                        if (address != null && !address.equals(config.serverAddress.getAddress())) {
                            config.serverAddress = new InetSocketAddress(address, config.serverAddress.getPort());
                        }
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {

            }
        }
    };
    private ArrayList<IPAddress> ipList;
    private ArrayList<IPAddress> dnsList;
    private ArrayList<IPAddress> remoteList;
    private HashMap<String, Boolean> domainMap;
    private int dnsTtl;
    private String welcomeInfo;
    private String sessionName;
    private String userAgent;
    private boolean outsideChinaUseProxy = true;
    private boolean isolateHttpHostHeader = true;
    private int mtu;
    private Timer timer;

    public ProxyConfig() {
        ipList = new ArrayList<>();
        dnsList = new ArrayList<>();
        remoteList = new ArrayList<>();
        proxyList = new ArrayList<>();
        domainMap = new HashMap<>();

        timer = new Timer();
        timer.schedule(task, 120000, 120000);//每两分钟刷新一次。
    }

    public static boolean isFakeIP(int ip) {
        return (ip & ProxyConfig.FAKE_NETWORK_MASK) == ProxyConfig.FAKE_NETWORK_IP;
    }

    public Config getDefaultProxy() {
        if (proxyList.size() > 0) {
            return proxyList.get(0);
        } else {
            return null;
        }
    }

    Config getDefaultTunnelConfig() {
        return getDefaultProxy();
    }

    public IPAddress getDefaultLocalIP() {
        if (ipList.size() > 0) {
            return ipList.get(0);
        } else {
            return new IPAddress("10.8.0.2", 32);
        }
    }

    public ArrayList<IPAddress> getDnsList() {
        return dnsList;
    }

    public ArrayList<IPAddress> getRouteList() {
        return remoteList;
    }

    public int getDnsTTL() {
        if (dnsTtl < 30) {
            dnsTtl = 30;
        }
        return dnsTtl;
    }

    public String getWelcomeInfo() {
        return welcomeInfo;
    }

    public String getSessionName() {
        if (sessionName == null) {
            sessionName = getDefaultProxy().serverAddress.getHostName();
        }
        return sessionName;
    }

    public String getUserAgent() {
        if (userAgent == null || userAgent.isEmpty()) {
            userAgent = System.getProperty("http.agent");
        }
        return userAgent;
    }

    public int getMTU() {
        if (mtu > 1400 && mtu <= 20000) {
            return mtu;
        } else {
            return 20000;
        }
    }

    private Boolean getDomainState(String domain) {
        domain = domain.toLowerCase();
        while (domain.length() > 0) {
            Boolean stateBoolean = domainMap.get(domain);
            if (stateBoolean != null) {
                return stateBoolean;
            } else {
                int start = domain.indexOf('.') + 1;
                if (start > 0 && start < domain.length()) {
                    domain = domain.substring(start);
                } else {
                    return null;
                }
            }
        }
        return null;
    }

//    public boolean needProxy(String host, int ip) {
//        if (globalMode) {
//            return true;
//        }
//        if (host != null) {
//            Boolean stateBoolean = getDomainState(host);
//            if (stateBoolean != null) {
//                return stateBoolean;
//            }
//        }
//
//        if (isFakeIP(ip))
//            return true;
//
//        if (outsideChinaUseProxy && ip != 0) {
//            return !ChinaIpMaskManager.isIPInChina(ip);
//        }
//        return false;
//    }

    public boolean isIsolateHttpHostHeader() {
        return isolateHttpHostHeader;
    }

    public void loadFromFile(InputStream inputStream) throws Exception {
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        loadFromLines(new String(bytes).split("\\r?\\n"));
    }

    private void loadFromLines(String[] lines) throws Exception {
        ipList.clear();
        dnsList.clear();
        remoteList.clear();
        proxyList.clear();
        domainMap.clear();

        int lineNumber = 0;
        for (String line : lines) {
            lineNumber++;
            String[] items = line.split("\\s+");
            if (items.length < 2) {
                continue;
            }

            String tagString = items[0].toLowerCase(Locale.ENGLISH).trim();
            try {
                if (!tagString.startsWith("#")) {
                    if (ProxyConfig.IS_DEBUG)
                        System.out.println(line);

                    switch (tagString) {
                        case "ip":
                            addIPAddressToList(items, ipList);
                            break;
                        case "dns":
                            addIPAddressToList(items, dnsList);
                            break;
                        case "route":
                            addIPAddressToList(items, remoteList);
                            break;
                        case "proxy":
                            addProxyToList(items);
                            break;
                        case "direct_domain":
                            addDomainToHashMap(items, false);
                            break;
                        case "proxy_domain":
                            addDomainToHashMap(items, true);
                            break;
                        case "dns_ttl":
                            dnsTtl = Integer.parseInt(items[1]);
                            break;
                        case "welcome_info":
                            welcomeInfo = line.substring(line.indexOf(" ")).trim();
                            break;
                        case "session_name":
                            sessionName = items[1];
                            break;
                        case "user_agent":
                            userAgent = line.substring(line.indexOf(" ")).trim();
                            break;
                        case "outside_china_use_proxy":
                            outsideChinaUseProxy = convertToBool(items[1]);
                            break;
                        case "isolate_http_host_header":
                            isolateHttpHostHeader = convertToBool(items[1]);
                            break;
                        case "mtu":
                            mtu = Integer.parseInt(items[1]);
                            break;
                    }
                }
            } catch (Exception e) {
                throw new Exception(String.format("config file parse error: line:%d, tag:%s, error:%s", lineNumber, tagString, e));
            }

        }

        //查找默认代理。
        if (proxyList.size() == 0) {
            tryAddProxy(lines);
        }
    }

    private void tryAddProxy(String[] lines) {
        for (String line : lines) {
            Pattern p = Pattern.compile("proxy\\s+([^:]+):(\\d+)", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(line);
            while (m.find()) {
                HttpConnectConfig config = new HttpConnectConfig();
                config.serverAddress = new InetSocketAddress(m.group(1), Integer.parseInt(m.group(2)));
                if (!proxyList.contains(config)) {
                    proxyList.add(config);
                    domainMap.put(config.serverAddress.getHostName(), false);
                }
            }
        }
    }

    private void addProxyToList(String proxyString) throws Exception {
        Config config;
        if (proxyString.startsWith("ss://")) {
            // empty impl
            return;
        } else {
            if (!proxyString.toLowerCase().startsWith("http://")) {
                proxyString = "http://" + proxyString;
            }
            config = HttpConnectConfig.parse(proxyString);
        }
        if (!proxyList.contains(config)) {
            proxyList.add(config);
            domainMap.put(config.serverAddress.getHostName(), false);
        }
    }

    private void addProxyToList(String[] items) throws Exception {
        for (int i = 1; i < items.length; i++) {
            addProxyToList(items[i].trim());
        }
    }

    private void addDomainToHashMap(String[] items, Boolean state) {
        for (int i = 1; i < items.length; i++) {
            String domainString = items[i].toLowerCase().trim();
            if (domainString.charAt(0) == '.') {
                domainString = domainString.substring(1);
            }
            domainMap.put(domainString, state);
        }
    }

    private boolean convertToBool(String valueString) {
        if (valueString == null || valueString.isEmpty())
            return false;
        valueString = valueString.toLowerCase(Locale.ENGLISH).trim();
        return valueString.equals("on") || valueString.equals("1") || valueString.equals("true") || valueString.equals("yes");
    }

    private void addIPAddressToList(String[] items, ArrayList<IPAddress> list) {
        for (int i = 1; i < items.length; i++) {
            String item = items[i].trim().toLowerCase();
            if (item.startsWith("#")) {
                break;
            } else {
                IPAddress ip = new IPAddress(item);
                if (!list.contains(ip)) {
                    list.add(ip);
                }
            }
        }
    }

    public class IPAddress {
        public final String Address;
        public final int PrefixLength;

        IPAddress(String address, int prefixLength) {
            this.Address = address;
            this.PrefixLength = prefixLength;
        }

        IPAddress(String ipAddresString) {
            String[] arrStrings = ipAddresString.split("/");
            String address = arrStrings[0];
            int prefixLength = 32;
            if (arrStrings.length > 1) {
                prefixLength = Integer.parseInt(arrStrings[1]);
            }
            this.Address = address;
            this.PrefixLength = prefixLength;
        }

        @SuppressLint("DefaultLocale")
        @Override
        public String toString() {
            return String.format("%s/%d", Address, PrefixLength);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            } else {
                return this.toString().equals(o.toString());
            }
        }
    }

}
