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
        return (attributes & 0b01000000) > 0;
    }


    public boolean isFlippedV() {
        return (attributes & 0b10000000) > 0;
    }

    public int getPalette() {
        return (attributes&0b11)+4;
    }

    @Override
    public String toString() {
        return "x:"+xPos+" y:"+yPos+" index:"+tileIndex;
    }
}
