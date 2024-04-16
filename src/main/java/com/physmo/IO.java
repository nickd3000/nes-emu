package com.physmo;

import com.physmo.minvio.BasicDisplay;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;


public class IO {

    CPU6502 cpu = null;
    Rig rig = null;

    Map<Integer, Integer[]> keymap_ = new HashMap<>();
    Map<Integer, Integer> joyMap2 = new HashMap<>();

    int [] controllerState = new int[2];
    int [] controllerSnapshotState = new int[2];


    // keymap_[SDL_SCANCODE_A] = std::make_pair(1,2);

    int[] keyboard_matrix_ = new int[8]; // REMOVE
    int[] portA = new int[8]; // Matrix Rows
    int[] portB = new int[8]; // Matrix Cols
    int joy1 = 0xff;
    int joy2 = 0xff;

    public IO(CPU6502 cpu, Rig rig) {
        System.out.println("Initialising IO");
        this.cpu = cpu;
        this.rig = rig;

    }


    public void handle_keydown(int k) {
        Integer[] pair = keymap_.get(k);
        if (pair == null)
            return;
        int row = pair[0];
        int column = pair[1];
        keyboard_matrix_[column] &= ~(1 << row);
        portB[column] &= ~(1 << row);
        portA[row] &= ~(1 << column);
    }


    public void checkKeyboard(BasicDisplay bd) {

        boolean control = bd.getKeyState()[KeyEvent.VK_CONTROL] > 0;

        // Toggle debug output
        if (isFirstPress(bd, KeyEvent.VK_D) && control) {
            if (!cpu.debugOutput) cpu.debugOutput = true;
            else cpu.debugOutput = false;
            return;
        }

        // Read controller.
        controllerState[0] = Utils.setBit(controllerState[0], 7-4, bd.getKeyState()[KeyEvent.VK_UP]>0);
        controllerState[0] = Utils.setBit(controllerState[0], 7-5, bd.getKeyState()[KeyEvent.VK_DOWN]>0);
        controllerState[0] = Utils.setBit(controllerState[0], 7-6, bd.getKeyState()[KeyEvent.VK_LEFT]>0);
        controllerState[0] = Utils.setBit(controllerState[0], 7-7, bd.getKeyState()[KeyEvent.VK_RIGHT]>0);
        controllerState[0] = Utils.setBit(controllerState[0], 7-0, bd.getKeyState()[KeyEvent.VK_Z]>0); // A
        controllerState[0] = Utils.setBit(controllerState[0], 7-1, bd.getKeyState()[KeyEvent.VK_X]>0); // B
        controllerState[0] = Utils.setBit(controllerState[0], 7-2, bd.getKeyState()[KeyEvent.VK_A]>0); // SELECT
        controllerState[0] = Utils.setBit(controllerState[0], 7-3, bd.getKeyState()[KeyEvent.VK_S]>0); // START

        bd.tickInput();

    }

    public boolean isFirstPress(BasicDisplay bd, int key) {
        if (bd.getKeyState()[key] == 0) return false;
        if (bd.getKeyStatePrevious()[key] != 0) return false;
        return true;
    }


}
