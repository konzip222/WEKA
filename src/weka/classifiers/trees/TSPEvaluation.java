package weka.classifiers.trees;

import weka.core.Instance;
import weka.core.Instances;

public class TSPEvaluation {

    public TopScorePair getBestPair(Instances data, double[] classProbs){
        TopScorePair topScorePair = new TopScorePair();
        for(int i = 0; i < data.numAttributes(); i++){
            for(int j = 0; j < data.numAttributes(); j++){
                if(j != i){
                    double score = calculateScore(i, j, data);
                    if(score > topScorePair.getScore()){
                        topScorePair.setFirstAtribiute(""+i);
                        topScorePair.setSecondAtribiute(""+j);
                        topScorePair.setScore(score);
                    }
                }
            }
        }
        return topScorePair;
    }

    // wersja dal dwoch klas pozniej zrobic na dowolna liczbe (jezeli nasza metoda w ogole przewiduje taka mozliwosc)
    private double calculateScore(int firstArgument, int secondArgument, Instances data){

        int firstGroupAmount = 0;
        int secondGroupAmount = 0;
        int firstGroupCorrect = 0;
        int secondGroupCorrect = 0;


        for(int i = 0; i < data.numInstances(); i++){
            Instance instance = data.instance(i);
            double classType = instance.value(instance.numAttributes()-1);
            if(classType == 0){
                firstGroupAmount++;
                if( instance.value(firstArgument) > instance.value(secondArgument) ){
                    firstGroupCorrect++;
                }
            }
            else{
                secondGroupAmount++;
                if( instance.value(firstArgument) > instance.value(secondArgument) ){
                    secondGroupCorrect++;
                }
            }
        }

        double firstProbability = 0.0;
        if(firstGroupAmount != 0){
            firstProbability = ((double)firstGroupCorrect)/firstGroupAmount;
        }

        double secondProbability = 0.0;
        if(firstGroupAmount != 0){
            secondProbability = ((double)secondGroupCorrect)/secondGroupAmount;
        }

        return Math.abs(firstProbability - secondProbability);
    }

}
