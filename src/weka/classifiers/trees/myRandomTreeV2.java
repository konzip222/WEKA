/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 *    myRandomTreeV2.java
 *    Copyright (C) 2001-2012 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.classifiers.trees;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Vector;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.ContingencyTables;
import weka.core.Drawable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Randomizable;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;

/**
 * <!-- globalinfo-start --> Class for constructing a tree that considers K
 * randomly chosen attributes at each node. Performs no pruning. Also has an
 * option to allow estimation of class probabilities based on a hold-out set
 * (backfitting).
 * <p/>
 * <!-- globalinfo-end -->
 * 
 * <!-- options-start --> Valid options are:
 * <p/>
 * 
 * <pre>
 * -K &lt;number of attributes&gt;
 *  Number of attributes to randomly investigate. (default 0)
 *  (&lt;0 = int(log_2(#predictors)+1)).
 * </pre>
 * 
 * <pre>
 * -M &lt;minimum number of instances&gt;
 *  Set minimum number of instances per leaf.
 * </pre>
 * 
 * <pre>
 * -S &lt;num&gt;
 *  Seed for random number generator.
 *  (default 1)
 * </pre>
 * 
 * <pre>
 * -depth &lt;num&gt;
 *  The maximum depth of the tree, 0 for unlimited.
 *  (default 0)
 * </pre>
 * 
 * <pre>
 * -N &lt;num&gt;
 *  Number of folds for backfitting (default 0, no backfitting).
 * </pre>
 * 
 * <pre>
 * -U
 *  Allow unclassified instances.
 * </pre>
 * 
 * <pre>
 * -D
 *  If set, classifier is run in debug mode and
 *  may output additional info to the console
 * </pre>
 * 
 * <!-- options-end -->
 * 
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
 * @version $Revision: 10993 $
 */
public class myRandomTreeV2 extends Classifier implements OptionHandler,
		WeightedInstancesHandler, Randomizable, Drawable {

	/** for serialization */
	static final long serialVersionUID = 8934314652175299374L;

	/** The Tree object */
	protected Tree m_Tree = null;

	/** The header information. */
	protected Instances m_Info = null;

	/** Minimum number of instances for leaf. */
	protected double m_MinNum = 1.0;

	/** The number of attributes considered for a split. */
	protected int m_KValue = 0;

	/** The random seed to use. */
	protected int m_randomSeed = 1;

	/** The maximum depth of the tree (0 = unlimited) */
	protected int m_MaxDepth = 0;

	/** Determines how much data is used for backfitting */

	/** Whether unclassified instances are allowed */
	protected boolean m_AllowUnclassifiedInstances = false;

	/** a ZeroR model in case no model can be built from the data */
	protected Classifier m_zeroR;
	
	protected ArrayList<Integer> selected_rules_tree = null;
	
	protected int m_NumFolds = 0;
	
	public int num_rules = 1;
	
	public boolean the_same_rules_node = true;
	
	public boolean the_same_rules_tree = true;
	
	public int percent_data = 100;
	
	public int random_type_val = 0;
	
	public int random_generator_type = 2;
	
	public static final int SET_NONE_RANDOM = 0;
	public static final int SET_NODE_RANDOM = 1;
	public static final int SET_TREE_RANDOM = 2;
	public static final Tag [] TAGS_RANDOM = {
		new Tag(SET_NONE_RANDOM, "not set random generator"),
	    new Tag(SET_NODE_RANDOM, "set the same random generator in one node"),
	    new Tag(SET_TREE_RANDOM, "set the same random generator in all nodes")
	};
			
	/**
	 * Returns a string describing classifier
	 * 
	 * @return a description suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String globalInfo() {

		return "Class for constructing a tree that considers K randomly "
				+ " chosen attributes at each node. Performs no pruning. Also has"
				+ " an option to allow estimation of class probabilities based on"
				+ " a hold-out set (backfitting).";
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String minNumTipText() {
		return "The minimum total weight of the instances in a leaf.";
	}
	
	public int getNumRules() {
		return num_rules;
	}

	public void setNumRules(int num_rules) {
		this.num_rules = num_rules;
	}
	
	public boolean getTheSameRulesNode() {
		return the_same_rules_node;
	}

	public void setTheSameRulesNode(boolean the_same_rules_node) {
		this.the_same_rules_node = the_same_rules_node;
	}
	
	public boolean getTheSameRulesTree() {
		return the_same_rules_tree;
	}

	public void setTheSameRulesTree(boolean the_same_rules_tree) {
		this.the_same_rules_tree = the_same_rules_tree;
	}
	
	public int getPercentData() {
		return percent_data;
	}

	public void setPercentData(int percent_data) {
		this.percent_data = percent_data;
	}
	
	public SelectedTag getRandomTypeGenerator() {
		return new SelectedTag(random_generator_type, TAGS_RANDOM);
	}

	public void setRandomTypeGenerator(SelectedTag newMethod) {
		if (newMethod.getTags() == TAGS_RANDOM) {
			random_generator_type = newMethod.getSelectedTag().getID();
	    }
	}

	/**
	 * Get the value of MinNum.
	 * 
	 * @return Value of MinNum.
	 */
	public double getMinNum() {

		return m_MinNum;
	}

	/**
	 * Set the value of MinNum.
	 * 
	 * @param newMinNum
	 *            Value to assign to MinNum.
	 */
	public void setMinNum(double newMinNum) {

		m_MinNum = newMinNum;
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String KValueTipText() {
		return "Sets the number of randomly chosen attributes. If 0, log_2(number_of_attributes) + 1 is used.";
	}

	/**
	 * Get the value of K.
	 * 
	 * @return Value of K.
	 */
	public int getKValue() {

		return m_KValue;
	}

	/**
	 * Set the value of K.
	 * 
	 * @param k
	 *            Value to assign to K.
	 */
	public void setKValue(int k) {

		m_KValue = k;
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String seedTipText() {
		return "The random number seed used for selecting attributes.";
	}

	/**
	 * Set the seed for random number generation.
	 * 
	 * @param seed
	 *            the seed
	 */
	public void setSeed(int seed) {

		m_randomSeed = seed;
	}

	/**
	 * Gets the seed for the random number generations
	 * 
	 * @return the seed for the random number generation
	 */
	public int getSeed() {

		return m_randomSeed;
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String maxDepthTipText() {
		return "The maximum depth of the tree, 0 for unlimited.";
	}

	/**
	 * Get the maximum depth of trh tree, 0 for unlimited.
	 * 
	 * @return the maximum depth.
	 */
	public int getMaxDepth() {
		return m_MaxDepth;
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String numFoldsTipText() {
		return "Determines the amount of data used for backfitting. One fold is used for "
				+ "backfitting, the rest for growing the tree. (Default: 0, no backfitting)";
	}

	/**
	 * Get the value of NumFolds.
	 * 
	 * @return Value of NumFolds.
	 */
	public int getNumFolds() {

		return m_NumFolds;
	}

	/**
	 * Set the value of NumFolds.
	 * 
	 * @param newNumFolds
	 *            Value to assign to NumFolds.
	 */
	public void setNumFolds(int newNumFolds) {

		m_NumFolds = newNumFolds;
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String allowUnclassifiedInstancesTipText() {
		return "Whether to allow unclassified instances.";
	}

	/**
	 * Get the value of NumFolds.
	 * 
	 * @return Value of NumFolds.
	 */
	public boolean getAllowUnclassifiedInstances() {

		return m_AllowUnclassifiedInstances;
	}

	/**
	 * Set the value of AllowUnclassifiedInstances.
	 * 
	 * @param newAllowUnclassifiedInstances
	 *            Value to assign to AllowUnclassifiedInstances.
	 */
	public void setAllowUnclassifiedInstances(
			boolean newAllowUnclassifiedInstances) {

		m_AllowUnclassifiedInstances = newAllowUnclassifiedInstances;
	}

	/**
	 * Set the maximum depth of the tree, 0 for unlimited.
	 * 
	 * @param value
	 *            the maximum depth.
	 */
	public void setMaxDepth(int value) {
		m_MaxDepth = value;
	}

	/**
	 * Lists the command-line options for this classifier.
	 * 
	 * @return an enumeration over all possible options
	 */
	@Override
	public Enumeration listOptions() {

		Vector newVector = new Vector();

		newVector.addElement(new Option(
				"\tNumber of attributes to randomly investigate. (default 0)\n"
						+ "\t(<0 = int(log_2(#predictors)+1)).", "K", 1,
				"-K <number of attributes>"));

		newVector.addElement(new Option(
				"\tSet minimum number of instances per leaf.", "M", 1,
				"-M <minimum number of instances>"));

		newVector.addElement(new Option("\tSeed for random number generator.\n"
				+ "\t(default 1)", "S", 1, "-S <num>"));

		newVector.addElement(new Option(
				"\tThe maximum depth of the tree, 0 for unlimited.\n"
						+ "\t(default 0)", "depth", 1, "-depth <num>"));

		newVector.addElement(new Option("\tNumber of folds for backfitting "
				+ "(default 0, no backfitting).", "N", 1, "-N <num>"));
		newVector.addElement(new Option("\tAllow unclassified instances.", "U",
				0, "-U"));
		
		Enumeration enu = super.listOptions();
		while (enu.hasMoreElements()) {
			newVector.addElement(enu.nextElement());
		}

		return newVector.elements();
	}

	/**
	 * Gets options from this classifier.
	 * 
	 * @return the options for the current setup
	 */
	@Override
	public String[] getOptions() {
		Vector result;
		String[] options;
		int i;

		result = new Vector();

		result.add("-K");
		result.add("" + getKValue());

		result.add("-M");
		result.add("" + getMinNum());

		result.add("-S");
		result.add("" + getSeed());
		
		result.add("-num_rules");
		result.add("" + num_rules);
		
		result.add("-percent_data");
		result.add("" + percent_data);
		
		result.add("-the_same_rules_node");
		result.add("" + the_same_rules_node);
		
		result.add("-the_same_rules_tree");
		result.add("" + the_same_rules_tree);
		
		result.add("-random_generator_type");
		result.add("" + random_generator_type);

		if (getMaxDepth() > 0) {
			result.add("-depth");
			result.add("" + getMaxDepth());
		}

		if (getNumFolds() > 0) {
			result.add("-N");
			result.add("" + getNumFolds());
		}

		if (getAllowUnclassifiedInstances()) {
			result.add("-U");
		}

		options = super.getOptions();
		for (i = 0; i < options.length; i++)
			result.add(options[i]);

		return (String[]) result.toArray(new String[result.size()]);
	}

	/**
	 * Parses a given list of options.
	 * <p/>
	 * 
	 * <!-- options-start --> Valid options are:
	 * <p/>
	 * 
	 * <pre>
	 * -K &lt;number of attributes&gt;
	 *  Number of attributes to randomly investigate
	 *  (&lt;0 = int(log_2(#attributes)+1)).
	 * </pre>
	 * 
	 * <pre>
	 * -M &lt;minimum number of instances&gt;
	 *  Set minimum number of instances per leaf.
	 * </pre>
	 * 
	 * <pre>
	 * -S &lt;num&gt;
	 *  Seed for random number generator.
	 *  (default 1)
	 * </pre>
	 * 
	 * <pre>
	 * -depth &lt;num&gt;
	 *  The maximum depth of the tree, 0 for unlimited.
	 *  (default 0)
	 * </pre>
	 * 
	 * <pre>
	 * -N &lt;num&gt;
	 *  Number of folds for backfitting (default 0, no backfitting).
	 * </pre>
	 * 
	 * <pre>
	 * -U
	 *  Allow unclassified instances.
	 * </pre>
	 * 
	 * <pre>
	 * -D
	 *  If set, classifier is run in debug mode and
	 *  may output additional info to the console
	 * </pre>
	 * 
	 * <!-- options-end -->
	 * 
	 * @param options
	 *            the list of options as an array of strings
	 * @throws Exception
	 *             if an option is not supported
	 */
	@Override
	public void setOptions(String[] options) throws Exception {
		String tmpStr;

		tmpStr = Utils.getOption('K', options);
		if (tmpStr.length() != 0) {
			m_KValue = Integer.parseInt(tmpStr);
		} else {
			m_KValue = 0;
		}

		tmpStr = Utils.getOption('M', options);
		if (tmpStr.length() != 0) {
			m_MinNum = Double.parseDouble(tmpStr);
		} else {
			m_MinNum = 1;
		}

		tmpStr = Utils.getOption('S', options);
		if (tmpStr.length() != 0) {
			setSeed(Integer.parseInt(tmpStr));
		} else {
			setSeed(1);
		}

		tmpStr = Utils.getOption("depth", options);
		if (tmpStr.length() != 0) {
			setMaxDepth(Integer.parseInt(tmpStr));
		} else {
			setMaxDepth(0);
		}
		String numFoldsString = Utils.getOption('N', options);
		if (numFoldsString.length() != 0) {
			m_NumFolds = Integer.parseInt(numFoldsString);
		} else {
			m_NumFolds = 0;
		}
		
		String num_rules_string = Utils.getOption("num_rules", options);
		if (num_rules_string.length() != 0) {
			num_rules = Integer.parseInt(num_rules_string);
		} else {
			num_rules = 1;
		}
		
		String percent_data_string = Utils.getOption("percent_data", options);
		if (percent_data_string.length() != 0) {
			percent_data = Integer.parseInt(percent_data_string);
		} else {
			percent_data = 100;
		}
		
		String the_same_rules_node_string = Utils.getOption("the_same_rules_node", options);
		if (the_same_rules_node_string.length() != 0) {
			the_same_rules_node = Boolean.parseBoolean(the_same_rules_node_string);
		} else {
			the_same_rules_node = false;
		}
		
		String the_same_rules_tree_string = Utils.getOption("the_same_rules_tree", options);
		if (the_same_rules_node_string.length() != 0) {
			the_same_rules_tree = Boolean.parseBoolean(the_same_rules_tree_string);
		} else {
			the_same_rules_tree = true;
		}
		
		String random_generator_type_string = Utils.getOption("random_generator_type", options);
		if (random_generator_type_string.length() != 0) {
			random_generator_type = Integer.parseInt(random_generator_type_string);
		}
			

		setAllowUnclassifiedInstances(Utils.getFlag('U', options));

		super.setOptions(options);

		Utils.checkForRemainingOptions(options);
	}

	/**
	 * Returns default capabilities of the classifier.
	 * 
	 * @return the capabilities of this classifier
	 */
	@Override
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();
		result.disableAll();

		// attributes
		result.enable(Capability.NOMINAL_ATTRIBUTES);
		result.enable(Capability.NUMERIC_ATTRIBUTES);
		result.enable(Capability.DATE_ATTRIBUTES);
		result.enable(Capability.MISSING_VALUES);

		// class
		result.enable(Capability.NOMINAL_CLASS);
		result.enable(Capability.MISSING_CLASS_VALUES);

		return result;
	}

	/**
	 * Builds classifier.
	 * 
	 * @param data
	 *            the data to train with
	 * @throws Exception
	 *             if something goes wrong or the data doesn't fit
	 */
	@Override
	public void buildClassifier(Instances data) throws Exception {

		// Make sure K value is in range
		if (m_KValue > data.numAttributes() - 1)
			m_KValue = data.numAttributes() - 1;
		if (m_KValue < 1)
			m_KValue = (int) Utils.log2(data.numAttributes() - 1) + 1;

		// can classifier handle the data?
		getCapabilities().testWithFail(data);

		// remove instances with missing class
		data = new Instances(data);
		data.deleteWithMissingClass();
		
		// only class? -> build ZeroR model
		if (data.numAttributes() == 1) {
			System.err
					.println("Cannot build model (only class attribute present in data!), "
							+ "using ZeroR model instead!");
			m_zeroR = new weka.classifiers.rules.ZeroR();
			m_zeroR.buildClassifier(data);
			return;
		} else {
			m_zeroR = null;
		}

		// Figure out appropriate datasets
		Instances train = null;
		Instances backfit = null;
		Random rand = data.getRandomNumberGenerator(m_randomSeed);
		if (m_NumFolds <= 0) {
			train = data;
		} else {
			data.randomize(rand);
			data.stratify(m_NumFolds);
			train = data.trainCV(m_NumFolds, 1, rand);
			backfit = data.testCV(m_NumFolds, 1);
		}

		// Create the attribute indices window
		int[] attIndicesWindow = new int[data.numAttributes() - 1];
		int j = 0;
		for (int i = 0; i < attIndicesWindow.length; i++) {
			if (j == data.classIndex())
				j++; // do not include the class
			attIndicesWindow[i] = j++;
		}

		// Compute initial class counts
		double[] classProbs = new double[train.numClasses()];
		for (int i = 0; i < train.numInstances(); i++) {
			Instance inst = train.instance(i);
			classProbs[(int) inst.classValue()] += inst.weight();
		}

		// Build tree
		m_Tree = new Tree();
		m_Info = new Instances(data, 0);
		
		if (!the_same_rules_tree) {
			selected_rules_tree = new ArrayList<Integer>();
		}
		
		m_Tree.buildTree(train, classProbs, 0, attIndicesWindow, rand);

		// Backfit if required
		if (backfit != null) {
			m_Tree.backfitData(backfit);
		}
	}

	/**
	 * Computes class distribution of an instance using the tree.
	 * 
	 * @param instance
	 *            the instance to compute the distribution for
	 * @return the computed class probabilities
	 * @throws Exception
	 *             if computation fails
	 */
	@Override
	public double[] distributionForInstance(Instance instance) throws Exception {

		if (m_zeroR != null) {
			return m_zeroR.distributionForInstance(instance);
		} else {
			return m_Tree.distributionForInstance(instance);
		}
	}

	/**
	 * Outputs the decision tree.
	 * 
	 * @return a string representation of the classifier
	 */
	@Override
	public String toString() {
		// only ZeroR model?
		if (m_zeroR != null) {
			StringBuffer buf = new StringBuffer();
			buf.append(this.getClass().getName().replaceAll(".*\\.", "") + "\n");
			buf.append(this.getClass().getName().replaceAll(".*\\.", "")
					.replaceAll(".", "=")
					+ "\n\n");
			buf.append("Warning: No model could be built, hence ZeroR model is used:\n\n");
			buf.append(m_zeroR.toString());
			return buf.toString();
		}

		if (m_Tree == null) {
			return "myRandomTreeV2: no model has been built yet.";
		} else {
			return "\nmyRandomTreeV2\n==========\n"
					+ m_Tree.toString(0)
					+ "\n"
					+ "\nSize of the tree : "
					+ m_Tree.numNodes()
					+ (getMaxDepth() > 0 ? ("\nMax depth of tree: " + getMaxDepth())
							: (""));
		}
	}

	/**
	 * Returns graph describing the tree.
	 * 
	 * @return the graph describing the tree
	 * @throws Exception
	 *             if graph can't be computed
	 */
	public String graph() throws Exception {

		if (m_Tree == null) {
			throw new Exception("myRandomTreeV2: No model built yet.");
		}
		StringBuffer resultBuff = new StringBuffer();
		m_Tree.toGraph(resultBuff, 0, null);
		String result = "digraph myRandomTreeV2 {\n" + "edge [style=bold]\n"
				+ resultBuff.toString() + "\n}\n";
		return result;
	}

	/**
	 * Returns the type of graph this classifier represents.
	 * 
	 * @return Drawable.TREE
	 */
	public int graphType() {
		return Drawable.TREE;
	}

	/**
	 * Builds the classifier to generate a partition.
	 */
	public void generatePartition(Instances data) throws Exception {

		buildClassifier(data);
	}

	/**
	 * Computes array that indicates node membership. Array locations are
	 * allocated based on breadth-first exploration of the tree.
	 */
	public double[] getMembershipValues(Instance instance) throws Exception {

		if (m_zeroR != null) {
			double[] m = new double[1];
			m[0] = instance.weight();
			return m;
		} else {

			// Set up array for membership values
			double[] a = new double[numElements()];

			// Initialize queues
			Queue<Double> queueOfWeights = new LinkedList<Double>();
			Queue<Tree> queueOfNodes = new LinkedList<Tree>();
			queueOfWeights.add(instance.weight());
			queueOfNodes.add(m_Tree);
			int index = 0;

			// While the queue is not empty
			while (!queueOfNodes.isEmpty()) {

				a[index++] = queueOfWeights.poll();
				Tree node = queueOfNodes.poll();

				// Is node a leaf?
				if (node.m_Attribute == null) {
					continue;
				}

				// Compute weight distribution
				double[] weights = new double[node.m_Successors.length];
				int leftObjects = 0;
				int rightObjects = 0;
				for (int i = 0; i < node.m_Attribute.length; i++) {
					if (instance.value(node.m_Attribute[i]) < node.m_SplitPoint[i]) {
						leftObjects++;
					} else {
						rightObjects++;
					}
				}

				if (leftObjects > rightObjects) {
					weights[0] = 1.0;
				} else {
					weights[1] = 1.0;
				}

				for (int i = 0; i < node.m_Successors.length; i++) {
					queueOfNodes.add(node.m_Successors[i]);
					queueOfWeights.add(a[index - 1] * weights[i]);
				}
			}
			return a;
		}
	}

	/**
	 * Returns the number of elements in the partition.
	 */
	public int numElements() throws Exception {

		if (m_zeroR != null) {
			return 1;
		}
		return m_Tree.numNodes();
	}

	/**
	 * The inner class for dealing with the tree.
	 */
	protected class Tree implements Serializable {

		/** For serializatiin */
		private static final long serialVersionUID = 3549573538656522569L;

		/** The subtrees appended to this tree. */
		protected Tree[] m_Successors;

		/** The attribute to split on. */
		protected int[] m_Attribute = null;

		protected MyForest forest = null;

		/** The split point. */
		protected double[] m_SplitPoint = null;

		/** The proportions of training instances going down each branch. */
		protected double[] m_Prop = null;

		/** Class probabilities from the training data. */
		protected double[] m_ClassDistribution = null;
		
		/**
		 * Backfits the given data into the tree.
		 */
		public void backfitData(Instances data) throws Exception {

			// Compute initial class counts
			double[] classProbs = new double[data.numClasses()];
			for (int i = 0; i < data.numInstances(); i++) {
				Instance inst = data.instance(i);
				classProbs[(int) inst.classValue()] += inst.weight();
			}

			// Fit data into tree
			backfitData(data, classProbs);
		}

		/**
		 * Computes class distribution of an instance using the decision tree.
		 * 
		 * @param instance
		 *            the instance to compute the distribution for
		 * @return the computed class distribution
		 * @throws Exception
		 *             if computation fails
		 */
		public double[] distributionForInstance(Instance instance)
				throws Exception {
			double[] returnedDist = null;
			if (m_Attribute != null) {
				double inst_class = forest.classify_instance(instance);
				if (inst_class == 0.0) {
					returnedDist = m_Successors[0]
							.distributionForInstance(instance);
				} else {
					returnedDist = m_Successors[1]
							.distributionForInstance(instance);
				}
			}

			// Node is a leaf or successor is empty?
			if ((m_Attribute == null) || (returnedDist == null)) {

				// Is node empty?
				if (m_ClassDistribution == null) {
					if (getAllowUnclassifiedInstances()) {
						return new double[m_Info.numClasses()];
					} else {
						return null;
					}
				}

				// Else return normalized distribution
				double[] normalizedDistribution = m_ClassDistribution.clone();
				Utils.normalize(normalizedDistribution);
				return normalizedDistribution;
			} else {
				return returnedDist;
			}
		}

		/**
		 * Outputs one node for graph.
		 * 
		 * @param text
		 *            the buffer to append the output to
		 * @param num
		 *            unique node id
		 * @return the next node id
		 * @throws Exception
		 *             if generation fails
		 */
		// public int toGraph(StringBuffer text, int num) throws Exception {
		//
		// int maxIndex = Utils.maxIndex(m_ClassDistribution);
		// String classValue =
		// Utils.backQuoteChars(m_Info.classAttribute().value(maxIndex));
		//
		// num++;
		// if (m_Attribute == -1) {
		// text.append("N" + Integer.toHexString(hashCode()) + " [label=\"" +
		// num
		// + ": " + classValue + "\"" + "shape=box]\n");
		// } else {
		// text.append("N" + Integer.toHexString(hashCode()) + " [label=\"" +
		// num
		// + ": " + classValue + "\"]\n");
		// for (int i = 0; i < m_Successors.length; i++) {
		// text.append("N" + Integer.toHexString(hashCode()) + "->" + "N"
		// + Integer.toHexString(m_Successors[i].hashCode()) + " [label=\""
		// + Utils.backQuoteChars(m_Info.attribute(m_Attribute).name()));
		// if (m_Info.attribute(m_Attribute).isNumeric()) {
		// if (i == 0) {
		// text.append(" < " + Utils.doubleToString(m_SplitPoint, 2));
		// } else {
		// text.append(" >= " + Utils.doubleToString(m_SplitPoint, 2));
		// }
		// } else {
		// text.append(" = " +
		// Utils.backQuoteChars(m_Info.attribute(m_Attribute).value(i)));
		// }
		// text.append("\"]\n");
		// num = m_Successors[i].toGraph(text, num);
		// }
		// }
		//
		// return num;
		// }

		/**
		 * Outputs a leaf.
		 * 
		 * @return the leaf as string
		 * @throws Exception
		 *             if generation fails
		 */
		protected String leafString() throws Exception {

			double sum = 0, maxCount = 0;
			int maxIndex = 0;
			if (m_ClassDistribution != null) {
				sum = Utils.sum(m_ClassDistribution);
				maxIndex = Utils.maxIndex(m_ClassDistribution);
				maxCount = m_ClassDistribution[maxIndex];
			}
			return " : " + m_Info.classAttribute().value(maxIndex) + " ("
					+ Utils.doubleToString(sum, 2) + "/"
					+ Utils.doubleToString(sum - maxCount, 2) + ")";
		}

		/**
		 * Recursively outputs the tree.
		 * 
		 * @param level
		 *            the current level of the tree
		 * @return the generated subtree
		 */
		protected String toString(int level) {

			try {
				StringBuffer text = new StringBuffer();

				if (m_Attribute == null) {
					// Output leaf info
					return leafString();
				}

				// For numeric attributes
				text.append("\n");
				for (int j = 0; j < level; j++) {
					text.append("|   ");
				}
				for (int i = 0; i < m_Attribute.length; i++) {
					text.append(m_Info.attribute(m_Attribute[i]).name() + " < "
							+ Utils.doubleToString(m_SplitPoint[i], 2));
					text.append(" ");
				}
				text.append(m_Successors[0].toString(level + 1));
				text.append("\n");

				for (int j = 0; j < level; j++) {
					text.append("|   ");
				}

				for (int i = 0; i < m_Attribute.length; i++) {
					text.append(m_Info.attribute(m_Attribute[i]).name()
							+ " >= " + Utils.doubleToString(m_SplitPoint[i], 2));
					text.append(" ");
				}
				text.append(m_Successors[1].toString(level + 1));

				return text.toString();
			} catch (Exception e) {
				e.printStackTrace();
				return "myRandomTreeV2: tree can't be printed";
			}
		}

		/**
		 * Recursively backfits data into the tree.
		 * 
		 * @param data
		 *            the data to work with
		 * @param classProbs
		 *            the class distribution
		 * @throws Exception
		 *             if generation fails
		 */
		protected void backfitData(Instances data, double[] classProbs)
				throws Exception {

			// Make leaf if there are no training instances
			if (data.numInstances() == 0) {
				m_Attribute = null;
				m_ClassDistribution = null;
				m_Prop = null;
				return;
			}

			// Check if node doesn't contain enough instances or is pure
			// or maximum depth reached
			m_ClassDistribution = classProbs.clone();

			/*
			 * if (Utils.sum(m_ClassDistribution) < 2 * m_MinNum ||
			 * Utils.eq(m_ClassDistribution
			 * [Utils.maxIndex(m_ClassDistribution)], Utils
			 * .sum(m_ClassDistribution))) {
			 * 
			 * // Make leaf m_Attribute = -1; m_Prop = null; return; }
			 */

			// Are we at an inner node
			if (m_Attribute != null) {

				// Compute new weights for subsets based on backfit data
				m_Prop = new double[m_Successors.length];
				// for (int i = 0; i < data.numInstances(); i++) {
				// Instance inst = data.instance(i);
				// if (!inst.isMissing(m_Attribute)) {
				// if (data.attribute(m_Attribute).isNominal()) {
				// m_Prop[(int) inst.value(m_Attribute)] += inst.weight();
				// } else {
				// m_Prop[(inst.value(m_Attribute) < m_SplitPoint) ? 0 : 1] +=
				// inst
				// .weight();
				// }
				// }
				// }

				// If we only have missing values we can make this node into a
				// leaf
				if (Utils.sum(m_Prop) <= 0) {
					m_Attribute = null;
					m_Prop = null;
					return;
				}

				// Otherwise normalize the proportions
				Utils.normalize(m_Prop);

				// Split data
				Instances[] subsets = splitData(data);

				// Go through subsets
				for (int i = 0; i < subsets.length; i++) {

					// Compute distribution for current subset
					double[] dist = new double[data.numClasses()];
					for (int j = 0; j < subsets[i].numInstances(); j++) {
						dist[(int) subsets[i].instance(j).classValue()] += subsets[i]
								.instance(j).weight();
					}

					// Backfit subset
					m_Successors[i].backfitData(subsets[i], dist);
				}

				// If unclassified instances are allowed, we don't need to store
				// the
				// class distribution
				if (getAllowUnclassifiedInstances()) {
					m_ClassDistribution = null;
					return;
				}

				boolean emptySuccessor = false;
				for (int i = 0; i < subsets.length; i++) {
					if (m_Successors[i].m_ClassDistribution == null) {
						emptySuccessor = true;
						return;
					}
				}
				m_ClassDistribution = null;
			}
		}
		
		/**
		 * Recursively generates a tree.
		 * 
		 * @param data
		 *            the data to work with
		 * @param classProbs
		 *            the class distribution
		 * @param attIndicesWindow
		 *            the attribute window to choose attributes from
		 * @param random
		 *            random number generator for choosing random attributes
		 * @param depth
		 *            the current depth
		 * @throws Exception
		 *             if generation fails
		 */
		protected void buildTree(Instances data, double[] classProbs,
				int depth, int[] attIndicesWindow, Random random) throws Exception {
			// Make leaf if there are no training instances
			if (data.numInstances() == 0) {
				m_Attribute = null;
				m_ClassDistribution = null;
				m_Prop = null;
				return;
			}

			m_ClassDistribution = classProbs.clone();
			if (Utils.sum(m_ClassDistribution) < 2 * m_MinNum
					|| Utils.eq(m_ClassDistribution[Utils
							.maxIndex(m_ClassDistribution)], Utils
							.sum(m_ClassDistribution))
					|| ((getMaxDepth() > 0) && (depth >= getMaxDepth()))) {
				m_Attribute = null;
				m_Prop = null;
				return;
			}

			double[][] bestDists = null;
			forest = new MyForest(num_rules, data, random, the_same_rules_node, 
					selected_rules_tree, percent_data, random_generator_type);

			// tu podmienic na inny najlepszy podzial?? ten nasz z TSP
			// ja to tu wrzucilem :D
			TSPEvaluation evaluation = new TSPEvaluation();
			TopScorePair bestPair = evaluation.getBestPair(data, classProbs);
			System.out.println("BEST PAIR: " + bestPair.getFirstAtribiute() + " > " + bestPair.getSecondAtribiute() + " Score: "+ bestPair.getScore());

			bestDists = forest.getDist(data);

			if (Arrays.equals(m_ClassDistribution, bestDists[1]) || forest.trees.size() == 0 || 
					Arrays.equals(m_ClassDistribution, bestDists[0])) {
				m_Attribute = null;
				m_Prop = null;
				return;
			}
														
			m_Prop = forest.getProps(bestDists);
			m_Attribute = forest.atributes;
			m_SplitPoint = forest.splits;
			
			if (selected_rules_tree != null) {
				for (Integer rule_index: m_Attribute) {
					selected_rules_tree.add(rule_index);
				}
			}

			// podzial
			Instances[] subsets = splitData(data);
			m_Successors = new Tree[bestDists.length];
			// tu sie tworza nowe drzewa i build tree leci jeszcze raz
			for (int i = 0; i < bestDists.length; i++) {
				m_Successors[i] = new Tree();
				m_Successors[i].buildTree(subsets[i], bestDists[i], depth + 1,
						attIndicesWindow, random);
			}

			boolean emptySuccessor = false;
			for (int i = 0; i < subsets.length; i++) {
				if (m_Successors[i].m_ClassDistribution == null) {
					emptySuccessor = true;
					break;
				}
			}
			if (!emptySuccessor) {
				m_ClassDistribution = null;
			}	
		}

		/**
		 * Computes size of the tree.
		 * 
		 * @return the number of nodes
		 */
		public int numNodes() {

			if (m_Attribute == null) {
				return 1;
			} else {
				int size = 1;
				for (int i = 0; i < m_Successors.length; i++) {
					size += m_Successors[i].numNodes();
				}
				return size;
			}
		}

		/**
		 * Splits instances into subsets based on the given split.
		 * 
		 * @param data
		 *            the data to work with
		 * @return the subsets of instances
		 * @throws Exception
		 *             if something goes wrong
		 */
		protected Instances[] splitData(Instances data) throws Exception {
			// Allocate array of Instances objects
			Instances[] subsets = new Instances[m_Prop.length];
			for (int i = 0; i < m_Prop.length; i++) {
				subsets[i] = new Instances(data, data.numInstances());
			}

			// Go through the data
			for (int i = 0; i < data.numInstances(); i++) {

				// Get instance
				Instance inst = data.instance(i);

				double inst_class = -1;
				inst_class = forest.classify_instance(inst);
				subsets[(inst_class == 0.0) ? 0 : 1].add(inst);
				continue;
			}

			// Save memory
			for (int i = 0; i < m_Prop.length; i++) {
				subsets[i].compactify();
			}

			// Return the subsets
			return subsets;
		}

		/**
		 * Computes class distribution for an attribute.
		 * 
		 * @param props
		 * @param dists
		 * @param att
		 *            the attribute index
		 * @param data
		 *            the data to work with
		 * @throws Exception
		 *             if something goes wrong
		 */
		protected double distribution(double[][] props, double[][][] dists,
				int att, Instances data) throws Exception {

			double splitPoint = Double.NaN;
			Attribute attribute = data.attribute(att);
			double[][] dist = null;
			int indexOfFirstMissingValue = data.numInstances();

			if (attribute.isNominal()) {

				// For nominal attributes
				dist = new double[attribute.numValues()][data.numClasses()];
				for (int i = 0; i < data.numInstances(); i++) {
					Instance inst = data.instance(i);
					if (inst.isMissing(att)) {

						// Skip missing values at this stage
						if (indexOfFirstMissingValue == data.numInstances()) {
							indexOfFirstMissingValue = i;
						}
						continue;
					}
					dist[(int) inst.value(att)][(int) inst.classValue()] += inst
							.weight();
				}
			} else {

				// For numeric attributes
				double[][] currDist = new double[2][data.numClasses()];
				dist = new double[2][data.numClasses()];

				// Sort data
				data.sort(att);

				// Move all instances into second subset
				for (int j = 0; j < data.numInstances(); j++) {
					Instance inst = data.instance(j);
					if (inst.isMissing(att)) {

						// Can stop as soon as we hit a missing value
						indexOfFirstMissingValue = j;
						break;
					}
					currDist[1][(int) inst.classValue()] += inst.weight();
				}

				// Value before splitting
				double priorVal = priorVal(currDist);

				// Save initial distribution
				for (int j = 0; j < currDist.length; j++) {
					System.arraycopy(currDist[j], 0, dist[j], 0, dist[j].length);
				}

				// Try all possible split points
				double currSplit = data.instance(0).value(att);
				double currVal, bestVal = -Double.MAX_VALUE;
				for (int i = 0; i < indexOfFirstMissingValue; i++) {
					Instance inst = data.instance(i);

					// Can we place a sensible split point here?
					if (inst.value(att) > currSplit) {

						// Compute gain for split point
						currVal = gain(currDist, priorVal);

						// Is the current split point the best point so far?
						if (currVal > bestVal) {

							// Store value of current point
							bestVal = currVal;

							// Save split point
							splitPoint = (inst.value(att) + currSplit) / 2.0;

							// Check for numeric precision problems
							if (splitPoint <= currSplit) {
								splitPoint = inst.value(att);
							}

							// Save distribution
							for (int j = 0; j < currDist.length; j++) {
								System.arraycopy(currDist[j], 0, dist[j], 0,
										dist[j].length);
							}
						}
						currSplit = inst.value(att);
					}

					// Shift over the weight
					currDist[0][(int) inst.classValue()] += inst.weight();
					currDist[1][(int) inst.classValue()] -= inst.weight();
				}
			}

			// Compute weights for subsets
			props[0] = new double[dist.length];
			for (int k = 0; k < props[0].length; k++) {
				props[0][k] = Utils.sum(dist[k]);
			}
			if (Utils.eq(Utils.sum(props[0]), 0)) {
				for (int k = 0; k < props[0].length; k++) {
					props[0][k] = 1.0 / props[0].length;
				}
			} else {
				Utils.normalize(props[0]);
			}

			// Distribute weights for instances with missing values
			for (int i = indexOfFirstMissingValue; i < data.numInstances(); i++) {
				Instance inst = data.instance(i);
				if (attribute.isNominal()) {

					// Need to check if attribute value is missing
					if (inst.isMissing(att)) {
						for (int j = 0; j < dist.length; j++) {
							dist[j][(int) inst.classValue()] += props[0][j]
									* inst.weight();
						}
					}
				} else {

					// Can be sure that value is missing, so no test required
					for (int j = 0; j < dist.length; j++) {
						dist[j][(int) inst.classValue()] += props[0][j]
								* inst.weight();
					}
				}
			}

			// Return distribution and split point
			dists[0] = dist;
			return splitPoint;
		}

		/**
		 * Computes value of splitting criterion before split.
		 * 
		 * @param dist
		 *            the distributions
		 * @return the splitting criterion
		 */
		protected double priorVal(double[][] dist) {

			return ContingencyTables.entropyOverColumns(dist);
		}

		/**
		 * Computes value of splitting criterion after split.
		 * 
		 * @param dist
		 *            the distributions
		 * @param priorVal
		 *            the splitting criterion
		 * @return the gain after the split
		 */
		protected double gain(double[][] dist, double priorVal) {

			return priorVal - ContingencyTables.entropyConditionedOnRows(dist);
		}

		/**
		 * Returns the revision string.
		 * 
		 * @return the revision
		 */
		public String getRevision() {
			return RevisionUtils.extract("$Revision: 10993 $");
		}

		/**
		 * Outputs one node for graph.
		 * 
		 * @param text
		 *            the buffer to append the output to
		 * @param num
		 *            the current node id
		 * @param parent
		 *            the parent of the nodes
		 * @return the next node id
		 * @throws Exception
		 *             if something goes wrong
		 */
		protected int toGraph(StringBuffer text, int num, Tree parent)
				throws Exception {

			num++;
			/*
			 * if (m_Attribute == -1) { text.append("N" +
			 * Integer.toHexString(Tree.this.hashCode()) + " [label=\"" + num +
			 * leafString() + "\"" + " shape=box]\n");
			 * 
			 * } else { text.append("N" +
			 * Integer.toHexString(Tree.this.hashCode()) + " [label=\"" + num +
			 * ": " + m_Info.attribute(m_Attribute).name() + "\"]\n"); for (int
			 * i = 0; i < m_Successors.length; i++) { text.append("N" +
			 * Integer.toHexString(Tree.this.hashCode()) + "->" + "N" +
			 * Integer.toHexString(m_Successors[i].hashCode()) + " [label=\"");
			 * if (m_Info.attribute(m_Attribute).isNumeric()) { if (i == 0) {
			 * text.append(" < " + Utils.doubleToString(m_SplitPoint, 2)); }
			 * else { text.append(" >= " + Utils.doubleToString(m_SplitPoint,
			 * 2)); } } else { text.append(" = " +
			 * m_Info.attribute(m_Attribute).value(i)); } text.append("\"]\n");
			 * num = m_Successors[i].toGraph(text, num, this); } }
			 */

			return num;
		}
	}

	/**
	 * Main method for this class.
	 * 
	 * @param argv
	 *            the commandline parameters
	 */
	public static void main(String[] argv) {
		runClassifier(new myRandomTreeV2(), argv);
	}
}
