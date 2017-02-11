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
import org.mechadojo.stateflow.messages.BooleanMessage;
import org.mechadojo.stateflow.opmode.StateFlowOpMode;


public class PushButtonAuto extends StateFlowOpMode {
    Hardware howler = new Hardware();
    Sensors sensors = new Sensors();
    TwoWheelOdometry odometry = new TwoWheelOdometry();
    Thread odometryThread;

    public String TeamColor;
    public String OpponentColor;

    double maxSpeed = 0.0;

    @Override
    public Controller loadController() {
        Controller result = new Controller();
        result.addLibrary("drive")
                .addComponents(new DriveByEncoderComponent("auto/drive_fwd_short", howler, "odometry/right_wheel", -1.0, -1.0)
                        .pid(.15, .2, 0, 0)
                        .delay(0, 100)
                        .power(.35, .15)
                        .target(1.5, 0, -999.0))

                .addComponents(new DriveByEncoderComponent("auto/drive_fwd", howler, "odometry/right_wheel", -1.0, -1.0)
                        .pid(0, .2, 0, 0)
                        .delay(0, 100)
                        .power(.35, 0)
                        .target(1.5, 0, -999.0))

                .addComponents(new DriveByEncoderComponent("auto/drive_rev", howler, "odometry/right_wheel", -1.0, -1.0)
                        .pid(-.15, .05, 0, 0)
                        .power(.35, .15)
                        .delay(0,0,500)
                        .target(3.5, 999.0, 0.0))

                .addComponents(new DriveByEncoderComponent("auto/back_turn_right", howler, "odometry/right_wheel", 0, -1.0)
                        .pid(-.15, .2, 0, 0)
                        .power(.35, .15)
                        .target(1.5, 999.0, 0.0))
                .addComponents(new DriveByEncoderComponent("auto/back_turn_left", howler, "odometry/left_wheel", -1.0, 0)
                        .pid(-0.15, 0.2, 0, 0)
                        .power(.35, .15)
                        .target(1.5, 999.9, 0.0))
                .addComponent("auto/fwd_to_shoot", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        msg.message = new DriveByEncoderMessage(.25);
                        action.next(msg);
                    }
                })

                .addComponent("auto/shoot_start", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        action.next(msg.delay(500));
                    }
                })

                .addComponent("auto/shoot_balls", new MessageHandler() {
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
                    }
                })

                .addComponent("auto/fwd_to_vortex", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        howler.shoot(0.0);
                        EncoderMessage m = (EncoderMessage)action.getParameter("odometry/right_wheel");
                        msg.message = new DriveByEncoderMessage(1.0 - m.position);
                        action.next(msg);
                    }
                })


                .addComponent("auto/turn_back_to_beacon", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        msg.message = new DriveByEncoderMessage(-3.25);
                        action.next(msg);
                    }
                })

                .addComponent("auto/rev_to_beacon", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        msg.message = new DriveByEncoderMessage(-4.8);
                        action.next(msg);
                    }
                })

                .addComponent("auto/check_for_line", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {

                        LineSensorMessage line = (LineSensorMessage)action.getParameter("sensor/line");
                        int count = 0;
                        if(line.line1 <  1.0) count++;
                        if(line.line2 < 1.0) count++;
                        if(line.line3 < 1.0) count++;

                        if (count < 3) {
                            action.info("Backup to line: " + count);
                            howler.drive(-.15, -.15);
                            action.idle(msg);
                        } else {
                            action.next(msg);
                            action.info("Found line.");
                        }
                    }
                })


                .addComponent("auto/wait_for_center_line", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {

                        LineSensorMessage line = (LineSensorMessage)action.getParameter("sensor/line");
                        int count = 0;
                        if(line.line1 <  1.0) count++;
                        if(line.line2 < 1.0) count++;
                        if(line.line3 < 1.0) count++;

                        if (count < 3) {
                            action.idle(msg);
                        } else {
                            action.next(msg);
                            action.info("Found line.");
                        }
                    }
                })

                .addComponent("auto/turn_to_beacon", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {

                        LineSensorMessage lm = (LineSensorMessage)action.getParameter("sensor/line");

                        if (lm.line4 < .05) {
                            action.info(String.format("Found line: %f", lm.line4));
                            howler.stopDrive();
                            action.next(msg.delay(1000));
                        } else {
                            action.info(String.format("Searching for line: %f", lm.line4));
                            if(TeamColor.equals("Red")) howler.drive(.20, -.20);
                            else howler.drive(-.20, .20);
                            action.idle(msg);
                        }
                    }
                })

                .addComponent("auto/press_button", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        action.info(String.format("Press %s Button", TeamColor));

                        ColorSensorMessage cm = (ColorSensorMessage) action.getParameter("sensor/color");
                        BooleanMessage leftStall = (BooleanMessage) action.getParameter("robot/left_stalled");
                        BooleanMessage rightStall = (BooleanMessage) action.getParameter("robot/right_stalled");

                        if (cm.isLeftValid() && cm.isRightValid()) {
                            if (leftStall.value || rightStall.value) {
                                action.info("Motor stalled!");
                                howler.stopDrive();
                                action.next(msg);
                                return;
                            }

                            if (cm.leftColor.equals(OpponentColor) || cm.rightColor.equals(OpponentColor)) {
                                howler.drive(.15,.15);
                                action.idle(msg);
                            } else {
                                howler.stopDrive();
                                action.next(msg);
                            }
                        } else {
                            action.error("Lost beacon! " + String.format("%s %s", cm.leftColor, cm.rightColor) );
                            howler.stopDrive();
                            action.idle(msg);
                        }
                    }
                })

                .addComponent("auto/back_from_beacon", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        msg.message = new DriveByEncoderMessage(.25);
                        action.next(msg);
                    }
                })

                .addComponent("auto/select_button", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        ColorSensorMessage cm = (ColorSensorMessage) action.getParameter("sensor/color");

                        if (cm.isLeftValid() && cm.isRightValid()) {
                            howler.stopDrive();
                            if (cm.leftColor.equals(OpponentColor) && cm.rightColor.equals(TeamColor)) {
                                action.info(String.format("Found %s. Press right button.", TeamColor));
                                howler.buttons(false, true);
                                action.next(msg.delay(500));
                            }

                            if (cm.leftColor.equals(TeamColor) && cm.rightColor.equals(OpponentColor)) {
                                action.info(String.format("Found %s. Press left button.", TeamColor));
                                howler.buttons(true, false);
                                action.next(msg.delay(500));
                            }

                            if (cm.leftColor.equals(OpponentColor) && cm.rightColor.equals(OpponentColor)) {
                                action.info("Opposite Color. Press both buttons.");
                                howler.buttons(true, true);
                                action.next(msg.delay(500));
                            }

                            if (cm.leftColor.equals(TeamColor) && cm.rightColor.equals(TeamColor)) {
                                action.info("Correct Color. Skip button press.");
                                howler.buttons(false, false);
                                action.out("SKIP", msg.delay(500));
                            }
                        } else {
                            action.error("Could not find beacon! " + String.format("%s %s", cm.leftColor, cm.rightColor) );

                            BooleanMessage leftStall = (BooleanMessage) action.getParameter("robot/left_stalled");
                            BooleanMessage rightStall = (BooleanMessage) action.getParameter("robot/right_stalled");
                            if (leftStall.value || rightStall.value) {
                                howler.stopDrive();
                                action.out("RETRY", msg);
                                return;
                            }

                            howler.drive(.15,.15);
                            action.idle(msg.delay(50));
                        }
                    }
                })

                .addComponent("auto/push_first_beacon", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        action.info("Push 1st beacon");
                        action.postEvent(MessageRoute.event("auto/push_first_beacon"));
                        howler.stopRobot();
                        howler.buttons(true, true);
                    }
                })

                .addComponent("auto/push_second_beacon", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        action.info("Push 2nd beacon");
                        action.postEvent(MessageRoute.event("auto/push_second_beacon"));
                        howler.stopRobot();
                        howler.buttons(true, true);
                    }
                })

                .addComponent("auto/drive_to_second_beacon", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        action.info("Drive to 2nd beacon");
                        action.postEvent(MessageRoute.event("auto/drive_to_second_beacon"));
                        howler.stopRobot();
                        howler.buttons(true, true);
                    }
                })


                .addComponent("auto/stop", new MessageHandler() {
                    @Override
                    public void handle(MessageRoute msg, Action action) {
                        howler.stopRobot();
                        howler.buttons(true, true);
                    }
                });



        result.addBehavior("main")

                .addConnection("shoot_1(auto/fwd_to_shoot) OUT -> IN shoot_2(auto/drive_fwd_short) -> IN shoot_start(auto/shoot_start)")
                .addConnection("shoot_start() OUT -> IN shoot_3(auto/shoot_balls)")
                .addConnection("shoot_3() OUT -> IN shoot_stop(auto/shoot_stop) -> IN beacon_1(auto/fwd_to_vortex)")

                .addConnection("beacon_1() OUT -> IN beacon_2(auto/drive_fwd_short)")
                .addConnection("beacon_2() OUT -> IN beacon_3(auto/turn_back_to_beacon)")
                .addConnection(TeamColor.equals("Red")
                        ? "beacon_3() OUT -> IN beacon_4(auto/back_turn_right)"
                        : "beacon_3() OUT -> IN beacon_4(auto/back_turn_left)")

                .addConnection("beacon_4() OUT -> IN beacon_5(auto/rev_to_beacon) -> IN wait_center_line(auto/wait_for_center_line)")
                .addConnection("beacon_5() OUT -> IN beacon_6(auto/drive_rev)")
                .addConnection("wait_center_line() OUT -> CANCEL beacon_6()")
                //.addConnection("beacon_6() OUT -> IN stop(auto/stop)")
                .addConnection("beacon_6() OUT -> IN beacon_7(auto/check_for_line)")
                .addConnection("beacon_7() OUT -> IN push_button(auto/push_first_beacon)")

                .addEventTrigger("opmode/start", "IN shoot_1()" );


        result.addBehavior("first_beacon")
                .addConnection("button_1(auto/turn_to_beacon) OUT -> IN button_2(auto/select_button)")
                .addConnection("button_2() OUT -> IN button_3(auto/press_button)")
                .addConnection("button_3() OUT -> IN button_4(auto/back_from_beacon)")
                .addConnection("button_4() OUT -> IN button_5(auto/drive_fwd_short)")
                .addConnection("button_5() OUT -> IN stop(auto/stop)")
                .addConnection("button_2() SKIP -> IN stop()")

                .addEventTrigger("auto/push_first_beacon", "IN button_1()" );
                //.addEventTrigger("opmode/start", "IN button_1()" );



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
        odometry.updates.add(howler);

        if (!odometry.running) {
            odometryThread = new Thread(odometry);
            odometryThread.start();
        }

    }
}

