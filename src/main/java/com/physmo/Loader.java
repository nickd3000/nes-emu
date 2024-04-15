package com.physmo;

import java.io.FileNotFoundException;

public class Loader {

    public static NESCart loadRom(String path) {

        int[] data;
        try {
            data = Utils.readFileToArray(path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        NESCart nesCart = new NESCart();
        nesCart.addData(data);

        return nesCart;
    }

}
