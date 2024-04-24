package com.physmo.mappers;


import com.physmo.NESCart;
import com.physmo.OutValue;
import com.physmo.Rig;

public class Mapper003 implements Mapper {

    Rig rig;
    NESCart nesCart;
    int bankSelect = 0;

    @Override
    public void attachRig(Rig rig, NESCart nesCart) {
        this.rig = rig;
        this.nesCart = nesCart;
    }

    @Override
    public boolean cpuRead(int addr, OutValue outValue) {
        if (addr >= 0x8000 && addr <= 0xFFFF) {
            int a = addr;
            if (nesCart.prgRomChunks == 1) a &= 0x3FFF;
            if (nesCart.prgRomChunks == 2) a &= 0x7FFF;
            outValue.value = nesCart.data[nesCart.prgRomOffset + a];
            return true;
        }

        return false;
    }

    @Override
    public boolean cpuWrite(int addr, int val) {
        if (addr >= 0x8000 && addr <= 0xFFFF) {
            bankSelect = val & 0b11;
            //System.out.println("setting bank to " + bankSelect);
            return true;
        }
        return false;
    }

    @Override
    public boolean ppuRead(int addr, OutValue outValue) {

        if (addr <= 0x1FFF) {
            outValue.value = nesCart.data[nesCart.chrRomOffset + addr + (bankSelect * 0x2000)];
            return true;
        }


        return false;
    }

    @Override
    public boolean ppuWrite(int addr, int val) {

        return false;
    }

}
