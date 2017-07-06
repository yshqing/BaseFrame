package com.ysq.android.utils.baseframe.application;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

//import com.android.ysq.utils.YNetWorkUtils;
import com.ysq.android.utils.crashlog.CrashLogHandlerUtils;
import com.ysq.android.utils.logger.LogLevel;
import com.ysq.android.utils.logger.Logger;
import com.ysq.android.utils.tools.YNetWorkUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class BaseApplication extends Application {

    private static final String ACTION_CONNECT_CLIENT_IF_NEED = "ACTION_CONNECT_CLIENT_IF_NEED";
    private final int MQTT_KEEP_ALIVE = 10;// 秒
    protected MqttAndroidClient mMqttClient;
    private BroadcastReceiver mNetReceiver;
    private AtomicBoolean isClientConnecting;
    private AlarmManager mAlarmManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.init(getLogTag()).errorLogFile(getErrorLogSaveDir()).logLevel(getLogLevel());
        if (enableCrashLog()) {
            if (getCrashLogSaveDir() != null) {
                CrashLogHandlerUtils.getInstance(this).setSavePath(getCrashLogSaveDir());
            }
            try {
                CrashLogHandlerUtils.getInstance(this).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (getMqttClient() != null) {
            isClientConnecting = new AtomicBoolean(false);
            registerNetWorkInfo();
            connectMqttIfNeed();
            setTimerTask();
        }
    }

    protected boolean enableCrashLog() {
        return true;
    }

    protected File getCrashLogSaveDir() {
        return null;
    }

    protected String getLogTag() {
        return "YLogTag";
    }

    protected LogLevel getLogLevel() {
        return LogLevel.FULL;
    }

    protected File getErrorLogSaveDir() {
        return null;
    }

    protected MqttAndroidClient getMqttClient() {
        return mMqttClient;
    }

    protected MqttCallback getMqttCallback() {
        return null;
    }

    protected IMqttActionListener getIMqttActionListener() {
        return null;
    }

    protected int getMqttKeepAliveTime() {
        return MQTT_KEEP_ALIVE;
    }

    private void registerNetWorkInfo() {
        if (mNetReceiver == null) {
            mNetReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    Logger.v("接收到的action：%s", intent.getAction());
                    if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)
                            || intent.getAction().equals(ACTION_CONNECT_CLIENT_IF_NEED)) {
                        connectMqttIfNeed();
                    }
                }
            };
        }
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mFilter.addAction(ACTION_CONNECT_CLIENT_IF_NEED);
        registerReceiver(mNetReceiver, mFilter);
    }

    private synchronized void connectMqttIfNeed() {
        if (YNetWorkUtils.isNetWorkAvailable(this)) {
            if (mMqttClient == null) {
                return;
            }
            if (!mMqttClient.isConnected() && !isClientConnecting.get()) {
                isClientConnecting.set(true);
                try {
                    MqttConnectOptions options = new MqttConnectOptions();
                    options.setConnectionTimeout(getMqttKeepAliveTime());
                    options.setKeepAliveInterval(getMqttKeepAliveTime());// 单位秒
                    mMqttClient.setCallback(getMqttCallback());
                    mMqttClient.connect(options, null, new IMqttActionListener() {

                        @Override
                        public void onSuccess(IMqttToken arg0) {
                            isClientConnecting.set(false);
                            if (getIMqttActionListener() != null) {
                                getIMqttActionListener().onSuccess(arg0);
                            }
                        }

                        @Override
                        public void onFailure(IMqttToken arg0, Throwable arg1) {
                            isClientConnecting.set(false);
                            if (getIMqttActionListener() != null) {
                                getIMqttActionListener().onFailure(arg0, arg1);
                            }
                        }
                    });

                } catch (MqttException e) {
                    e.printStackTrace();
                    isClientConnecting.set(false);
                } catch (Exception e) {
                    e.printStackTrace();
                    isClientConnecting.set(false);
                }
            }
        }
    }

    private void setTimerTask() {
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setAction(ACTION_CONNECT_CLIENT_IF_NEED);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + getMqttKeepAliveTime() * 1000,
                getMqttKeepAliveTime() * 1000, pi);
    }
}
