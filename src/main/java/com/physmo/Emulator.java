package com.physmo;

import com.physmo.minvio.BasicDisplay;
import com.physmo.minvio.BasicDisplayAwt;

public class Emulator {

    static BasicDisplay basicDisplay = null;

    public static void main(String[] args) {

        NESCart nesCart = Loader.loadRom("/Users/nick/dev/emulatorsupportfiles/nes/games/" + getGame());

        basicDisplay = new BasicDisplayAwt((320 + 80) * 2, (240 + 80) * 2);
        basicDisplay.setTitle("NES Emulator");

        Rig rig = new Rig(basicDisplay);
        nesCart.mapper.attachRig(rig, nesCart);
        rig.attachCart(nesCart);

        rig.runForCycles(2000000);

        Utils.printMem(rig.cpu.mem.RAM, 0x0400, 0x07FF - 0x0400); // Screen ram
        Utils.printMem(rig.cpu.mem.RAM, 0x00, 0xff); // Zero page
        Utils.printTextScreen(rig.cpu.mem.RAM, 0x0400);

    }

    public static String getGame() {
//        return "super mario bros.nes";
//        return "donkey kong.nes";
        return "balloon fight.nes";
//        return "pac-man.nes";
//        return "bomberman.nes";
    }

}
