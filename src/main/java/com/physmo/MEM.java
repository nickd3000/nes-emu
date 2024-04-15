package com.physmo;

public class MEM {

    public int RAM[] = new int[0x10000]; // 64k
    public int VRAM[] = new int[0x10000]; // 64k

    CPU6502 cpu = null;
    Rig rig = null;
    NESCart nesCart;


    public MEM(CPU6502 cpu, Rig rig) {
        this.cpu = cpu;
        this.rig = rig;
    }

    public void setCart(NESCart nesCart) {
        this.nesCart = nesCart;
    }

    public void pokeWord(int addr, int val) {
        // evidence that we need to write low byte first
        int hi = (val >> 8) & 0xff;
        int lo = val & 0xff;
        poke(addr, lo);
        poke(addr + 1, hi);
    }

    public void poke(int addr, int val) {

        addr &= 0xFFFF;
        val &= 0xFF;

        // First give the mapper a chance to claim this memory area.
        if (!nesCart.mapper.cpuWrite(addr, val)) {

            if (addr > 0 && addr <= 0x1FFF) {
                // 2K of internal RAM, mirrored 4 times in 0x0800 sized pages.
                RAM[addr & 0x07FF] = val;

            } else if (addr <= 0x3FFF) {
                // PPU - 8 Bytes mirrored.
                System.out.println("Write to PPU");
                cpu.ppu.cpuWrite(addr & 0b0000_0111, val);
            }
        }

    }

    public int peekWord(int addr) {
        int lo = peek(addr);
        int hi = peek(addr + 1);
        return ((hi << 8) | lo) & 0xFFFF;
    }

    public int peek(int addr) {

        OutValue outValue = new OutValue();
        addr &= 0xFFFF;

        // First give the mapper a chance to claim this memory area.
        if (!nesCart.mapper.cpuRead(addr, outValue)) {


            if (addr > 0 && addr < 0x2000) {
                // 2K of internal RAM, mirrored 4 times in 0x0800 sized pages.
                outValue.value = RAM[addr & 0x07FF];
            } else if (addr <= 0x3FFF) {
                // PPU - 8 Bytes mirrored.
                outValue.value = cpu.ppu.cpuRead(addr & 0b0000_0111);
            }

//            // Most basic cartridge support...
//            if (addr>=0x8000 && addr<=0xBFFF) {
//                return nesCart.data[nesCart.prgRomOffset+(addr-0x8000)];
//            }
//            if (addr>=0xC000 && addr<=0xFFFF) {
//                return nesCart.data[nesCart.prgRomOffset+(addr-0xC000)+KB16];
//            }
        }


        return outValue.value;
    }

    public int peek_char(int addr) {
        return nesCart.data[nesCart.chrRomOffset + (addr)];
    }

}
