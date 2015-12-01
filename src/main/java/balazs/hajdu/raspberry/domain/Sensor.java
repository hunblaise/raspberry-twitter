package balazs.hajdu.raspberry.domain;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Implemented communication with the BMP180 sensor.
 *
 * @author Hajdu Balazs
 */
public class Sensor {

    private static final String TOO_HOT_MESSAGE = "The room is too hot, please turn off the heating";

    // Calibration data
    private static final int EEPROM_START = 0xAA;
    private static final int EEPROM_END = 0xBF;

    // Raspberry's I2C bus
    private static final int I2C_BUS = 1;
    // Device address
    private static final int ADDRESS = 0x77;
    // Temperature Control Register Data
    private static final int CONTROL_REGISTER = 0xF4;
    private static final int CALIBRATION_BYTES = 22;
    // Temperature read address
    private static final byte TEMP_ADDR = (byte) 0xF6;
    // Read temperature command
    private static final byte GET_TEMP_CMD = (byte) 0X2E;

    // EEPROM registers - these represent calibration data
    private short AC1;
    private short AC2;
    private short AC3;
    private int AC4;
    private int AC5;
    private int AC6;
    private short B1;
    private short B2;
    private short MB;
    private short MC;
    private short MD;

    private int B5;

    // Uncompensated Temperature data
    private int ut;

    // I2C bus
    I2CBus bus;
    // Device object
    private I2CDevice bmp180;

    private DataInputStream bmp180CalIn;
    private DataInputStream bmp180In;

    private Led led;
    private TwitterClient twitterClient;

    public Sensor() {

        try {
            led = new Led();
            twitterClient = new TwitterClient();

            bus = I2CFactory.getInstance(I2CBus.BUS_1);
            System.out.println("Connected to bus");

            bmp180 = bus.getDevice(ADDRESS);
            System.out.println("Connected to device");

            Thread.sleep(500);

            gettingCalibration();
            
            while (true) {
                Thread.sleep(1000);
                if (readTemp() > 30) {
                    led.lightsOn();
                    twitterClient.updateStatus(TOO_HOT_MESSAGE);
                } else {
                    led.lightsOff();
                }
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("InterruptedException: " + e.getMessage());
        }
    }

    private float readTemp() {

        byte[] bytesTemp = new byte[2];

        try {
            bmp180.write(CONTROL_REGISTER, GET_TEMP_CMD);

            Thread.sleep(500);

            int readTotal = bmp180.read(TEMP_ADDR, bytesTemp, 0, 2);
            if (readTotal < 2) {
                System.out.format("Error: %n bytes read/n", readTotal);
            }

            bmp180In = new DataInputStream(new ByteArrayInputStream(bytesTemp));

            ut = bmp180In.readUnsignedShort();
        } catch (IOException e) {
            System.out.println("Error reading temp: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("InterruptedException: " + e.getMessage());
        }

        return calculateTemperature(ut);
    }

    private float calculateTemperature(int ut) {
        int X1 = ((ut - AC6) * AC5) >> 15;
        int X2 = (MC << 11) / (X1 + MD);

        B5 = X1 + X2;

        float celsius = ((B5 + 8) >> 4) / 10;
        System.out.println("Temperature: " + celsius);
        return celsius;
    }

    private void gettingCalibration() {
        try {
            byte[] bytes = new byte[CALIBRATION_BYTES];

            int readTotal = bmp180.read(EEPROM_START, bytes, 0, CALIBRATION_BYTES);
            if (readTotal != CALIBRATION_BYTES) {
                System.out.println("Error bytes read: " + readTotal);
            }

            bmp180CalIn = new DataInputStream(new ByteArrayInputStream(bytes));

            AC1 = bmp180CalIn.readShort();
            AC2 = bmp180CalIn.readShort();
            AC3 = bmp180CalIn.readShort();

            AC4 = bmp180CalIn.readUnsignedShort();
            AC5 = bmp180CalIn.readUnsignedShort();
            AC6 = bmp180CalIn.readUnsignedShort();

            B1 = bmp180CalIn.readShort();
            B2 = bmp180CalIn.readShort();
            MB = bmp180CalIn.readShort();
            MC = bmp180CalIn.readShort();

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
