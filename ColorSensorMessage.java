package org.firstinspires.ftc.teamcode;


import org.mechadojo.stateflow.Message;

public class ColorSensorMessage extends Message {
    public String leftColor;

    public double leftHue;
    public double leftSat;
    public double leftValue;

    public String rightColor;
    public double rightHue;
    public double rightSat;
    public double rightValue;



    public ColorSensorMessage() {};
    public ColorSensorMessage(float[] left, float[] right) {
        super();
        this.leftHue = left[0];
        this.leftSat = left[1];
        this.leftValue = left[2];

        this.rightHue = right[0];
        this.rightSat = right[1];
        this.rightValue = right[2];

        this.leftColor = leftSat < .5 ? "None" : leftHue > 100 ? "Blue" : "Red";
        this.rightColor = rightSat < .5 ? "None" : rightHue > 100 ? "Blue" : "Red";
    }

    public ColorSensorMessage(ColorSensorMessage msg) {
        super(msg);

        leftHue = msg.leftHue;
        leftSat = msg.leftSat;
        leftValue = msg.leftValue;

        leftColor = msg.leftColor;

        rightHue = msg.rightHue;
        rightSat = msg.rightSat;
        rightValue = msg.rightValue;

        rightColor = msg.rightColor;
    }

    public boolean isLeftValid() {return !leftColor.equals("None");}
    public boolean isRightValid() {return !rightColor.equals("None");}

    @Override
    public Message clone() {
        return new ColorSensorMessage(this);
    }


    @Override
    public String getLogHeader() {
        return "timestamp,leftColor,rightColor,leftHue,leftSat,leftValue,rightHue,rightSat,rightValue\r\n";
    }

    @Override
    public String getLogRow() {
        StringBuilder sb = new StringBuilder();

        sb.append(timestamp).append(',');
        sb.append(leftColor).append(',');
        sb.append(leftHue).append(',');
        sb.append(leftSat).append(',');
        sb.append(leftValue).append(',');

        sb.append(rightColor).append(',');
        sb.append(rightHue).append(',');
        sb.append(rightSat).append(',');
        sb.append(rightValue).append("\r\n");

        return sb.toString();
    }


}
