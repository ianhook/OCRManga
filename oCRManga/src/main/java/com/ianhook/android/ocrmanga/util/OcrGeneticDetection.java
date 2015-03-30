package com.ianhook.android.ocrmanga.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.BooleanGene;
import org.jgap.impl.DoubleGene;
import org.jgap.impl.IntegerGene;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.googlecode.eyesfree.ocr.client.Ocr;
import com.googlecode.eyesfree.ocr.client.Ocr.InitCallback;
import com.googlecode.eyesfree.ocr.client.OcrResult;
import com.googlecode.eyesfree.ocr.client.Ocr.CompletionCallback;
import com.googlecode.eyesfree.textdetect.HydrogenTextDetector;
import com.googlecode.eyesfree.textdetect.HydrogenTextDetector.Parameters;
import com.googlecode.tesseract.android.TessBaseAPI;

public class OcrGeneticDetection extends AsyncTask<Void, Void, Void>{
    private static final String TAG = "com.ianhook.OcrGD";
    //whether the app should attempt learning
    public final static Boolean mDoGA = true;
    //whether learning should evaluate final message text or just boxes
    public final static Boolean textEval = true;
    
    private Context mContext;
    private int mLastQueued;
    private int mFinished;
    private Ocr mCurrentOcr;
    private Boolean mNotDone;
    private int mResult;
    private Genotype population;
    private boolean mDebug = false;
    private int mCurrentChrome;
    private int mBest;
    private int mGeneration;
    private Object mServiceLock;
    private FitnessFunction myFunc;
    private OcrTestBase mOcrTest;
    
    public class GeneDescriptor {
        public String name;
        public String type;
        public Object min;
        public Object max;
        
        public GeneDescriptor(String name, String type, Object min, Object max) {
            this.name = name;
            this.type = type;
            this.min = min;
            this.max = max;
        }
        
        public Gene getGene(Configuration conf) throws InvalidConfigurationException {
            Gene gene;
            Log.d(TAG, String.format("params: %s, %s, %s, %s", name, type, min.toString(), max.toString()));
            if(type == "int") {
                gene = new IntegerGene(conf, Integer.parseInt(min.toString()), Integer.parseInt(max.toString()));
            } else if(type == "double") {
                gene = new DoubleGene(conf, ((Float)min).doubleValue(), ((Float)max).doubleValue());
            } else {//if(type == "boolean") {
                gene = new BooleanGene(conf);
            } 
            return gene;
        }
    }

    public void setTest(OcrTestBase ot) {
        mOcrTest = ot;
        mOcrTest.mDebug = true;
    }
    
    private List<GeneDescriptor> geneDescriptions = new ArrayList<GeneDescriptor>();
    
    public OcrGeneticDetection(Context context){
        mContext = context;
        mGeneration = 0;
        mServiceLock = new Object();
        
        // Edge-based thresholding
        geneDescriptions.add(new GeneDescriptor("edge_tile_x", "int", 10, 50));
        geneDescriptions.add(new GeneDescriptor("edge_tile_y", "int", 10, 100));
        geneDescriptions.add(new GeneDescriptor("edge_thresh", "int", 10, 100));
        geneDescriptions.add(new GeneDescriptor("edge_avg_thresh", "int", 1, 50));
        geneDescriptions.add(new GeneDescriptor("single_min_aspect", "double", .01f, 1.0f));
        geneDescriptions.add(new GeneDescriptor("single_max_aspect", "double", 1.0f, 10.0f));
        geneDescriptions.add(new GeneDescriptor("single_min_area", "int", 4, 100));
        geneDescriptions.add(new GeneDescriptor("single_min_density", "double", .01f, .9f));
        geneDescriptions.add(new GeneDescriptor("pair_h_ratio", "double", 0.0f, 2.0f));
        geneDescriptions.add(new GeneDescriptor("pair_d_ratio", "double", 0.0f, 2.0f));
        geneDescriptions.add(new GeneDescriptor("pair_h_dist_ratio", "double", 0.0f, 2.0f));
        geneDescriptions.add(new GeneDescriptor("pair_v_dist_ratio", "double", 0.0f, 2.0f));
        geneDescriptions.add(new GeneDescriptor("pair_h_shared", "double", 0.0f, 2.0f));
        geneDescriptions.add(new GeneDescriptor("cluster_width_spacing", "int", 1, 10));
        geneDescriptions.add(new GeneDescriptor("cluster_shared_edge", "double", 0.0f, 1.0f));
        geneDescriptions.add(new GeneDescriptor("cluster_h_ratio", "double", 0.1f, 2.0f));
        geneDescriptions.add(new GeneDescriptor("cluster_min_blobs", "int", 1, 10));
        geneDescriptions.add(new GeneDescriptor("cluster_min_aspect", "double", 0.1f, 5.0f));
        geneDescriptions.add(new GeneDescriptor("cluster_min_fdr", "double", 0.1f, 5.0f));
        geneDescriptions.add(new GeneDescriptor("cluster_min_edge", "int", 1, 50));
        geneDescriptions.add(new GeneDescriptor("cluster_min_edge_avg", "int", 1, 50));
        
        /* skew correction off for first runs
        // Skew angle correction
        //public boolean skew_enabled;
        sampleGenes[21] = new BooleanGene(conf);

        //public float skew_min_angle;
        sampleGenes[22] = new DoubleGene(conf, 0.0, 10.0f);

        //public float skew_sweep_range;
        sampleGenes[23] = new DoubleGene(conf, 20.0f, 40.0f);

        //public float skew_sweep_delta;
        sampleGenes[24] = new DoubleGene(conf, );

        public int skew_sweep_reduction;
        sampleGenes[25] = new IntegerGene(conf, 1, 10 );

        public int skew_search_reduction;
        sampleGenes[26] = new IntegerGene(conf, 1, 10 );

        public float skew_search_min_delta;
        sampleGenes[27] = new DoubleGene(conf, 0.0f, 1.0f);
         */

    }
    
    public void startOcr() {
        InitCallback ocrInit = new InitCallback() {

            @Override
            public void onInitialized(int status) {

                synchronized(mServiceLock) {
                    com.googlecode.eyesfree.ocr.client.Ocr.Parameters params1 = mCurrentOcr.getParameters();
                    params1.setFlag(com.googlecode.eyesfree.ocr.client.Ocr.Parameters.FLAG_DEBUG_MODE, false);
                    params1.setFlag(com.googlecode.eyesfree.ocr.client.Ocr.Parameters.FLAG_ALIGN_TEXT, false);
                    params1.setLanguage("jpn");
                    params1.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK_VERT_TEXT);
                    //params.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
                    mCurrentOcr.setParameters(params1);
                    mServiceLock.notify();
                }
            }
            
            
        };
        
        synchronized(mServiceLock) {
            //mCurrentOcr = new Ocr(mContext, null);
            mCurrentOcr = new Ocr(mContext, ocrInit);
            try {
                Log.d(TAG, "wait on service");
                mServiceLock.wait();
                //Log.d(TAG, "wait on service done");
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    public void doLearning() throws InvalidConfigurationException {

        //File temp_f = new File(Environment.getExternalStorageDirectory(), "test_data");
        //mImageDir = (new File(temp_f, "test_images")).listFiles();
        
        Gene[] sampleGenes = new Gene[ geneDescriptions.size() ];
        Configuration conf = new DefaultConfiguration();
        
        for(int i = 0; i < geneDescriptions.size(); i++) {
            sampleGenes[i] = geneDescriptions.get(i).getGene(conf);
        }
        myFunc = new OcrFitnessFunction( );

        conf.setFitnessFunction( myFunc );
        
        conf.setPopulationSize(100);
        Chromosome returnVal = new Chromosome(conf, sampleGenes);
        conf.setSampleChromosome(returnVal);
        
        population = Genotype.randomInitialGenotype(conf);
        mBest = 0;
        int success_runs = 0;

        startOcr();
        
        while(success_runs < 2) {
            mCurrentChrome = 0;
            Log.d(TAG, String.format("start evolution: %d", mGeneration));
            population.evolve();
            ((OcrFitnessFunction) myFunc).printBest();
            Log.d(TAG, String.format("end evolution: %d", mGeneration));
            if(mBest > 20401) {
                success_runs += 1;
            }
        }
        mCurrentOcr.release();
        
    }
    
    public class OcrFitnessFunction extends FitnessFunction {
        
        public void printBest() {
            boolean currentDebug = mDebug;
            mDebug = true;
            IChromosome best = population.getFittestChromosome();
            evaluate(best);
            mDebug = currentDebug;
            
        }

        @Override
        protected double evaluate(IChromosome arg0) {

            //create a new HydrogentextDetector with the parameters from the Chromosome
            HydrogenTextDetector tester = new HydrogenTextDetector();
            Parameters params = tester.getParameters();
            Field[] fields = params.getClass().getFields();
            com.googlecode.eyesfree.ocr.client.Ocr.Parameters params1 = mCurrentOcr.getParameters();
            
            for(Field f : fields) {
                if(mDebug)
                    Log.d("setParams", f.getName());
                for (int i = 0; i < geneDescriptions.size(); i++) {
                    //Log.d("setParams", "gene:" + geneDescriptions.get(i).name);
                    if(f.getName().equals(geneDescriptions.get(i).name)) {
                        //Log.d("setParams", "found");
                        
                        try {
                            params1.setVariable(f.getName(), arg0.getGene(i).getAllele().toString());
                            if(mDebug)
                                Log.d("setParams", String.format("%s: %s, %s", f.getName(), f.get(params), 
                                        arg0.getGene(i).getAllele().toString()));
                        } catch (IllegalAccessException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            Log.e("setParams", f.getName() + ":" + e.getMessage());
                        } catch (IllegalArgumentException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            Log.e("setParams", f.getName() + ":" + e.getMessage());
                        }
                        break;
                    }
                }
            }

            mCurrentOcr.setParameters(params1);
            
            //Initialize the trials
            mNotDone = true;
            mResult = 0;
            mLastQueued = -1;
            mFinished = -1;
            mOcrTest.resetPosition();
            
            while(mNotDone) {
                doOcr();
            }
            
            Log.d(TAG, String.format("Fitness Result %d: %d -> %d", mCurrentChrome, mResult, mBest));
            if(mResult > mBest || mDebug) {
                Log.d("BestParams", String.format("Fitness Result %d: %d -> %d", mCurrentChrome, mResult, mBest));
                for (int i = 0; i < geneDescriptions.size(); i++) {
                    Log.d("BestParams", String.format("%s: %s", geneDescriptions.get(i).name, 
                            arg0.getGene(i).getAllele().toString()));
                }
                mBest = mResult;

                //save the best for easy usage
                try
                {
                    FileOutputStream fileOut =
                            new FileOutputStream("/storage/sdcard/Manga/ga_best_params.ser");
                    ObjectOutputStream out = new ObjectOutputStream(fileOut);
                    out.writeObject(params1);
                    out.close();
                    fileOut.close();
                }catch(IOException i)
                {
                    i.printStackTrace();
                }
            }

            mCurrentChrome += 1;
            
            return mResult;
        }
        
    }
    
    public void doOcr(){

        synchronized(mOcrTest) {
            //Log.d(TAG, String.format("looping %d, %d, %d", mCurrentPosition, mLastQueued, mFinished));
            //for each image string pair
            if(mOcrTest.canContinue(mLastQueued)) {
                // get bitmap at current position
                mLastQueued = mOcrTest.getPosition();
    
                if (mDebug) {
                    Log.d(TAG, String.format("image %d, %s", mLastQueued, mOcrTest.getCurrentName()));
                }
    
                CompletionCallback displayText = new OcrCompleted();
                mCurrentOcr.setCompletionCallback(displayText);
                mCurrentOcr.enqueue(mOcrTest.getImage());
    
                try {
                    mOcrTest.wait();
                } catch (InterruptedException e) {
                    Log.d(TAG, "wait error");
                    e.printStackTrace();
                }
            } else if(mFinished == mLastQueued) {
                // we're done so we can stop busy waiting
                mNotDone = false;
            }
        }
    }

    private class OcrCompleted implements CompletionCallback {

        @Override
        public void onCompleted(List<OcrResult> results) {
            Log.d(TAG, "got some results");

            synchronized(mOcrTest) {
                //score message
                mResult += mOcrTest.evalResults(results);

                //repeat
                mFinished += 1;
                mOcrTest.next();
                //mCurrentPosition += 1;
                mOcrTest.notify();
                //doOcr();
            }
                
        }
   
    }

    @Override
    protected Void doInBackground(Void... params) {
        // TODO Auto-generated method stub
        Log.d(TAG, "background learning");
        try {
            doLearning();
        } catch (InvalidConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    /*
    @Override
    protected void onPostExecute(Void v) {
        IChromosome bestSolutionSoFar = population.getFittestChromosome();
        bestSolutionSoFar.
    }
    */
}
