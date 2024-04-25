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
    int PPUADDR_hi = 0;
    int PPUADDR_lo = 0;
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

    boolean showBoundingBoxes = true;

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

            if ((PPUMASK & 0b0000_1000) > 0) renderBgRow(bd, raster);

            if ((PPUMASK & 0b0001_0000) > 0) {
                if ((PPUCTRL & 0b0010_0000) == 0) {
                    renderSpriteRow(bd, raster);
                } else {
                    renderSpriteRow16(bd, raster);
                }
            }

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
            spriteOverflow = false;
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
                break;
            case 0x03:
                OAMADDR = val;
                break;
            case 0x04:
                OAMDATA = val;
                int oamId = (OAMADDR >> 2);// & 0b11;
                if ((OAMADDR & 0x03) == 0) oamTable[oamId].yPos = val;
                if ((OAMADDR & 0x03) == 1) oamTable[oamId].tileIndex = val;
                if ((OAMADDR & 0x03) == 2) oamTable[oamId].attributes = val;
                if ((OAMADDR & 0x03) == 3) oamTable[oamId].xPos = val;
                OAMADDR = (OAMADDR + 1) & 0xFF;
                break;
            case 0x05:
                if (wRegister) {
                    scrollCourseY = (val & 0xFF) >> 3;
                    scrollFineY = val & 0x07;
                    wRegister = false;
                } else {
                    scrollCourseX = (val & 0xFF) >> 3;
                    scrollFineX = val & 0x07;
                    wRegister = true;
                }
                PPUSCROLL = val;
                break;
            case 0x06:
                if (wRegister) {
                    PPUADDR_lo = val & 0xFF;
                    PPUADDR = (PPUADDR_hi << 8) | (PPUADDR_lo);
                    wRegister = false;
                } else {
                    PPUADDR_hi = val & 0x3F;

                    wRegister = true;
                }

                break;
            case 0x07:
                //PPUDATA = cpu.mem.peek(PPUADDR&0x3FFF);
                //System.out.println("PPU data Write addr:"+Utils.toHex4(PPUADDR));
                ppuWrite(PPUADDR /*& 0x3FFF*/, val & 0xff);
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
                return OAMADDR;

            case 0x02:
                if (vBlankActive) retVal |= 0b1000_0000;
                if (spriteZeroHit) retVal |= 0b0100_0000;
                if (spriteOverflow) retVal |= 0b0010_0000;
                retVal |= dataBuffer & 0x1F;
                vBlankActive = false;
                wRegister = false;
                break;

            case 0x03:
                break;

            case 0x04:
                return getOAMData();
            case 0x05:
                break;
            case 0x06:
                return 0;

            case 0x07:
                retVal = dataBuffer;
                dataBuffer = ppuRead(PPUADDR);

                // Palette data does not have the same delay.
                if (PPUADDR >= 0x3F00) retVal = dataBuffer;

                PPUADDR += getAddressIncrementSize();
                break;

        }

        return retVal;
    }

    public void ppuWrite(int addr, int val) {
        addr &= 0x3FFF;
        val &= 0xFF;

        // First give the mapper a chance to claim this memory area.
        if (!rig.nesCart.mapper.ppuWrite(addr, val)) {
            // Pattern table.
            if (addr <= 0x1FFF) {
                int page = (addr & 0x1000) >> 12;
                patternTable[page][addr & 0x0FFF] = val;
            } else if (addr <= 0x3EFF) {
                // Name table.

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
        addr &= 0x3FFF;

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
        if ((PPUCTRL & 0x0000_0100) == 0) return 1;
        return 32;
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

        int cx = scrollCourseX;
        int cy = scrollCourseY;
        int fx = scrollFineX;
        int fy = scrollFineY;
        int sy = (cy * 8) | fy;
        int sx = (cx * 8) | fx;

        int numColumns = 32;
        int row = (scanline) / 8;
        int yOffs = (scanline) % 8;
        int nameTableBaseAddress = 0x2000; //getBaseNametable();//0x2000; //
        int charBaseAddress = 0x0000;

        if ((PPUCTRL & 0b10000) > 0) charBaseAddress = 0x1000;


        for (int col = 0; col < numColumns; col++) {

            nameTableBaseAddress = getBaseNametable(); //0x2000;

            int combinedX = col + cx;
            int combinedY = row + cy;

            if ((yOffs + fy) > 7) combinedY++;

            if (combinedX >= 32) {
                nameTableBaseAddress += 0x400;
                combinedX %= 32;
            }
            if (combinedY >= 30) {
                if ((PPUCTRL & 0b10) > 0) {
                    nameTableBaseAddress -= 0x800;
                } else {
                    nameTableBaseAddress += 0x800;
                }
                combinedY %= 30;
            }


            int attributePalette = getAttributePalette(nameTableBaseAddress, combinedX % 32, combinedY % 30);
            loadBGPalette(attributePalette);

            int tile = ppuRead(nameTableBaseAddress + (combinedX + ((combinedY) * numColumns))) & 0xff;
            int d1 = ppuRead(charBaseAddress + (tile * 16) + ((yOffs + fy) % 8));
            int d2 = ppuRead(charBaseAddress + (tile * 16) + ((yOffs + fy) % 8) + 8);
//
            renderTileRow(bd, (col * 8) - fx, (row * 8) + (scanline) % 8, d1, d2, 0, false, false);
        }

    }

    public void renderSpriteRow(BasicDisplay bd, int scanline) {
        OAM oam;
        int spriteCount = 0;

        int charBaseAddress = 0x0;
        if ((PPUCTRL & 0b1000) > 0) charBaseAddress = 0x1000;

        for (int i = 0; i < 64; i++) {
            oam = oamTable[i];
            if (oam.yPos + 7 < scanline || oam.yPos > scanline) continue;
            spriteCount++;

            int yOffs = scanline - oam.yPos;

            // Handle y-flipped sprite
            int dataOffset = ((oam.attributes & 0b10000000) == 0) ? yOffs : 7 - yOffs;

            int d1 = ppuRead(charBaseAddress + (oam.tileIndex * 16) + dataOffset);
            int d2 = ppuRead(charBaseAddress + (oam.tileIndex * 16) + dataOffset + 8);
            boolean flipH = (oam.attributes & 0b01000000) > 0;

            loadSpritePalette(oam.attributes & 0b11);

            //bd.drawRect(oam.xPos * 2, oam.yPos * 2, 8 * 2, 8 * 2);
            renderTileRow(bd, oam.xPos, oam.yPos + yOffs, d1, d2, yOffs, true, flipH);
        }

        if (spriteCount > 6) spriteOverflow = true;
    }

    // Double height sprites
    public void renderSpriteRow16(BasicDisplay bd, int scanline) {
        OAM oam;

        int charBaseAddress;

        int secondSprite = 0;
        for (int i = 0; i < 64; i++) {
            oam = oamTable[i];

            charBaseAddress = ((oam.tileIndex & 0b01) == 0) ? 0 : 0x1000;
            int tileIndex = oam.tileIndex & 0b1111_1110;

            if (oam.yPos + 15 < scanline || oam.yPos > scanline) continue;

            int yOffs = scanline - oam.yPos;
            int tileToggle = 0;
            if (yOffs > 7) {
                secondSprite = 1;
                tileToggle = 1;
                yOffs &= 0b111;
            }

            // Handle y-flipped sprite
            int dataOffset = ((oam.attributes & 0b10000000) == 0) ? yOffs : 7 - yOffs;
            boolean flipH = (oam.attributes & 0b01000000) > 0;

            // If y is flipped, we need to swap the tiles too.
            if ((oam.attributes & 0b10000000) > 0) {
                if (tileToggle == 0) tileToggle = 1;
                else tileToggle = 0;
            }

            int d1 = ppuRead(charBaseAddress + ((tileIndex + tileToggle) * 16) + dataOffset);
            int d2 = ppuRead(charBaseAddress + ((tileIndex + tileToggle) * 16) + dataOffset + 8);


            loadSpritePalette(oam.attributes & 0b11);

            //bd.drawRect(oam.xPos * 2, oam.yPos * 2, 8 * 2, 8 * 2);
            renderTileRow(bd, oam.xPos, oam.yPos + (secondSprite * 8) + yOffs, d1, d2, yOffs & 0b111, true, flipH);
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
                bd.drawFilledRect((x + p) * scale, (y) * scale, scale, scale);
            }
        }
    }

    public int getAttributePalette(int nameTableBaseAddress, int col, int row) {
        int x = col >> 2;
        int y = row >> 2;
        //int nameTableBaseAddress = getBaseNametable();
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

    public int getOAMData() {
        switch (OAMADDR & 0x03) {
            case 0x0:
                return oamTable[OAMADDR >> 2].yPos & 0xFF;
            case 0x1:
                return oamTable[OAMADDR >> 2].tileIndex & 0xFF;
            case 0x2:
                return oamTable[OAMADDR >> 2].attributes & 0xFF;
            case 0x3:
                return oamTable[OAMADDR >> 2].xPos & 0xFF;
            default:
                return 0;
        }
    }


    public void checkSprite0() {

        if (oamTable[0].yPos == raster) spriteZeroHit = true;

    }

    public void renderDebugOAM() {
        bd.setDrawColor(Color.blue);
        OAM oam;
        for (int i = 0; i < 64; i++) {
            oam = oamTable[i];
            bd.drawRect(oam.xPos * 2, oam.yPos * 2, 8 * 2, 8 * 2);
        }

        // Draw sprite zero again since it's special.
        bd.setDrawColor(Color.green);
        oam = oamTable[0];
        bd.drawRect((oam.xPos * 2) + 1, (oam.yPos * 2) + 1, 8 * 2, 8 * 2);
    }
}
