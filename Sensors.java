package org.firstinspires.ftc.teamcode;

import android.content.pm.PackageInfo;
import android.graphics.Color;

import com.qualcomm.hardware.adafruit.AdafruitBNO055IMU;
import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cColorSensor;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cAddr;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

import java.util.Locale;

public class Sensors {
    public ColorSensor colorLeft;
    public ColorSensor colorRight;

    public AnalogInput midProx;
    public AnalogInput farProx;

    public BNO055IMU imu;

    public DojoLineArray lineSensor;

    float hsvLeft[] = {0, 0, 0};
    float hsvRight[] = {0, 0, 0};

    String leftColor = "";
    String rightColor = "";
    boolean isLeftColorValid = false;
    boolean isRightColorValid = false;

    HardwareMap hwmap;

    double midProximity = 0;
    double farProximity = 0;

    public Orientation orient;
    int lineArray;
    double linePosition;

    Sensors() {
    }

    public void init(HardwareMap ahwmap) {
        hwmap = ahwmap;


        lineSensor = new DojoLineArray(hwmap.i2cDeviceSynch.get("LineFront"), 0x40);

        colorLeft = hwmap.colorSensor.get("colorLeft");
        colorLeft.setI2cAddress(I2cAddr.create8bit(0x3c));
        colorRight = hwmap.colorSensor.get("colorRight");
        colorRight.setI2cAddress(I2cAddr.create8bit(0x3a));


        colorLeft.enableLed(false);
        colorRight.enableLed(false);

        midProx = hwmap.analogInput.get("MidProximity");
        farProx = hwmap.analogInput.get("FarProximity");

        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.mode = BNO055IMU.SensorMode.NDOF;


        // Retrieve and initialize the IMU. We expect the IMU to be attached to an I2C port
        // on a Core Device Interface Module, configured to be a sensor of type "AdaFruit IMU",
        // and named "imu".
        imu = hwmap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);
    }

    public void updateSensors() {
        Color.RGBToHSV(colorLeft.red() * 8, colorLeft.green() * 8, colorLeft.blue() * 8, hsvLeft);
        Color.RGBToHSV(colorRight.red() * 8, colorRight.green() * 8, colorRight.blue() * 8, hsvRight);

        isLeftColorValid = hsvLeft[1] > .5;
        isRightColorValid = hsvRight[1] > .5;

        leftColor = hsvLeft[0] > 100 ? "Blue" : "Red";
        rightColor = hsvRight[0] > 100 ? "Blue" : "Red";

        midProximity = midProx.getVoltage();
        farProximity = farProx.getVoltage();

        orient = imu.getAngularOrientation().toAxesReference(AxesReference.INTRINSIC).toAxesOrder(AxesOrder.ZXY);
        lineArray = lineSensor.ReadArray();
        linePosition = lineSensor.ReadPosition(lineArray);
    }


    String formatAngle(AngleUnit angleUnit, double angle) {
        return formatDegrees(AngleUnit.DEGREES.fromUnit(angleUnit, angle));
    }

    String formatDegrees(double degrees) {
        return String.format(Locale.getDefault(), "%.1f", AngleUnit.DEGREES.normalize(degrees));
    }

    public void updateSensorTelemetry(Telemetry t) {

        t.addData("Colors", (isLeftColorValid ? leftColor : "None") + " " + (isRightColorValid ? rightColor : "None"));
        t.addData("Proximity", String.format("Mid: %.2f Far: %.2f", midProximity, farProximity));

        String front = Integer.toBinaryString(lineArray);
        front = "00000000".substring(front.length()) + front;
        t.addData("Line", String.format("%s %.1f", front, linePosition));

        t.addData("heading", formatAngle(orient.angleUnit, orient.firstAngle));
        t.addData("pitch", formatAngle(orient.angleUnit, orient.secondAngle));
        t.addData("roll", formatAngle(orient.angleUnit, orient.thirdAngle));


    }
}