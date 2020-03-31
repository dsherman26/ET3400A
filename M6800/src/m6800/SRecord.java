/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
**Revisions:
** 3/30/2020 Ignore bad checksum in s-record
*/
package m6800;

/**
 *
 * @author daves
 */
public class SRecord {
    final static int SRECORD_MAX_BYTES = 32;
    final static int NO_ERROR = 0;
    final static int ERR_INVALID_SIZE = -1;
    final static int ERR_INVALID_CHECKSUM = -2;
    final static int ERR_NO_S = -3;
    final static String HexVals [] = {"0", "1", "2", "3", "4", "5", "6", "7", "8",
                                "9", "A", "B", "C", "D", "E", "F"};
    enum ParseStates {
        IDLE,
        S,
        TYPE,
        SIZE,
        ADDRESS,
        DATA,
        CHECKSUM,
        END
    }
    
    int Type;
    int size;
    int dataBytes;
    int address;
    int data[];
    int checksum;
    
    public SRecord ()
    {
        data = new int[SRECORD_MAX_BYTES]; 
        Type = 0;
        address = 0;
        checksum = 0;
    }
    
    public int ParseFromString(String inputString)
    {
        int retVal = NO_ERROR;
        int stringPos = 0;
        String smallString;
        int value, iCounter, addressSize = 0;
        int index = 0;
        int testchecksum =  0;
        ParseStates state = ParseStates.IDLE;
        do
        {
            switch(state)
            {
                case IDLE:
                    state = ParseStates.S;
                break;
                case S:
                    if(inputString.charAt(stringPos) != 'S')
                    {
                        retVal = ERR_NO_S;
                        return (retVal);
                    }
                    else
                    {
                        address = 0;
                        state = ParseStates.TYPE;
                        stringPos++;
                    } 
                break;
                case TYPE:
                    Type = inputString.charAt(stringPos) - '0';
                    stringPos++;
                    state = ParseStates.SIZE;
                    addressSize = calcAddressSize();
                break;
                case SIZE:
                    smallString = inputString.substring(stringPos, stringPos+2);
                    value = HexToInt(smallString);
                    size = value;
                    testchecksum += value;
                    state = ParseStates.ADDRESS;
                    stringPos+=2;
                break;
                case ADDRESS:
                    iCounter = addressSize;
                    while(iCounter > 0)
                    {
                        address *= 256;
                        smallString = inputString.substring(stringPos, stringPos+2);
                        value = HexToInt(smallString);
                        address = address + value;
                        testchecksum += value;
                        stringPos += 2;
                        iCounter--;
                    }
                    state = ParseStates.DATA;
                break;
                case DATA:
                    iCounter = size - (addressSize) - 1;
                    dataBytes = iCounter;
                    while (iCounter > 0)
                    {
                        smallString = inputString.substring(stringPos, stringPos+2);
                        value = HexToInt(smallString);
                        data[index++] = value;
                        testchecksum+=value;
                        stringPos += 2;
                        iCounter--;
                    }
                    state = ParseStates.CHECKSUM;
                break;
                case CHECKSUM:
                    testchecksum = (~testchecksum & 0xFF);
                    smallString = inputString.substring(stringPos, stringPos+2);
                    value = HexToInt(smallString);
                    //if(value != testchecksum)
                    //{
                    //    retVal = ERR_INVALID_CHECKSUM;
                    //    return (retVal);
                    //}
                    //else
                        checksum = value;
                    state = ParseStates.END;
                break;       
            }
        } while (state != ParseStates.END);
        return (retVal);
    }
    
    public String SRecordToString ()
    {
        int iCounter;
        int iValue, iValue2, index;
        int addressSize;
        String tempString="S";
        tempString = tempString + IntToHex(Type);
        tempString = tempString + IntToHex2(size);
        addressSize = calcAddressSize();
        iCounter = addressSize;
        iValue = address;
        while(iCounter > 0)
        {
            iValue2 = (iValue >> (8 * (iCounter-1))) & 0xFF;
            tempString = tempString + IntToHex2(iValue2);
            iCounter--;
        }
        iCounter = dataBytes;
        index = 0;
        while(iCounter > 0)
        {
            iValue = data[index++];
            tempString = tempString + IntToHex2(iValue);
            iCounter--;
        }
        tempString = tempString + IntToHex2(checksum);
        tempString = tempString + "\n";
        return tempString; 
    }
    
    private int calcAddressSize()
    {
        int addressSize = 0;
        switch(Type)
        {
            case 0: // S0
            case 1: // S1
            case 5: // S5
            case 9: // S9
                addressSize = 2;
            break;
            case 2: // S2
            case 8: // S8
                addressSize = 3;
            break;
            case 3: // S3
            case 7: // S7
                addressSize = 4;
            break;  
        }
        return (addressSize);
    }
    
    public void calcChecksum()
    {
        int addressSize = calcAddressSize();
        int iCounter, iValue, index;
        int sum = 0;
        int count = dataBytes + addressSize + 1;
        sum += count;
        iCounter = addressSize;
        while(iCounter > 0)
        {
            iValue = (address >> (8 * (iCounter - 1)));
            sum += (iValue & 0xFF);
            iCounter--;
        }
        iCounter = dataBytes;
        index = 0;
        while(iCounter > 0)
        {
            sum += data[index++];
            iCounter--;
        }
        sum = (~sum) & 0xFF;
        checksum = sum;
    }
    
    private String IntToHex (int iValue)
    {
        return (HexVals[iValue]);
    }
    
    private String IntToHex2 (int iValue)
    {
        String tempString = "";
        int iTemp = iValue >> 4;
        tempString = tempString + HexVals[iTemp];
        iTemp = iValue & 0xF;
        tempString = tempString + HexVals[iTemp];
        return tempString;
    }
    
    private int HexToInt (String in)
    {
        int value = 0;
        if(in.charAt(0) >= 'A')
        {
            value = value + 16 * (10 + (in.charAt(0) - 'A'));
        }
        else
        {
            value = value + 16 * (in.charAt(0) - '0');
        }
        if(in.charAt(1) >= 'A')
        {
            value = value + (10 + (in.charAt(1) - 'A'));
        }
        else
        {
            value = value + (in.charAt(1) - '0');
        }
        return (value);
    }
    
}
