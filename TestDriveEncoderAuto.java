package org.firstinspires.ftc.teamcode;

import android.os.Environment;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.mechadojo.navigation.EncoderMessage;
import org.mechadojo.navigation.TwoWheelOdometry;
import org.mechadojo.stateflow.Action;
import org.mechadojo.stateflow.Controller;
import org.mechadojo.stateflow.MessageFileLog;
import org.mechadojo.stateflow.MessageHandler;
import org.mechadojo.stateflow.MessageRoute;
import org.mechadojo.stateflow.opmode.StateFlowOpMode;

/**
 * Created by Chad on 2/7/2017.
 */

@Autonomous (name = "Test Drive Encoder", group = "StateFlow Test")
public class TestDriveEncoderAuto extends StateFlowOpMode {
    Hardware howler = new Hardware();
    Sensors sensors = new Sensors();
    TwoWheelOdometry odometry = new TwoWheelOdometry();
    Thread odometryThread;

    double maxSpeed = 0.0;

    @Override
    public Controller loadController() {
        Controller result = new Controller();
        result.addLibrary("drive")
                .addComponents(new DriveByEncoderComponent("auto/drive_fwd", howler, "odometry/right_wheel", -1.0, -1.0)
                                    .pid(0, .2, 0, 0)
                                    .delay(0, 1000)
                                    .power(.35, 0)
                                    .target(1.5, 0, -999.0))

                .addComponents(new DriveByEncoderComponent("auto/drive_rev", howler, "odometry/right_wheel", -1.0, -1.0)
                        .pid(0, .2, 0, 0)
                        .power(.35, 0)
                        .target(1.5, 999.0, 0.0))

                .addComponent("auto/fwd_two", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        msg.message = new DriveByEncoderMessage(2.0);
                        action.next(msg);
                    }
                })

                .addComponent("auto/back_two", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        msg.message = new DriveByEncoderMessage(-2.0);
                        action.next(msg);
                    }
                })

                .addComponent("auto/stop", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        howler.stopRobot();
                    }
                });

        result.addBehavior("main")
                .addConnection("step_1(auto/fwd_two) OUT -> IN move_1(auto/drive_fwd)")
                .addConnection("move_1() OUT -> IN step_2(auto/back_two)")
                .addConnection("step_2() OUT -> IN move_2(auto/drive_rev)")
                .addConnection("move_2() OUT -> IN stop(auto/stop)")

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

        log = new MessageFileLog("^sensor/line$", "/sdcard/MechaDojo/line.csv", true );
        controller.addMessageLog("line", log);

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

        odometry.updates.add(sensors);

        if (!odometry.running) {
            odometryThread = new Thread(odometry);
            odometryThread.start();
        }

    }
}
