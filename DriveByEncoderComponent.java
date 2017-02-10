package org.firstinspires.ftc.teamcode;

import org.mechadojo.navigation.EncoderMessage;
import org.mechadojo.navigation.PidController;
import org.mechadojo.stateflow.Action;
import org.mechadojo.stateflow.Component;
import org.mechadojo.stateflow.Message;
import org.mechadojo.stateflow.MessageRoute;

import java.util.ArrayList;
import java.util.HashMap;

public class DriveByEncoderComponent extends Component {

    Hardware robot;

    public double targetMax = 0.0;
    public double targetMin = 0.0;

    public double maxError = 0.0;
    public double zeroPower = 0.0;
    public double kP = 0.0;
    public double kI = 0.0;
    public double kD = 0.0;

    public double maxPower = 1.0;
    public double minPower = 0.0;

    public double leftPowerFactor = 0;
    public double rightPowerFactor = 0;

    public boolean stopOnTarget = true;

    // Add the current encoder position to the target when initializing the controller
    public boolean offsetTarget = true;

    public long delayLoop = 20;
    public long delayOnStart = 0;
    public long delayOnTarget = 0;

    public String encoder = "";
    public String output = "";

    public DriveByEncoderComponent(String name, Hardware robot, String encoder, double left, double right) {
        this.name = name;
        this.robot = robot;
        this.encoder = encoder;
        this.leftPowerFactor = left;
        this.rightPowerFactor = right;

        addInput("IN,WAIT,CANCEL");
        addOutputs("OUT,WAIT");
    }

    public DriveByEncoderComponent power(double maxPower, double minPower) {
        this.maxPower = maxPower;
        this.minPower = minPower;
        return this;
    }

    public DriveByEncoderComponent pid(double zeroPower, double kP, double kI, double kD) {

        this.kP = kP;
        this.kI = kI;
        this.kD = kD;
        return this;
    }

    public DriveByEncoderComponent stop(boolean stopOnTarget) {
        this.stopOnTarget = stopOnTarget;
        return this;
    }

    public DriveByEncoderComponent delay(long delayOnStart, long delayOnTarget) {
        this.delayOnStart = delayOnStart;
        this.delayOnTarget = delayOnTarget;
        return this;
    }


    public DriveByEncoderComponent delay(long delayOnStart, long delayLoop, long delayOnTarget) {
        this.delayOnStart = delayOnStart;
        this.delayOnTarget = delayOnTarget;
        this.delayLoop = delayLoop;
        return this;
    }

    public DriveByEncoderComponent target(double maxError, double targetMax, double targetMin) {
        this.maxError = maxError;
        this.targetMax = targetMax;
        this.targetMin = targetMin;
        return this;
    }

    public void run(HashMap<String, ArrayList<MessageRoute>> messages, Action action) {
        if (messages.containsKey("IN")) {
            for(MessageRoute msg : messages.get("IN")) {
                init(msg, action);
            }
        }

        if (messages.containsKey("CANCEL") && messages.containsKey("WAIT")) {
            MessageRoute m = messages.get("CANCEL").get(0);

            action.info("Received cancel signal: " + (m.source != null ? m.source.path : m.event));
            next(m, action);
            return;
        }

        if (messages.containsKey("WAIT")) {
            for(MessageRoute msg : messages.get("WAIT")) {
                wait(msg, action);
            }
        }


    }

    public boolean containsHandler(String port) {
        return (port.equals("IN") || port.equals("WAIT"));
    }


    public void init(MessageRoute msg, Action action) {
        if (msg.message instanceof DriveByEncoderMessage ) {
            DriveByEncoderMessage m = (DriveByEncoderMessage)msg.message;

            EncoderMessage e = getEncoder(action);

            m.pid.maxError = maxError;
            m.pid.zeroPower = zeroPower;
            m.pid.kP = kP;
            m.pid.kI = kI;
            m.pid.kD = kD;
            m.pid.maxResult = maxPower;
            m.pid.minResult = minPower;

            m.pid.value = e.position;
            m.pid.set(m.target + e.position);

            msg.target.port = "WAIT";

            if (delayOnStart > 0) {

                action.idle(msg.delay(delayOnStart));
            }
            else
                action.idle(msg);
        }
    }

    public EncoderMessage getEncoder(Action action) {
        Message msg = action.getParameter(encoder);
        if (msg == null) return null;
        if (msg instanceof EncoderMessage) return (EncoderMessage)msg;
        return null;
    }



    public void wait(MessageRoute msg,  Action action) {
        if (msg.message instanceof DriveByEncoderMessage) {
            DriveByEncoderMessage m = (DriveByEncoderMessage) msg.message;

            EncoderMessage e = getEncoder(action);
            if (e == null) { action.error("No encoder message found: " + encoder); return; }

            double power = m.pid.update(e.position, e.period / 1000000000.0, 0.0);
            if (m.pid.error <= targetMax && m.pid.error >= targetMin)
            {
                action.info(String.format("Reached target: %.2f (%.2f)", e.position, m.pid.target));

                if (stopOnTarget) robot.stopDrive();
                if (delayOnTarget > 0)
                    action.next(msg.delay(delayOnTarget));
                else
                    action.next(msg);

                return;
            }

            action.verbose(String.format("Driving: %.2f (%.2f) Power: %.2f", e.position, m.pid.target, power));


            robot.drive(power * leftPowerFactor, power * rightPowerFactor);

            if (delayLoop > 0)
                action.idle(msg.delay(delayLoop));
            else
                action.idle(msg);
        }
    }

    public void next(MessageRoute msg, Action action) {
        if (stopOnTarget) robot.stopDrive();
        if (delayOnTarget > 0)
            action.next(msg.delay(delayOnTarget));
        else
            action.next(msg);
    }

}
