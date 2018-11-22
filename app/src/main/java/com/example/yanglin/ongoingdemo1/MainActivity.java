package com.example.yanglin.ongoingdemo1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.example.yanglin.ongoingdemo1.Util.AMapUtil;
import com.example.yanglin.ongoingdemo1.Util.ToastUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
/**
 * Created by yanglin on 2018/10/31.
 */
public class MainActivity  extends AppCompatActivity implements AMap.OnMapClickListener,
        AMap.OnMarkerClickListener, RouteSearch.OnRouteSearchListener{
    private AMap aMap;   //定义AMap 地图对象的操作方法与接口
    private MapView mapView;//显示地图的视图，它负责从服务器端获取地图数据，它将会捕捉屏幕触控手势
    private Context mContext; //抽象类
    private RouteSearch mRouteSearch; //该类路径规划搜索入口，定义此类开始路径规划搜索
    private DriveRouteResult mDriveRouteResult;  //驾车路径规划结果集
    LatLonPoint mStartPoint =new LatLonPoint(39.942295, 116.335891);// new LatLonPoint(108.925302, 34.233669) ; //起点，116.335891,39.942295  几何点对象类  = new LatLonPoint(39.942295, 116.335891)
     LatLonPoint mEndPoint =new LatLonPoint(39.995576, 116.481288);// new LatLonPoint(108.953196, 34.229055)  ;//终点，116.481288,39.995576   = new LatLonPoint(39.995576, 116.481288)
    LatLonPoint GetStartPoint = null;
    LatLonPoint GetEndPoint=null;
    private final int ROUTE_TYPE_DRIVE = 2;
   EditText startText,endText;
//定义角色选择按钮
    private Button btn_server;
    private Button btn_client;
  GeocodeSearch geocoderSearchStart,geocoderSearchEnd ;

    File sdcard= Environment.getExternalStorageDirectory();
//    DrivePath    drivePath;
//
//    public void setDrivePath(DrivePath drivePath) {
//        this.drivePath = drivePath;
//    }
//
//    public DrivePath getDrivePath() {
//        return drivePath;
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this.getApplicationContext();//获得整个应用的上下文生命周期
        mapView = (MapView) findViewById(R.id.route_map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        init(); //初始化
        setfromandtoMarker(); //进行路径规划
        select_role();  //选择角色
    }
    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        registerListener();
        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);
    }
    /**
     * 注册监听
     */
    private void registerListener() {
        aMap.setOnMapClickListener(this); //设置地图点击事件监听接口
        aMap.setOnMarkerClickListener(this);//设置marker点击事件监听接口
    }
    //查询起点经纬度
    public LatLonPoint GetValueStart() {
        startText = (EditText) findViewById(R.id.et_start);
        String startStr = startText.getText().toString();
        geocoderSearchStart = new GeocodeSearch(this);
        geocoderSearchStart.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

            }
            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
                GeocodeAddress addr = geocodeResult.getGeocodeAddressList().get(0);
                GetStartPoint = addr.getLatLonPoint();
            }
        });
        GeocodeQuery query = new GeocodeQuery(startStr, " ");
        geocoderSearchStart.getFromLocationNameAsyn(query);
    return GetStartPoint;
    }
    //查询目的地
    public LatLonPoint GetValueEnd(){
        endText = (EditText) findViewById(R.id.et_end);
        String  endStr = endText.getText().toString();
        geocoderSearchEnd = new GeocodeSearch(this);
        geocoderSearchEnd.setOnGeocodeSearchListener( new GeocodeSearch.OnGeocodeSearchListener(){
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
            }
            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
                GeocodeAddress addr = geocodeResult.getGeocodeAddressList().get(0);
                GetEndPoint = addr.getLatLonPoint();
            }
        });
        GeocodeQuery query1 = new GeocodeQuery(endStr,"");
        geocoderSearchEnd.getFromLocationNameAsyn(query1);
         return GetEndPoint;
    }
    public void onDriveClick(View view) {
        searchRouteResult(ROUTE_TYPE_DRIVE, RouteSearch.DRIVING_SINGLE_DEFAULT);
        mapView.setVisibility(View.VISIBLE);
    }
    private void setfromandtoMarker() {
        aMap.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(mStartPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.start)));
        aMap.addMarker(new MarkerOptions()
                .position(AMapUtil.convertToLatLng(mEndPoint))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.end)));
    }
    /**
     * 开始搜索路径规划方案
     */
    public void searchRouteResult(int routeType, int mode) {
       mStartPoint= GetValueStart();
       mEndPoint = GetValueEnd();
        if (mStartPoint == null) {
            ToastUtil.show(mContext, "起点未设置");
            return;
        }
        if (mEndPoint == null) {
            ToastUtil.show(mContext, "终点未设置");
        }
     final  RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(mStartPoint, mEndPoint);
       if (routeType == ROUTE_TYPE_DRIVE) {// 驾车路径规划
            RouteSearch.DriveRouteQuery query = new RouteSearch.DriveRouteQuery(fromAndTo, mode, null,
                    null, "");// 第一个参数表示路径规划的起点和终点，第二个参数表示驾车模式，第三个参数表示途经点，第四个参数表示避让区域，第五个参数表示避让道路
             mRouteSearch.calculateDriveRouteAsyn(query);// 异步路径规划驾车模式查询
        }
    }
    @Override
    public void onMapClick(LatLng latLng) {

    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

 @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int errorCode) {
     aMap.clear();// 清理地图上的所有覆盖物
     if (errorCode == 1000) {
         if (driveRouteResult != null && driveRouteResult.getPaths() != null) {
             if (driveRouteResult.getPaths().size() > 0) {
                 mDriveRouteResult = driveRouteResult;
          DrivePath      drivePath = mDriveRouteResult.getPaths()
                         .get(0);
                 float sfloat = drivePath.getDistance();
                 Log.d("路径的长度",String.valueOf(sfloat));

                 DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
                         mContext, aMap, drivePath,
                         mDriveRouteResult.getStartPos(),
                         mDriveRouteResult.getTargetPos(), null);
                     drivingRouteOverlay.setNodeIconVisibility(false);//设置节点marker是否显示
                   drivingRouteOverlay.setIsColorfulline(true);//是否用颜色展示交通拥堵情况，默认true
                 drivingRouteOverlay.removeFromMap();//去掉DriveLineOverlay上的线段和标记
                 drivingRouteOverlay.addToMap();//添加驾车路线添加到地图上显示。
                 drivingRouteOverlay.zoomToSpan();//移动镜头到当前的视角

                WriteIntoFile(drivePath);
             }
         }
     }
 }
       public void WriteIntoFile(DrivePath drivePath){
           File myfile = new File(sdcard,"onefile.txt");
           if(!sdcard.exists()){
                 Toast.makeText(getApplicationContext(),"当前系统不具备SD卡目录",Toast.LENGTH_LONG).show();
               return;
           }try {
                  Toast.makeText(getApplicationContext(),"文件已经建成",Toast.LENGTH_LONG).show();
               FileOutputStream fos = new FileOutputStream(myfile);
               OutputStreamWriter osw = new OutputStreamWriter(fos);
               List<DriveStep> steps=drivePath.getSteps();
               for( DriveStep step:steps){
                   List<LatLonPoint> points = step.getPolyline();
                   for(LatLonPoint point:points){
                       double lat = point.getLatitude();
                       double lng = point.getLongitude();
                       String s = String.valueOf(lat)+" "+String.valueOf(lng);
                       System.out.println(s);
                       osw.write(s+"\r\n");
                   }
               }
               osw.flush();
               osw.close();
               fos.close();
                Toast.makeText(getApplicationContext(),"文件已经写入完成",Toast.LENGTH_LONG).show();
           }catch (IOException e){
               e.printStackTrace();
           }
       }
    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


    //定义select_role（）
public void select_role(){
    btn_server=(Button)findViewById ( R.id.btn_server );
    btn_client=(Button)findViewById ( R.id.btn_client );
    btn_server.setOnClickListener ( new View.OnClickListener ( )
    {
        @Override
        public void onClick(View v)
        {
            startActivity ( new Intent( MainActivity.this, receiveFile.class ) );
        }
    } );
    btn_client.setOnClickListener ( new View.OnClickListener ( )
    {
        @Override
        public void onClick(View v)
        {
            startActivity ( new Intent ( MainActivity.this,sendFile.class ) );
        }
    } );
}



}
