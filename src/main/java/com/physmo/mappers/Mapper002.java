package com.physmo.mappers;


import com.physmo.NESCart;
import com.physmo.OutValue;
import com.physmo.Rig;

public class Mapper002 implements Mapper {

    Rig rig;
    NESCart nesCart;
    int bankSelectLow = 0;
    int bankSelectHigh = 0;
    int [] charMem = new int[20000]; // hack

    @Override
    public void attachRig(Rig rig, NESCart nesCart) {
        this.rig = rig;
        this.nesCart = nesCart;
    }

    @Override
    public boolean cpuRead(int addr, OutValue outValue) {
        bankSelectHigh = nesCart.prgRomChunks - 1; // High always points to last bank.

        if (addr >= 0x8000 && addr <= 0xBFFF) {
            outValue.value = nesCart.data[nesCart.prgRomOffset + (bankSelectLow * 0x4000) + (addr & 0x3FFF)];
            return true;
        }
        if (addr >= 0xC000) {
            int a = (bankSelectHigh * 0x4000) + (addr & 0x3FFF);
            outValue.value = nesCart.data[nesCart.prgRomOffset + (bankSelectHigh * 0x4000) + (addr & 0x3FFF)];
            return true;
        }


        return false;
    }

    @Override
    public boolean cpuWrite(int addr, int val) {
        if (addr >= 0x8000) {
            bankSelectLow = val & 0x0F;
            //System.out.println("Bank select: "+bankSelectLow +"  "+val);
            return true;
        }
        return false;
    }

    @Override
    public boolean ppuRead(int addr, OutValue outValue) {

        if (addr <= 0x1FFF) {
            outValue.value = charMem[ addr]&0xFF;
            return true;
        }


        return false;
    }

    @Override
    public boolean ppuWrite(int addr, int val) {
        if (addr <= 0x1FFF) {
            charMem[ addr] = val;
            return true;
        }
        return false;
    }

}
