package edu.incense.android.datatask.data;

public class AccRMSData extends Data{
	public final static int RMS = 0;
    public final static int TIMESTAMP = 1;
    private double[][] frame;

    
    private AccRMSData(DataType type, double[][] frame) {
        super(type);
        this.frame = frame;
    }
    
    public AccRMSData(double[][] frame) {
        this(DataType.ACCRMS, frame);
    }

    /**
     * @param frame
     *            the frame to set
     */
    public void setFrame(double[][] frame) {
        this.frame = frame;
    }

    /**
     * @return the frame
     */
    public double[][] getFrame() {
        return frame;
    }
}
