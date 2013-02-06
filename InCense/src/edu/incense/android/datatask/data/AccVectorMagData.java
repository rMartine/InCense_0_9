package edu.incense.android.datatask.data;

public class AccVectorMagData extends Data{
	public final static int MAGNITUDE = 0;
    public final static int TIMESTAMP = 1;
    private double[][] frame;

    
    private AccVectorMagData(DataType type, double[][] frame) {
        super(type);
        this.frame = frame;
    }
    
    public AccVectorMagData(double[][] frame) {
        this(DataType.ACCVECMAG, frame);
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
