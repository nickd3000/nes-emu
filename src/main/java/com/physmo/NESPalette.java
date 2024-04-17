package com.physmo;

import java.awt.Color;

public class NESPalette {
    public static Color[] palette = new Color[64];

    static {
        init();
    }

    // http://www.romdetectives.com/Wiki/index.php?title=NES_Palette
    public static void init() {
        int i=0;
        palette[i++] = new Color(0x7C7C7C);
        palette[i++] = new Color(0x0000FC);
        palette[i++] = new Color(0x0000BC);
        palette[i++] = new Color(0x4428BC);
        palette[i++] = new Color(0x940084);
        palette[i++] = new Color(0xA80020);
        palette[i++] = new Color(0xA81000);
        palette[i++] = new Color(0x881400);
        palette[i++] = new Color(0x503000);
        palette[i++] = new Color(0x007800);
        palette[i++] = new Color(0x006800);
        palette[i++] = new Color(0x005800);
        palette[i++] = new Color(0x004058);
        palette[i++] = new Color(0x000000);
        palette[i++] = new Color(0x000000);
        palette[i++] = new Color(0x000000);
        palette[i++] = new Color(0xBCBCBC);
        palette[i++] = new Color(0x0078F8);
        palette[i++] = new Color(0x0058F8);
        palette[i++] = new Color(0x6844FC);
        palette[i++] = new Color(0xD800CC);
        palette[i++] = new Color(0xE40058);
        palette[i++] = new Color(0xF83800);
        palette[i++] = new Color(0xE45C10);
        palette[i++] = new Color(0xAC7C00);
        palette[i++] = new Color(0x00B800);
        palette[i++] = new Color(0x00A800);
        palette[i++] = new Color(0x00A844);
        palette[i++] = new Color(0x008888);
        palette[i++] = new Color(0x000000);
        palette[i++] = new Color(0x000000);
        palette[i++] = new Color(0x000000);
        palette[i++] = new Color(0xF8F8F8);
        palette[i++] = new Color(0x3CBCFC);
        palette[i++] = new Color(0x6888FC);
        palette[i++] = new Color(0x9878F8);
        palette[i++] = new Color(0xF878F8);
        palette[i++] = new Color(0xF85898);
        palette[i++] = new Color(0xF87858);
        palette[i++] = new Color(0xFCA044);
        palette[i++] = new Color(0xF8B800);
        palette[i++] = new Color(0xB8F818);
        palette[i++] = new Color(0x58D854);
        palette[i++] = new Color(0x58F898);
        palette[i++] = new Color(0x00E8D8);
        palette[i++] = new Color(0x787878);
        palette[i++] = new Color(0x000000);
        palette[i++] = new Color(0x000000);
        palette[i++] = new Color(0xFCFCFC);
        palette[i++] = new Color(0xA4E4FC);
        palette[i++] = new Color(0xB8B8F8);
        palette[i++] = new Color(0xD8B8F8);
        palette[i++] = new Color(0xF8B8F8);
        palette[i++] = new Color(0xF8A4C0);
        palette[i++] = new Color(0xF0D0B0);
        palette[i++] = new Color(0xFCE0A8);
        palette[i++] = new Color(0xF8D878);
        palette[i++] = new Color(0xD8F878);
        palette[i++] = new Color(0xB8F8B8);
        palette[i++] = new Color(0xB8F8D8);
        palette[i++] = new Color(0x00FCFC);
        palette[i++] = new Color(0xF8D8F8);
        palette[i++] = new Color(0x000000);
        palette[i++] = new Color(0x000000);
    }
}
