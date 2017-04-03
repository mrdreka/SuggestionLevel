package com.example.speciale.suggestion;

/**
 * Created by viiis on 21-03-2017.
 */

public class AccObj {
    private double x, y, z;

    public AccObj(double x, double y, double z) {

        this.x = x;
        this.y = y;
        this.z = z;

    }
    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }

    public double getZ(){
        return z;
    }


    public String toString() {
        return "x:   " +getX()+ " " + getY() + " " + getZ();
    }

}
