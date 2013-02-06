package edu.incense.android.datatask.data;

public class StepsData extends Data{
    public final static int EVENTTIMESTAMP = 0;
    private double[] frame;

    public StepsData(DataType type, double[] frame) {
        super(type);
        this.frame = frame;
    }
    
    public StepsData(double[] frame) {
        this(DataType.STEPSCOUNTER, frame);
    }

    /**
     * @param frame
     *            the frame to set
     */
    public void setFrame(double[] frame) {
        this.frame = frame;
    }

    /**
     * @return the frame
     */
    public double[] getFrame() {
        return frame;
    }
}
