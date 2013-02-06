package edu.incense.android.datatask.filter;

import edu.incense.android.datatask.data.Data;
import edu.incense.android.datatask.data.NfcData;

public class NFCPublicTransportFilter extends DataFilter{
	public NFCPublicTransportFilter() {
        super();
        setFilterName(this.getClass().getName());
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    protected void computeSingleData(Data data) {
        Data newData = getNFCTag(data);
        pushToOutputs(newData);
    }

    private Data getNFCTag(Data data) {
    	NfcData nfcData = (NfcData) data;
    	data.getExtras().putBoolean(nfcData.getMessage(), true);
        return data;
    }
}
