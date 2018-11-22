package com.example.yanglin.ongoingdemo1;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class receiveFile extends AppCompatActivity{
    private TextView txt;
    private Button btn;
    private EditText edit_server;
    private EditText edit_ip;
    private Button server_OK;
    private LinearLayout mLinearLayout_port;
    private LinearLayout mLinearLayout_ip;

    /**启动服务端端口
     * 服务端IP为手机IP
     * */
    //传感器样式
    Button btn_start,btn_stop;
    private int pite;
    String s;  //获取线程内的数据
    private ReceiveHandler receiveHandler = new ReceiveHandler();
    final int countSize = 12800;
    int count=0;
    //存放修正后的数据
    String mfileName = "update.txt" ;

    File sdcard= Environment.getExternalStorageDirectory();
    int i=0;
    //设置LOG标签
    private static final String TAG = "sensor";
    private SensorManager sm;
    private Sensor accelerometer; // 加速度传感器
    private Sensor magnetic; // 地磁场传感器
    private Sensor Gry;//线性加速度传感器

    //用于之后计算方向
    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];
    //存放旋转矩阵
    private float[] mRotationMatrix = new float[9];

    boolean tag_acc;   //标志位 tag_acc标志产生了加速度
    boolean tag_g;  // tag_g标志产生了新的磁场数据
    boolean tag_Gry; // tag_lineAcc标志产生了新的线性加速度数据

    //定义x y z 存放当前实际线性加速度数据
    float X_Gry;
    float Y_Gry;
    float Z_Gry;
    class ReceiveHandler extends Handler {
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            txt.setText("收到文件");
            Toast.makeText(receiveFile.this, "我是LV，我接收到文件", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_udp);
        txt = (TextView) findViewById ( R.id.textView );
        btn = (Button) findViewById ( R.id.btn );
        edit_server=(EditText)findViewById ( R.id.editText_server );
        edit_ip=(EditText)findViewById ( R.id.client_ip );
        server_OK=(Button)findViewById ( R.id.server_OK );
        //样式
        mLinearLayout_port=(LinearLayout)findViewById ( R.id.lin_1 ) ;
        mLinearLayout_ip=(LinearLayout)findViewById ( R.id.lin_ip ) ;
        //隐去以下按钮
        btn.setVisibility(View.GONE);
        mLinearLayout_ip.setVisibility ( View.GONE );

        server_OK.setOnClickListener ( new View.OnClickListener ( )
        {@Override
            public void onClick(View v)
            {
                new receiveThread().start();
            }
        } );

      initView();
    }

    public class receiveThread extends Thread {
        @Override
        public void run() {

            receive();

        }
    }
    public void receive(){

        pite= Integer.parseInt(edit_server.getText ().toString ());
        DatagramSocket ds=null;
        DatagramPacket dp=null,messagepkg=null;
        FileOutputStream fos=null;
        try {
              ds= new DatagramSocket(pite);
            byte[] by = new byte[1024];
            fos = new FileOutputStream((new File(sdcard, "twofile.txt")));
             dp = new DatagramPacket(by,by.length);
            System.out.println("等待接受数据");
            byte[] messagebuf= new byte[1024];  //反馈信息
            messagebuf="ok".getBytes();

            while(true) {
                ds.receive(dp);
                if(new String(dp.getData(),0,dp.getLength()).equals("end")) {
                   System.out.print("文件接收完毕");

                    messagepkg= new DatagramPacket(messagebuf,messagebuf.length, InetAddress.getByName(dp.getAddress().getHostAddress()),dp.getPort() );
                     ds.send(messagepkg);

                    receiveHandler.sendEmptyMessage(1); //通知本机接受完毕
              }
                fos.write(dp.getData(), 0, dp.getLength());
                fos.flush();
                String str= new String(dp.getData(),0,dp.getLength());
                System.out.println(str+"--->"+dp.getAddress().getHostAddress()
                        +":"+dp.getPort());
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                fos.close();
                ds.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }


    public  void initView(){
        //传感器的样式,当 反馈数值为..
        btn_start =(Button)findViewById(R.id.btn_start);
        btn_stop=(Button)findViewById(R.id.btn_stop) ;

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                begin();
            }
        });
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });
    }

    public void begin(){
        Toast.makeText(receiveFile.this,"start...",Toast.LENGTH_SHORT).show();//提示开始记录
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // 初始化加速度传感器
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // 初始化地磁场传感器
        magnetic = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        //初始化线性加速度传感器
        Gry = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        //初始化标志位
        tag_acc = false;
        tag_g = false;
        tag_Gry = false;

        //20Hz=50000,50Hz=20000 100Hz=10000
        //注册线性加速度传感器
        sm.registerListener(GryListener, Gry, 10000);
        //注册磁场传感器
        sm.registerListener(GryListener,magnetic,10000);
        //注册加速度传感器
        sm.registerListener(GryListener,accelerometer,10000);

    }
    final SensorEventListener GryListener = new SensorEventListener(){
        //复写onSensorChanged方法
        public void onSensorChanged(SensorEvent sensorEvent){

            //三组数据都有，开始通过旋转矩阵修正线性加速度数据
            if (tag_Gry && tag_g && tag_acc)
            {
                //根据磁场数据（磁场传感器）和加速度数据（加速度传感器）计算旋转矩阵
                calculateRotationMatrix();
                float f[] = {X_Gry, Y_Gry, Z_Gry};

                File file = new File(sdcard,mfileName);

                UpdateRealDate(f);  //通过旋转矩阵，修正实际加速度数据
                try {

                    FileOutputStream out = new FileOutputStream(file,true);

                    OutputStreamWriter osw = new OutputStreamWriter(out);

                    i=i+1;
                    String  str = String.valueOf(i)+" "+String.valueOf(f[2]);
                    osw.write(str+"\r\n");

                    osw.flush();
                    osw.close();
                    out.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }

//                count++;
//                if (count == countSize) {
//                    sm.unregisterListener(GryListener);
//
//                    Toast.makeText(receiveFile.this, "时间到，已保存文件！.", Toast.LENGTH_SHORT).show(); //提示停止记录
//                    tag_Gry = false;
//                    tag_acc = false;
//                    tag_g = false;
//                }

                tag_Gry = false;
                tag_acc = false;
                tag_g = false;
            }
            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) //加速度
            {
                accelerometerValues = sensorEvent.values;
                tag_acc = true;
            }
            if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) //磁场
            {
                magneticFieldValues = sensorEvent.values;
                tag_g = true;
            }
            if(sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) //陀螺仪
            {
                Log.i(TAG, "onSensorChanged");
                float X_lateral = sensorEvent.values[0];
                float Y_longitudinal = sensorEvent.values[1];
                float Z_vertical = sensorEvent.values[2];
                X_Gry = X_lateral;
                Y_Gry = Y_longitudinal;
                Z_Gry = Z_vertical;
                tag_Gry = true;
            }
        }
        //复写onAccuracyChanged方法
        public void onAccuracyChanged(Sensor sensor , int accuracy){
            Log.i(TAG, "onAccuracyChanged");
        }
    };

    //计算旋转矩阵
    private void calculateRotationMatrix() {
        SensorManager.getRotationMatrix(mRotationMatrix, null, accelerometerValues,
                magneticFieldValues);
    }
    //通过旋转矩阵，修正实际陀螺仪数据
    private void UpdateRealDate(float [] f) {
        f[0] = mRotationMatrix[0]*f[0]+mRotationMatrix[1]*f[1]+mRotationMatrix[2]*f[2];
        f[1] = mRotationMatrix[3]*f[0]+mRotationMatrix[4]*f[1]+mRotationMatrix[5]*f[2];
        f[2] = mRotationMatrix[6]*f[0]+mRotationMatrix[7]*f[1]+mRotationMatrix[8]*f[2];
    }
    public void stop(){
        sm.unregisterListener(GryListener);
        tag_Gry = false;
        tag_acc = false;
        tag_g = false;
       Toast.makeText(receiveFile.this,"stop！.",Toast.LENGTH_SHORT).show();
    }



}
