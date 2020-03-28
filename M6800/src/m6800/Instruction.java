/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package m6800;

/**
 *
 * @author daves
 */
public class Instruction {
    public enum CommandID {
        ABA,
        ADCA,
        ADCB,
        ADDA,
        ADDB,
        ANDA,
        ANDB,
        ASL,
        ASLA,
        ASLB,
        ASR,
        ASRA,
        ASRB,
        BCC,
        BCS,
        BEQ,
        BGE,
        BGT,
        BHI,
        BITA,
        BITB,
        BLE,
        BLS,
        BLT,
        BMI,
        BNE,
        BPL,
        BRA,
        BSR,
        BVC,
        BVS,
        CBA,
        CLC,
        CLI,
        CLR,
        CLRA,
        CLRB,
        CLV,
        CMPA,
        CMPB,
        COM,
        COMA,
        COMB,
        CPX,
        DAA,
        DEC,
        DECA,
        DECB,
        DES,
        DEX,
        EORA,
        EORB,
        INC,
        INCA,
        INCB,
        INS,
        INX,
        JMP,
        JSR,
        LDAA,
        LDAB,
        LDX,
        LDS,
        LSR,
        LSRA,
        LSRB,
        NEG,
        NEGA,
        NEGB,
        NOP,
        ORAA,
        ORAB,
        PSHA,
        PSHB,
        PULA,
        PULB,
        ROL,
        ROLA,
        ROLB,
        ROR,
        RORA,
        RORB,
        RTI,
        RTS,
        SBA,
        SBCA,
        SBCB,
        SEC,
        SEI,
        SEV,
        STAA,
        STAB,
        STS,
        STX,
        SUBA,
        SUBB,
        SWI,
        TAB,
        TAP,
        TBA,
        TPA,
        TST,
        TSTA,
        TSTB,
        TSX,
        TXS,
        WAI,
        INVALID,
    };
    
    public enum AddressMode {
        IMMEDIATE,
        DIRECT,
        EXTENDED,
        INDEXED,
        RELATIVE,
        INHERENT
    };
    
    final CommandID ID;
    final int opcode;
    final AddressMode mode;
    final int cycles;
    final int commandlength;
    
    public Instruction(CommandID ID, int opcode, AddressMode mode, int cycles, int length)
    {
        this.ID = ID;
        this.opcode = opcode;
        this.mode = mode;
        this.cycles = cycles;
        this.commandlength = length;
    }
}
