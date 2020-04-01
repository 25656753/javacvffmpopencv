package com.mass.javacvffmpopencv;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import androidx.appcompat.app.AppCompatActivity;

public class UsbtActivity extends AppCompatActivity {
    private UsbManager usbManager;
    private ServerSocket mServerSocket;
    private Boolean mCanRun=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usbt);
      findViewById(R.id.btnusb).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

              try {

                  mServerSocket = new ServerSocket(9000);
              } catch (IOException e) {
                  e.printStackTrace();
              }
              new Thread(new Runnable() {
                      @Override
                      public void run() {
                          while (mCanRun) {
                              Log.i("ggggg---","等待连接..." + mServerSocket.getInetAddress());

                              try {
                                  Socket socket = mServerSocket.accept();
                              } catch (IOException e) {
                                  e.printStackTrace();
                              }
                              Log.i("ggggg---","已连接...");


                          }
                      }
                  }).start();



          }
      });
    }


    public static final String action_usb_permission = "org.zhuhailong.myviewproject.permission";



    public void hostAccessory(Context context) {

        //获取UsbManager
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        //获取usb设备列表
        UsbAccessory[] accessoryList = usbManager.getAccessoryList();
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Toast.makeText(context, "accessoryList="+accessoryList+"---->deviceList"+deviceList.size(), Toast.LENGTH_SHORT).show();
        if (accessoryList == null || accessoryList.length <= 0) {
            Toast.makeText(context, "未查询到对应设备", Toast.LENGTH_SHORT).show();
            return;
        }

        UsbAccessory mUsbAccessory = null;
        for (UsbAccessory usbAccessory : accessoryList) {
            if (filterDevice(usbAccessory)) {//判断是否使我们要连接的usb设备
                break;
            }
        }

        if (usbManager.hasPermission(mUsbAccessory)) {//检测是否有usb权限
            requestPermission(mUsbAccessory,usbManager);//请求usb权限
        } else {
            connect(mUsbAccessory);//连接usb设备
        }

    }

    /**
     * 请求usb权限
     *
     * @param usbManager
     * @param parcelable
     */
    public void requestPermission(Parcelable parcelable, UsbManager usbManager) {
        UsbAccessory usbAccessory = (UsbAccessory) parcelable;
        if (!usbManager.hasPermission(usbAccessory)) {
            IntentFilter intentFilter = new IntentFilter(action_usb_permission);
            registerReceiver(mMyBroadcastReceiver, intentFilter);
            PendingIntent broadcast = PendingIntent.getBroadcast(this, 0, new Intent(action_usb_permission), 0);
            usbManager.requestPermission(usbAccessory, broadcast);
        } else {
            Toast.makeText(UsbtActivity.this, "already have permission", Toast.LENGTH_SHORT).show();
        }
    }

    private FileInputStream mFileInputStream;

    private FileOutputStream mFileOutputStream;

    /**
     * 连接设备
     * @param usbAccessory
     * @return
     */
    public boolean connect(UsbAccessory usbAccessory) {
        ParcelFileDescriptor parcelFileDescriptor = usbManager.openAccessory(usbAccessory);
        if (parcelFileDescriptor != null) {
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

            mFileInputStream = new FileInputStream(fileDescriptor);

            mFileOutputStream = new FileOutputStream(fileDescriptor);

        }
        return false;
    }

    /**
     * 写入数据
     * @param data
     */
    public void write(byte[] data) {
        try {
            mFileOutputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 读取数据
     */
    public void read() {
        int max=1000;//字节数据根据协议来确定
        byte[] data = new byte[max];
        try {
            mFileInputStream.read(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 权限广播接收器
     */
    private BroadcastReceiver mMyBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action_usb_permission.equals(action)) {
                synchronized (this) {
                    UsbAccessory usbAccessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (usbAccessory != null) {
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            connect(usbAccessory);
                            Toast.makeText(context, "success get permission", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "failure get permission", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "the usbDevice be null", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    /**
     * 筛选出我们想要的usb设备
     *
     * @param usbAccessory
     * @return
     */
    public boolean filterDevice(UsbAccessory usbAccessory) {
        // TODO: 2019/3/13 对应判断设备是否是我们要连接的设备
        return false;
    }


}
