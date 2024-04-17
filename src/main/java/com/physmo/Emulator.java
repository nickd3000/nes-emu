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

    }

    public static String getGame() {
//        return "mario bros.nes";
//        return "super mario bros.nes";
//        return "donkey kong.nes";
//        return "balloon fight.nes";
//        return "pac-man.nes";
        return "bomberman.nes";
//        return "golf.nes";
//        return "ice climber.nes";
//        return "ice hockey.nes";
    }

}
