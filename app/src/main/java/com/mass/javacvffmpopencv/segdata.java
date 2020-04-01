package com.mass.javacvffmpopencv;

import android.os.Parcel;
import android.os.Parcelable;

public class segdata implements Parcelable {
    public Integer getSa() {
        return Sa;
    }

    @Override
    public String toString() {
        return "分析结论-->首帧精子个数："+Bc+
                "，尾帧精子个数："+Lc+"，活跃个数=" +Sa+
                "，不活跃个数=" +Sb+"，不活动个数=" +Sc+
                ",浓度="+Math.round(Md*100)/100+"（百万/毫升）";
    }

    public void setSa(Integer sa) {
        Sa = sa;
    }

    public Integer getSb() {
        return Sb;
    }

    public void setSb(Integer sb) {
        Sb = sb;
    }

    public Integer getSc() {
        return Sc;
    }

    public void setSc(Integer sc) {
        Sc = sc;
    }

    public Integer getBc() {
        return Bc;
    }

    public void setBc(Integer bc) {
        Bc = bc;
    }

    public Integer getLc() {
        return Lc;
    }

    public void setLc(Integer lc) {
        Lc = lc;
    }

    private Integer Sa;   //活跃数
    private Integer Sb;   //不活跃数
    private Integer Sc;       //不活动数
    private Integer Bc;      //开始个数
    private Integer Lc;      //结束个数
    private Float   Md;    //浓度

    public Float getMd() {
        return Md;
    }

    public void setMd(Float md) {
        Md = md;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(Sa);
        dest.writeInt(Sb);
        dest.writeInt(Sc);
        dest.writeInt(Bc);
        dest.writeInt(Lc);
        dest.writeFloat(Md);
    }


    public static final Parcelable.Creator<segdata> CREATOR=
            new  Parcelable.Creator<segdata>(){

                @Override
                public segdata createFromParcel(Parcel source) {
                    segdata d=new segdata();
                    d.Sa=source.readInt();
                    d.Sb=source.readInt();
                    d.Sc=source.readInt();
                    d.Bc=source.readInt();
                    d.Lc=source.readInt();
                    d.Md=source.readFloat();
                    return d;
                }

                @Override
                public segdata[] newArray(int size) {
                    return new segdata[size];
                }
            };
}
