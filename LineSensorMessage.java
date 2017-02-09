package org.firstinspires.ftc.teamcode;


import org.mechadojo.navigation.EncoderMessage;
import org.mechadojo.stateflow.Message;

public class LineSensorMessage extends Message {
    public double line1;
    public double line2;
    public double line3;


    public LineSensorMessage() {};
    public LineSensorMessage(double line1, double line2, double line3) {
        super();
        this.line1 = line1;
        this.line2 = line2;
        this.line3 = line3;
    }

    public LineSensorMessage(LineSensorMessage msg) {
        super(msg);

        line1 = msg.line1;
        line2 = msg.line2;
        line3 = msg.line3;
    }

    @Override
    public Message clone() {
        return new LineSensorMessage(this);
    }


    @Override
    public String getLogHeader() {
        return "timestamp,line1,line2,line3\r\n";
    }

    @Override
    public String getLogRow() {
        StringBuilder sb = new StringBuilder();
        sb.append(timestamp).append(',');
        sb.append(line1).append(',');
        sb.append(line2).append(',');
        sb.append(line3).append("\r\n");
        return sb.toString();
    }


}
