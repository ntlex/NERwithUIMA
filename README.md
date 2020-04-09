# Named Entity Recognition engine Documentation

## Overview 

In this section, we present our Named Entity Recognition engine. First, we go through into the different bits of our workflow, from the pre-processing of our data, into the creation of our model and the final step, the classification. The used features are presented in the final part of the documentation, along with a description of the involved classes. 

## The workflow

* **Model Language**: English
* **Resources directory:** *src/main/resources/ner/*

The workflow of our model consists of the three stages, which are described in detail below. The methods of all three stages can be found in class `ExecuteNER.java` in ***src/main/java/ner***. All relevant resources that are used for training and classifying, can be found in the **Resources directory**.  

1. **Writing the model**: This stage is handled by the `writeModel` method. This method runs a pipeline that pre-processes the training data. The pipeline is fed with the ***ner_eng.train*** data-set and consists of the following engines: 
     1. The `NERReader.class` reads the data-set, extracts the gold value annotations of each token and adds it to the indexes of Jcas for further processing. 
     2. An engine that uses the `SnowballStemmer.class` for stemming 
     3. The NERAnnotator.class which contains our feature extractors for training the model. The output of the NERAnnotator is written in ***src/test/resources/model/***, and class `CrfSuiteStringOutcomeDataWriter.class` is used to write the output data in CRF format which will be used for the training in the next step of the workflow.  
2. **Training the model**: This stage is handled by method `trainModel`, which uses the `Train.main` method od cleartk (`org.cleartk.ml.jar.Train.main`) to train the model based on the output that was generated on the previous step (`writeModel`) in ***src/test/resources/model/***. As soon as the training is completed, this method generates the model `model.jar` in ***src/test/resources/model/***. 

3. **Classifying test data**: This stage is handled by method `classifyTestFile`. The method is fed with the test data ***ner_eng.test*** which are processed by the same engines two first engines which are used for in `writeModel`. Following the `NERReader` and `SnowballStemmer` the pipeline which contains the additional engines: 
    1. The `NERAnnotator.class` is used to annotate the test data-set based on the trained model `model.jar`. 
    2. The AnalyzeFeatures.class engine, which is used to compare and analyze the results that are extracted from the `NERAnnotator` engine, by calculates the **Recall**, **Precision** and **F1** score. 

### AnalyzeFeatures.class
This class compares the token instance which contains the predicted value (`extractor.extract(jCas, token).get(0).getValue().toString()`) by the `NERAnnotator`, with the golden value of the token that is included in the test data-set. It then keeps a count of the all **true positive**, **true negative**, **false positive** and **false negative** findings which is used to the calculations of the **Recall**, **Precision** and **F1** score.

### NERAnnotator.class
This class consists of the following methods: 
 1. `initialize` - This method instantiates the feature extractors which are applied to the training and test data for the Named Entity classification. 
 2. `process` - Processes the Jcas created from the `NERReader.class`. 
    * When this method is accessed through the `writeModel` method for training purposes, a list of instances of the tokens that match the requirements of the feature extractors is created. The instances are additionally written into a file by the `dataWriter` in ***src/test/resources/model/*** which will then be used to train the model (`trainModel`). 
    * When this method is accessed through the `classifyTestFile` method, the generated list of instances are annotated with the `NEIOBAnnotation` to set the predicted value of each token. 
     
### Features 

In this model, the following feature extractors are used to classify the desired name entities: 

1. `TypePathExtractor` for stemming 
2. `TypePathExtractor` for Part-of-speech tags
3. `FeatureFunctionExtractor` with the following functions: 
    * `CoveredTextExtractor<Token>` - Gets the covered text of the token's annotation 
    * `LowerCaseFeatureFunction()` - Checks if the characters in question are lowercased 
    * `CapitalTypeFeatureFunction()` - Checks if the characters in question follow any of these four patterns (all uppercase - `ALL_UPPERCASE`, the first characters is uppercase - `INITIAL_UPERCASE`, all characters are lowercased - `ALL_LOWERCASE`, the characters are mixed cased - `MIXED_CASE`)
    * `NumericTypeFeatureFunction()` - Checks is the characters in question follow any of these three patterns (are digits - `DIGITS`, are year digits - `YEAR_DIGITS`, are alphanumeric - `ALPHANUMERIC)
    * `CharacterNgramFeatureFunction(fromRight, 0, 2)`: With the orientation set to `RIGHT_TO_LEFT` for characters from 0 to 2, we are checking for prefixes.
4. `CleartkExtractor<Token, Token>` for `Preceding(1)`: We extract the annotation of the previous word.
5. `CleartkExtractor<Token, Token>`  with `TypePathExtractor` POS for `Preceding(2)`: Extracting the Part-of-speech tag of the two previous words.
6. `CleartkExtractor<Token, Token>` for class `SurroundingCapFeature.class`:This feature extractor is checking if the two previous words and the two following words begin with a capital letter. 
7. `FeatureFunctionExtractor` for class `NameEntityExtractor.class`: Reads a list of gazetteer provided in ***src/main/resources/ner/eng.list***, and creates a new feature based on each NER tag as shown below.
    * Tag: **MISC** - `new Feature("miscName-" + tag)`
    * Tag: **PER** - `new Feature("perName-" + tag)`
    * Tag: **ORG** - `new Feature("orgName-" + tag)`
    * Tag: **LOC** - `new Feature("locName-" + tag)`
    
8. `FeatureFunctionExtractor` for class `CityEntityExtractor.class`: Reads a list of gazetteer provided in ***src/main/resources/ner/worldcitiespop.txt***, and creates a new feature based on the **LOC** NER tag as shown below.
    * Tag: **LOC** - `new Feature("locName-" + tag)`
    
The following features were attempted but did not introduce any improvement, or actually decreased the performance of the classification process:
* POS tags of the tokens following the focus token;
* POS tag of one token directly preceding the focus token.    