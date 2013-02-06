package edu.incense.android.results;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class SendFileTask extends AsyncTask<Void, Void, Integer> {
    private Context context;

    public SendFileTask(Context context) {
        super();
        this.context = context;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        ResultsUploader resultsUploader = new ResultsUploader(context);
        return resultsUploader.sendFiles();
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (result > 0) {
            Toast.makeText(context, result + " files were uploaded.",
                    Toast.LENGTH_LONG).show();
        }

    }
}
