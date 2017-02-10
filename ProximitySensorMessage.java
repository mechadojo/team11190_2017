package org.firstinspires.ftc.teamcode;


import org.mechadojo.stateflow.Message;

public class ProximitySensorMessage extends Message {
    public double distance;


    public ProximitySensorMessage() {};
    public ProximitySensorMessage(double distance) {
        super();
        this.distance = distance;
    }

    public ProximitySensorMessage(ProximitySensorMessage msg) {
        super(msg);

        distance = msg.distance;
    }

    @Override
    public Message clone() {
        return new ProximitySensorMessage(this);
    }


    @Override
    public String getLogHeader() {
        return "timestamp,distance\r\n";
    }

    @Override
    public String getLogRow() {
        StringBuilder sb = new StringBuilder();
        sb.append(timestamp).append(',');
        sb.append(distance).append("\r\n");
        return sb.toString();
    }


}
