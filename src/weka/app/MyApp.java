package weka.app;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

//import JFlex.Out;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.FT;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.myRandomTreeV2;
import weka.classifiers.trees.RandomTree;
import weka.classifiers.trees.SimpleCart;
import weka.classifiers.trees.Id3;
import weka.classifiers.trees.myRandomTreeV3;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.pmml.FieldMetaInfo.Value;
import weka.gui.treevisualizer.Node;

public class MyApp {
  
	public static void main(String[] args) throws Exception 
	{
		String rootPath="data/Alizadeh-2000-v2.arff";
		ArffLoader loader = new ArffLoader();
	    loader.setFile(new File(rootPath));
	    Instances originalTrain = loader.getDataSet();
	    originalTrain.setClassIndex(originalTrain.numAttributes() - 1);
		myRandomTreeV2 mrf2 = new myRandomTreeV2();
		mrf2.num_rules = 2;
		mrf2.percent_data = 90;
		mrf2.random_generator_type = 1;
		mrf2.the_same_rules_node = true;
		mrf2.the_same_rules_tree = false;
		mrf2.random_generator_type = 2;
	    mrf2.buildClassifier(originalTrain);
		System.out.println(mrf2.toString());
		
		loader.setFile(new File("data/Alizadeh-2000-v2.arff"));
		Instances testData = loader.getDataSet();
		testData.setClassIndex(testData.numAttributes() - 1);
		
		Evaluation ev = new Evaluation(originalTrain);
	    Random rand = new Random(1);
	    int folds = 10;
		ev.crossValidateModel(mrf2, testData, folds, rand);
		System.out.println(ev.toSummaryString());
	}
}
