package com.ianhook.android.ocrmanga.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import com.googlecode.eyesfree.ocr.client.Ocr;
import com.googlecode.eyesfree.ocr.client.Ocr.Job;
import com.googlecode.eyesfree.ocr.client.OcrResult;
import com.googlecode.eyesfree.ocr.client.Ocr.CompletionCallback;
import com.googlecode.eyesfree.textdetect.HydrogenTextDetector;
import com.googlecode.eyesfree.textdetect.HydrogenTextDetector.Parameters;
import com.googlecode.tesseract.android.TessBaseAPI;

public class OcrGeneticDetection extends AsyncTask<Void, Void, Void>{
    private static final String TAG = "com.ianhook.OcrGeneticDetection";
    public final static Boolean mDoGA = false;
    
    private Context mContext;
    private int mCurrentPosition;
    private int mLastQueued;
    private int mFinished;
    private Ocr mCurrentOcr;
    private Boolean mNotDone;
    private int mResult;
    private File[] mImageDir;
    private Genotype population;
    private boolean mDebug = false;
    private int mCurrentChrome;
    private int mBest;
    
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
                gene = new IntegerGene(conf, (int)min, (int)max);
            } else if(type == "double") {
                gene = new DoubleGene(conf, ((Float)min).doubleValue(), ((Float)max).doubleValue());
            } else {//if(type == "boolean") {
                gene = new BooleanGene(conf);
            } 
            return gene;
        }
    }
    
    private List<GeneDescriptor> geneDescriptions = new ArrayList<GeneDescriptor>();
    
    public OcrGeneticDetection(Context context){
        mContext = context;
        mCurrentPosition = 0;
        
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
      
        startOcr();

    }
    
    private void startOcr() {
        mCurrentOcr = new Ocr(mContext, null);
        com.googlecode.eyesfree.ocr.client.Ocr.Parameters params1 = mCurrentOcr.getParameters();
        params1.setFlag(com.googlecode.eyesfree.ocr.client.Ocr.Parameters.FLAG_DEBUG_MODE, false);
        params1.setFlag(com.googlecode.eyesfree.ocr.client.Ocr.Parameters.FLAG_ALIGN_TEXT, false);
        params1.setLanguage("jpn");
        params1.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK_VERT_TEXT);
        //params.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
        mCurrentOcr.setParameters(params1);
    }
    
    public void doLearning() throws InvalidConfigurationException {

        File temp_f = new File(Environment.getExternalStorageDirectory(), "test_data");
       
        mImageDir = (new File(temp_f, "test_images")).listFiles();
        
        Gene[] sampleGenes = new Gene[ geneDescriptions.size() ];
        Configuration conf = new DefaultConfiguration();
        
        for(int i = 0; i < geneDescriptions.size(); i++) {
            sampleGenes[i] = geneDescriptions.get(i).getGene(conf);
        }
        FitnessFunction myFunc =
                new OcrFitnessFunction( );

        conf.setFitnessFunction( myFunc );
        
        conf.setPopulationSize(100);
        Chromosome returnVal = new Chromosome(conf, sampleGenes);
        conf.setSampleChromosome(returnVal);
        
        population = Genotype.randomInitialGenotype(conf);
        int generation = 0;
        mBest = 0;
        while(true) {
            startOcr();
            mCurrentChrome = 0;
            Log.d(TAG, String.format("start evolution: %d", generation));
            population.evolve();
            mCurrentOcr.release();
            ((OcrFitnessFunction) myFunc).printBest();
            Log.d(TAG, String.format("end evolution: %d", generation));
            
        }
        
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
                        } catch (IllegalAccessException
                                | IllegalArgumentException e) {
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
            mCurrentPosition = 0;
            
            //busy wait :(
            while(mNotDone) {
                doOcr();
            }
            
            Log.d(TAG, String.format("Fitness Result %d: %d -> %d", mCurrentChrome, mResult, mBest));
            if(mResult > mBest) {
                Log.d("BestParams", String.format("Fitness Result %d: %d -> %d", mCurrentChrome, mResult, mBest));
                for (int i = 0; i < geneDescriptions.size(); i++) {
                    Log.d("BestParams", String.format("%s: %s", geneDescriptions.get(i).name, 
                            arg0.getGene(i).getAllele().toString()));
                }
                mBest = mResult;
            }

            mCurrentChrome += 1;
            
            return mResult;
        }
        
    }
    
    public void doOcr(){

        synchronized(mImageDir) {
            //Log.d(TAG, String.format("looping %d, %d, %d", mCurrentPosition, mLastQueued, mFinished));
            //for each image string pair
            if(mLastQueued != mCurrentPosition 
                    && mCurrentPosition < mImageDir.length) {
                // get bitmap at current position
                mLastQueued = mCurrentPosition;
    
                //Log.d(TAG, String.format("image %d, %s", mCurrentPosition, mImageDir[mCurrentPosition].getName()));
    
                CompletionCallback displayText = new OcrCompleted();
                mCurrentOcr.setCompletionCallback(displayText);
                Job j = mCurrentOcr.enqueue(mImageDir[mCurrentPosition]);
    
                try {
                    //Log.d(TAG, "waiting...");
                    mImageDir.wait();
                } catch (InterruptedException e) {
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
            //Log.d(TAG, "got some results");

            synchronized(mImageDir) {
                //  get the ocr results for an image
                String message = "";
                if(!results.isEmpty()) {
                    for(int i = 0; i < results.size(); i++)
                        message += results.get(i).getString() + "\n";
                }
                
                //  get the string distance from the actual string            
                String encoding = "UTF-8";
                StringBuilder text = new StringBuilder();
                String NL = System.getProperty("line.separator");
                Scanner scanner;
                String imageName = mImageDir[mCurrentPosition].getName();
                String textName = imageName.substring(0, imageName.length() - 4) + ".txt";
                File stringFile = new File(Environment.getExternalStorageDirectory(), "test_data/test_strings/" + textName);
                try {
                    
                    scanner = new Scanner(new FileInputStream(stringFile), encoding);
                    while (scanner.hasNextLine()){
                      text.append(scanner.nextLine() + NL);
                    }
                    scanner.close();
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }   
                
                String expected = text.toString();
                int distance = StringUtils.getLevenshteinDistance(message, expected);
                int base = expected.length() * 100;
                int temp = 0;
                
                // we give no reward for finding nothing
                if(message.length() > 0 || expected.length() == 0)
                    temp = Math.abs(distance) + Math.abs(message.length() - expected.length());
                    mResult += base - temp;
                if(base < temp) {
                    Log.e(TAG, String.format("base too low: %d", temp));
                }
                
                /*
                ArrayList<Integer> list = new ArrayList<Integer>();
                list.add(331);
                list.add(717);
                list.add(1103);
                list.add(1477);
                list.add(1523);
                list.add(1915);
                list.add(2303);
                */

                if(mDebug) {
                    Log.d(TAG, String.format("image %d, %s", mCurrentPosition, mImageDir[mCurrentPosition].getName()));
                    //Log.d(TAG, String.format("str %d, %s", mCurrentPosition, stringFile.getAbsolutePath()));
                    Log.d(TAG, "Message: " + message.replace("\n","\\n"));
                    Log.d(TAG, "Expected: " + expected.replace("\n", "\\n"));
                    Log.d(TAG, String.format("distance: %d, %d", temp, mResult));
                }
                
                //score message
                //repeat
                mFinished += 1;
                mCurrentPosition += 1;
                mImageDir.notify();
                //doOcr();
            }
                
        }
    
    }

    @Override
    protected Void doInBackground(Void... params) {
        // TODO Auto-generated method stub
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
