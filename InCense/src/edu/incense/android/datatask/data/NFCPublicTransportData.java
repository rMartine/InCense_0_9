package edu.incense.android.datatask.data;

public class NFCPublicTransportData extends Data{
	private String tag;
	
	public NFCPublicTransportData(DataType type, String tag) {
        super(type);
        this.tag = tag;
    }
    
    public NFCPublicTransportData(String tag) {
        this(DataType.NFCTAG, tag);
    }

    /**
     * @param frame
     *            the frame to set
     */
    public void setFrame(String tag) {
        this.tag = tag;
    }

    /**
     * @return the frame
     */
    public String getFrame() {
        return tag;
    }

}
