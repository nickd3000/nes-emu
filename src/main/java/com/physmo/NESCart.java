package com.physmo;

import com.physmo.mappers.Mapper;
import com.physmo.mappers.Mapper000;
import com.physmo.mappers.Mapper001;
import com.physmo.mappers.Mapper002;
import com.physmo.mappers.Mapper003;
import com.physmo.mappers.Mapper004;

public class NESCart {

    public static int KB8 = 1024*8;
    public static int KB16 = 1024*16;

    public int[] data;

    public String fileIdentifier;
    public int prgRomSize;
    public int chrRomSize;
    public int prgRomChunks;
    public int chrRomChunks;
    public int prgRomOffset;
    public int chrRomOffset;
    public int flags6;
    public int flags7;
    public int flags8;
    public int flags9;
    public int flags10;
    public int mapperNumber;
    public boolean NES2Format = false;
    public int mirrorMode = 0; // 0 = H, 1 = V

    Mapper mapper;

    public void addData(int[] data) {
        this.data = data;
        interpretHeader();

        initMapper(mapperNumber);
    }

    private void initMapper(int id) {
        if (id==0) {
            mapper = new Mapper000();
        }
        if (id==1) {
            mapper = new Mapper001();
        }
        if (id==2) {
            mapper = new Mapper002();
        }
        if (id==3) {
            mapper = new Mapper003();
        }
        if (id==4) {
            mapper = new Mapper004();
        }
    }

    public void interpretHeader() {

        fileIdentifier = ""+(char)data[0]+(char)data[1]+(char)data[2]+(char)data[3];
        prgRomChunks = data[4];
        chrRomChunks = data[5];
        prgRomSize = data[4] * KB16;
        chrRomSize = data[5] * KB8;
        flags6 = data[6];
        flags7 = data[7];
        flags8 = data[8];
        flags9 = data[9];
        flags10 = data[10];

        prgRomOffset = 16;
        chrRomOffset = prgRomOffset+prgRomSize;

        // Detect mirroring mode. 0 = H, 1 = V
        if ((flags6&0x01)>0) mirrorMode=1;
        else mirrorMode=0;

        if ((flags6&0x04)>0) System.out.println("PADDING?");

        // Detect NES2 format file.
        if ((flags7&0x0c)==0x08) NES2Format=true;

        mapperNumber = (flags7&0xF0)|((flags6&0xF0)>>4);

        System.out.println("fileIdentifier="+fileIdentifier);
        System.out.println("NES2Format="+NES2Format);
        System.out.println("mapperNumber="+mapperNumber);


        System.out.println("prgRomSize="+prgRomSize);
        System.out.println("chrRomSize="+chrRomSize);

        System.out.println("prgRomChunks="+prgRomChunks);
        System.out.println("chrRomChunks="+chrRomChunks);

        System.out.println("flags6="+Utils.toBinary(flags6)+" "+((((flags6&0x01)>0))?"VERTICAL":"HORIZONTAL"));
        System.out.println("flags7="+Utils.toBinary(flags7));
        System.out.println("flags8="+Utils.toBinary(flags8));
        System.out.println("flags9="+Utils.toBinary(flags9));
        System.out.println("flags10="+Utils.toBinary(flags10));


        System.out.println("File size " + data.length);

        System.out.println("First few bytes of PRG...");
        for (int i=0;i<20;i++) {
            System.out.println(""+i+" - 0x"+Utils.toHex2(data[prgRomOffset+i]));
        }

    }


}
