package com.physmo.mappers;


import com.physmo.NESCart;
import com.physmo.OutValue;
import com.physmo.Rig;

import java.net.SocketTimeoutException;

public class Mapper001 implements Mapper {

    Rig rig;
    NESCart nesCart;
    int bankSelect = 0;
    int shiftRegister = 0;
    int shiftCount = 0;
    int control = 0x1c;
    int chrBank0 = 0;
    int chrBank1 = 0;
    int prgBank = 0;

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
            outValue.value = nesCart.data[nesCart.prgRomOffset + ((prgBank&0b1111)*0x2000) + a];
            return true;
        }

        return false;
    }

    @Override
    public boolean cpuWrite(int addr, int val) {
        if (addr >= 0x8000 && addr <= 0xFFFF) {

            // detect clear
            if ((val & 0b1000_0000) > 0) {
                shiftRegister = 0;
                shiftCount = 0;
            } else {
                if ((val & 1) > 0) {
                    shiftRegister |= 0b100000;
                    shiftRegister >>= 1;
                    shiftCount++;
                    if (shiftCount == 5) {
                        setRegister(addr, shiftRegister);
                        //bankSelect = shiftRegister;
                        shiftCount = 0;
                        shiftRegister = 0;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public void setRegister(int address, int val) {
        if (address >= 0x8000 && address <= 0x9fff) {
            System.out.println("Setting control to "+val);
            control = val;
        }
        if (address >= 0xA000 && address <= 0xBFFF) {
            System.out.println("Setting chrBank0 to "+val);
            chrBank0 = val;
        }
        if (address >= 0xC000 && address <= 0xDFFF) {
            System.out.println("Setting chrBank1 to "+val);
            chrBank1 = val;
        }
        if (address >= 0xE000 && address <= 0xFFFF) {
            System.out.println("Setting prgBank to "+val);
            prgBank = val;
        }
    }


    @Override
    public boolean ppuRead(int addr, OutValue outValue) {

        if (addr <= 0x0FFF) {
            outValue.value = nesCart.data[nesCart.chrRomOffset + (addr&0x0FFF) + (chrBank0 * 0x1000)];
            return true;
        } else if (addr <= 0x1FFF) {
            outValue.value = nesCart.data[nesCart.chrRomOffset + (addr&0x0FFF) + (chrBank1 * 0x1000)];
            return true;
        }

        return false;
    }

    @Override
    public boolean ppuWrite(int addr, int val) {

        return false;
    }

}
