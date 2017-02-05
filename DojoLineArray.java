package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;

public class DojoLineArray {
    I2cDeviceSynch device;
    I2cAddr i2cAddr;

    public DojoLineArray(I2cDeviceSynch device, int addr) {
        this.device = device;
        this.i2cAddr = I2cAddr.create8bit(addr);

        device.setI2cAddress(i2cAddr);
        device.setReadWindow(new I2cDeviceSynch.ReadWindow(0x00, 1, I2cDeviceSynch.ReadMode.REPEAT));
        device.engage();

        DisableArray();
    }

    public void EnableArray()
    {
        device.write8(0x07, (byte)0x00, true);
        device.write8(0x03, (byte)0x00, true);
    }

    public void DisableArray()
    {
        device.write8(0x07, (byte)0xFF, true);
        device.write8(0x03, (byte)0xFF, true);
    }

    public int ReadArray()
    {
        int temp = device.read8(0x00);
        temp  = temp & 0xFF;

        int result = ((temp & 0x01) != 0 ? 0x20 : 0);
        result +=    ((temp & 0x02) != 0 ? 0x10 : 0);
        result +=    ((temp & 0x04) != 0 ? 0x80 : 0);
        result +=    ((temp & 0x08) != 0 ? 0x40 : 0);
        result +=    ((temp & 0x10) != 0 ? 0x04 : 0);
        result +=    ((temp & 0x20) != 0 ? 0x08 : 0);
        result +=    ((temp & 0x40) != 0 ? 0x02 : 0);
        result +=    ((temp & 0x80) != 0 ? 0x01 : 0);

        return result;
    }

    public double ReadPosition(int array)
    {
        double result = 0;
        int count = 0;
        if (array == 0) return -1;

        int fl = array;
        for(int i = 1; i <= 8; i++) {
            if((fl& 1) != 0) {
                result += i;
                count++;
            }

            fl = fl >> 1;
        }

        result = result / (8.0 * count);
        return result;
    }
}
