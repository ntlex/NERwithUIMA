package de.unihamburg.informatik.nlp4web.tutorial.tut5.ner;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.cleartk.ml.crfsuite.CrfSuiteStringOutcomeDataWriter;
import org.cleartk.ml.jar.DefaultDataWriterFactory;
import org.cleartk.ml.jar.DirectoryDataWriterFactory;
import org.cleartk.ml.jar.GenericJarClassifierFactory;
import org.cleartk.util.cr.FilesCollectionReader;

import de.tudarmstadt.ukp.dkpro.core.snowball.SnowballStemmer;
import de.unihamburg.informatik.nlp4web.tutorial.tut5.annotator.NERAnnotator;
import de.unihamburg.informatik.nlp4web.tutorial.tut5.reader.NERReader;

public class ExecuteNER {

    public static void writeModel(File nerTrain, String modelDirectory, String language)
            throws ResourceInitializationException, UIMAException, IOException {

        runPipeline(
                FilesCollectionReader.getCollectionReaderWithSuffixes(nerTrain.getAbsolutePath(),
                        NERReader.CONLL_VIEW, nerTrain.getName()),
                createEngine(NERReader.class),
                createEngine(SnowballStemmer.class, SnowballStemmer.PARAM_LANGUAGE, language),
                createEngine(NERAnnotator.class, NERAnnotator.PARAM_IS_TRAINING, true,
                        DirectoryDataWriterFactory.PARAM_OUTPUT_DIRECTORY, modelDirectory,
                        DefaultDataWriterFactory.PARAM_DATA_WRITER_CLASS_NAME,
                        CrfSuiteStringOutcomeDataWriter.class));
    }

    public static void trainModel(String modelDirectory) throws Exception {
        org.cleartk.ml.jar.Train.main(modelDirectory);

    }
    
    // The pipeline do not include the output writer. you SHOULD write a consumer which extract the predicted Named entities. You can compute then the scores accordingly
    
    public static void classifyTestFile(String modelDirectory, File nerTest, String language)
            throws ResourceInitializationException, UIMAException, IOException {
        runPipeline(
                FilesCollectionReader.getCollectionReaderWithSuffixes(nerTest.getAbsolutePath(),
                        NERReader.CONLL_VIEW, nerTest.getName()),
                createEngine(NERReader.class),
                createEngine(SnowballStemmer.class, SnowballStemmer.PARAM_LANGUAGE, language),
                createEngine(NERAnnotator.class, 
                        GenericJarClassifierFactory.PARAM_CLASSIFIER_JAR_PATH, modelDirectory + "model.jar"), /*TODO: replace this with your NER consumer*/
                createEngine(AnalyzeFeatures.class, AnalyzeFeatures.PARAM_INPUT_FILE, nerTest.getAbsolutePath(),
                        AnalyzeFeatures.PARAM_TOKEN_VALUE_PATH, "pos/PosValue"));
    }

    public static void main(String[] args) throws Exception {

        long start = System.currentTimeMillis();
        String modelDirectory = "src/test/resources/model/";
        String language = "en";
        File nerTrain = new File("src/main/resources/ner/ner_eng.train");
        File nerTest = new File("src/main/resources/ner/ner_eng.test");
        new File(modelDirectory).mkdirs();
        writeModel(nerTrain, modelDirectory, language);
        trainModel(modelDirectory);
        classifyTestFile(modelDirectory, nerTest, language);
        long now = System.currentTimeMillis();
        UIMAFramework.getLogger().log(Level.INFO, "Time: " + (now - start) + "ms");
    }
}