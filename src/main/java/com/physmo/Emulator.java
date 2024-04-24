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

        rig.runForCycles(200000000);

    }

    public static String getGame() {
        // Mapper 000
//        return "mario bros.nes";
//        return "super mario bros.nes";
//        return "donkey kong.nes";
//        return "balloon fight.nes";
//        return "pac-man.nes";
//        return "bomberman.nes";
//        return "golf.nes";
//        return "ice climber.nes";
        return "ice hockey.nes";

        // Mapper 001
//        return "tetris.nes";

        // Mapper 002
//        return "Castlevania.nes";
//        return "RainbowIslands.nes";
//        return "SuperPitfall.nes";
//        return  "Ikari Warriors.nes";
//        return "Life Force.nes";
//        return "TotalRecall.nes"; // 002


        // Mapper 003
//        return "arkanoid.nes";
//        return "gradius.nes";
//        return "q*bert.nes";
//        return "BumpNJump.nes";
//        return "Castlequest.nes";
//        return "MightyBombJack.nes";
//            return "SolomonsKey.nes";

//        return "BuraiFighter.nes"; // 004
//        return "MysteryQuest.nes"; // 64

//        return "MegaMan.nes"; // 066
    }

}
