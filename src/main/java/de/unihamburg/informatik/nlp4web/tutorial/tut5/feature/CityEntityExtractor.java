package de.unihamburg.informatik.nlp4web.tutorial.tut5.feature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.jcas.JCas;
import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.extractor.CleartkExtractorException;
import org.cleartk.ml.feature.extractor.NamedFeatureExtractor1;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class CityEntityExtractor implements NamedFeatureExtractor1<Token> {

    private final Map<String, String> neToTag = new HashMap<>();

    public CityEntityExtractor(File list) {
        try (BufferedReader reader = new BufferedReader(new FileReader(list));) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");
                neToTag.put(split[1], "LOC");
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public List<Feature> extract(JCas view, Token focusAnnotation) throws CleartkExtractorException {
        String coveredText = focusAnnotation.getCoveredText().toLowerCase();
        String tag = neToTag.get(coveredText);
        if (tag == null)
        {
        	return Collections.emptyList();
        }
        else
        {
            return Collections.singletonList(new Feature("cityName-" + tag));
        }
    }

    @Override
    public String getFeatureName() {
        return "nerCityListFeature";
    }
}