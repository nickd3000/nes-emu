package com.physmo;

import com.physmo.minvio.BasicDisplay;

import java.awt.Color;

public class PPU {

    // Internal storage
    int[][] nameTable = new int[2][1024]; // Tile Id's
    int[][] patternTable = new int[2][4096]; // Images
    int[] paletteRam = new int[32];

    BasicDisplay bd;
    int PPUCTRL;
    int PPUMASK;
    int PPUSTATUS;
    int OAMADDR;
    int OAMDATA;
    int PPUSCROLL;
    int PPUADDR;
    int PPUDATA;
    int OAMDMA;

    Color[] bgPalette = new Color[4];
    OAM[] oamTable = new OAM[64];

    boolean vBlankActive = false;

    boolean PPUSCROLLToggle = false;
    int scrollCourseX;
    int scrollFineX;
    int scrollCourseY;
    int scrollFineY;
    int scale = 2;
    CPU6502 cpu;
    Rig rig;
    long nextRasterAt = 0;
    int raster = 0;
    boolean wRegister = false;
    int dataBuffer = 0;
    boolean nmi = false;
    boolean spriteOverflow = false;
    boolean spriteZeroHit = false;
    int oamAddress = 0;

    boolean showBoundingBoxes = false;

    public PPU(BasicDisplay bd, CPU6502 cpu, Rig rig) {
        this.bd = bd;
        this.cpu = cpu;
        this.rig = rig;
        init();
    }

    public void init() {
        bgPalette[0] = Color.BLACK;
        bgPalette[1] = Color.RED;
        bgPalette[2] = Color.BLUE;
        bgPalette[3] = Color.YELLOW;
        PPUCTRL = 0b1000_0000;
        for (int i = 0; i < 64; i++) {
            oamTable[i] = new OAM();
        }
    }

    public void tick() {
        if (cpu.cycles < nextRasterAt) return;

        if (raster == 0) vBlankActive = false;

        checkSprite0();

        if (raster >= 0 && raster < 240) {
            renderBgRow(bd, raster);
            renderSpriteRow(bd, raster);
        }

        if (raster == 241) {
            vBlankActive = true;
            if ((PPUCTRL & 0b1000_0000) > 0) {
                nmi = true;
            } else {
                //System.out.println("NMI disabled");
            }

        }


        if (raster == 261) {
            raster = 0;
            vBlankActive = false;
            if (showBoundingBoxes) renderDebugOAM();
            bd.repaint();
            spriteZeroHit = false;
        }

//        if (Math.random() < 0.1) spriteZeroHit = true;

        raster++;
        nextRasterAt += 341;

    }


    public void cpuWrite(int addr, int val) {

        switch (addr) {
            case 0x00:
                PPUCTRL = val;
                break;
            case 0x01:
                PPUMASK = val;
                break;
            case 0x02:
                PPUSTATUS = val;
                break;
            case 0x03:
                OAMADDR = val;
                break;
            case 0x04:
                OAMDATA = val;
                break;
            case 0x05:
                if (wRegister) {
                    scrollCourseY = val >> 3;
                    scrollFineY = val & 0x07;
                    wRegister = false;
                } else {
                    scrollCourseX = val >> 3;
                    scrollFineX = val & 0x07;
                    wRegister = true;
                }
                PPUSCROLL = val;
                break;
            case 0x06:
                if (wRegister) {
                    PPUADDR = (PPUADDR & 0xFF00) | (val & 0xff);
                    wRegister = false;
                } else {
                    PPUADDR = (PPUADDR & 0x00FF) | ((val & 0x3f) << 8);
                    wRegister = true;
                }

                break;
            case 0x07:
                //PPUDATA = cpu.mem.peek(PPUADDR&0x3FFF);
                //System.out.println("PPU data Write addr:"+Utils.toHex4(PPUADDR));
                ppuWrite(PPUADDR & 0x3FFF, val);
                PPUADDR += getAddressIncrementSize();
                break;
        }

    }


    public int cpuRead(int addr) {
        int retVal = 0;
        switch (addr) {
            case 0x00:
                return PPUCTRL;
            case 0x01:
                return 0;

            case 0x02:
                wRegister = false;
                if (vBlankActive) {
                    vBlankActive = false;
                    retVal |= 0b1000_0000;
                }
                if (spriteZeroHit) retVal |= 0b0100_0000;
                if (spriteOverflow) retVal |= 0b0010_0000;
                break;

            case 0x03:
                break;
            case 0x04:
                break;
            case 0x05:
                break;
            case 0x06:
                return 0;

            case 0x07:
                retVal = dataBuffer;
                dataBuffer = ppuRead(PPUADDR & 0x3FFF);
                PPUADDR += getAddressIncrementSize();

                return retVal;

        }

        return retVal;
    }

    public void ppuWrite(int addr, int val) {
        addr &= 0xFFFF;
        val &= 0xFF;

        // First give the mapper a chance to claim this memory area.
        if (!rig.nesCart.mapper.ppuWrite(addr, val)) {
            // Pattern table.
            if (addr <= 0x1FFF) {
                int page = (addr & 0x1000) >> 12;
                patternTable[page][addr & 0x0FFF] = val;
            } else if (addr <= 0x3EFF) {
                // Name table.
                // TODO: Handle mirror mode.
                addr &= 0x0FFF;
                if (rig.nesCart.mirrorMode == 1) {
                    if (addr <= 0x3FF) {
                        nameTable[0][addr & 0x03FF] = val;
                    } else if (addr <= 0x7FF) {
                        nameTable[1][addr & 0x03FF] = val;
                    } else if (addr <= 0xBFF) {
                        nameTable[0][addr & 0x03FF] = val;
                    } else {
                        nameTable[1][addr & 0x03FF] = val;
                    }
                } else {

                    if (addr <= 0x3FF) {
                        nameTable[0][addr & 0x03FF] = val;
                    } else if (addr <= 0x7FF) {
                        nameTable[0][addr & 0x03FF] = val;
                    } else if (addr <= 0xBFF) {
                        nameTable[1][addr & 0x03FF] = val;
                    } else {
                        nameTable[1][addr & 0x03FF] = val;
                    }

                }
            } else {
                addr &= 0x1F;
                if (addr == 0x0010) addr = 0x0000;
                if (addr == 0x0014) addr = 0x0004;
                if (addr == 0x0018) addr = 0x0008;
                if (addr == 0x001C) addr = 0x000C;
                paletteRam[addr] = val;
            }
        }
    }

    public int ppuRead(int addr) {

        OutValue outValue = new OutValue();
        addr &= 0xFFFF;

        // First give the mapper a chance to claim this memory area.
        if (!rig.nesCart.mapper.ppuRead(addr, outValue)) {

            // Pattern table.
            if (addr <= 0x1FFF) {
                int page = (addr & 0x1000) >> 12;
                outValue.value = patternTable[page][addr & 0x0FFF];
            } else if (addr <= 0x3EFF) {
                // Name table.
                // TODO: Handle mirror mode.
                addr &= 0x0FFF;
                if (rig.nesCart.mirrorMode == 1) {
                    if (addr <= 0x3FF) {
                        outValue.value = nameTable[0][addr & 0x03FF];
                    } else if (addr <= 0x7FF) {
                        outValue.value = nameTable[1][addr & 0x03FF];
                    } else if (addr <= 0xBFF) {
                        outValue.value = nameTable[0][addr & 0x03FF];
                    } else {
                        outValue.value = nameTable[1][addr & 0x03FF];
                    }
                } else {
                    if (addr <= 0x3FF) {
                        outValue.value = nameTable[0][addr & 0x03FF];
                    } else if (addr <= 0x7FF) {
                        outValue.value = nameTable[0][addr & 0x03FF];
                    } else if (addr <= 0xBFF) {
                        outValue.value = nameTable[1][addr & 0x03FF];
                    } else {
                        outValue.value = nameTable[1][addr & 0x03FF];
                    }
                }

            } else {
                addr &= 0x1F;
                if (addr == 0x0010) addr = 0x0000;
                if (addr == 0x0014) addr = 0x0004;
                if (addr == 0x0018) addr = 0x0008;
                if (addr == 0x001C) addr = 0x000C;
                outValue.value = paletteRam[addr];
            }

        }

        return outValue.value;
    }

    public int getAddressIncrementSize() {
        if ((PPUCTRL & 0x00000100) == 0) return 1;
        else return 32;
    }


    public int getBaseNametable() {
        switch (PPUCTRL & 0b11) {
            case 0:
                return 0x2000;
            case 1:
                return 0x2400;
            case 2:
                return 0x2800;
            case 3:
                return 0x2C00;
        }
        return 0;
    }

    public void renderBgRow(BasicDisplay bd, int scanline) {

        int numColumns = 32;
        int row = scanline / 8;
        int yOffs = scanline % 8;
        int nameTableBaseAddress = getBaseNametable();//0x2000; //
        int charBaseAddress = 0x0000;

        if ((PPUCTRL & 0b10000) > 0) charBaseAddress = 0x1000;

        int mx = bd.getMouseButtonLeft() ? bd.getMouseX() * 8 : 0;
        int my = bd.getMouseButtonLeft() ? bd.getMouseY() * 8 : 0;

        int scrollX = PPUSCROLL & 0xF;
        int scrollY = (PPUSCROLL >> 4) & 0xF;
        mx += scrollCourseX;// * 8;
        my += scrollCourseY;// * 8;

        for (int col = 0; col < numColumns; col++) {
//            int tile = cpu.mem.peek(nameTableBaseAddress + (col + mx + ((row + my) * numColumns * 2))) & 0xff;
//            int d1 = cpu.mem.peek_char(charBaseAddress + (tile * 16) + yOffs);
//            int d2 = cpu.mem.peek_char(charBaseAddress + (tile * 16) + yOffs + 8);
//
            int attributePalette = getAttributePalette(col, row);
            loadBGPalette(attributePalette);
            int tile = ppuRead(nameTableBaseAddress + (col + mx + ((row + my) * numColumns))) & 0xff;
            int d1 = ppuRead(charBaseAddress + (tile * 16) + yOffs);
            int d2 = ppuRead(charBaseAddress + (tile * 16) + yOffs + 8);
//
            renderTileRow(bd, col * 8, row * 8, d1, d2, scanline % 8, false, false);
        }

    }

    public void renderSpriteRow(BasicDisplay bd, int scanline) {
        OAM oam;

        int charBaseAddress = 0x0;
        if ((PPUCTRL & 0b1000) > 0) charBaseAddress = 0x1000;

        for (int i = 0; i < 64; i++) {
            oam = oamTable[i];
            if (oam.yPos + 7 < scanline || oam.yPos > scanline) continue;

            int yOffs = scanline - oam.yPos;

            // Handle y-flipped sprite
            int dataOffset = ((oam.attributes & 0b10000000) == 0) ? yOffs : 7-yOffs;

            int d1 = ppuRead(charBaseAddress + (oam.tileIndex * 16) + dataOffset);
            int d2 = ppuRead(charBaseAddress + (oam.tileIndex * 16) + dataOffset + 8);
            boolean flipH = (oam.attributes & 0b01000000) > 0;

            loadSpritePalette(oam.attributes & 0b11);

            //bd.drawRect(oam.xPos * 2, oam.yPos * 2, 8 * 2, 8 * 2);
            renderTileRow(bd, oam.xPos, oam.yPos, d1, d2, yOffs, true, flipH);
        }
    }

    public void renderTileRow(BasicDisplay bd, int x, int y, int d1, int d2, int yOffset, boolean transparent, boolean flipH) {

        int bit;
        for (int p = 0; p < 8; p++) {

            if (!flipH)
                bit = 0b10000000 >> p;
            else
                bit = 0b10000000 >> (7 - p);

            int val = 0;
            if ((d1 & bit) > 0) val |= 0b01;
            if ((d2 & bit) > 0) val |= 0b10;

            if (!(transparent && val == 0)) {
                bd.setDrawColor(bgPalette[val]);
                bd.drawFilledRect((x + p) * scale, (y + yOffset) * scale, scale, scale);
            }
        }
    }

    public int getAttributePalette(int col, int row) {
        int x = col >> 2;
        int y = row >> 2;
        int nameTableBaseAddress = getBaseNametable();
        int attributeOffset = 0x3C0 + x + (y * 8);
        int data = ppuRead(nameTableBaseAddress + attributeOffset);
        int pal;

        boolean top = (row >> 1) % 2 == 0;
        boolean left = (col >> 1) % 2 == 0;

        if (top) {
            if (left) {
                pal = (data) & 0b11; // TOP LEFT

            } else {
                pal = (data >> 2) & 0b11; // TOP RIGHT
            }
        } else {
            if (left) {
                pal = (data >> 4) & 0b11; // LOW LEFT
            } else {
                pal = (data >> 6) & 0b11; // LOW RIGHT
            }
        }

        return pal;
    }

    public void loadBGPalette(int p) {
        int pUniversalBg = 0x3F00;
        int pIndex = 0x3f01 + ((p & 0b111) * 4);
        bgPalette[0] = NESPalette.palette[ppuRead(pUniversalBg)];
        bgPalette[1] = NESPalette.palette[ppuRead(pIndex)];
        bgPalette[2] = NESPalette.palette[ppuRead(pIndex + 1)];
        bgPalette[3] = NESPalette.palette[ppuRead(pIndex + 2)];
    }

    public void loadSpritePalette(int p) {
        int pUniversalBg = 0x3F00;
        int pIndex = 0x3f11 + ((p & 0b111) * 4);
        bgPalette[0] = NESPalette.palette[ppuRead(pUniversalBg)];
        bgPalette[1] = NESPalette.palette[ppuRead(pIndex)];
        bgPalette[2] = NESPalette.palette[ppuRead(pIndex + 1)];
        bgPalette[3] = NESPalette.palette[ppuRead(pIndex + 2)];
    }


    public boolean getNmi() {
        if (nmi) {
            nmi = false;
            return true;
        }
        return false;
    }

    public void checkSprite0() {

        if (oamTable[0].yPos == raster) spriteZeroHit = true;

    }

    public void renderDebugOAM() {
        bd.setDrawColor(Color.cyan);
        OAM oam;
        for (int i = 0; i < 64; i++) {
            oam = oamTable[i];
            bd.drawRect(oam.xPos * 2, oam.yPos * 2, 8 * 2, 8 * 2);
        }

        // Draw sprite zero again since it's special.
        bd.setDrawColor(Color.red);
        oam = oamTable[0];
        bd.drawRect((oam.xPos * 2) + 1, (oam.yPos * 2) + 1, 8 * 2, 8 * 2);
    }
}
