package org.firstinspires.ftc.teamcode;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.mechadojo.stateflow.Controller;
import org.mechadojo.stateflow.MessageRoute;
import org.mechadojo.stateflow.examples.DelayedHelloWorldController;
import org.mechadojo.stateflow.examples.GamepadHelloWorldController;
import org.mechadojo.stateflow.examples.HelloWorldController;
import org.mechadojo.stateflow.opmode.StateFlowOpMode;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

@TeleOp(name = "State Flow ", group = "Test")
public class StateFlowAuto extends StateFlowOpMode {
    @Override
    public Controller loadController() {
        return new DelayedHelloWorldController();
    }

    @Override
    public void start() {
        try {
            FileOutputStream os = new FileOutputStream("/sdcard/FIRST/test.txt");
            os.write("This is another test!".getBytes());
            os.close();
        }catch (Exception ex) {
            Log.e("StateFlow", "open file: ", ex );
        }

        super.start();
        //controller.addMessage( "IN helloworld.step_1()" );
    }
}
