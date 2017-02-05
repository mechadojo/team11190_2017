package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.Servo;

public class Hardware {
    public DcMotor leftFront;
    public DcMotor leftMid;
    public DcMotor leftBack;

    public DcMotor rightFront;
    public DcMotor rightMid;
    public DcMotor rightBack;

    public DcMotor collection;
    public DcMotor shooter;

    public Servo leftFlicker;
    public Servo rightFlicker;

    public ColorSensor colorLeft;
    public ColorSensor colorRight;

    HardwareMap hwmap;

    double leftPower;
    double rightPower;
    double collectionPower;
    double shooterPower;

    double leftPos;
    double rightPos;

    double shooterPos;

    static double LEFT_FLICKER_DOWN = 1.0;
    static double LEFT_FLICKER_UP = 0.0;
    static double RIGHT_FLICKER_DOWN = 0.0;
    static double RIGHT_FLICKER_UP = 1.0;


    Hardware() {}

    public void init(HardwareMap ahwmap) {
        hwmap = ahwmap;

        leftFront = hwmap.dcMotor.get("leftFront");
        leftMid = hwmap.dcMotor.get("leftMid");
        leftBack = hwmap.dcMotor.get("leftBack");

        rightFront = hwmap.dcMotor.get("rightFront");
        rightMid = hwmap.dcMotor.get("rightMid");
        rightBack = hwmap.dcMotor.get("rightBack");

        collection = hwmap.dcMotor.get("collector");
        shooter = hwmap.dcMotor.get("shooter");

        leftFlicker = hwmap.servo.get("leftFlicker");
        rightFlicker = hwmap.servo.get("rightFlicker");

        leftBack.setDirection(DcMotorSimple.Direction.REVERSE);
        rightFront.setDirection(DcMotorSimple.Direction.REVERSE);
        rightMid.setDirection(DcMotorSimple.Direction.REVERSE);

        stopRobot();
        resetEncoders(false);
        resetShooter(false);
        setCoastMode(false);
    }

    public void stopDrive() {
        leftFront.setPower(0);
        leftMid.setPower(0);
        leftBack.setPower(0);

        rightFront.setPower(0);
        rightMid.setPower(0);
        rightBack.setPower(0);
    }

    public void stopRobot() {
        stopDrive();
        collection.setPower(0);
        shooter.setPower(0);
    }

    public void resetEncoders(boolean encoderMode) {
        leftBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        if(encoderMode) {
            leftBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            rightBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
        else {
            leftBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            rightBack.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }
    }

    public void resetShooter(boolean encoderMode) {
        shooter.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        if(encoderMode) {
            shooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
        else {
            shooter.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }
    }

    public void readEncoders() {
        leftPos = leftBack.getCurrentPosition();
        rightPos = rightBack.getCurrentPosition();

        shooterPos = shooter.getCurrentPosition();
    }

    public double encoderAvg() {
        double avg = (leftPos + rightPos)/2;
        return avg;
    }

    public boolean distCheck(double goal) {
        if(leftPos < Math.abs(goal) && rightPos < Math.abs(goal)) {
            return true;
        }
        else {
            return false;
        }
    }

    public void setCoastMode(boolean coastMode) {
        if(coastMode) {
            leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            leftMid.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

            rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            rightMid.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        }
        else {
            leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            leftMid.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

            rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            rightMid.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }
    }

    public void drive(double lp, double rp) {
        leftPower = lp;
        rightPower = rp;

        leftFront.setPower(leftPower);
        leftMid.setPower(leftPower);
        leftBack.setPower(leftPower);

        rightFront.setPower(rightPower);
        rightMid.setPower(rightPower);
        rightBack.setPower(rightPower);
    }

    public void drive(double p) {
        leftPower = p;
        rightPower = p;

        leftFront.setPower(leftPower);
        leftMid.setPower(leftPower);
        leftBack.setPower(leftPower);

        rightFront.setPower(rightPower);
        rightMid.setPower(rightPower);
        rightBack.setPower(rightPower);
    }

    public void collect(double cp) {
        collectionPower = cp;
        collection.setPower(collectionPower);
    }

    public void shoot(double sp) {
        shooterPower = sp;
        shooter.setPower(shooterPower);
    }

    public void moveServo(Servo servo, double pos) {
        servo.setPosition(pos);
    }
}
