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


@Autonomous (name = "Red Button Auto", group = "Auto")
public class RedButtonAuto extends PushButtonAuto {

    @Override
    public void init() {
        TeamColor = "Red";
        OpponentColor = "Blue";

        super.init();
    }
}
