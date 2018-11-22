package com.example.yanglin.ongoingdemo1;

import com.mathworks.toolbox.javabuilder.MWNumericArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import FrechetLib.FrechetClass;

/**
 * Created by yanglin on 2018/11/21.
 */
public  class Compare {

//    public static void main(String[] args){
//        Compare c = new Compare();
//       double d = c.cmp();
//        System.out.print("输出结果是："+d);
//    }

    public double cmp(File f1,File f2) //传入文件地址
    {
        Compare c= new Compare();
        double d = 0;
        List<String> list = new ArrayList<String>();
        List<String> listc = new ArrayList<String>();
        String str[] = c.createList(list,f1);
        String strc[] = c.createList(listc,f2);
        Calendar timer = Calendar.getInstance();
        System.out.println("开始时间"+timer.get(Calendar.HOUR_OF_DAY)+":"+timer.get(Calendar.MINUTE)+":"+
                timer.get(Calendar.SECOND)+":"+timer.get(Calendar.MILLISECOND));
        try {
            FrechetClass f = new FrechetClass();
            Object[] result = f.FrechetDist(1,str,strc);
            MWNumericArray res = (MWNumericArray)result[0];
            d = res.getDouble(1);
            System.out.println("转化后的数据"+d);

        }catch (Exception e){
            e.printStackTrace();
        }
        Calendar timer1 = Calendar.getInstance();
        System.out.println("结束时间"+timer1.get(Calendar.HOUR_OF_DAY)+":"+timer1.get(Calendar.MINUTE)+":"+
                timer1.get(Calendar.SECOND)+":"+timer1.get(Calendar.MILLISECOND));
        return d;
    }

    public String[]  createList(List<String> list,File f) {
        String line = null;
        String[] str=null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            while ((line = br.readLine()) != null) {
                list.add(line);
                str = list.toArray(new String[0]);

                for (int i = 0; i < str.length; i++) {
                    System.out.println(str[i]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }


}
