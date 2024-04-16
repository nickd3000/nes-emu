package com.physmo.mappers;


import com.physmo.NESCart;
import com.physmo.OutValue;
import com.physmo.Rig;

import static com.physmo.NESCart.KB16;
import static com.physmo.NESCart.KB8;

public class Mapper003 implements Mapper {

    Rig rig;
    NESCart nesCart;
    int bankSelect =0;

    @Override
    public void attachRig(Rig rig, NESCart nesCart) {
        this.rig = rig;
        this.nesCart = nesCart;
    }

    @Override
    public boolean cpuRead(int addr, OutValue outValue) {
        if (addr>=0x8000 && addr<=0xBFFF) {
            outValue.value = nesCart.data[nesCart.prgRomOffset+(addr-0x8000)];
            return true;
        }
        if (addr>=0xC000 && addr<=0xFFFF) {
            outValue.value = nesCart.data[nesCart.prgRomOffset+(addr-0xC000)];
            return true;
        }

        return false;
    }

    @Override
    public boolean cpuWrite(int addr, int val) {
        if (addr>=0x8000 && addr<=0xFFFF) {
            bankSelect = val &0b0011;
            return true;
        }
        return false;
    }

    @Override
    public boolean ppuRead(int addr, OutValue outValue) {

        if (addr<0x1FFF) {
            outValue.value = nesCart.data[nesCart.chrRomOffset+addr+(bankSelect*0x2000)];
            return true;
        }


        return false;
    }

    @Override
    public boolean ppuWrite(int addr, int val) {

        return false;
    }

}
