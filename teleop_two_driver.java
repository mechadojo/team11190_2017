package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Howler: Teleop", group = "Howler")
public class teleop_two_driver extends LinearOpMode {
    Hardware howler = new Hardware();
    Sensors sensors = new Sensors();

    public void runOpMode() {
        howler.init(hardwareMap);
        sensors.init(hardwareMap);

        double deadZone = 0.15;

        waitForStart();

        sensors.lineSensorArray.EnableArray();

        boolean yButtonPressed = false;
        boolean buttonMode = false;

        boolean startPressed = false;
        boolean teleMode = true;

        howler.setCoastMode(true);

        boolean leftPressed = false;
        boolean leftButtonMode = false;

        boolean rightPressed = false;
        boolean rightButtonMode = false;

        while(opModeIsActive()) {
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

            if(teleMode) {
                howler.updateRobotTelemetry(telemetry);
            }
            else {
                sensors.updateSensorTelemetry(telemetry);
            }

            telemetry.update();

            double leftPower = drivePower + turnPower;
            double rightPower = drivePower - turnPower;

            howler.moveServo(howler.leftFlicker, leftFlick);
            howler.moveServo(howler.rightFlicker, rightFlick);
            howler.drive(leftPower, rightPower);
            howler.collect(collectPower);
            howler.shoot(shootPower);

        }
    }
}
