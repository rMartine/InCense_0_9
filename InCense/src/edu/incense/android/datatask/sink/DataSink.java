package edu.incense.android.datatask.sink;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import edu.incense.android.datatask.DataTask;
import edu.incense.android.datatask.Input;
import edu.incense.android.datatask.InputEnabledTask;
import edu.incense.android.datatask.data.Data;

public class DataSink extends DataTask implements InputEnabledTask {
    private final static String TAG = "DataSink";
    private final static int DEFAULT_MAX_SINK_SIZE = 60;
    private final static long DEFAULT_DRAIN_TIME = 36000000L;//1L * 60L * 60L * 1000L; // each hour
    private long lastDrainTime;
    protected String name;
    protected List<Data> sink = null;

    protected SinkWritter sinkWritter;

    public DataSink(SinkWritter sinkWritter) {
        this.sinkWritter = sinkWritter;
        inputs = new ArrayList<Input>();
        lastDrainTime = System.currentTimeMillis();

        clear();
        initSinkList();
        setPeriodTime(1000);
    }

    public void start() {
        super.start();
        initSinkList();
    }
    
    public void stop() {
        super.stop();
        clearOutputs();
        sinkWritter.writeSink(name, removeSink());
        Log.d(TAG, "Sink sent to writter with size: "+sink.size());
    }

    protected void clearOutputs() {
        // No outputs for DataSink
        // outputs.removeAll(outputs);
        outputs = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.incense.datatask.DataTask#compute() Collects the most
     * recent data from all inputs in a "sink" (List)
     */
    @Override
    protected void compute() {
        Data latestData = null;
        for (Input i : inputs) {
            do {
                long timeSinceLastDrain = System.currentTimeMillis() - lastDrainTime;
                latestData = i.pullData();
                if (latestData != null) {
                    sink.add(latestData);
                    //Log.d(TAG, "Data added to sink!");
                    if(sink.size() >= DEFAULT_MAX_SINK_SIZE || timeSinceLastDrain >= DEFAULT_DRAIN_TIME){
                        drain();
                    }
                } else {
//                    Log.d(TAG, "Data NOT added to sink!");
                }
            } while (latestData != null);
        }
    }

    public List<Data> getSink() {
        return sink;
    }
    
    private void drain(){
        //Log.d(TAG, "Trying to write data...");
        sinkWritter.writeSink(name, removeSink());
        lastDrainTime = System.currentTimeMillis();
    }
    
    /**
     * @param sink the sink to set
     */
    public void setSink(List<Data> sink) {
        this.sink = sink;
    }

    public List<Data> removeSink() {
        List<Data> temp = getSink();
        initSinkList();
        return temp;
    }

    private void initSinkList() {
        sink = new ArrayList<Data>();
    }

    @Override
    public void addInput(Input i) {
        super.addInput(i);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
