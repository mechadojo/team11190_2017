package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.mechadojo.stateflow.Controller;
import org.mechadojo.stateflow.MessageRoute;
import org.mechadojo.stateflow.examples.DelayedHelloWorldController;
import org.mechadojo.stateflow.examples.GamepadHelloWorldController;
import org.mechadojo.stateflow.examples.HelloWorldController;
import org.mechadojo.stateflow.opmode.StateFlowOpMode;

@TeleOp(name = "State Flow ", group = "Test")
public class StateFlowAuto extends StateFlowOpMode {
    @Override
    public Controller loadController() {
        return new GamepadHelloWorldController();
    }

    @Override
    public void start() {
        super.start();
        //controller.addMessage( "IN helloworld.step_1()" );
    }
}
