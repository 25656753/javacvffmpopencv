package com.mass.javacvffmpopencv;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;

public class UsbtActivity extends AppCompatActivity {
    private UsbManager usbManager;
    private ServerSocket mServerSocket;
    private Boolean mCanRun=true;
    private UsbDeviceConnection mUsbDeviceConnection;
    private UsbEndpoint mUsbEndpoint_in,mUsbEndpoint_out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usbt);
      findViewById(R.id.btnusb).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

            /*  try {

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
                  }).start();*/


              hostModel(UsbtActivity.this);
          }
      });
    }


    public static final String action_usb_permission = "org.zhuhailong.myviewproject.permission";

    public void hostModel(Context context) {

        //获取UsbManager
         usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        //获取usb设备列表
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

        Set<Map.Entry<String, UsbDevice>> entries = deviceList.entrySet();

        UsbDevice mUsbDevice = null;

        for (Map.Entry<String, UsbDevice> entry : entries) {
            UsbDevice value = entry.getValue();
            String key = entry.getKey();

            if (filterDevice(key, value)) {//判断是否使我们要连接的usb设备
                mUsbDevice = value;
                break;
            }
        }

        if (usbManager.hasPermission(mUsbDevice)) {//检测是否有usb权限
            requestPermission(mUsbDevice,usbManager);
        } else {
            connect(mUsbDevice);
        }

    }

    /**
     * 请求usb权限
     * @param usbManager
     * @param parcelable
     */
    public void requestPermission(Parcelable parcelable,UsbManager usbManager) {
        UsbDevice usbDevice = (UsbDevice) parcelable;
        if (!usbManager.hasPermission(usbDevice)) {
            IntentFilter intentFilter = new IntentFilter(action_usb_permission);
            registerReceiver(mMyBroadcastReceiver, intentFilter);
            PendingIntent broadcast = PendingIntent.getBroadcast(this, 0, new Intent(action_usb_permission), 0);
            usbManager.requestPermission(usbDevice, broadcast);
        } else {
            Toast.makeText(UsbtActivity.this, "already have permission", Toast.LENGTH_SHORT).show();
        }
    }


    public boolean connect(UsbDevice mUsbDevice) {
        if (mUsbDevice != null) {
            UsbInterface anInterface = mUsbDevice.getInterface(0);
            mUsbDeviceConnection = usbManager.openDevice(mUsbDevice);//连接usb设备
            if (mUsbDeviceConnection == null) {
                Toast.makeText(UsbtActivity.this, "mUsbDeviceConnection can't be null", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (mUsbDeviceConnection.claimInterface(anInterface, true)) {
                Toast.makeText(UsbtActivity.this, "找到USB接口", Toast.LENGTH_SHORT).show();
                int endpointCount = anInterface.getEndpointCount();
                for (int i = 0; i < endpointCount; i++) {
                    UsbEndpoint endpoint = anInterface.getEndpoint(i);
                    if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        if (UsbConstants.USB_DIR_IN == endpoint.getDirection()) {
                            mUsbEndpoint_in = endpoint;//获取读数据通道
                        } else {
                            mUsbEndpoint_out = endpoint;//获取写数据通道
                        }
                    }
                }
                return true;
            } else {
                mUsbDeviceConnection.close();//关闭连接
                Toast.makeText(UsbtActivity.this, "找不到USB接口", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(UsbtActivity.this, "mUsbDevice can't be null", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * 从usb通信设备中读取数据
     * @return
     */
    public byte[] readData() {
        int inMax = mUsbEndpoint_in.getMaxPacketSize();
        byte[] bytes = new byte[inMax];
        ByteBuffer byteBuffer = ByteBuffer.allocate(inMax);
        UsbRequest usbRequest = new UsbRequest();
        usbRequest.initialize(mUsbDeviceConnection, mUsbEndpoint_in);
        usbRequest.queue(byteBuffer, inMax);
        if (mUsbDeviceConnection.requestWait() == usbRequest) {
            bytes = byteBuffer.array();
        }
        return bytes;
    }

    /**
     * 将数据写入到usb设备中
     * @param bytes
     */
    public void sendData(byte[] bytes) {
        if (mUsbDeviceConnection == null) {
            Toast.makeText(UsbtActivity.this, "mUsbDeviceConnection can't be null", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mUsbEndpoint_out == null) {
            Toast.makeText(UsbtActivity.this, "mUsbEndpoint_out can't be null", Toast.LENGTH_SHORT).show();
            return;
        }

        int i = mUsbDeviceConnection.bulkTransfer(mUsbEndpoint_out, bytes, bytes.length, 1000);
        if (i < 0) {
            Toast.makeText(UsbtActivity.this, "failure to write", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(UsbtActivity.this, "success to write", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 权限广播接收器
     */
    private BroadcastReceiver mMyBroadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action_usb_permission.equals(action)) {
                synchronized (this) {
                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (usbDevice != null) {
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            connect(usbDevice);
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
     * @param name
     * @param usbDevice
     * @return
     */
    public boolean filterDevice(String name, UsbDevice usbDevice) {
        // TODO: 2019/3/13 对应判断设备是否是我们要连接的设备
       if ((usbDevice.getVendorId()==1423) && (usbDevice.getProductId()==25479))
           return  true;
        return false;
    }






}
