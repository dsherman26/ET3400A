/*
 * CPU.java
** Implements  functionality of Motorola 6800/6802/6802 processor.
**
** Dave Sherman 02/25/2020
**
** Revisions:
** 3/14/2020 Fix BGT instruction
** 3/18/2020 Fix BGE instruction
** 3/18/2010 Set command state appropriately in reset()
** 3/28/2020 Add halt
 */
package m6800;
/**
 *
 * @author daves
 */
public class CPU {
        private int ACCA;
        private int ACCB;
        private int IX;
        private int PC;
        private int SP;
        private boolean C;
        private boolean V;
        private boolean Z;
        private boolean N;
        private boolean I;
        private boolean H;
        private boolean WAIFlag;
        private boolean IRQFlag;
        private boolean NMIFlag;
        private boolean ResetReq;
        private boolean Halted = false;
        public final int NUMCOMMANDS = 198; // per 6800 reference, there are 197
                                     // possible opcodes, add 1 for invalid opcode
        public final int MEMEND = 0xFFFF;
        public static final int MinClockDelay = 10;
        public static final int MaxClockDelay = 1000;
        public static final int DefaultClockDelay = 100;
        private int lastLocation; // for instructions that modify a value
        
        private int debugstop = 0x0024;
        private boolean debug;
        private enum CommandStates {
            COMMAND,
            CLOCKWAIT
        };
        
        private enum Register
        {
            A,
            B,
            IX,
            SP
        };
        
        private CommandStates state;
        private MemoryModule mem;
        private int clockstep;
        private Instruction CurrentInstruction;

        private int ClockDelay = DefaultClockDelay;
/*
**      Reset - init CPU to reset state
*/
        public void Reset ()
        {
            ACCA = 0;
            ACCB = 0;
            IX = 0;
            SP = 0;
            PC = ((mem.MemRead(MEMEND-1) << 8) + mem.MemRead(MEMEND));
            clockstep = 0;
            state = CommandStates.COMMAND;
            WAIFlag = false;
            IRQFlag = false;
            NMIFlag = false;
            ResetReq = false;
        }
        
        public void Halt (boolean bHalt)
        {
            Halted = bHalt;
        }
        
        public void ResetRequest ()
        {
            ResetReq = true;
        }
/*
**      Clock - excute one clock cycle.  Decrement clock cycles and execute
**      instruction when cycles have expired.
*/        
        public void clock()
        {
            if(ResetReq)
                Reset();
            else if(!WAIFlag && !Halted)
            {
                switch(state)
                {
                    case COMMAND:
                        if(PC == debugstop)
                            debug = true;
                        CurrentInstruction = InstructionLookup(mem.MemRead(PC));
                        clockstep = CurrentInstruction.cycles - 1;
                        state = CommandStates.CLOCKWAIT;
                        PC++;
                    break;
                    case CLOCKWAIT:
                        if((clockstep == 0) || (--clockstep == 0))
                        {
                            DoInstruction(CurrentInstruction.ID, CurrentInstruction.mode);
                            state = CommandStates.COMMAND;
                        }
                    break;
                }
            }
        }

/*
**      IRQ - simulate user IRQ
*/
        public void IRQ ()
        {
            if(!I)
            {
                if(!WAIFlag)
                {
                    push16(PC);
                    push16(IX);
                    push8(ACCA);
                    push8(ACCB);
                    push8(GetConditionCode());
                }
                I = true;
                IRQFlag = true;
            }
        }

/*
**      NMI - Simulate NMI signal
*/        
        public void NMI ()
        {
            NMIFlag = true;
            push16(PC);
            push16(IX);
            push8(ACCA);
            push8(ACCB);
            push8(GetConditionCode());
            I = true;
        }
        
        private Instruction CPUInstructions[];

/*
**      InitInstructins - populate instruction set with opcode, enumerated instruction, clock cycles, and addressing mode
*/
        private void InitInstructions () 
        {
            int icounter = 0;
            CPUInstructions  = new Instruction[NUMCOMMANDS];
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ABA, 0x1B, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ADCA,0x89, Instruction.AddressMode.IMMEDIATE, 2, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ADCA,0x99, Instruction.AddressMode.DIRECT, 3, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ADCA, 0xB9, Instruction.AddressMode.EXTENDED, 4, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ADCA, 0xA9, Instruction.AddressMode.INDEXED, 5, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ADCB,0xC9, Instruction.AddressMode.IMMEDIATE, 2, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ADCB,0xD9, Instruction.AddressMode.DIRECT, 3, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ADCB, 0xF9, Instruction.AddressMode.EXTENDED, 4, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ADCB, 0xE9, Instruction.AddressMode.INDEXED, 5, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ADDA,0x8B, Instruction.AddressMode.IMMEDIATE, 2, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ADDA,0x9B, Instruction.AddressMode.DIRECT, 3, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ADDA, 0xBB, Instruction.AddressMode.EXTENDED, 4, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ADDA, 0xAB, Instruction.AddressMode.INDEXED, 5, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ADDB,0xCB, Instruction.AddressMode.IMMEDIATE, 2, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ADDB,0xDB, Instruction.AddressMode.DIRECT, 3, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ADDB, 0xFB, Instruction.AddressMode.EXTENDED, 4, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ADDB, 0xEB, Instruction.AddressMode.INDEXED, 5, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ANDA,0x84, Instruction.AddressMode.IMMEDIATE, 2, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ANDA,0x94, Instruction.AddressMode.DIRECT, 3, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ANDA, 0xB4, Instruction.AddressMode.EXTENDED, 4, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ANDA, 0xA4, Instruction.AddressMode.INDEXED, 5, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ANDB, 0xC4, Instruction.AddressMode.IMMEDIATE, 2, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ANDB, 0xD4, Instruction.AddressMode.DIRECT, 3, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ANDB, 0xF4, Instruction.AddressMode.EXTENDED, 4, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ANDB, 0xE4, Instruction.AddressMode.INDEXED, 5, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ASL, 0x78, Instruction.AddressMode.EXTENDED, 6, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ASL, 0x68, Instruction.AddressMode.INDEXED, 7, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ASLA, 0x48, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ASLB, 0x58, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ASR, 0x77, Instruction.AddressMode.EXTENDED, 6, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ASR, 0x67, Instruction.AddressMode.INDEXED, 7, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ASRA, 0x47, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ASRB, 0x57, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BCC, 0x24, Instruction.AddressMode.INHERENT, 4, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BCS, 0x25, Instruction.AddressMode.INHERENT, 4, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BEQ, 0x27, Instruction.AddressMode.INHERENT, 4, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BGE, 0x2C, Instruction.AddressMode.INHERENT, 4, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BGT, 0x2E, Instruction.AddressMode.INHERENT, 4, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BHI, 0x22, Instruction.AddressMode.INHERENT, 4, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BITA, 0x85, Instruction.AddressMode.IMMEDIATE, 2, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BITA, 0x95, Instruction.AddressMode.DIRECT, 3, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BITA, 0xB5, Instruction.AddressMode.EXTENDED, 4, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BITA, 0xA5, Instruction.AddressMode.INDEXED, 5, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BITB, 0xC5, Instruction.AddressMode.IMMEDIATE, 2, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BITB, 0xD5, Instruction.AddressMode.DIRECT, 3, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BITB, 0xF5, Instruction.AddressMode.EXTENDED, 4, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BITB, 0xE5, Instruction.AddressMode.INDEXED, 5, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BLE, 0x2F, Instruction.AddressMode.INHERENT, 4, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BLS, 0x23, Instruction.AddressMode.INHERENT, 4, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BLT, 0x2D, Instruction.AddressMode.INHERENT, 4, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BMI, 0x2B, Instruction.AddressMode.INHERENT, 4, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BNE, 0x26, Instruction.AddressMode.INHERENT, 4, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BPL, 0x2A, Instruction.AddressMode.INHERENT, 4, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BRA, 0x20, Instruction.AddressMode.INHERENT, 4, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BSR, 0x8D, Instruction.AddressMode.INHERENT, 8, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BVC, 0x28, Instruction.AddressMode.INHERENT, 4, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.BVS, 0x29, Instruction.AddressMode.INHERENT, 4, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.CBA, 0x11, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.CLC, 0x0C, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.CLI, 0x0E, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.CLR, 0x7F, Instruction.AddressMode.EXTENDED, 6, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.CLR, 0x6F, Instruction.AddressMode.INDEXED, 7, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.CLRA, 0x4F, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.CLRB, 0x5F, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.CLV, 0x0A, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.CMPA, 0x81, Instruction.AddressMode.IMMEDIATE, 2, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.CMPA, 0x91, Instruction.AddressMode.DIRECT, 3, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.CMPA, 0xB1, Instruction.AddressMode.EXTENDED, 4, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.CMPA, 0xA1, Instruction.AddressMode.INDEXED, 5, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.CMPB, 0xC1, Instruction.AddressMode.IMMEDIATE, 2, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.CMPB, 0xD1, Instruction.AddressMode.DIRECT, 3, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.CMPB, 0xF1, Instruction.AddressMode.EXTENDED, 4, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.CMPB, 0xE1, Instruction.AddressMode.INDEXED, 5, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.COM, 0x73, Instruction.AddressMode.EXTENDED, 6, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.COM, 0x63, Instruction.AddressMode.INDEXED, 7, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.COMA, 0x43, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.COMB, 0x53, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.CPX, 0x8C, Instruction.AddressMode.IMMEDIATE, 3, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.CPX, 0x9C, Instruction.AddressMode.DIRECT, 4, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.CPX, 0xBC, Instruction.AddressMode.EXTENDED, 5, 3);           
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.CPX, 0xAC, Instruction.AddressMode.INDEXED, 6, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.DAA, 0x19, Instruction.AddressMode.INDEXED, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.DEC, 0x7A, Instruction.AddressMode.EXTENDED, 6, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.DEC, 0x6A, Instruction.AddressMode.INDEXED, 7, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.DECA, 0x4A, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.DECB, 0x5A, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.DES, 0x34, Instruction.AddressMode.INHERENT, 4, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.DEX, 0x09, Instruction.AddressMode.INHERENT, 4, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.EORA, 0x88, Instruction.AddressMode.IMMEDIATE, 2, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.EORA, 0x98, Instruction.AddressMode.DIRECT, 3, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.EORA, 0xB8, Instruction.AddressMode.EXTENDED, 4, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.EORA, 0xA8, Instruction.AddressMode.INDEXED, 5, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.EORB, 0xC8, Instruction.AddressMode.IMMEDIATE, 2, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.EORB, 0xD8, Instruction.AddressMode.DIRECT, 3, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.EORB, 0xF8, Instruction.AddressMode.EXTENDED, 4, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.EORB, 0xE8, Instruction.AddressMode.INDEXED, 5, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.INC, 0x7C, Instruction.AddressMode.EXTENDED, 6, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.INC, 0x6C, Instruction.AddressMode.INDEXED, 7, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.INCA, 0x4C, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.INCB, 0x5C, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.INS, 0x31, Instruction.AddressMode.INHERENT, 4, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.INX, 0x08, Instruction.AddressMode.INHERENT, 4, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.JMP, 0x7E, Instruction.AddressMode.EXTENDED, 3, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.JMP, 0x6E, Instruction.AddressMode.INDEXED, 4, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.JSR, 0xBD, Instruction.AddressMode.EXTENDED, 9, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.JSR, 0xAD, Instruction.AddressMode.INDEXED, 8, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.LDAA, 0x86, Instruction.AddressMode.IMMEDIATE, 2, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.LDAA, 0x96, Instruction.AddressMode.DIRECT, 3, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.LDAA, 0xB6, Instruction.AddressMode.EXTENDED, 4, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.LDAA, 0xA6, Instruction.AddressMode.INDEXED, 5, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.LDAB, 0xC6, Instruction.AddressMode.IMMEDIATE, 2, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.LDAB, 0xD6, Instruction.AddressMode.DIRECT, 3, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.LDAB, 0xF6, Instruction.AddressMode.EXTENDED, 4, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.LDAB, 0xE6, Instruction.AddressMode.INDEXED, 5, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.LDS, 0x8E, Instruction.AddressMode.IMMEDIATE, 3, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.LDS, 0x9E, Instruction.AddressMode.DIRECT, 4, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.LDS, 0xBE, Instruction.AddressMode.EXTENDED, 5, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.LDS, 0xAE, Instruction.AddressMode.INDEXED, 6, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.LDX, 0xCE, Instruction.AddressMode.IMMEDIATE, 3, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.LDX, 0xDE, Instruction.AddressMode.DIRECT, 4, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.LDX, 0xFE, Instruction.AddressMode.EXTENDED, 5, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.LDX, 0xEE, Instruction.AddressMode.INDEXED, 6, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.LSR, 0x74, Instruction.AddressMode.EXTENDED, 6, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.LSR, 0x64, Instruction.AddressMode.INDEXED, 7, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.LSRA, 0x44, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.LSRB, 0x54, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.NEG, 0x70, Instruction.AddressMode.EXTENDED, 6, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.NEG, 0x60, Instruction.AddressMode.INDEXED, 7, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.NEGA, 0x40, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.NEGB, 0x50, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.NOP, 0x1, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ORAA, 0x8A, Instruction.AddressMode.IMMEDIATE, 2, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ORAA, 0x9A, Instruction.AddressMode.DIRECT, 3, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ORAA, 0xBA, Instruction.AddressMode.EXTENDED, 4, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ORAA, 0xAA, Instruction.AddressMode.INDEXED, 5, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ORAB, 0xDA, Instruction.AddressMode.IMMEDIATE, 2, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ORAB, 0xDA, Instruction.AddressMode.DIRECT, 3, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ORAB, 0xFA, Instruction.AddressMode.EXTENDED, 4, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ORAB, 0xEA, Instruction.AddressMode.INDEXED, 5, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.PSHA, 0x36, Instruction.AddressMode.INHERENT, 4, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.PSHB, 0x37, Instruction.AddressMode.INHERENT, 4, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.PULA, 0x32, Instruction.AddressMode.INHERENT, 4, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.PULB, 0x33, Instruction.AddressMode.INHERENT, 4, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ROL, 0x79, Instruction.AddressMode.EXTENDED, 6, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ROL, 0x69, Instruction.AddressMode.INDEXED, 7, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ROLA, 0x49, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ROLB, 0x59, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ROR, 0x76, Instruction.AddressMode.EXTENDED, 6, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.ROR, 0x66, Instruction.AddressMode.INDEXED, 7, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.RORA, 0x46, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.RORB, 0x56, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.RTI, 0x3B, Instruction.AddressMode.INHERENT, 10, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.RTS, 0x39, Instruction.AddressMode.INHERENT, 5, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.SBA, 0x10, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.SBCA, 0x82, Instruction.AddressMode.IMMEDIATE, 2, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.SBCA, 0x92, Instruction.AddressMode.DIRECT, 3, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.SBCA, 0xB2, Instruction.AddressMode.EXTENDED, 4, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.SBCA, 0xA2, Instruction.AddressMode.IMMEDIATE, 5, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.SBCB, 0xC2, Instruction.AddressMode.IMMEDIATE, 2, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.SBCB, 0xD2, Instruction.AddressMode.DIRECT, 3, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.SBCB, 0xF2, Instruction.AddressMode.EXTENDED, 4, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.SBCB, 0xE2, Instruction.AddressMode.IMMEDIATE, 5, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.SEC, 0x0D, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.SEI, 0x0F, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.SEV, 0x0B, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.STAA, 0x97, Instruction.AddressMode.DIRECT, 4, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.STAA, 0xB7, Instruction.AddressMode.EXTENDED, 5, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.STAA, 0xA7, Instruction.AddressMode.INDEXED, 6, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.STAB, 0xD7, Instruction.AddressMode.DIRECT, 4, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.STAB, 0xF7, Instruction.AddressMode.EXTENDED, 5, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.STAB, 0xE7, Instruction.AddressMode.INDEXED, 6, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.STS, 0x9F, Instruction.AddressMode.DIRECT, 5, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.STS, 0xBF, Instruction.AddressMode.EXTENDED, 6, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.STS, 0xAF, Instruction.AddressMode.INDEXED, 7, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.STX, 0xDF, Instruction.AddressMode.DIRECT, 5, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.STX, 0xFF, Instruction.AddressMode.EXTENDED, 6, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.STX, 0xEF, Instruction.AddressMode.INDEXED, 7, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.SUBA, 0x80, Instruction.AddressMode.IMMEDIATE, 2, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.SUBA, 0x90, Instruction.AddressMode.DIRECT, 3, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.SUBA, 0xB0, Instruction.AddressMode.EXTENDED, 4, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.SUBA, 0xA0, Instruction.AddressMode.IMMEDIATE, 5, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.SUBB, 0xC0, Instruction.AddressMode.IMMEDIATE, 2, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.SUBB, 0xD0, Instruction.AddressMode.DIRECT, 3, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.SUBB, 0xF0, Instruction.AddressMode.EXTENDED, 4, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.SUBB, 0xE0, Instruction.AddressMode.IMMEDIATE, 5, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.SWI, 0x3F, Instruction.AddressMode.INHERENT, 12, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.TAB, 0x16, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.TAP, 0x06, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.TBA, 0x17, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.TPA, 0x07, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.TST, 0x7D, Instruction.AddressMode.EXTENDED, 6, 3);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.TST, 0x6D, Instruction.AddressMode.INDEXED, 7, 2);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.TSTA, 0x4D, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.TSTB, 0x5D, Instruction.AddressMode.INHERENT, 2, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.TSX, 0x30, Instruction.AddressMode.INHERENT, 4, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.TXS, 0x35, Instruction.AddressMode.INHERENT, 4, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.WAI, 0x3E, Instruction.AddressMode.INHERENT, 9, 1);
            CPUInstructions[icounter++] = new Instruction(Instruction.CommandID.INVALID, 0x0, Instruction.AddressMode.INHERENT, 1, 1); //terminator
        }   

/*
**      InstructionLookup - Decode instruction opcode
*/        
        Instruction InstructionLookup(int opcode)
        {
            Instruction instruction = CPUInstructions[0];
            int icounter;
            for(icounter = 0; icounter < NUMCOMMANDS; icounter++)
            {
                instruction = CPUInstructions[icounter];
                if(instruction.opcode == opcode)
                    break;
            }
            return (instruction);
        }

/*
**      CPU - Constructor.  Pass MemoryModule object as argument
*/
        public CPU(MemoryModule mem)
        {
            this.mem = mem;
            state = CommandStates.COMMAND;
            InitInstructions();
            Reset();
        }

/*
**      DoInstruction - Execute instruction using appropriate addressing mode for arguments
*/        
        private void DoInstruction(Instruction.CommandID ID, Instruction.AddressMode mode)
        {
            switch(ID)
            {
                case ABA:
                    ABA();
                break;
                case ADCA:
                    ADD(mode, Register.A, true);
                break;
                case ADCB:
                    ADD(mode, Register.B, true);
                break;
                case ADDA:
                    ADD(mode, Register.A, false);
                break;
                case ADDB:
                    ADD(mode, Register.B, false);
                break;
                case ANDA:
                    AND(mode, Register.A);
                break;
                case ANDB:
                    AND(mode, Register.B);
                break;
                case ASL:
                    ASL(mode);
                break;
                case ASLA:
                    ASLReg(Register.A);
                break;
                case ASLB:
                    ASLReg(Register.B);
                break;
                case ASR:
                    ASR(mode);
                break;
                case ASRA:
                    ASRReg(Register.A);
                break;
                case ASRB:
                    ASRReg(Register.B);
                break;
                case BCC:
                    if(!C)
                        branch();
                    else
                        PC++;
                break;
                case BCS:
                    if(C)
                        branch();
                    else
                        PC++;
                break;
                case BEQ:
                    if(Z)
                        branch();
                    else
                        PC++;
                break;
                case BGE:
                    if((N && V) || (!N && !V))
                        branch();
                    else
                        PC++;
                break;
                case BGT:
                    if(!Z && ((N && V) || (!N && !V)))
                        branch();
                    else
                        PC++;
                break;
                case BHI:
                    if(!C && !Z)
                        branch();
                    else
                        PC++;
                break;
                case BITA:
                    BIT(mode, Register.A);
                break;
                case BITB:
                    BIT(mode, Register.B);
                break;
                case BLE:
                    if(Z || ((N && !V) || (!N && V)))
                        branch();
                    else
                        PC++;
                break;
                case BLS:
                    if(C || Z)
                        branch();
                    else
                        PC++;
                break;
                case BLT:
                    if((N && !V) || (!N && V))
                        branch();
                    else
                        PC++;
                break;
                case BMI:
                    if (N)
                        branch();
                    else
                        PC++;
                break;
                case BNE:
                    if (!Z)
                        branch();
                    else
                        PC++;
                break;
                case BPL:
                    if (!N)
                        branch();
                    else
                        PC++;
                break;
                case BRA:
                    branch();
                break;
                case BSR:
                    push16(PC + 1);
                    branch();
                break;
                case BVC:
                    if(!V)
                        branch();
                    else
                        PC++;
                break;
                case BVS:
                    if(V)
                        branch();
                    else
                        PC++;
                break;
                case CBA:
                    CBA();
                break;
                case CLC:
                    C = false;
                break;
                case CLI:
                    I = false;
                break;
                case CLR:
                    CLR(mode);
                break;
                case CLRA:
                    ACCA = 0;
                    Z = true;
                    N = false;
                    V = false;
                    C = false;
                break;
                case CLRB:
                    ACCB = 0;
                    Z = true;
                    N = false;
                    V = false;
                    C = false;
                break;
                case CLV:
                    V = false;
                break;
                case CMPA:
                    CMP(mode, Register.A);
                break;
                case CMPB:
                    CMP(mode, Register.B);
                break;
                case COM:
                    COM(mode);
                break;
                case COMA:
                    COMReg(Register.A);
                break;
                case COMB:
                    COMReg(Register.B);
                break;
                case CPX:
                    CPX(mode);
                break;
                case DAA:
                    DAA();
                break;
                case DEC:
                    DEC(mode);
                break;
                case DECA:
                    DECReg(Register.A);
                break;
                case DECB:
                    DECReg(Register.B);
                break;
                case DES:
                    DES();
                break;
                case DEX:
                    DEX();
                break;
                case EORA:
                    EOR(mode, Register.A);
                break;
                case EORB:
                    EOR(mode, Register.B);
                break;
                case INC:
                    INC(mode);
                break;
                case INCA:
                    INCReg(Register.A);
                break;
                case INCB:
                    INCReg(Register.B);
                break;
                case INS:
                    INS();
                break;
                case INX:
                    INX();
                break;
                case JMP:
                    JMP(mode);
                break;
                case JSR:
                    JSR(mode);
                break;
                case LDAA:
                    LD(mode, Register.A);
                break;
                case LDAB:
                    LD(mode, Register.B);
                break;
                case LDS:
                    LD(mode, Register.SP);
                break;
                case LDX:
                    LD(mode, Register.IX);
                break;
                case LSR:
                    LSR(mode);
                break;
                case LSRA:
                    LSRReg(Register.A);
                break;
                case LSRB:
                    LSRReg(Register.B);
                break;
                case NEG:
                    NEG(mode);
                break;
                case NEGA:
                    NEGReg(Register.A);
                break;
                case NEGB:
                    NEGReg(Register.B);
                break;
                case NOP:
                break;
                case ORAA:
                    ORA(mode, Register.A);
                break;
                case ORAB:
                    ORA(mode, Register.B);
                break;
                case PSHA:
                    push8(GetReg(Register.A));
                break;
                case PSHB:
                    push8(GetReg(Register.B));
                break;
                case PULA:
                    StoreReg(Register.A, pull8());
                break;
                case PULB:
                    StoreReg(Register.B, pull8());
                break;
                case ROL:
                    ROL(mode);
                break;
                case ROLA:
                    ROLReg(Register.A);
                break;
                case ROLB:
                    ROLReg(Register.B);
                break;
                case ROR:
                    ROR(mode);
                break;
                case RORA:
                    RORReg(Register.A);
                break;
                case RORB:
                    RORReg(Register.B);
                break; 
                case RTI:
                    RTI();
                break;
                case RTS:
                    RTS();
                break;
                case SBA:
                    SBA();
                break;
                case SBCA:
                    SBC(mode, Register.A);
                break;
                case SBCB:
                    SBC(mode, Register.B);
                break;
                case SEC:
                    C = true;
                break;
                case SEI:
                    I = true;
                break;
                case SEV:
                    V = true;
                break;
                case STAA:
                    STA(mode, Register.A);
                break;
                case STAB:
                    STA(mode, Register.B);
                break;
                case STS:
                    ST16(mode, SP);
                break;
                case STX:
                    ST16(mode, IX);
                break;
                case SUBA:
                    SUB(mode, Register.A);
                break;
                case SUBB:
                    SUB(mode, Register.B);
                break;
                case SWI:
                    SWI();
                break;
                case TAB:
                    ACCB = ACCA;
                    SetConditionLoad(ACCB);
                break;
                case TAP:
                    SetConditionCode(ACCA);
                break;
                case TBA:
                    ACCA = ACCB;
                    SetConditionLoad(ACCA);
                break;
                case TPA:
                    ACCA = GetConditionCode();
                break;
                case TST:
                    TST(mode);
                break;
                case TSTA:
                    TSTReg(Register.A);
                break;
                case TSTB:
                    TSTReg(Register.B);
                break;
                case TSX:
                    IX = SP + 1;
                break;
                case TXS:
                    SP = IX - 1;
                break;
                case WAI:
                    WAI();
                break;
                case INVALID: // intentional fall through
                default: //unknown instruction, treat as NOP but count for debugging purposes
                    
                break;
                    
            }
             
        }

/*
**      ABA - implement 6800 ABA command
*/
        private void ABA()
        {
            int result = ACCA + ACCB;
            SetConditionAdd(ACCA, ACCB, result);
            ACCA = result & 0xFF;
        }

/*
**      ADD - implment ADD and ADC commands.
*/
        
        private void ADD(Instruction.AddressMode mode, Register reg, boolean bCarry)
        {
            int value1, value2, result;
            value1 = GetReg(reg);
            value2 = GetArgument(mode);

            result = (value1 + value2 + (C && bCarry ? 1 : 0)) & 0xFF;
            SetConditionAdd(value1, value2, result);
            StoreReg(reg, result);
        }
        
        private void AND(Instruction.AddressMode mode, Register reg)
        {
            int value1, value2;
            value1 = GetReg(reg);
            value2 = GetArgument(mode);
            value1 = value1 & value2;
            SetConditionLoad(value1);
            StoreReg(reg, value1);
        }

        private void ASLReg(Register reg)
        {
            int value = GetReg(reg);
            C = BitTest(value,7);
            value <<= 1;
            value &= 0xFF;
            N = BitTest(value,7);
            Z = (value == 0);
            V = ((N && !C) || (!N && C));
            StoreReg(reg, value);
        }
        
        private void ASRReg(Register reg)
        {
            int value = GetReg(reg);
            boolean B7 = BitTest(value,7);
            C = BitTest(value,0);
            value >>= 1;
            if(B7)
                value |= (1<<7);
            N = BitTest(value,7);
            Z = (value == 0);
            V = ((N && !C) || (!N && C));
            StoreReg(reg, value);
        }
        
        private void ASL(Instruction.AddressMode mode)
        {
            int value = GetArgument(mode);
            C = BitTest(value,7);
            value <<= 1;
            value &= 0xFF;
            N = BitTest(value,7);
            Z = (value == 0);
            V = (N ^ C);
            StoreValue(-1, value);
        }
        
        private void ASR(Instruction.AddressMode mode)
        {
            int value = GetArgument(mode);
            C = BitTest(value,0);
            value >>= 1;
            N = BitTest(value,7);
            Z = (value == 0);
            V = (N ^ C);
            StoreValue(-1, value);
        }
        
        private void BIT(Instruction.AddressMode mode, Register reg)
        {
            int result;
            result = GetArgument(mode);
            result = result & GetReg(reg);
            N = (BitTest(result, 7));
            Z = (result == 0);
            V = false;   
        }
        
        private void CBA ()
        {
            int result = subtract8(ACCA, ACCB);
            SetConditionSubtract(ACCA, ACCB, result);
        }
        
        private void CLR (Instruction.AddressMode mode)
        {
            int value = GetArgument(mode);
            StoreValue(-1, 0);
            Z = true;
            N = false;
            V = false;
            C = false;
        }
        
        private void CMP (Instruction.AddressMode mode, Register reg)
        {
            int value1, value2 = GetArgument(mode);
            int result;
            
            if (reg == Register.A)
                value1 = ACCA;
            else
                value1 = ACCB;
            result = subtract8(value1, value2);
            SetConditionSubtract(value1, value2, result);
        }
        
        private void COM (Instruction.AddressMode mode)
        {
            int value = GetArgument(mode);
            value = ~value & 0xFF;
            N = BitTest(value, 7);
            Z = (value == 0);
            V = false;
            C = true;
            StoreValue(-1, value);
        }
        
        private void COMReg (Register reg)
        {
            int value = GetReg(reg);
            value = ~value & 0xFF;
            N = BitTest(value, 7);
            Z = (value == 0);
            V = false;
            C = true;
            StoreReg(reg, value);
        }
        
        private void CPX (Instruction.AddressMode mode)
        {
            int value2 = GetArgument16(mode);
            int result = subtract16 (IX, value2);
            N = BitTest(result, 15);
            Z = (result == 0);
            V = ((BitTest(IX, 15) && !BitTest(value2, 7) && !BitTest(result,15)) ||
                (!BitTest(IX, 15) && BitTest(value2, 7) && BitTest(result,15)));
        }
        
        private void DAA ()
        {
            int nibble_hi, nibble_low;
            nibble_hi = ACCA >> 4;
            nibble_low = ACCA & 0xF;
            
            if(!C)
            {
                if ((nibble_hi < 9) && (nibble_low > 9) && !H)
                    ACCA += 0x6;
                if ((nibble_hi < 10) && (nibble_low < 4) && H)
                    ACCA += 0x6;
                if((nibble_hi > 9) && (nibble_low < 10) && !H)
                {
                    ACCA += 0x60;
                    C = true;
                }
                if((nibble_hi > 8) && (nibble_low > 9) && !H)
                {
                    ACCA += 0x66;
                    C = true;
                }
                if((nibble_hi > 9) && (nibble_low  < 4) && H)
                {
                    ACCA += 0x66;
                    C = true;
                }
            }
            else
            {
                if((nibble_hi < 3) && (nibble_low < 10) && !H)
                    ACCA += 0x60;
                if((nibble_hi < 3) && (nibble_low > 9) && !H)
                    ACCA += 0x66;
                if((nibble_hi < 4) && (nibble_low < 4) && H)
                    ACCA += 0x66;
            }
            N = (BitTest(ACCA, 7));
            Z = (ACCA == 0);
        }
        
        private void DEC (Instruction.AddressMode mode)
        {
            int value = GetArgument(mode);
            value = DEC8 (value);
            N = BitTest(value, 7);
            Z = (value == 0);
            V = (value == 0x7F);
            StoreValue(-1, value);
        }
        
        private void DECReg (Register reg)
        {
            int value = GetReg(reg);
            value = DEC8 (value);
            N = BitTest(value, 7);
            Z = (value == 0);
            V = (value == 0x7F);
            StoreReg(reg, value);
        }
        
        private int DEC8 (int arg)
        {
            if(arg > 0)
                return (arg - 1);
            else
                return (255);
        }
        
        private void DES ()
        {
            if(SP > 0)
                SP--;
            else
                SP = 65535;
        }
        
        private void DEX ()
        {
            if(IX > 0)
                IX--;
            else
                IX = 65535;
            Z = (IX == 0);          
        }
        
        private void EOR (Instruction.AddressMode mode, Register reg)
        {
            int result = GetArgument(mode);
            result = (result ^ GetReg(reg));
            N = BitTest(result, 7);
            Z = (result == 0);
            V = false;
            StoreReg(reg, result);
        }
        
        private void INC (Instruction.AddressMode mode)
        {
            int value = GetArgument(mode);
            value = INC8 (value);
            N = BitTest(value, 7);
            Z = (value == 0);
            V = (value == 0x80);
            StoreValue(-1, value);
        }
        
        private void INCReg (Register reg)
        {
            int value = GetReg(reg);
            value = INC8 (value);
            N = BitTest(value, 7);
            Z = (value == 0);
            V = (value == 0x80);
            StoreReg(reg, value);
        }
        
        private int INC8 (int arg)
        {
            if(arg < 255)
                return (arg + 1);
            else
                return (0);
        }
        
        private void INS ()
        {
            if (SP < 65535)
                SP++;
            else
                SP = 0;
        }
        
        private void INX ()
        {
            if (IX < 65535)
                IX++;
            else
                IX = 0;
            Z = (IX == 0);
        }

        private void JMP (Instruction.AddressMode mode)
        {
            int result = GetJump(mode);
            PC = result;
        }
        
        private void JSR (Instruction.AddressMode mode)
        {
            int result = GetJump(mode);
            //note that GetArgument has already incremented PC according to the mode
            push16(PC);
            PC = result;
        }

/*
**      branch - do branch for all Bxx instructions.  Calculate positive or negative
        offset of PC using 8 bit two's complement value from the instruction
*/
        private void branch ()
        {
            int rel = mem.MemRead(PC);
            rel = TwosComplement8Bit(rel);
            PC = PC + 1 + rel;
        }
        
        private void LD(Instruction.AddressMode mode, Register reg)
        {
            int value;

            switch (reg)
            {
                case A:
                    value = GetArgument(mode);
                    SetConditionLoad(value);
                    ACCA = value;
                break;
                case B:
                    value = GetArgument(mode);
                    SetConditionLoad(value);
                    ACCB = value;
                break;
                case IX:
                    value = GetArgument16(mode);
                    SetConditionLoad16(value);
                    IX = value;
                break;
                case SP:
                    value = GetArgument16(mode);
                    SetConditionLoad16(value);
                    SP = value;
                break;
            }
        }
        
        private void LSR (Instruction.AddressMode mode)
        {
            int ivalue = GetArgument(mode);
            ivalue = SetConditionLSR(ivalue);
            StoreValue(-1, ivalue);
        }
        
        private void LSRReg(Register reg)
        {
            int ivalue = GetReg(reg);
            ivalue = SetConditionLSR(ivalue);
            StoreReg(reg, ivalue);
        }
        
        private void NEG (Instruction.AddressMode mode)
        {
            int ivalue = GetArgument(mode);
            ivalue = Negate8(ivalue);
            StoreValue(-1, ivalue);
        }
        
        private void NEGReg (Register reg)
        {
            int ivalue = GetReg(reg);
            ivalue = Negate8(ivalue);
            StoreReg(reg, ivalue);
        }
        
        private void ORA (Instruction.AddressMode mode, Register reg)
        {
            int ivalue = GetArgument(mode);
            int ivalue2 = GetReg(reg);
            ivalue2 = ivalue2 | ivalue;
            N = BitTest(ivalue2, 7);
            Z = (ivalue2 == 0);
            V = false;
            StoreReg(reg, ivalue2);
        }
        
        private void ROL (Instruction.AddressMode mode)
        {
            int ivalue = GetArgument(mode);
            ivalue = SetConditionROL(ivalue);
            StoreValue(-1, ivalue);
        }
 
        private void ROLReg (Register reg)
        {
            int ivalue = GetReg(reg);
            ivalue = SetConditionROL(ivalue);
            StoreReg(reg, ivalue);
        }
        
        private void ROR (Instruction.AddressMode mode)
        {
            int ivalue = GetArgument(mode);
            ivalue = SetConditionROR(ivalue);
            StoreValue(-1, ivalue);
        }
 
        private void RORReg (Register reg)
        {
            int ivalue = GetReg(reg);
            ivalue = SetConditionROR(ivalue);
            StoreReg(reg, ivalue);
        }
        
        private void RTI ()
        {
            SetConditionCode(pull8());
            StoreReg(Register.B, pull8());
            StoreReg(Register.A, pull8());
            IX = pull16();
            PC = pull16();
        }
        
        private void RTS ()
        {
            PC = pull16();
        }
     
        private void SBA ()
        {
            int result = subtract8(ACCA, ACCB);
            SetConditionSubtract(ACCA, ACCB, result);
            ACCA = result;
        }
        
        private void SBC (Instruction.AddressMode mode, Register reg)
        {
            int ivalue1 = GetReg(reg);
            int ioldValue = GetArgument(mode);
            int ivalue2 = ioldValue + (C ? 1 : 0);
            int result = subtract8(ivalue1, ivalue2);
            SetConditionSubtract(ivalue1, ioldValue, result);
            StoreReg(reg, result);
        }
        
        private void STA (Instruction.AddressMode mode, Register reg)
        {
            int ivalue = GetReg(reg);
            int index = 0;
            SetConditionLoad(ivalue);
            switch (mode)
            {
                case DIRECT:
                  index = mem.MemRead(PC++);
                break;
                case EXTENDED:
                  index = (mem.MemRead(PC) << 8) + mem.MemRead(PC+1);
                  PC += 2;
                break;
                case INDEXED:
                  index = IX + mem.MemRead(PC++);
                break;
            }
            StoreValue(index, ivalue);
        }
        
        private void ST16 (Instruction.AddressMode mode, int value)
        {
            int index = 0;
            switch (mode)
            {
                case DIRECT:
                    index = mem.MemRead(PC++);
                break;
                case EXTENDED:
                    index = (mem.MemRead(PC) << 8) + mem.MemRead(PC+1);
                    PC += 2;
                break;
                case INDEXED:
                    index = IX + mem.MemRead(PC++);
                break;
            }
            N = BitTest(value, 15);
            Z = (value == 0);
            V = false;
            mem.MemWrite(index, value >> 8);
            mem.MemWrite(index+1, value);
        }
        
        private void SUB (Instruction.AddressMode mode, Register reg)
        {
            int ivalue1 = GetReg(reg);
            int ivalue2 = GetArgument(mode);
            int result = subtract8(ivalue1, ivalue2);
            SetConditionSubtract(ivalue1, ivalue2, result);
            StoreReg(reg, result);
        }
        
        private void SWI ()
        {
            push16(PC);
            push16(IX);
            push8(ACCA);
            push8(ACCB);
            push8(GetConditionCode());
            I = true;
            PC = ((mem.MemRead(MEMEND-5) << 8) + mem.MemRead(MEMEND-4));
        }
        
        private void TST (Instruction.AddressMode mode)
        {
            int ivalue = GetArgument(mode);
            V = false;
            C = false;
            N = (BitTest(ivalue, 7));
            Z = (ivalue == 0);
        }
        
        private void TSTReg (Register reg)
        {
            int ivalue = GetReg(reg);
            V = false;
            C = false;
            N = (BitTest(ivalue, 7));
            Z = (ivalue == 0);
        }
        
        private void WAI ()
        {
            push16(PC);
            push16(IX);
            push8(ACCA);
            push8(ACCB);
            push8(GetConditionCode());
            WAIFlag = true;
        }
        
        private int Negate8 (int ivalue)
        {
            int result = 0;
            if (ivalue != 0x80 && ivalue != 0x0)
            {
                if (!BitTest(ivalue, 7))
                    result = 256 - ivalue;
                else
                    result = (~ivalue & 0xFF) + 1;
            }
            C = (ivalue != 0);
            V = (ivalue == 0x80);
            N = BitTest(result,7);
            Z = (result == 0);
            return (result);
        }
        
        private void SetConditionAdd (int accumulator, int current, int result)
        {
            H = (BitTest(accumulator, 3) && BitTest(current, 3)) ||
                (BitTest(current, 3) && !BitTest(result, 3)) ||
                 (!BitTest(result, 3) && BitTest(accumulator, 3));
            N = BitTest(result, 7);
            Z = (result == 0);
            V = (BitTest(accumulator, 7) && BitTest(current, 7) && !BitTest(result, 7)) ||
                (!BitTest(accumulator, 7) && !BitTest(current, 7) && BitTest(result, 7));
            C = (BitTest(accumulator, 7) && BitTest(current, 7)) ||
                (BitTest(current, 7) && !BitTest(result, 7)) ||
                (!BitTest(result, 7) && BitTest(accumulator, 7));      
        }
        
        private void SetConditionSubtract (int accumulator, int current, int result)
        {
            N = BitTest(result, 7);
            Z = (result == 0);
            V = ((BitTest(accumulator, 7) && !BitTest(current, 7) && !BitTest(result,7)) ||
                (!BitTest(accumulator, 7) && BitTest(current,7) && BitTest(result, 7)));
            C = ((!BitTest(accumulator, 7) && BitTest(current, 7)) ||
                (BitTest(current, 7) && BitTest(result, 7)) ||
                (BitTest(result,7) && !BitTest(accumulator, 7)));
        }
        
        private int subtract8 (int arg1, int arg2)
        {
            if(arg1 >= arg2)
                return (arg1 - arg2);
            else
                return (256 - (arg2 - arg1));
        }
        
        private int subtract16 (int arg1, int arg2)
        {
            if(arg1 >= arg2)
                return (arg1 - arg2);
            else
                return (65536 - (arg2 - arg1));
        }

        private void SetConditionLoad (int value)
        {
            N = BitTest(value, 7);
            Z = (value == 0);
            V = false;
        }
        
        private void SetConditionLoad16 (int value)
        {
            N = BitTest(value, 15);
            Z = (value == 0);
            V = false;
        }
        
        private int SetConditionLSR (int value)
        {
            C = BitTest(value, 0);
            N = false;
            V = C;
            value >>= 1;
            Z = (value == 0);
            return (value);
        }
 
        private int SetConditionROL (int value)
        {
            int result;
            boolean bTemp = BitTest(value, 7);
            result = ((value << 1) & 0xFF) + (C ? 1 : 0);
            C = bTemp;
            N = BitTest(result, 7);
            V = ((N && !C) || (!N && C));
            Z = (result == 0);
            return (result);
        }
        
        private int SetConditionROR (int value)
        {
            int result;
            boolean bTemp = BitTest(value, 0);
            result = ((value >> 1) & 0xFF) + (C ? 0x80 : 0);
            C = bTemp;
            N = BitTest(result, 7);
            V = ((N && !C) || (!N && C));
            Z = (result == 0);
            return (result);
        }
        
        private int ImmediateValue(int length)
        {
            int result;
            // PC has incremented past the ID
            if(length > 1)
                result = (mem.MemRead(PC) << 8) + mem.MemRead(PC+1);
            else
                result = mem.MemRead(PC);
            PC+=length;
            return (result);
        }
        
        private int DirectValue()
        {
            int result = mem.MemRead(mem.MemRead(PC));
            lastLocation = mem.MemRead(PC);
            PC++;
            return (result);
        }

        private int ExtendedValue()
        {
            int result;
            result = (mem.MemRead(PC) << 8) + (mem.MemRead(PC+1));
            lastLocation = result;
            PC+=2;
            return (result);
        }
        
        private int ExtendedValueByAddress()
        {
            int address, result;
            address = (mem.MemRead(PC) << 8) + (mem.MemRead(PC+1));
            result = mem.MemRead(address);
            lastLocation = address;
            PC+=2;
            return (result);
        }        

/*
**      IndexedValue - Get 8-bit value for instructions using indexed addressing
*/        
        private int IndexedValue ()
        {
            int result,offset;
            offset = mem.MemRead(PC);
            lastLocation = IX+offset;
            result = mem.MemRead(lastLocation);
            PC++;
            return (result);
        }
        
        private int GetArgument(Instruction.AddressMode mode)
        {
            int value;
            switch(mode)
            {
                case IMMEDIATE:
                    value = ImmediateValue(CurrentInstruction.commandlength-1);         
                break;
                case DIRECT:
                    value = DirectValue();
                break;
                case EXTENDED:
                    value = ExtendedValueByAddress();
                break;
                case INDEXED:
                    value = IndexedValue();
                break;
                default:
                    value = 0;
                break;
            }
            return (value);
        }
/*
**      IndexedValue16 - Get 16 bit value for instructions using indexed addressing
*/
        private int IndexedValue16()
        {
            int result,offset;
            offset = mem.MemRead(PC);
            lastLocation = IX+offset;
            result = mem.MemRead(lastLocation);
            result = (result << 8) + mem.MemRead(lastLocation+1);
            PC++;
            return (result);
        }

/*
**      IndexedAddress - get address for jump instructions
*/        
        private int IndexedAddress()
        {
            int offset, result;
            offset = mem.MemRead(PC);
            result = IX + offset;
            PC++;
            return (result);
        }
        
        private int DirectValue16()
        {
            int result = (mem.MemRead(mem.MemRead(PC)) << 8) + mem.MemRead(mem.MemRead(PC)+1);
            lastLocation = mem.MemRead(PC);
            PC++;
            return (result);
        }
        
        private int GetArgument16(Instruction.AddressMode mode)
        {
            int value = 0;
            switch (mode)
            {
                case IMMEDIATE:
                    value = ImmediateValue(2);
                break;
                case DIRECT:
                    value = DirectValue16();
                break;
                case EXTENDED:
                    value = ExtendedValue();
                break;
                case INDEXED:
                    value = IndexedValue16();
                break;
            }
            return (value);
        }
        
        private int GetJump (Instruction.AddressMode mode)
        {
            int result = 0;
            
            switch (mode)
            {
                case EXTENDED:
                    result = ExtendedValue();
                break;
                case INDEXED:
                    result = IndexedAddress();
                break;
            }
            return (result);
        }
/*
**      StoreValue - store value in memory.
**      pass -1 for location to use last known (e.g.: ASL,ASR)
*/
        private void StoreValue(int location, int value)
        {
            if(location < 0)
                mem.MemWrite(lastLocation, value);
            else
                mem.MemWrite(location, value);
        }
        
        private int GetReg (Register reg)
        {
            return (reg == Register.A ? ACCA : ACCB);
        }
        
        private void StoreReg (Register reg, int value)
        {
            if(reg == Register.A)
                ACCA = value;
            else
                ACCB = value;
        }

/*
**      TwosComplement8Bit
**      Calculate 32-bit signed value from 8-bit unsigned representation
**      of two's complment value.  Used for calculation of branch offsets.
*/
        private int TwosComplement8Bit (int arg)
        {
            int value = arg;
            if(BitTest(arg,7))
            {
                value =  -256 + arg;
            }
            return value;
        }

/*
**      BitTest - test bit by bit position (0-7)
*/
        private boolean BitTest (int arg, int bitnum)
        {
            if((arg & (1<<bitnum)) > 0)
                return (true);
            else
                return (false);
        }

/*
**      push8 - Push 8 bit value onto stack
*/
        private void push8 (int arg)
        {
            mem.MemWrite(SP, arg);
            SP--;
        }

/*
**      pull8 - Pull 8 bit value from stack
*/
        private int pull8 ()
        {
            SP++;
            return (mem.MemRead(SP));
        }

/*
**      push16 - Push 16 bit value onto stack
*/
        private void push16 (int arg)
        {
            mem.MemWrite(SP, arg);
            SP--;
            mem.MemWrite(SP, (arg >> 8));
            SP--;
        }

/*
**      pull16 - Pull 16 bit value from stack
*/
        private int pull16 ()
        {
            int result;
            SP++;
            result = mem.MemRead(SP);
            SP++;
            result = (result << 8) + mem.MemRead(SP);
            return (result);
        }

/*
**      GetConditionCode - return condition code value based on condition bits.
**      Note that bits 7 and 6 are always set.  Used when pushing condition code
**      onto stack.
*/
        private int GetConditionCode()
        {
            int result = 0xC0;
            result += (C ? 1 : 0);
            result += (V ? 2 : 0);
            result += (Z ? 4 : 0);
            result += (N ? 8 : 0);
            result += (I ? 0x10 : 0);
            result += (H ? 0x20 : 0);
            return (result);
        }

/*
**      SetConditionCode - Set condition bits based on CC value.  Used when
**      pulling from stack.
*/
        private void SetConditionCode(int ivalue)
        {
            C = BitTest(ivalue, 0);
            V = BitTest(ivalue, 1);
            Z = BitTest(ivalue, 2);
            N = BitTest(ivalue, 3);
            I = BitTest(ivalue, 4);
            H = BitTest(ivalue, 5);
        }
        
        
        public void SetClockDelay(int iValue)
        {
            if(iValue < MinClockDelay)
                iValue = MinClockDelay;
            if(iValue > MaxClockDelay)
                iValue = MaxClockDelay;
            ClockDelay = MaxClockDelay - iValue;
            if(ClockDelay <= 0)
                ClockDelay = MinClockDelay;
        }
        
        public int GetClockDelay()
        {
            return (MaxClockDelay - ClockDelay);
        }
        
        public int GetRealClockDelay()
        {
            return (ClockDelay);
        }
}
