package edu.incense.android.datatask.data;

public class AccActivityDetectorSMOData extends Data{
	public final static int CLASS = 0;
    public final static int TIMESTAMP = 1;
    public final static int ACTIVIDAD = 0;
    public final static int REPOSO = 1;
    private double[][] frame;

    
    private AccActivityDetectorSMOData(DataType type, double[][] frame) {
        super(type);
        this.frame = frame;
    }
    
    public AccActivityDetectorSMOData(double[][] frame) {
        this(DataType.ACCACTxINACT, frame);
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
