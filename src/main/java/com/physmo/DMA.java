package com.physmo;

public class DMA {
    public boolean dmaActive = false;
    public int dmaPage = 0;
    public int dmaAddr = 0;
    public int dmaData = 0;
    public int dmaDelay = 0;
    PPU ppu;
    MEM mem;

    public DMA(PPU ppu, MEM mem) {
        this.ppu = ppu;
        this.mem = mem;
    }

    public void doNextDmaTransferCycle(long tickNumber) {
        if (--dmaDelay>0) return;

        if ((tickNumber&1)==0) {
            dmaData = mem.peek(dmaPage<<8 | dmaAddr);
        } else {
            switch (dmaAddr&0x03) {
                case 0:
                    //if (dmaData==0) dmaData=1;
                    ppu.oamTable[dmaAddr>>2].yPos = dmaData;
                    break;
                case 1:
                    ppu.oamTable[dmaAddr>>2].tileIndex = dmaData;
                    break;
                case 2:
                    ppu.oamTable[dmaAddr>>2].attributes = dmaData;
                    break;
                case 3:
                    ppu.oamTable[dmaAddr>>2].xPos = dmaData;
                    break;
            }
            dmaAddr=(dmaAddr+1)&0xFF;
            if (dmaAddr==0) {
                dmaActive = false;
                dmaDelay = 3;
            }
        }

    }
}
