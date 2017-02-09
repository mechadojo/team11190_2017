package org.firstinspires.ftc.teamcode;

import android.graphics.Color;

import com.qualcomm.hardware.adafruit.BNO055IMU;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cAddr;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

import java.util.Locale;

public class Sensors {
    public ColorSensor colorLeft;
    public ColorSensor colorRight;

    public AnalogInput midProx;
    public AnalogInput farProx;

    public BNO055IMU imu;

    public DojoLineArray lineSensorArray;

    public AnalogInput lineSensor1;
    public AnalogInput lineSensor2;
    public AnalogInput lineSensor3;

    public AnalogInput leftSonar;
    //public AnalogInput rightSonar;

    float hsvLeft[] = {0, 0, 0};
    float hsvRight[] = {0, 0, 0};

    String leftColor = "";
    String rightColor = "";
    boolean isLeftColorValid = false;
    boolean isRightColorValid = false;

    HardwareMap hwmap;

    double midProximity = 0;
    double farProximity = 0;

    double line1 = 0;
    double line2 = 0;
    double line3 = 0;

    boolean onLine1 = false;
    boolean onLine2 = false;
    boolean onLine3 = false;

    double lineThreshold = 1.5;

    double leftSonarDist = 0;
    double rightSonarDist = 0;

    boolean vortexSeen = false;

    public Orientation orient;
    int lineArray;
    double linePosition;

    Sensors() {
    }

    public void init(HardwareMap ahwmap) {
        hwmap = ahwmap;


        lineSensorArray = new DojoLineArray(hwmap.i2cDeviceSynch.get("LineFront"), 0x40);
        lineSensor1 = hwmap.analogInput.get("line1");
        lineSensor2 = hwmap.analogInput.get("line2");
        lineSensor3 = hwmap.analogInput.get("line3");

        colorLeft = hwmap.colorSensor.get("colorLeft");
        colorLeft.setI2cAddress(I2cAddr.create8bit(0x3c));
        colorRight = hwmap.colorSensor.get("colorRight");
        colorRight.setI2cAddress(I2cAddr.create8bit(0x3a));


        colorLeft.enableLed(false);
        colorRight.enableLed(false);

        midProx = hwmap.analogInput.get("MidProximity");
        farProx = hwmap.analogInput.get("FarProximity");

        leftSonar = hwmap.analogInput.get("leftSonar");
        //rightSonar = hwmap.analogInput.get("rightSonar");

        /*BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.mode = BNO055IMU.SensorMode.NDOF;*/


        // Retrieve and initialize the IMU. We expect the IMU to be attached to an I2C port
        // on a Core Device Interface Module, configured to be a sensor of type "AdaFruit IMU",
        // and named "imu".
        //imu = hwmap.get(BNO055IMU.class, "imu");
        //imu.initialize(parameters);
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

        line1 = lineSensor1.getVoltage();
        line2 = lineSensor2.getVoltage();
        line3 = lineSensor3.getVoltage();

        onLine1 = line1 > lineThreshold;
        onLine2 = line2 > lineThreshold;
        onLine3 = line3 > lineThreshold;

        leftSonarDist = leftSonar.getVoltage();
        //rightSonarDist = rightSonar.getVoltage();

        vortexSeen = leftSonarDist > 0.08 || rightSonarDist > 0.08;

        //orient = imu.getAngularOrientation().toAxesReference(AxesReference.INTRINSIC).toAxesOrder(AxesOrder.ZXY);
        lineArray = lineSensorArray.ReadArray();
        linePosition = lineSensorArray.ReadPosition(lineArray);
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
        t.addData("Sonar", String.format("Left: %.2f, Right: %.2f", leftSonarDist, rightSonarDist));
        t.addData("Line", String.format("1: %.2f, 2: %.2f, 3: %.2f", line1, line2, line3));
        t.addData("On Line", "1: " + onLine1 + ", 2:" + onLine2 + ", 3:" + onLine3);
        t.addData("Vortex Seen", vortexSeen);

        String front = Integer.toBinaryString(lineArray);
        front = "00000000".substring(front.length()) + front;
        t.addData("Line Array", String.format("%s %.1f", front, linePosition));

        /*t.addData("heading", formatAngle(orient.angleUnit, orient.firstAngle));
        t.addData("pitch", formatAngle(orient.angleUnit, orient.secondAngle));
        t.addData("roll", formatAngle(orient.angleUnit, orient.thirdAngle));*/


    }

    boolean onLine(double threshold) {
        if(line1 < threshold || line2 < threshold || line3 < threshold) {
            return true;
        }
        else {
            return false;
        }
    }
}