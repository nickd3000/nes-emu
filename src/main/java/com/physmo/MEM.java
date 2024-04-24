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

            if (addr <= 0x1FFF) {
                // 2K of internal RAM, mirrored 4 times in 0x0800 sized pages.
                RAM[addr & 0x07FF] = val;

            } else if (addr <= 0x3FFF) {
                // PPU - 8 Bytes mirrored.
                cpu.ppu.cpuWrite(addr & 0b0000_0111, val);
            } else if (addr <=0x4013 || addr==0x4015 || addr==0x4017) {
                // Sound stuff
            }
            else if (addr == 0x4016) {
                // Controllers - copy current state to snapshot.
                if ((val&1)>0) {
                    rig.io.controllerSnapshotState[0] = rig.io.controllerState[0];
                    rig.io.controllerSnapshotState[1] = rig.io.controllerState[1];
                }
            } else if (addr == 0x4014) {
                // DMA
                rig.dma.dmaActive=true;
                rig.dma.dmaAddr=0;
                rig.dma.dmaPage=val;
                rig.ppu.OAMADDR=0;
                rig.dma.dmaDelay=3;
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


            if (addr < 0x2000) {
                // 2K of internal RAM, mirrored 4 times in 0x0800 sized pages.
                outValue.value = RAM[addr & 0x07FF];
            } else if (addr <= 0x3FFF) {
                // PPU - 8 Bytes mirrored.
                outValue.value = cpu.ppu.cpuRead(addr & 0b0000_0111);
            } else if (addr == 0x4015) {
                // SOUND
            }
            else if (addr >= 0x4016 && addr<=0x4017) {
                // Read controller 1
                outValue.value = (rig.io.controllerSnapshotState[addr & 0b01] & 0x80) > 0 ? 1:0;
                rig.io.controllerSnapshotState[addr  & 0b01] <<= 1;
            }

        }


        return outValue.value;
    }

    public int peek_char(int addr) {
        return nesCart.data[nesCart.chrRomOffset + (addr)];
    }

}
