package edu.incense.android.datatask.filter;

import edu.incense.android.datatask.data.AccHighPassData;
import edu.incense.android.datatask.data.AccVectorMagData;
import edu.incense.android.datatask.data.Data;

public class AccVectorMagFilter extends DataFilter {
	//private static final String ATT_STEPS = "HighPassFilterAcc:";
	public AccVectorMagFilter(){
		super();
		setFilterName(this.getClass().getName());
	}
	
	@Override
    public void start() {
		super.start();
	}
	
	@Override
	protected void computeSingleData(Data data) {
		vMagFilter(data);
	}
	
	private void vMagFilter(Data data){
		AccHighPassData accData = (AccHighPassData) data;
        double[][] dataAccLineal = accData.getFrame();
        double[][] vMag = new double[dataAccLineal.length][2];
        for(int i=0; i<dataAccLineal.length; i++){
        	vMag[i][0] = Math.sqrt(Math.pow(dataAccLineal[i][0],2) + Math.pow(dataAccLineal[i][1],2) + Math.pow(dataAccLineal[i][2],2));
        	vMag[i][1] = dataAccLineal[i][3];
    	}
        //Crear tipo de dato especial para este filtro y mandarle como parametro vMag
        AccVectorMagData accMagnitude = new AccVectorMagData(vMag); 
		pushToOutputs(accMagnitude);
	}
}
