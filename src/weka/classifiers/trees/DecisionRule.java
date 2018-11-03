package weka.classifiers.trees;

import java.util.ArrayList;

import weka.core.Attribute;

public class DecisionRule implements java.io.Serializable
{	
	private Attribute attr = null;
	
	private double splitPoint = 0;
		
	
	public double getSplitPoint() {
		return splitPoint;
	}

	public void setSplitPoint(double splitPoint) {
		this.splitPoint = splitPoint;
	}

	public DecisionRule(){}
	
	public Attribute getAttr() {
		return attr;
	}

	public void setAttr(Attribute attr) {
		this.attr = attr;
	}
	
	public int getIntAtrr()
	{
		return attr.index();
	}
}
