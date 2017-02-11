package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.mechadojo.navigation.EncoderMessage;
import org.mechadojo.stateflow.Controller;
import org.mechadojo.stateflow.ParameterRefresh;
import org.mechadojo.stateflow.messages.BooleanMessage;

public class Hardware implements ParameterRefresh {
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
    public Servo leftButton;
    public Servo rightButton;

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

    public boolean firstUpdate = true;

    public boolean leftMoving = false;
    public boolean leftStalled = false;
    public boolean rightMoving = false;
    public boolean rightStalled = false;
    public long leftStallStart = 0;
    public long rightStallStart = 0;

    static double LEFT_FLICKER_DOWN = 1.0;
    static double LEFT_FLICKER_UP = 0.0;
    static double RIGHT_FLICKER_DOWN = 0.0;
    static double RIGHT_FLICKER_UP = 1.0;

    static double LEFT_BUTTON_DOWN = 0.0;
    static double LEFT_BUTTON_UP = 1.0;
    static double RIGHT_BUTTON_DOWN = 1.0;
    static double RIGHT_BUTTON_UP = 0.0;

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

        leftButton = hwmap.servo.get("leftButton");
        rightButton = hwmap.servo.get("rightButton");

        moveServo(leftFlicker, LEFT_FLICKER_DOWN);
        moveServo(rightFlicker, RIGHT_FLICKER_DOWN);
        moveServo(leftButton, LEFT_BUTTON_DOWN);
        moveServo(rightButton, RIGHT_BUTTON_DOWN);

        leftBack.setDirection(DcMotorSimple.Direction.REVERSE);
        rightFront.setDirection(DcMotorSimple.Direction.REVERSE);
        rightMid.setDirection(DcMotorSimple.Direction.REVERSE);

        stopRobot();
        resetEncoders(false);
        resetShooter(false);
        setCoastMode(false);

        firstUpdate = true;
    }

    public void update(Controller controller) {
        boolean lm = leftMoving;
        boolean ls = leftStalled;
        boolean rm = rightMoving;
        boolean rs = rightStalled;

        if (leftPower != 0) {
            EncoderMessage left = (EncoderMessage) controller.getParameter("odometry/left_wheel");

            if (leftMoving) {
                if (left.speed == 0)
                {
                    long dt = controller.getTime() - leftStallStart;
                    if ( ((double)dt / 1000000.0) > 500.0)
                        leftStalled = true;
                } else {
                    leftStallStart = controller.getTime();
                }
            } else {
                if (left.speed != 0) {
                    leftMoving = true;
                }
                leftStallStart = controller.getTime();
            }
        } else {
            leftMoving = false;
            leftStalled = false;
            leftStallStart = controller.getTime();

        }

        if (rightPower != 0) {
            EncoderMessage right = (EncoderMessage) controller.getParameter("odometry/right_wheel");

            if (rightMoving) {
                if (right.speed == 0) {
                    long dt = controller.getTime() - rightStallStart;
                    if ( ((double)dt / 1000000.0) > 500.0)
                        rightStalled = true;
                }
                else {
                    rightStallStart = controller.getTime();
                }
            } else {
                if (right.speed != 0) {
                    rightMoving = true;
                    rightStallStart = controller.getTime();
                }
            }
        } else {
            rightMoving = false;
            rightStalled = false;
            rightStallStart = controller.getTime();
        }

        if (lm != leftMoving || firstUpdate) controller.setParameter("robot/left_moving", "robot", new BooleanMessage(leftMoving));
        if (ls != leftStalled || firstUpdate) controller.setParameter("robot/left_stalled", "robot", new BooleanMessage(leftStalled));
        if (rm != rightMoving || firstUpdate) controller.setParameter("robot/right_moving", "robot", new BooleanMessage(rightMoving));
        if (rs != rightStalled || firstUpdate) controller.setParameter("robot/right_stalled", "robot", new BooleanMessage(rightStalled));

        firstUpdate = false;
    }

    public void stopDrive() {
        leftFront.setPower(0);
        leftMid.setPower(0);
        leftBack.setPower(0);

        rightFront.setPower(0);
        rightMid.setPower(0);
        rightBack.setPower(0);

        leftPower = 0;
        rightPower = 0;
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

    public void buttons(boolean left, boolean right) {
        leftButton.setPosition(left ? LEFT_BUTTON_DOWN : LEFT_BUTTON_UP);
        rightButton.setPosition(right ? RIGHT_BUTTON_DOWN : RIGHT_BUTTON_UP);
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

    public void updateRobotTelemetry(Telemetry t) {
        readEncoders();
        t.addData("Shooter Pos", shooterPos);
        t.addData("Drive Pos", "L: " + leftPos + " R: " + rightPos);
        t.addData("Drive Power", "L: " + leftPower + " R: " + rightPower);
        t.addData("Flicker Pos", "L: " + (leftFlicker.getPosition() == LEFT_FLICKER_DOWN ? "Down" : "Up") +
            " R: " + (rightFlicker.getPosition() == RIGHT_FLICKER_DOWN ? "Down" : "Up"));
        t.addData("Button Pos", "L: " + (leftButton.getPosition() == LEFT_BUTTON_DOWN ? "Down" : "Up") +
                ", R: " + (rightButton.getPosition() == RIGHT_BUTTON_DOWN ? "Down" : "Up"));
    }
}
