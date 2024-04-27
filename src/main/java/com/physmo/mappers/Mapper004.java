package com.physmo.mappers;


import com.physmo.NESCart;
import com.physmo.OutValue;
import com.physmo.Rig;

// https://www.nesdev.org/wiki/MMC3

public class Mapper004 implements Mapper {

    private final int[] register;
    private final int[] chrBanks;
    private final int[] prgBanks;
    Rig rig;
    NESCart nesCart;
    int bankSelect = 0;
    int targetRegister = 0;
    boolean modePrgBank = false;
    boolean modeCharInversion = false;
    int mirrorMode = 0;

    int[] cartRam;

    public Mapper004() {
        register = new int[8];
        chrBanks = new int[8];
        prgBanks = new int[4];
        cartRam = new int[32 * 1024];
    }

    @Override
    public void attachRig(Rig rig, NESCart nesCart) {
        this.rig = rig;
        this.nesCart = nesCart;

        prgBanks[0] = 0;
        prgBanks[1] = 0x2000;
        prgBanks[2] = (nesCart.prgRomChunks * 2 - 2) * 0x2000;
        prgBanks[3] = (nesCart.prgRomChunks * 2 - 1) * 0x2000;
    }

    @Override
    public boolean cpuRead(int addr, OutValue outValue) {

        if (addr >= 0x6000 && addr <= 0x7FFF) {
            outValue.value = cartRam[addr & 0x1FFF] & 0xFF;
            return true;
        }

        if (addr >= 0x8000 && addr <= 0x9FFF) {
            outValue.value = nesCart.data[nesCart.prgRomOffset + prgBanks[0] + (addr & 0x1FFF)];
            return true;
        }
        if (addr >= 0xA000 && addr <= 0xBFFF) {
            outValue.value = nesCart.data[nesCart.prgRomOffset + prgBanks[1] + (addr & 0x1FFF)];
            return true;
        }
        if (addr >= 0xC000 && addr <= 0xDFFF) {
            outValue.value = nesCart.data[nesCart.prgRomOffset + prgBanks[2] + (addr & 0x1FFF)];
            return true;
        }
        if (addr >= 0xE000 && addr <= 0xFFFF) {
            outValue.value = nesCart.data[nesCart.prgRomOffset + prgBanks[3] + (addr & 0x1FFF)];
            return true;
        }
        return false;
    }


    @Override
    public boolean cpuWrite(int addr, int val) {

        if (addr >= 0x6000 && addr <= 0x7FFF) {
            cartRam[addr & 0x1FFF] = val & 0xFF;
            return true;
        }

        if (addr >= 0x8000 && addr <= 0x9FFE) {
            if ((addr & 1) == 0) {
                // Bank select (even)
                bankSelect = val;
                targetRegister = val & 0x7;
                modePrgBank = (val & 0x40) > 0;
                modeCharInversion = (val & 0x80) > 0;
                return true;
            } else {
                // Bank Data (odd)
                register[targetRegister] = val;

                // Logic derived from NEmuS
                if (modeCharInversion) {
                    chrBanks[0] = register[2] * 0x0400;
                    chrBanks[1] = register[3] * 0x0400;
                    chrBanks[2] = register[4] * 0x0400;
                    chrBanks[3] = register[5] * 0x0400;
                    chrBanks[4] = (register[0] & 0xFE) * 0x0400;
                    chrBanks[5] = register[0] * 0x0400 + 0x0400;
                    chrBanks[6] = (register[1] & 0xFE) * 0x0400;
                    chrBanks[7] = register[1] * 0x0400 + 0x0400;
                } else {
                    chrBanks[0] = (register[0] & 0xFE) * 0x0400;
                    chrBanks[1] = register[0] * 0x0400 + 0x0400;
                    chrBanks[2] = (register[1] & 0xFE) * 0x0400;
                    chrBanks[3] = register[1] * 0x0400 + 0x0400;
                    chrBanks[4] = register[2] * 0x0400;
                    chrBanks[5] = register[3] * 0x0400;
                    chrBanks[6] = register[4] * 0x0400;
                    chrBanks[7] = register[5] * 0x0400;
                }

                if (modePrgBank) {
                    prgBanks[2] = (register[6] & 0x3F) * 0x2000;
                    prgBanks[0] = (nesCart.prgRomChunks * 2 - 2) * 0x2000;
                } else {
                    prgBanks[0] = (register[6] & 0x3F) * 0x2000;
                    prgBanks[2] = (nesCart.prgRomChunks * 2 - 2) * 0x2000;
                }
                prgBanks[1] = (register[7] & 0x3F) * 0x2000;
                prgBanks[3] = (nesCart.prgRomChunks * 2 - 1) * 0x2000;
                return true;
            }

        }

        // Mirror mode
        if (addr >= 0xA000 && addr <= 0xBFFF) {
            if ((addr & 0x1) != 0x1) {
                if ((val & 0x1) == 0x1)
                    mirrorMode = 0;
                else
                    mirrorMode = 1;
            }
            return true;
        }

        // Scanline...
        if (addr >= 0xC000 && addr <= 0xDFFF) {
            return true;
        }

        // Irq...
        if (addr >= 0xE000) {
            return true;
        }

        return false;
    }

    @Override
    public boolean ppuRead(int addr, OutValue outValue) {

//        if (addr <= 0x1FFF) {
//            outValue.value = nesCart.data[nesCart.chrRomOffset + addr + (bankSelect * 0x2000)];
//            return true;
//        }

        // Again thank you to NEmuS for the following
        if (addr <= 0x03FF) {
            outValue.value = nesCart.data[nesCart.chrRomOffset + chrBanks[0] + (addr & 0x03FF)];
            return true;
        } else if (addr <= 0x07FF) {
            outValue.value = nesCart.data[nesCart.chrRomOffset + chrBanks[1] + (addr & 0x03FF)];
            return true;
        } else if (addr <= 0x0BFF) {
            outValue.value = nesCart.data[nesCart.chrRomOffset + chrBanks[2] + (addr & 0x03FF)];
            return true;
        } else if (addr <= 0x0FFF) {
            outValue.value = nesCart.data[nesCart.chrRomOffset + chrBanks[3] + (addr & 0x03FF)];
            return true;
        } else if (addr <= 0x13FF) {
            outValue.value = nesCart.data[nesCart.chrRomOffset + chrBanks[4] + (addr & 0x03FF)];
            return true;
        } else if (addr <= 0x17FF) {
            outValue.value = nesCart.data[nesCart.chrRomOffset + chrBanks[5] + (addr & 0x03FF)];
            return true;
        } else if (addr <= 0x1BFF) {
            outValue.value = nesCart.data[nesCart.chrRomOffset + chrBanks[6] + (addr & 0x03FF)];
            return true;
        } else if (addr <= 0x1FFF) {
            outValue.value = nesCart.data[nesCart.chrRomOffset + chrBanks[7] + (addr & 0x03FF)];
            return true;
        }

        return false;
    }

    @Override
    public boolean ppuWrite(int addr, int val) {

        return false;
    }

}
