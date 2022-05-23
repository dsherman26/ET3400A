/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
** Revision history
** V1.0 initial vesion
** V1.1 Fix appearance when using MacOS
** V1.2:
**      -Fix BGE and BGT instructions; ET3400A sample1 program
**      would only execute once
**      -Add delay to slow down execution
**      -Fix glitch in reset so reset works properly
**      -Add menus to UI
**      -Add ability to control clock rate
** V1.3:
**      -Expect data in first s-record in case there is no S0 record
**      -Bug fix to read other S-records that may have gaps in addresses
**      -Ignore checksum mismatches in s-record being loaded
**      -Clear RAM when loading a new s-record
** 
** V1.4;
**      -Bug fix in ASR instruction, wasn't keeping Bit 7 set
**      -Fixed extended addressing in LDX, LDS, CPX
**
** V1.5:
**      -Add NMI and IRQ
**      -Get WAI working properly
**      -Clean up compiler warnings
** V1.6
**      -Add ability to load S-Record into ROM
**      -Improve default clock speed, better speed adjustement
**      -To match ET-3400 more closely, writing to display addresses with the
**       "don't care" bits, they behave as "don't care" bits
**      -Change default filename filter to show all files when loading
**       S-records
*/

package m6800;

/**
 *
 * @author daves
 */
import java.io.*;

public class M6800 {

    /**
     * @param args the command line arguments
     */
    final static String VERSION = "V1.6";
    
    final static int BYTESPERSRECORD = 16;
    
    // the following is the example 1 program from the ET-3400A manual
    final static int [] SAMPLE1 = {
        0xbd,
        0xfc,
        0xbc,
        0x86,
        0x01,
        0x20,
        0x07,
        0xd6,
        0xf1,
        0xcb,
        0x10,
        0xd7,
        0xf1,
        0x48,
        0xbd,
        0xfe,
        0x3a,
        0xce,
        0x2f,
        0x00,
        0x09,
        0x26,
        0xfd,
        0x16,
        0x5d,
        0x26,
        0xec,
        0x86,
        0x01,
        0xde,
        0xf0,
        0x8c,
        0xc1,
        0x0f,
        0x26,
        0xea,
        0x20,
        0xda
    };
    
    public static void main(String[] args) {
        // TODO code application logic here
        int icounter;
        int icounter2;
        int junk = 0;
        
        MemoryModule CPUMem = new MemoryModule();
        /* uncomment these line to pre-load the example program
        for(icounter = 0;icounter <= 0x25; icounter++)
        {
            CPUMem.MemWrite(icounter, sample1[icounter]);
        }*/
        CPU CPU6800 = new CPU(CPUMem);
        CPU6800.Reset();
        UI gui = new UI(CPU6800, CPUMem);
        gui.FinishUIInit();
        gui.setVisible(true);
        
        while(true)
        {   
            for(icounter = 0; icounter < CPU6800.GetRealClockDelay(); icounter++)
            {
                for(icounter2 = 0; icounter2 < CPU6800.GetRealClockDelay(); icounter2++)
                {
                    junk++;
                }
            }
            CPU6800.clock();
        }   
    }
    
    public static void WriteSRecordFile (FileWriter out, MemoryModule mem, CPU aCPU)
    {
        int iAddress;
        int index;
        SRecord srec = new SRecord();
        srec.Type = 0;
        srec.address = 0;
        srec.data[0] = 0x45; // "E"
        srec.data[1] = 0x54; // "T"
        srec.data[2] = 0x2d; // "-"
        srec.data[3] = 0x33; // "3"
        srec.data[4] = 0x34; // "4"
        srec.data[5] = 0x30; // "0"
        srec.data[6] = 0x30; // "0"
        srec.size = 10;
        srec.dataBytes = 7;
        srec.calcChecksum();
        try {
            out.write(srec.SRecordToString());
        } catch (IOException exc) {
            
        }
        aCPU.Halt(true);
        for(iAddress = 0; iAddress < mem.RAMSIZE; iAddress += BYTESPERSRECORD)
        {
            srec.address = iAddress;
            srec.Type = 1;
            for(index = 0; index < BYTESPERSRECORD; index++)
            {
                srec.data[index] = mem.MemRead(iAddress + index);
            }
            srec.dataBytes = BYTESPERSRECORD;
            srec.size = 19; //2 byte address + 16 data bytes + checksum
            srec.calcChecksum();
            try {
                out.write(srec.SRecordToString());
            }  catch (IOException exc)  {
                
            }
        }
        // write ending S-record
        srec.Type = 9;
        srec.address = 0;
        srec.size = 3;
        srec.dataBytes = 0;
        srec.calcChecksum();
        try {
            out.write(srec.SRecordToString());
        } catch (IOException exc) {
            
        }

        aCPU.Halt(false);
    }
    
    public static int ReadSRecordFile (FileReader in,  MemoryModule mem, CPU aCPU)
    {
        String instring;
        SRecord srec = new SRecord();
        int result;
        int iAddress;
        int index;
        instring = ReadString(in);
        result = srec.ParseFromString(instring); //try starting srecord
        if(result != SRecord.NO_ERROR)
        {
            return (result);
        }
        aCPU.Halt(true);
        //for(index=0;index < mem.RAMSIZE;index++)
        //    mem.MemWrite(index, 0);
        
        if(srec.Type == 1)
        {
            iAddress = srec.address;
            for(index=0;index < srec.dataBytes;index++)
            {
                mem.MemWrite(iAddress++, srec.data[index]);
            }
        }
        else
            iAddress = 0;
        while(iAddress < mem.RAMSIZE)
        {
            instring = ReadString(in);
            if(instring.compareTo("") == 0) // may have reached the end
                break;
            result = srec.ParseFromString(instring); //try starting srecord
            if (result == SRecord.NO_ERROR)
            {
                if(srec.Type == 1)
                {
                    iAddress = srec.address;
                    for(index=0;index < srec.dataBytes;index++)
                    {
                        mem.MemWrite(iAddress++, srec.data[index]);
                    }
                }
                else if(srec.Type == 9) //reached the end
                {
                    break;
                }
            }
            else
                return (result);
        }
        
        aCPU.ResetRequest();
        aCPU.Halt(false);
        return (result);
    }
    
    public static int ReadSRecordFileROM (FileReader in, MemoryModule mem, CPU aCPU)
    {
        String instring;
        SRecord srec = new SRecord();
        int result;
        int iAddress;
        int index;
        instring = ReadString(in);
        result = srec.ParseFromString(instring); //try starting srecord
        if(result != SRecord.NO_ERROR)
        {
            return (result);
        }
        aCPU.Halt(true);
        //for(index=0;index < mem.RAMSIZE;index++)
        //    mem.MemWrite(index, 0);
        
        iAddress = MemoryModule.ROMSTART;
        if(srec.Type == 1)
        {
            for(index=0;index < srec.dataBytes;index++)
            {
                mem.ROMWrite(iAddress++, srec.data[index]);
            }
        }
  
        while(iAddress < (MemoryModule.ROMSTART + mem.ROMSIZE))
        {
            instring = ReadString(in);
            if(instring.compareTo("") == 0) // may have reached the end
                break;
            result = srec.ParseFromString(instring); //try starting srecord
            if (result == SRecord.NO_ERROR)
            {
                if(srec.Type == 1)
                {
                    //iAddress = srec.address;
                    for(index=0;index < srec.dataBytes;index++)
                    {
                        mem.ROMWrite(iAddress++, srec.data[index]);
                    }
                }
                else if(srec.Type == 9) //reached the end
                {
                    break;
                }
            }
            else
                return (result);
        }
        
        aCPU.ResetRequest();
        aCPU.Halt(false);
        return (result);
    }
    
    public static String ReadString (FileReader in)
    {
        String instring = "";
        int inchar;
        do 
        {
            try {
                inchar = in.read();
            } catch (IOException exc) {
                return (instring);
            }
            if(inchar != -1) //EOF reached
                instring = instring + (char)inchar;
            else
                return (instring);
        } while ((inchar != '\n') && (inchar != 0));
        return (instring);
    }
}