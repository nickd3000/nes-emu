package com.physmo;

public class OAM {
    public int yPos;
    public int xPos;
    public int tileIndex;
    public int attributes;

    public OAM() {
        yPos=0;
        xPos=0;
        tileIndex=0;
        attributes=0;
    }

    public boolean isFlippedH() {
        if ((attributes&0b01000000)>0) return true;
        return false;
    }


    public boolean isFlippedV() {
        if ((attributes&0b10000000)>0) return true;
        return false;
    }

    public int getPalette() {
        return (attributes&0b11)+4;
    }

}
