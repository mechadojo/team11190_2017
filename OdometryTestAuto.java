package org.firstinspires.ftc.teamcode;

import android.os.Environment;
import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.robocol.TelemetryMessage;

import org.mechadojo.navigation.EncoderMessage;
import org.mechadojo.navigation.TwoWheelOdometry;
import org.mechadojo.stateflow.Controller;
import org.mechadojo.stateflow.Message;
import org.mechadojo.stateflow.MessageFileLog;
import org.mechadojo.stateflow.examples.DelayedHelloWorldController;
import org.mechadojo.stateflow.opmode.StateFlowOpMode;

import java.io.FileOutputStream;
import java.util.concurrent.ThreadPoolExecutor;

@TeleOp(name = "Odometry Test ", group = "Test")
public class OdometryTestAuto extends StateFlowOpMode {
    Hardware howler = new Hardware();
    Sensors sensors = new Sensors();
    TwoWheelOdometry odometry = new TwoWheelOdometry();
    Thread odometryThread;
    double maxSpeed = 0.0;

    @Override
    public Controller loadController() {
        Controller result = new Controller();
        return result;
    }

    @Override
    public void init() {
        super.init();

        howler.init(hardwareMap);
        howler.setCoastMode(true);
        sensors.init(hardwareMap);

        odometry.controller = controller;
        odometry.leftMotor = howler.leftBack;
        odometry.rightMotor = howler.rightBack;

        odometry.leftWheelFactor = -8.0 / 3752.0;
        odometry.rightWheelFactor = -8.0 / 3754.0;

        if (!odometry.running) {
            odometryThread = new Thread(odometry);
            odometryThread.start();
        }
    }

    @Override
    public void start() {
        super.start();

        String sdcard = Environment.getExternalStorageDirectory().getPath();
        MessageFileLog log = new MessageFileLog("^odometry/left_wheel$", "/sdcard/MechaDojo/left_wheel.csv", true );
        controller.addMessageLog("encoders", log);



    }

    @Override
    public void stop() {
        odometry.running = false;
        super.stop();
    }

    @Override
    public void loop() {
        handleTeleop();
        super.loop();
    }

    public void updateOdometryTelemetry() {

        double avg = odometry.stats.mean();
        double stddev = odometry.stats.standardDeviation();

        telemetry.addData("Odometry", String.format("%.2f msec σ: %.4f)", avg, stddev));
        Message m = controller.getParameter("odometry/left_wheel");
        if (m != null && m instanceof  EncoderMessage) {
            updateEncoderTelemetry("Left", (EncoderMessage)m);
        }

        m = controller.getParameter("odometry/right_wheel");
        if (m != null && m instanceof  EncoderMessage) {
            updateEncoderTelemetry("Right", (EncoderMessage)m);
        }

        telemetry.addData("Max Speed:", maxSpeed);
    }

    public void updateEncoderTelemetry(String caption, EncoderMessage msg) {
        telemetry.addData(caption, String.format("T:%d  P:%.4f  S:%.4f  A:%.4f", msg.ticks, msg.position, msg.speed, msg.acceleration));
        if (Math.abs(msg.speed) > maxSpeed) maxSpeed = Math.abs(msg.speed);
    }

    public void updateInitTelemetry() {
        super.updateInitTelemetry();
        updateOdometryTelemetry();
    }

    public void updateRunTelemetry() {
        super.updateRunTelemetry();

        double avg = odometry.stats.mean();
        double stddev = odometry.stats.standardDeviation();

        updateOdometryTelemetry();
    }


    double deadZone = 0.15;
    boolean yButtonPressed = false;
    boolean buttonMode = false;

    boolean startPressed = false;
    boolean teleMode = true;

    boolean leftPressed = false;
    boolean leftButtonMode = false;

    boolean rightPressed = false;
    boolean rightButtonMode = false;

    public void handleTeleop() {

        sensors.updateSensors();

        double drivePower = gamepad1.left_stick_y;
        double turnPower = -gamepad1.right_stick_x;

        if(Math.abs(drivePower) < deadZone) {
            drivePower = 0;
        } else {
            if (drivePower > 0) drivePower -= deadZone;
            else drivePower += deadZone;
        }

        if(Math.abs(turnPower) < deadZone) {
            turnPower = 0;
        } else {
            if (turnPower > 0) drivePower -= deadZone;
            else turnPower += deadZone;
        }

        if(Math.abs(turnPower) < 0.95) {
            if(Math.abs(drivePower) < 0.25) {
                turnPower *= 0.60;
            }
            else {
                turnPower *= 0.80;
            }
        }

        if(Math.abs(drivePower) < 0.95) {
            drivePower *= 0.75;
        }

        double collectPower;
        if(gamepad1.right_trigger > deadZone || gamepad2.right_trigger > deadZone) {
            collectPower = 1;
        }
        else if(gamepad1.left_trigger > deadZone || gamepad2.left_trigger > deadZone){
            collectPower = -1;
        }
        else {
            collectPower = 0;
        }

        double shootPower;
        if(gamepad2.right_bumper || gamepad1.right_bumper) {
            shootPower = 1;
        }
        else if(gamepad2.left_bumper || gamepad1.left_bumper) {
            shootPower = 0.5;
        }
        else {
            shootPower = 0;
        }

        double leftFlick;
        if(gamepad2.x || gamepad1.x) {
            leftFlick = Hardware.LEFT_FLICKER_UP;
        }
        else {
            leftFlick = Hardware.LEFT_FLICKER_DOWN;
        }

        double rightFlick;
        if(gamepad2.b || gamepad1.b) {
            rightFlick = Hardware.RIGHT_FLICKER_UP;
        }
        else {
            rightFlick = Hardware.RIGHT_FLICKER_DOWN;
        }

        if(gamepad1.dpad_down) {
            howler.resetShooter(true);
        }

        if(gamepad1.dpad_up) {
            howler.resetEncoders(true);
        }

        if(gamepad1.start && !startPressed) {
            teleMode = !teleMode;
        }
        startPressed = gamepad1.start;

        if(gamepad1.y && !yButtonPressed) {
            buttonMode = !buttonMode;
        }
        yButtonPressed = gamepad1.y;

        if(buttonMode) {
            drivePower *= -0.5;
        }

        if(buttonMode) {
            drivePower *= -0.5;
        }

        double leftButton;
        if(gamepad1.dpad_left && !leftPressed) {
            leftButtonMode = !leftButtonMode;
        }
        leftPressed = gamepad1.dpad_left;

        double rightButton;
        if(gamepad1.dpad_right && !rightPressed) {
            rightButtonMode = !rightButtonMode;
        }
        rightPressed = gamepad1.dpad_right;

        if(leftButtonMode) {
            leftButton = howler.LEFT_BUTTON_UP;
        }
        else {
            leftButton = howler.LEFT_BUTTON_DOWN;
        }

        if(rightButtonMode) {
            rightButton = howler.RIGHT_BUTTON_UP;
        }
        else {
            rightButton = howler.RIGHT_BUTTON_DOWN;
        }

        double leftPower = drivePower + turnPower;
        double rightPower = drivePower - turnPower;

        howler.moveServo(howler.leftFlicker, leftFlick);
        howler.moveServo(howler.rightFlicker, rightFlick);
        howler.moveServo(howler.leftButton, leftButton);
        howler.moveServo(howler.rightButton, rightButton);
        howler.drive(leftPower, rightPower);
        howler.collect(collectPower);
        howler.shoot(shootPower);

    }
}
