package weka.classifiers.trees;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.lang.Math;

import javax.swing.text.rtf.RTFEditorKit;

import weka.core.ContingencyTables;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

public class MyForest implements java.io.Serializable {	
	List<RandomTree> trees = null;
	int [] atributes = null;
	double [] splits = null;
	
	public MyForest(int num_rules, Instances data, Random random_tree, 
			boolean the_same_rules_node, ArrayList<Integer> 
			selected_rules_tree, 
			int percent_data, int random_generator_type) throws Exception {
		List<RandomTree> trees = new ArrayList<RandomTree>();
		int[] trees_attributes = new int[num_rules];
		double[] splits = new double[num_rules];
		ArrayList<Integer> selected_rules_node = new ArrayList<Integer>();
		ArrayList<Integer> selected_rules_all = new ArrayList<Integer>();
		Random percent_data_rand = data.getRandomNumberGenerator(1);
		Random random_node = data.getRandomNumberGenerator(1);
		int trainSize = (int) Math.round(data.numInstances() * percent_data / 100);
		for (int i = 0; i < num_rules; i++) {
			RandomTree rtree = new RandomTree();
			if (percent_data != 100) {
				data.randomize(percent_data_rand);
				data = new Instances(data, 0, trainSize);
			}
			if (random_generator_type == myRandomTreeV2.SET_TREE_RANDOM)
				rtree.random_val = random_tree;
			else if (random_generator_type == myRandomTreeV2.SET_NODE_RANDOM)
				rtree.random_val = random_node;
			rtree.setMaxDepth(1);
			if(!the_same_rules_node)
				selected_rules_all.addAll(selected_rules_node);
			if (selected_rules_tree != null)
				selected_rules_all.addAll(selected_rules_tree);
			rtree.selected_rules = selected_rules_all;
			rtree.buildClassifier(data);
			if (rtree.attribute != -1) {
				trees.add(rtree);
				trees_attributes[i] = rtree.attribute;
				splits[i] = rtree.splitPoint;
				selected_rules_node.add(rtree.attribute);
			} 
		}

		this.trees = trees;
		this.atributes = trees_attributes;
		this.splits = splits;
	}
	
	public double classify_instance(Instance inst) throws Exception {
    	int leftSite = 0;
    	int righSite = 0;
    	for (int i = 0; i < this.atributes.length; i++) {
    		double inst_class = get_class(i, inst);
    	  	if (inst_class == 1.0) 
	    		righSite++;
	    	 else 
	    		leftSite++;
    	}
    	
    	if (righSite > leftSite)
    		return 1.0;
    	else if(righSite < leftSite)
    		return 0.0;
    	else {
    		return get_class(0, inst);
    	}
    }
	
	private double get_class(int index, Instance inst) {
		if (inst.value(atributes[index]) >= splits[index]) 
			return 1.0;
		 else
			return 0.0;
	}
    
    public double [][] getDist(Instances data) throws Exception {
    	double[][] currDist = new double[2][data.numClasses()];
    	for (int i = 0; i < data.numInstances(); i++) {
            Instance inst = data.instance(i);
            double inst_class = this.classify_instance(inst);
            if (inst_class == 0.0) {
                currDist[0][(int) inst.classValue()] += inst.weight();
            } else {
            	currDist[1][(int) inst.classValue()] += inst.weight();
            }  
        }

    	return currDist;
    }
   
    public double [] getProps(double[][] dist) {
    	double[] props = new double[2];
        int sum_left = (int) Utils.sum(dist[0]);
        int sum_right = (int) Utils.sum(dist[1]);
        int total = sum_left + sum_right;
        double prop_left = (double)sum_left / total;
        double prop_righ = (double)sum_right / total;
        props[0] = Math.round(prop_left*100)/100.0d;
        props[1] = Math.round(prop_righ*100)/100.0d;
    	return props;
    }
}
