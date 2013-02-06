package edu.incense.android.datatask.filter;

import edu.incense.android.datatask.data.CaloriesData;
import edu.incense.android.datatask.data.Data;
import edu.incense.android.datatask.data.StepsData;

public class CaloriesFilter extends DataFilter{
	private double estatura;
	private double peso;
	private double distXPaso = 0.0;
	private double factorDiv = 0.0;
	private double[][] strideLenght = {{8, 1}, {6, 1.2}, {5, 2}, {4, 3}, {3, 4}, {2, 5}};
	private double eventoAnt = 0.0;
	private short pasos2Seg = 0;
	private double distancia = 0.0;
	private double calorias = 0.0;
	private double velProm = 0.0;
	private double ventana = 2000.0;
	
	public CaloriesFilter (double height, double weight){
		super();
		setFilterName(this.getClass().getName());
		this.estatura = height;
		this.peso = weight;
	}
	
	public CaloriesFilter (){
		super();
		setFilterName(this.getClass().getName());
		this.estatura = 1.83;
		this.peso = 90;
	}
	
	@Override
    public void start() {
		super.start();
    }

	@Override
	protected void computeSingleData(Data data) {
		calcCalories(data);
	}

	private void calcCalories(Data data) {
		StepsData steps = (StepsData) data;
		double[] frame = steps.getFrame();
		double[][] calExp = new double[frame.length][4];
		short conAux = 0;
		short i;
		
		for (i=0; i<frame.length; i++){
			pasos2Seg++;
			if (eventoAnt == 0.0){
				eventoAnt = frame[i];
			}
			if ((frame[i] - eventoAnt) >= ventana){
				for (i=0; i<strideLenght.length; i++){
					if (pasos2Seg < strideLenght[i][0]){
						factorDiv = strideLenght[i][1];
					}
				}
				if (factorDiv == 0){
					distXPaso = estatura * 1.2; 
				} else {
					distXPaso = estatura / factorDiv;
				}
				distancia = pasos2Seg * distXPaso;
				velProm = pasos2Seg * (distXPaso / 2.0);
				calorias = velProm * (peso/1800.0);
				calExp[conAux][0] = distancia;
				calExp[conAux][0] = velProm;
				calExp[conAux][0] = calorias;
				calExp[conAux][0] = frame[i];
				conAux++;
				distancia = 0.0;
				velProm = 0.0;
				calorias = 0.0;
				pasos2Seg = 0;
				eventoAnt = frame[i];
				factorDiv = 0.0;
			}
		}
		CaloriesData caloriesData = new CaloriesData(calExp);
		pushToOutputs (caloriesData);
	}
}
