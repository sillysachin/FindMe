package com.appbootup.findme;

/**
 * Created by sachinsr on 19/12/14.
 */
public class OpenCell {
    private double lon;
    private double lat;
    private int mcc;
    private int mnc;
    private int lac;
    private int cellid;
    private int averageSignalStrength;
    private int range;
    private int samples;
    private boolean changeable;
    private String radio;
    private int sid;
    private int nid;
    private int bid;

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public int getMcc() {
        return mcc;
    }

    public void setMcc(int mcc) {
        this.mcc = mcc;
    }

    public int getMnc() {
        return mnc;
    }

    public void setMnc(int mnc) {
        this.mnc = mnc;
    }

    public int getLac() {
        return lac;
    }

    public void setLac(int lac) {
        this.lac = lac;
    }

    public int getCellid() {
        return cellid;
    }

    public void setCellid(int cellid) {
        this.cellid = cellid;
    }

    public int getAverageSignalStrength() {
        return averageSignalStrength;
    }

    public void setAverageSignalStrength(int averageSignalStrength) {
        this.averageSignalStrength = averageSignalStrength;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public int getSamples() {
        return samples;
    }

    public void setSamples(int samples) {
        this.samples = samples;
    }

    public boolean isChangeable() {
        return changeable;
    }

    public void setChangeable(boolean changeable) {
        this.changeable = changeable;
    }

    public String getRadio() {
        return radio;
    }

    public void setRadio(String radio) {
        this.radio = radio;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public int getNid() {
        return nid;
    }

    public void setNid(int nid) {
        this.nid = nid;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }
}
