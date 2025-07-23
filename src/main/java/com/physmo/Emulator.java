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
        // Mapper 000 (260 games)
//        return "mario bros.nes";
//        return "super mario bros.nes";
//        return "donkey kong.nes";
//        return "balloon fight.nes";
//        return "pac-man.nes";
//        return "bomberman.nes";
//        return "golf.nes";
//        return "ice climber.nes";
//        return "ice hockey.nes";
//        return "xevious.nes";
//        return "pinball.nes";
//        return "othello.nes";

        // Mapper 001 (481 games)
//      return "tetris.nes"; // PRG=2 CHR=2 HORIZONTAL
//      return "metroid.nes"; // PRG=8 CHR=0 VERTICAL
//      return "megaman2.nes"; // PRG=16 CHR=0 VERTICAL
//        return "DrMario.nes"; // PRG=2 CHR=4 HORIZONTAL
//        return "UltimaExodus.nes"; // PRG=16 CHR=0 VERTICAL
//        return "BubbleBobble.nes"; // BAD PRG=8 CHR=4 VERTICAL
        return "Boulderdash.nes"; // BAD PRG=2 CHR=4 HORIZONTAL
//        return "BombermanII.nes"; // GOOD PRG=8 CHR=0 HORIZONTAL

        // Mapper 002 (200 g)
//        return "Castlevania.nes";
//        return "RainbowIslands.nes";
//        return "SuperPitfall.nes";
//        return  "Ikari Warriors.nes";
//        return "Life Force.nes";
//        return "TotalRecall.nes"; // 002
//        return "Prince of Persia.nes";
//        return "heroquest.nes";

        // Mapper 003 (145 g)
//        return "arkanoid.nes"; // Good
//        return "gradius.nes"; // Scrolling and sprite flickering
//        return "q*bert.nes"; // Good
//        return "BumpNJump.nes"; // Good
//        return "Castlequest.nes"; // Good
//        return "MightyBombJack.nes"; // Good
//        return "SolomonsKey.nes";

        // Mapper 004 (569 g)
//        return "BuraiFighter.nes"; // 004
//        return "Batman Returns.nes"; // 004
//        return "YoshisCookie.nes"; // 004
//        return "GauntletII.nes";

//        return "MysteryQuest.nes"; // 64

//        return "MegaMan.nes"; // 066

//        return "1942.nes"; // 064

    }

}
