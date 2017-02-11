package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

/**
 * Created by 64006039 on 2/10/17.
 */

@Autonomous (name = "Blue Button Auto", group = "Blue")
public class BlueButtonAuto extends PushButtonAuto {
    @Override
    public void init() {
        TeamColor = "Blue";
        OpponentColor = "Red";

        super.init();
    }
}
