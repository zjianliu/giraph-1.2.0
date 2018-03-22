package org.apache.giraph.worker;

import org.apache.hadoop.io.Text;

import java.util.regex.Pattern;

public class KMeansNodeWorkerContext extends DefaultWorkerContext {
    private double[][] centers;

    private int numberOfClusters;
    private int numberOfDimensions;
    private int maxIterations;

    private final static Pattern commaPattern = Pattern.compile(",");

    @Override
    public void preApplication() throws InstantiationException,
            IllegalAccessException{
        numberOfClusters = 3;
        numberOfDimensions = 2;
        centers = new double[numberOfClusters][numberOfDimensions];
        maxIterations = 80;
    }
    @Override
    public void preSuperstep(){
        Text pointsText = ((Text)getAggregatedValue("custom.center.point"));

        if (pointsText != null) {
            String pointsString = pointsText.toString();
            if (!pointsString.isEmpty()) {
                String[] points = commaPattern.split(pointsString);
                int pointIndex = 0;
                for (int c = 0; c < numberOfClusters; c++) {
                    for (int d = 0; d < numberOfDimensions; d++) {

                        //System.out.println("centers[" + c + "][" + d + "]=" + points[pointIndex]);

                        centers[c][d] = Double.parseDouble(points[pointIndex]);
                        pointIndex++;
                    }
                }
            }
        }
    }

    public double[][] getCenters() {
        return centers;
    }

    public int getNumberOfClusters() {
        return numberOfClusters;
    }

    public int getNumberOfDimensions() {
        return numberOfDimensions;
    }

    public int getMaxIterations() {
        return maxIterations;
    }
}
