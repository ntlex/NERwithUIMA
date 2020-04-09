package de.unihamburg.informatik.nlp4web.tutorial.tut5.feature;

import java.util.Collections;
import java.util.List;

import org.cleartk.ml.Feature;
import org.cleartk.ml.feature.function.FeatureFunction;

public class SurroundingCapFeature implements FeatureFunction{
	
	public static final String SMALL_CAP = "LWRCASE";
	public static final String BIG_CAP = "UPRCASE";

	@Override
	public List<Feature> apply(Feature arg0) {
		String fName = Feature.createName("caseType-", arg0.getName());
        Object fVal = arg0.getValue();
        if (fVal == null)
        {
        	return Collections.emptyList();
        }
        else{
        	if (fVal instanceof String)
        	{
        		if (fVal.equals(((String) fVal).toLowerCase())){
        			// text is all lower case
        			return Collections.singletonList(new Feature(fName, SMALL_CAP));
        		}
        		else if (Character.isUpperCase(((String) fVal).charAt(0)))
        		{
        			return Collections.singletonList(new Feature(fName, BIG_CAP));
        		}
        		else
        		{
        			return Collections.emptyList();
        		}
        	}
        	else
        	{
        		return Collections.emptyList();
        	}
        }
	}

}
