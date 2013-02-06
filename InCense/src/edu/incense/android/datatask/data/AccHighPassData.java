package edu.incense.android.datatask.data;
import edu.incense.android.datatask.data.DataType;

public class AccHighPassData extends Data{
	public final static int X_AXIS = 0;
    public final static int Y_AXIS = 1;
    public final static int Z_AXIS = 2;
    public final static int TIMESTAMP = 3;
    private double[][] frame;

    private AccHighPassData(DataType type, double[][] frame) {
        super(type);
        this.frame = frame;
    }
    
    public AccHighPassData(double[][] frame) {
        this(DataType.ACCHIGHPASSFRAME, frame);
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
