package com.physmo.microcode;

public enum MicroOp {
    TODO,
    NOP, BRK,

    GET_NEXT_BYTE,      // Read next byte to dataBus

    // Addressing modes
    SET_ADDRESS_ABSOLUTE,
    SET_ADDRESS_ABSOLUTE_INDIRECT,
    SET_ADDRESS_ABSOLUTE_X,
    SET_ADDRESS_ABSOLUTE_Y,
    SET_ADDRESS_ZERO_PAGE,
    SET_ADDRESS_ZERO_PAGE_X,
    SET_ADDRESS_ZERO_PAGE_Y,
    SET_ADDRESS_ZERO_PAGE_INDIRECT_X,
    SET_ADDRESS_ZERO_PAGE_INDIRECT_Y,
    SET_ADDRESS_RELATIVE,

    FETCH_BYTE_FROM_ADDRESS,
    STORE_BYTE_AT_ADDRESS,
    FETCH_A,
    STORE_A,


//    FETCH_8_ADDRESS,    // (Zero Page) Fetches the next 1 byte onto the lower end of the address bus
//    FETCH_16_ADDRESS,   // (Absolute) Fetches the next 2 bytes onto the Address bus
//    FETCH_BYTE_FROM_ADDR,
//    FETCH_WORD_FROM_ADDR,
//    FETCH_BYTE_FROM_ADDR_X,
//    FETCH_WORD_FROM_ADDR_Y,

    AND,
    ORA,
    EOR,


    // STORE
    STA, // Store A in memory
    STX,
    STY,


    // LOADS
    LDA,
    LDX, // Load value to X
    LDY, // Load value to Y

    INC, DEC,
    CMP,
    CPX,
    CPY,
    ADC,
    SBC,

    // INC and DEC X and Y
    DEX, DEY, INX, INY,

    // TRANSFERS
    TAX, TAY, TSX, TXA, TXS, TYA,

    // FLAGS
    CLC, CLD, CLI, CLV, SEC, SED, SEI,

    // JUMP and CALL
    JSR,
    JMP,

    // RETURN
    RTS, RTI,

    // BRANCH
    BCC, BCS, BEQ, BMI, BNE, BPL, BVC, BVS,

    // ROTATE and SHIFT
    ROL,
    ROR,
    ASL,
    LSR,

    // BINARY
    BIT,

    // STACK
    PHA, PHP, PLA, PLP,

    /////////////////////////////////////////////////////
    // UNDOCUMENTED INSTRUCTIONS
    SAX, ANC, ASR, JAM,

}

