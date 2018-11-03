package weka.classifiers.trees;

public class TopScorePair {

    private String firstAtribiute;
    private String secondAtribiute;
    private Double score;

    public TopScorePair(){
        this.firstAtribiute = "";
        this.secondAtribiute = "";
        this.score = -1.0;
    }

    public String getFirstAtribiute() {
        return firstAtribiute;
    }

    public void setFirstAtribiute(String firstAtribiute) {
        this.firstAtribiute = firstAtribiute;
    }

    public String getSecondAtribiute() {
        return secondAtribiute;
    }

    public void setSecondAtribiute(String secondAtribiute) {
        this.secondAtribiute = secondAtribiute;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

}
