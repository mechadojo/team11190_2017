package org.firstinspires.ftc.teamcode;

import org.mechadojo.navigation.PidController;
import org.mechadojo.stateflow.Action;
import org.mechadojo.stateflow.Message;

public class DriveByEncoderMessage extends Message {
    public double target;
    public PidController pid = new PidController();

    public DriveByEncoderMessage(double target) {
        this.target = target;
    };

}
