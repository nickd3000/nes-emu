package com.physmo.mappers;


import com.physmo.NESCart;
import com.physmo.OutValue;
import com.physmo.Rig;

public class Mapper001 implements Mapper {

    Rig rig;
    NESCart nesCart;
    int bankSelect = 0;
    int shiftRegister = 0;
    int shiftCount = 0;
    int control = 0x1c;

    int[] cartRam;
    int bankSelectLow16k = 0;
    int bankSelectHigh16k = 0;
    int bankSelect32k = 0;
    int mirrorMode = 0;
    int charBankSelectLow4k = 0;
    int charBankSelectHigh4k = 0;
    int charBankSelect8k = 0;

    public Mapper001() {
        cartRam = new int[32768];
    }

    @Override
    public void attachRig(Rig rig, NESCart nesCart) {
        this.rig = rig;
        this.nesCart = nesCart;
        bankSelectHigh16k = nesCart.prgRomChunks - 1;
    }

    @Override
    public boolean cpuRead(int addr, OutValue outValue) {

        if (addr >= 0x6000 && addr <= 0x7FFF) {
            outValue.value = cartRam[addr & 0x1FFF] & 0xFF;
            return true;
        }

        if (addr >= 0x8000) {
            if ((control & 0b01000) > 0) {
                if (addr <= 0xBFFF) {
                    outValue.value = nesCart.data[nesCart.prgRomOffset + (bankSelectLow16k * 0x4000) + (addr & 0x3FFF)];
                    return true;
                } else {
                    outValue.value = nesCart.data[nesCart.prgRomOffset + (bankSelectHigh16k * 0x4000) + (addr & 0x3FFF)];
                    return true;
                }
            } else {
                outValue.value = nesCart.data[nesCart.prgRomOffset + (bankSelect32k * 0x8000) + (addr & 0x7FFF)];
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean cpuWrite(int addr, int val) {

        if (addr >= 0x6000 && addr <= 0x7FFF) {
            cartRam[addr & 0x1FFF] = val & 0xFF;
            return true;
        }

        if (addr >= 0x8000 && addr <= 0xFFFF) {

            // detect clear
            if ((val & 0b1000_0000) > 0) {
                shiftRegister = 0;
                shiftCount = 0;
                control = control | 0x0C;
            } else {

                shiftRegister >>= 1;
                shiftRegister |= ((val & 0x01) << 4);
                shiftCount = (shiftCount + 1) & 0xFF;

                if (shiftCount == 5) {
                    int targetRegister = (addr >> 13) & 0x03;

                    setRegister(targetRegister);
                    //bankSelect = shiftRegister;
                    shiftCount = 0;
                    shiftRegister = 0;
                }

            }
            return true;
        }
        return false;
    }

    public void setRegister(int address) {

        if (address == 0) {
            control = shiftRegister & 0x1F;
            switch (control & 0x03) {
                case 0 -> mirrorMode = 3;
                case 1 -> mirrorMode = 2;
                case 2 -> mirrorMode = 1;
                case 3 -> mirrorMode = 0;
            }
            nesCart.mirrorMode=mirrorMode;
        } else if (address == 1) { // 0xA000 - 0xBFFF
            if ((control & 0b10000) == 0b10000) //We change the lower half of the CHR Memory range
                charBankSelectLow4k = shiftRegister & 0x1F;
            else //We change the entire CHR Memory range
                charBankSelect8k = (shiftRegister & 0x1E) >> 1;
        } else if (address == 2) { // 0xC000 - 0xDFFF
            if ((control & 0b10000) == 0b10000) //We change the lower half of the CHR Memory range
                charBankSelectHigh4k = shiftRegister & 0x1F;
        } else { // 0xE000 - 0xFFFF
            //We extract the PRG Mode (2 16K Banks or 1 32K Bank
            int prgMode = (control >> 2) & 0x03;
            if (prgMode == 0 || prgMode == 1) { //32K mode, the selected bank is represented by bit 1 to 4, bit 0 is ignored
                bankSelect32k = (shiftRegister & 0x0E) >> 1;
            } else if (prgMode == 2) { //16K Mode with lower half fixed to the first bank
                bankSelectLow16k = 0;
                bankSelectHigh16k = shiftRegister & 0x0F;
            } else { //16K Mode with higher half fixed to the last bank
                bankSelectLow16k = shiftRegister & 0x0F;
                bankSelectHigh16k = nesCart.prgRomChunks - 1;
            }
        }


    }

    @Override
    public boolean ppuRead(int addr, OutValue outValue) {

        if (addr <= 0x1FFF) {
            if (nesCart.chrRomChunks == 0) {
                outValue.value = nesCart.data[nesCart.prgRomOffset + addr];
                return false;
            } else {

                if ((control & 0b10000) == 0b10000) {
                    if (addr <= 0x0FFF) {
                        outValue.value = nesCart.data[nesCart.chrRomOffset + (this.charBankSelectLow4k * 0x1000) + (addr & 0x0FFF)];
                        //mapped.value = (selected_CHR_bank_low_4K * 0x1000) + (addr & 0x0FFF);
                        return true;
                    } else {
                        outValue.value = nesCart.data[nesCart.chrRomOffset + (this.charBankSelectHigh4k * 0x1000) + (addr & 0x0FFF)];
                        return true;
                        //mapped.value = (selected_CHR_bank_high_4K * 0x1000) + (addr & 0x0FFF);
                    }
                } else {
                    outValue.value = nesCart.data[nesCart.chrRomOffset + (this.charBankSelect8k * 0x2000) + (addr & 0x1FFF)];
                    return true;
                    //mapped.value = (selected_CHR_bank_8K * 0x2000) + (addr & 0x1FFF);
                }

            }

            //outValue.value = nesCart.data[nesCart.chrRomOffset + (addr & 0x0FFF) + (chrBank0 * 0x1000)];
            //return true;
        }

//        else if (addr <= 0x1FFF) {
//            outValue.value = nesCart.data[nesCart.chrRomOffset + (addr & 0x0FFF) + (chrBank1 * 0x1000)];
//            return true;
//        }

        return false;
    }

    @Override
    public boolean ppuWrite(int addr, int val) {

        return false;
    }

}
