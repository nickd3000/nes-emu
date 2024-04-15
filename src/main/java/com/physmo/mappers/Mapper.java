package com.physmo.mappers;

import com.physmo.NESCart;
import com.physmo.OutValue;
import com.physmo.Rig;

public interface Mapper {
    void attachRig(Rig rig, NESCart nesCart);

    boolean cpuRead(int addr, OutValue value);

    boolean cpuWrite(int addr, int val);

    boolean ppuRead(int addr, OutValue value);

    boolean ppuWrite(int addr, int val);

}
