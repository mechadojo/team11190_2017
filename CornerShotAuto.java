package org.firstinspires.ftc.teamcode;

import android.os.Environment;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.mechadojo.navigation.TwoWheelOdometry;
import org.mechadojo.stateflow.Action;
import org.mechadojo.stateflow.Controller;
import org.mechadojo.stateflow.MessageFileLog;
import org.mechadojo.stateflow.MessageHandler;
import org.mechadojo.stateflow.MessageRoute;
import org.mechadojo.stateflow.opmode.StateFlowOpMode;

/**
 * Created by 64006039 on 2/10/17.
 */

@Autonomous(name = "Corner Shot Auto")
public class CornerShotAuto extends StateFlowOpMode{
    Hardware howler = new Hardware();
    Sensors sensors = new Sensors();
    TwoWheelOdometry odometry = new TwoWheelOdometry();
    Thread odometryThread;

    @Override
    public Controller loadController() {
        Controller results = new Controller();
        results.addLibrary("corner_shot")
                .addComponents(new DriveByEncoderComponent("auto/drive_to_shot", howler, "odometry/right_wheel", -1.0, -1.0)
                        .pid(.15, .2, 0, 0)
                        .delay(0, 100)
                        .power(.35, .15)
                        .target(1.5, 0, -999.0))
                .addComponent("auto/drive_fwd_shoot", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        msg.message = new DriveByEncoderMessage(2.0);
                        action.next(msg);
                    }
                })
                .addComponent("auto/shoot", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        howler.shoot(1.0);
                        action.next(msg.delay(1500));
                    }
                })
                .addComponent("auto/shoot_stop", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        howler.shoot(0.0);
                        action.next(msg.delay(15.0));
                    }
                })
                .addComponents(new DriveByEncoderComponent("auto/drive_to_cap", howler, "odometry/right_wheel", -1.0, -1.0)
                        .pid(.15, .2, 0, 0)
                        .delay(0, 100)
                        .power(.35, .15)
                        .target(1.5, 0, -999.0))
                .addComponent("auto/knock_cap_ball", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        msg.message = new DriveByEncoderMessage(3.0);
                        howler.collect(-1.0);
                        action.next(msg);
                    }
                })
                .addComponent("auto/stop", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        howler.stopRobot();
                    }
                });
        results.addBehavior("cornerShot")
                .addConnection("drive_1(auto/drive_fwd_shoot) OUT -> IN drive_2(auto/drive_to_shot)")
                .addConnection("drive_2() OUT -> IN shoot_1(auto/shoot)")
                .addConnection("shoot_1() OUT -> IN shoot_2(auto/shoot_stop)")
                .addConnection("shoot_2() OUT -> IN cap_1(auto/knock_cap_ball)")
                .addConnection("cap_1() OUT -> IN cap_2(auto/drive_to_cap)")
                .addConnection("cap_2() OUT -> IN stop(auto/stop)")

                .addEventTrigger("opmode/start", "IN drive_1()");

        results.initialize();
        return results;
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
        odometry.updates.add(howler);

        if (!odometry.running) {
            odometryThread = new Thread(odometry);
            odometryThread.start();
        }

    }
}
