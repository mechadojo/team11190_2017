package org.firstinspires.ftc.teamcode;

import android.os.Environment;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.mechadojo.navigation.EncoderMessage;
import org.mechadojo.navigation.TwoWheelOdometry;
import org.mechadojo.stateflow.Action;
import org.mechadojo.stateflow.Controller;
import org.mechadojo.stateflow.Message;
import org.mechadojo.stateflow.MessageFileLog;
import org.mechadojo.stateflow.MessageHandler;
import org.mechadojo.stateflow.MessageRoute;
import org.mechadojo.stateflow.StateFlowObject;
import org.mechadojo.stateflow.opmode.StateFlowOpMode;

/**
 * Created by Chad on 2/7/2017.
 */

@Autonomous (name = "Line Up Auto", group = "StateFlow Test")
public class LineupAuto extends StateFlowOpMode {
    Hardware howler = new Hardware();
    Sensors sensors = new Sensors();
    TwoWheelOdometry odometry = new TwoWheelOdometry();
    Thread odometryThread;

    double maxSpeed = 0.0;

    @Override
    public Controller loadController() {
        Controller result = new Controller();
        result.addLibrary("drive")
                .addComponent("auto/drive_to_vortex", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        howler.drive(0.45, 0.45);
                        action.next(msg.delay(1250));
                    }
                })
                .addComponent("auto/stop_drive", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        howler.stopRobot();
                    }
                })
                .addComponent("auto/turn_to_beacon_1", "OUT,WAIT", "IN,WAIT", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        odometry.zeroEncoders();
                        howler.drive(0, 0.45);
                        action.info("Wait for encoders");
                        action.out("WAIT", msg);
                    }
                }, new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        EncoderMessage wheel = (EncoderMessage)action.getParameter("odometry/right_wheel");
                        if (wheel != null) {
                            if (wheel.position < -3.2) {
                                action.info("Reached encoder: " + wheel.position);
                                action.next(msg);
                                return;
                            }

                            double dist = Math.abs(wheel.position);
                            double power = .45 - (.25/3.2) * dist;
                            howler.drive(0, power);
                        }
                        action.out("WAIT", msg);
                    }
                });

        result.addBehavior("main")
                .addConnection("step_1(auto/turn_to_beacon_1) OUT -> IN step_2(auto/drive_to_vortex)")
                .addConnection("step_1() WAIT -> WAIT step_1()")
                .addConnection("step_2() OUT -> IN step_3(auto/stop_drive)")
                .addEventTrigger("opmode/start", "IN step_1()" );
        result.initialize();
        return result;
    }

    @Override
    public void start() {
        super.start();

        String sdcard = Environment.getExternalStorageDirectory().getPath();
        MessageFileLog log = new MessageFileLog("^odometry/right_wheel$", "/sdcard/MechaDojo/right_wheel.csv", true );
        controller.addMessageLog("encoders", log);
    }


    @Override
    public void stop() {
        odometry.running = false;
        super.stop();
    }



    @Override
    public void init() {
        super.init();

        howler.init(hardwareMap);
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
}
