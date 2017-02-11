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
                .addComponent("auto/drive_to_vortex", "OUT,WAIT", "IN,WAIT", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        odometry.reset = true;
                        action.info("Wait for encoders");
                        action.out("WAIT", msg);
                    }
                }, new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        EncoderMessage wheel = (EncoderMessage)action.getParameter("odometry/right_wheel");
                        if (wheel != null && !odometry.reset) {
                            if (wheel.position > 1.0) {
                                action.info("Reached encoder: " + wheel.position);
                                action.next(msg);
                                return;
                            }

                            double dist = Math.abs(wheel.position);
                            double power = .35 - (.25/1.0) * dist;
                            howler.drive(-power, -power);
                        }
                        action.out("WAIT", msg);
                    }
                })
                .addComponent("auto/shoot", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        howler.stopDrive();
                        howler.shoot(1.0);
                        action.next(msg.delay(1500));
                    }
                })
                .addComponent("auto/stop_drive", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        howler.stopRobot();
                        action.next(msg);
                    }
                })
                .addComponent("auto/stop", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        howler.stopRobot();
                    }
                })
                .addComponent("auto/turn_to_beacon_1", "OUT,WAIT", "IN,WAIT", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        odometry.reset = true;
                        action.info("Wait for encoders");
                        action.out("WAIT", msg);
                    }
                }, new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        EncoderMessage wheel = (EncoderMessage)action.getParameter("odometry/right_wheel");
                        if (wheel != null && !odometry.reset) {
                            if (wheel.position < -3.2) {
                                action.info("Reached encoder: " + wheel.position);
                                action.next(msg);
                                return;
                            }

                            double dist = Math.abs(wheel.position);
                            double power = .35 - (.20/3.2) * dist;
                            howler.drive(0, power);
                        }
                        action.out("WAIT", msg);
                    }
                })
                .addComponent("auto/drive_to_beacon", "OUT,WAIT", "IN,WAIT", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        odometry.reset = true;
                        action.info("Wait for encoders");
                        action.out("WAIT", msg);
                    }
                }, new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        EncoderMessage wheel = (EncoderMessage)action.getParameter("odometry/right_wheel");
                        LineSensorMessage line = (LineSensorMessage)action.getParameter("sensor/line");
                        if (wheel != null && !odometry.reset) {
                            int count = 0;
                            if(line.line1 < 1.0) count++;
                            if(line.line2 < 1.0) count++;
                            if(line.line3 < 1.0) count++;

                            if(count > 1) {
                                action.info("Reached Line");
                                action.next(msg);
                                return;
                            }
                            if (wheel.position < -5.0) {
                                action.info("Reached encoder: " + wheel.position);
                                action.next(msg);
                                return;
                            }

                            double dist = Math.abs(wheel.position);
                            double power = .45;
                            if(dist > 2.0) {
                                power = .20 - (0.1/3.0) * (dist - 2.0) ;
                            }
                            howler.drive(power, power);
                        }
                        action.out("WAIT", msg);
                    }
                });

        result.addBehavior("main")
                .addConnection("step_1(auto/shoot) OUT -> IN stop(auto/stop_drive)")
                .addConnection("stop() OUT -> IN step_2(auto/drive_to_vortex)")
                .addConnection("step_2() OUT -> IN step_3(auto/stop_drive)")
                .addConnection("step_2() WAIT -> WAIT step_2()")
                .addConnection("step_3() OUT -> IN step_4(auto/turn_to_beacon_1)")
                .addConnection("step_4() OUT -> IN step_5(auto/drive_to_beacon)")
                .addConnection("step_4() WAIT -> WAIT step_4()")
                .addConnection("step_5() OUT -> IN step_6(auto/stop)")
                .addConnection("step_5() WAIT -> WAIT step_5()")
                .addEventTrigger("opmode/start", "IN step_2()" );
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
