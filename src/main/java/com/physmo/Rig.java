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


    public Rig(BasicDisplay basicDisplay) {
        C64Palette.init();

        this.basicDisplay = basicDisplay;

        cpu = new CPU6502();
//		vic = new VIC(cpu, basicDisplay, this);
//		cia1 = new CIA1(cpu, this);
//		cia2 = new CIA2(cpu, this);
        io = new IO(cpu, this);
        mem = new MEM(cpu, this);
        ppu = new PPU(basicDisplay, cpu, this);
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

    public void runForCycles(int runFor) {
        //cpu.debugOutput = true;
        int displayLines = 500; //
        int instructionsPerScanLine = 160;
        int tickCount = 0;


        for (int i = 0; i < runFor + displayLines; i++) {

            for (int ii = 0; ii < instructionsPerScanLine; ii++) {

                if (tickCount % 50 == 0) {
                    io.checkKeyboard(basicDisplay);
                }

                // Don't tick other components if unit test is active.
                cpu.tick2();
                ppu.tick();

                tickCount++;
            }

            if (cpu.firstUnimplimentedInstructionIndex != -1) {
                System.out.println("### First unimplimented instruction occured at call number: "
                        + cpu.firstUnimplimentedInstructionIndex);
                break;
            }

        }
    }

}
