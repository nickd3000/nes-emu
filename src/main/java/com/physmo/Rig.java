package com.physmo;


import com.physmo.minvio.BasicDisplay;


public class Rig {

    BasicDisplay basicDisplay = null;

    CPU6502 cpu;
    MEM mem = null;
    //	VIC vic = null;
//	CIA1 cia1 = null;
//	CIA2 cia2 = null;
    IO io = null;
    PPU ppu;
    DMA dma;


    public Rig(BasicDisplay basicDisplay) {
        NESPalette.init();

        this.basicDisplay = basicDisplay;

        cpu = new CPU6502();
//		vic = new VIC(cpu, basicDisplay, this);
//		cia1 = new CIA1(cpu, this);
//		cia2 = new CIA2(cpu, this);
        io = new IO(cpu, this);
        mem = new MEM(cpu, this);
        ppu = new PPU(basicDisplay, cpu, this);
        dma = new DMA(ppu, mem);
        cpu.attachHardware(mem, ppu);

//		if (cpu.unitTest == false)
//			cpu.reset();
//		else
//			cpu.resetAndTest();

    }

    public NESCart nesCart;

    public void attachCart(NESCart nesCart) {
        this.nesCart = nesCart;
        mem.setCart(nesCart);
        cpu.reset();
    }

    public void runForCycles(long runFor) {
        //cpu.debugOutput = true;
        int displayLines = 500; //
        int instructionsPerScanLine = 160;
        long tickCount = 0;


        //for (int i = 0; i < runFor + displayLines ; i++) {
        for (;;) {


            if (tickCount % 50 == 0) {
                io.checkKeyboard(basicDisplay);
            }

            ppu.tick();

            if (dma.dmaActive) {
                dma.doNextDmaTransferCycle(tickCount);
            } else {
                cpu.tick2();
            }

            tickCount++;


//            if (cpu.firstUnimplimentedInstructionIndex != -1) {
//                System.out.println("### First unimplimented instruction occured at call number: "
//                        + cpu.firstUnimplimentedInstructionIndex);
//                break;
//            }

        }
    }

}
