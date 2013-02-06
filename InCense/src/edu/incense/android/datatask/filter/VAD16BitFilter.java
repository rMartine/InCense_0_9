package edu.incense.android.datatask.filter;
import edu.incense.android.datatask.data.AudioData;
import edu.incense.android.datatask.data.Data;
import edu.incense.android.datatask.data.VAD16BitData;

public class VAD16BitFilter extends DataFilter{
	private double spectralEntropy = 0.0;
	private double spectralCentroid = 0.0;
	private double bandWidth = 0.0;
	private double zeroCrossingRate = 0.0;
	private double rootMeanSquare = 0.0;
	double[] modelRms = {43.3096, -2.0891};
	double[] normRms = {12751.1318344319};
	double[] model = {3.3501, 18.6294, -26.1744, 9.0646, 11.2483, -4.3009};
	double[] norm = {8.08836432868427, 752.049542798517, 211182.176627803, 1544.4, 12751.1318344319};
	
	public VAD16BitFilter(double[] modelRms, double[] normRms, double[] model, double[] norm){
		super();
		setFilterName(this.getClass().getName());
		this.modelRms = modelRms;
		this.normRms = normRms;
		this.model = model;
		this.norm = norm;
	}
	
	public VAD16BitFilter(){
		super();
		setFilterName(this.getClass().getName());
	}
	
	@Override
    public void start() {
        super.start();
    }

	@Override
	protected void computeSingleData(Data data) {
		signalProcess(data);
	}

	private void signalProcess(Data data) {
		AudioData audioStream = (AudioData) data;
		byte[] stream = audioStream.getAudioFrame();
		short [] muestras = new short[2];
		muestras[0] = 0;
		muestras[1] = 0;
		short frameMsb = 0;
		short frameLsb = 0;
		int nMuestra = 0;
		double ventaneo = 0;
		double sign0 = 0;
		double sign1 = 0;
		short tamBuffer = 2048;
		Complex[] buffer = new Complex[tamBuffer];
		int i = 0;
		int nFrame = 0;
		double[][] vadResults = new double[(stream.length/(2*tamBuffer))][6];
		
		for (i = 0; i < stream.length; i += 2){
			muestras[0] = muestras[1];
			frameMsb = stream[i+1];
			frameLsb = stream[i];
			if (frameLsb < 0){
				frameLsb = (short) ((frameLsb * (-1)) + 128);
			}
			if (frameMsb < 0){
				muestras[1] = (short) ((-1) * (frameLsb + (frameMsb * (-1) * Math.pow(2,8))));
			} else {
				muestras[1] = (short) (frameLsb + (frameMsb * Math.pow(2,8)));
			}
			sign0 = 0;
			sign1 = 0;
			if (muestras[1]>=0){
				sign1 = 1;
			} else {
				sign1 = -1;
			}
			if (muestras[0]>=0){
				sign0 = 1;
			} else {
				sign0 = -1;
			}
			ventaneo = muestras[1] * (0.5 - 0.5*Math.cos(2*3.14159265*nMuestra/(tamBuffer-1)));
			buffer[nMuestra] = new Complex (ventaneo, 0);
			rootMeanSquare += Math.pow((muestras[1]), 2);
			zeroCrossingRate += Math.sqrt(Math.pow((sign1 - sign0),2));
			nMuestra++;
			if (nMuestra == tamBuffer){
				double[] featuresRMS = {rootMeanSquare};
				rootMeanSquare = Math.sqrt((rootMeanSquare / ((double) tamBuffer)));
				zeroCrossingRate = zeroCrossingRate / 2.0;
				if (classify(featuresRMS, modelRms, normRms)>0){
					vadResults[nFrame][0] = calcFeatures(buffer, rootMeanSquare, zeroCrossingRate);
					/*if (calcFeatures(buffer, rootMeanSquare, zeroCrossingRate)>0){
						vadResults[nFrame][0] = 1.0;
					} else {
						vadResults[nFrame][0] = -1.0;
					}*/
				} else {
					vadResults[nFrame][0] = -1.0;
				}
				
				vadResults[nFrame][1] = spectralEntropy;
				vadResults[nFrame][2] = spectralCentroid;
				vadResults[nFrame][3] = bandWidth;
				vadResults[nFrame][4] = zeroCrossingRate;
				vadResults[nFrame][5] = rootMeanSquare;
				buffer = null;
				buffer = new Complex[tamBuffer];
				nMuestra = 0;
				rootMeanSquare = 0.0;
				zeroCrossingRate = 0.0;
				nFrame++;
			}
		}
		VAD16BitData results = new VAD16BitData(vadResults);
		pushToOutputs(results);
	}
	
	public double calcFeatures (Complex[] elBuffer, double rms, double zcr){
		double energiaAcum = 0;
		int tamBuffer = elBuffer.length;
		double centroidSum = 0.0;
		
		Complex[] bufferFft = new Complex[tamBuffer];
		bufferFft = FFT.fft(elBuffer);
		
		short i = 0;
		for(i=1; i <= bufferFft.length/2; i++){
			energiaAcum += bufferFft[i].abs();
		}
		
		double[] fftNorm = new double[tamBuffer/2];
		for (i=1; i <= (tamBuffer/2); i++){
			fftNorm[i-1] = bufferFft[i].abs() / energiaAcum;
		}
		
		for (i=0; i<fftNorm.length; i++){
			spectralEntropy += fftNorm[i] * Math.log(fftNorm[i]);
			spectralCentroid += ((i+1)*Math.pow(fftNorm[i], 2));
			centroidSum += Math.pow(fftNorm[i], 2);
		}
		spectralEntropy = spectralEntropy * (-1);
		spectralCentroid = spectralCentroid / centroidSum;
		for (i=0; i<fftNorm.length; i++){
			bandWidth += (Math.pow(fftNorm[i], 2) * Math.pow(((i+1) - spectralCentroid), 2));
		}
		bandWidth = bandWidth / centroidSum;
		double[] features = {spectralEntropy, spectralCentroid, bandWidth, zcr, rms};
		return classify(features, model, norm);
	}
	
	public double classify(double[] features, double[] model, double[] normalize){
		byte i = 0;
		double eval = 0.0;
		for (i=0; i<features.length; i++){
			eval += (model[i]*(features[i]/normalize[i]));
		}
		eval += model[i];
		return eval; 
	}
}
