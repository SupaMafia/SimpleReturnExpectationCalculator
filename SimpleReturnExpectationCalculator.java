/*
 * Date: 2021-8-5.
 * File Name: SimpleReturnExpectationCalculator.java
 * Version: 0.3
 * Author: Weikang Ke
 */

import java.io.*;
import java.util.InputMismatchException;
import java.util.Scanner;

/*
 * A program that estimates the return of a given index based on historical data of one selected market index by CAPM function
 */

public class SimpleReturnExpectationCalculator {
    public static void main(String[] args) {

        //input indices, and selected market performance
        String filename0 = "IndicesE.txt";
        Scanner inputStream0 = null;
        try {
            inputStream0 = new Scanner(new FileInputStream(filename0));
        } catch (FileNotFoundException e) {
            System.out.println("File " + filename0 + " was not found, or could not be opened");
            System.exit(0);
        }

        //read total number of dates for indices
        int dateNum = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename0))) {
            while (reader.readLine() != null) dateNum++;
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Total number of date is: " + dateNum);

        //data into array: indexMarket, write orginal data into array for calculation
        double[][] indexMarket = new double[2][dateNum];
        for (int i = 0; i < dateNum; i++) {
            double date = inputStream0.nextDouble();
            indexMarket[0][i] = inputStream0.nextDouble();
            indexMarket[1][i] = inputStream0.nextDouble();
            String line = inputStream0.nextLine();
        }
        inputStream0.close();
        for (int i = 0; i < dateNum; i++) {
            System.out.println("Line: " + i + " index: " + indexMarket[0][i] + " market: " + indexMarket[1][i]);
        }

        //calculate index mean and market mean
        double sumIndex = 0;
        double sumMarket = 0;
        for (int i = 0; i < dateNum; i++) {
            sumIndex += indexMarket[0][i];
            sumMarket += indexMarket[1][i];
        }
        double indexMean = sumIndex / dateNum;  //index as ȳ since it is dependent on market
        double marketMean = sumMarket / dateNum; //market as x̄ since it is independent
        //System.out.println("Index Mean (ȳ) =" + indexMean + " Market Mean (x̄) =" + marketMean);

        //calculate covariance between index and market, assume population data
        double pod; //product of difference
        double sopod = 0; //sum of product of difference
        for (int i = 0; i < dateNum; i++) {
            double diffI = indexMarket[0][i] - indexMean;
            double diffM = indexMarket[1][i] - marketMean;
            pod = diffI * diffM;
            sopod += pod;
        }
        double covIM = sopod / dateNum; //n, not n-1, due to population data
        //System.out.println("Covariance between Index and market =" + covIM);

        //calculate Std.Dev() and Var() of index and market
        double stdDevI;
        double stdDevM;
        double varI;
        double varM;
        double sodI; //Square of difference
        double sodM;
        double sosodI = 0; //sum of square of difference
        double sosodM = 0;
        for (int i = 0; i < dateNum; i++) {
            double diffI = indexMarket[0][i] - indexMean;
            double diffM = indexMarket[1][i] - marketMean;
            sodI = Math.pow(diffI, 2);
            sosodI += sodI;
            sodM = Math.pow(diffM, 2);
            sosodM += sodM;
        }
        varI = sosodI / dateNum;
        varM = sosodM / dateNum;
        stdDevI = Math.sqrt(varI);
        stdDevM = Math.sqrt(varM);
        System.out.println("Std.Dev. of index (y) =" + stdDevI + " Std.Dev. of market (x)=" + stdDevM);

        //calculate persons correlation coefficient between index and market
        double corIM = 0;
        corIM = covIM / (stdDevI * stdDevM);
        System.out.println("Persons correlation coefficient between index and market =" + corIM);

        //β and α calculation, βi = cor(i,m)(σi/σm), α = ȳ-βx̄
        //double beta = corIM * (stdDevI / stdDevM);
        double beta = covIM / varM;
        double alpha = indexMean - beta * marketMean;

        //r square calculation, market explains r% of the data variation
        double[] sodfm = new double[dateNum]; //index, Square of Difference from Mean or (y- ȳ)^2
        for (int i = 0; i < dateNum; i++) {
            sodfm[i] = Math.pow((indexMarket[0][i] - indexMean), 2);
        }
        double[] regressEst = new double[dateNum]; //y hat, estimated value of y, index, from the regression function
        for (int i = 0; i < dateNum; i++) {
            regressEst[i] = alpha + beta * indexMarket[1][i]; //est index value = α + β * historical market index
        }
        double[] soefm = new double[dateNum]; //Square of estimate from mean, (y hat - ȳ)^2
        for (int i = 0; i < dateNum; i++) {
            soefm[i] = Math.pow((regressEst[i] - indexMean), 2);
        }
        double sosodfm = 0; //sum of square of difference from mean
        for (int i = 0; i < dateNum; i++) {
            sosodfm += sodfm[i];
        }
        double sosoefm = 0; //sum of square of estimate from mean
        for (int i = 0; i < dateNum; i++) {
            sosoefm += soefm[i];
        }
        double rSquare = sosoefm / sosodfm; // sosoefm / sosodfm
        System.out.println("α= [" + String.format("%.3f", alpha) + "], β= [" + String.format("%.3f", beta) + "], r^2 (market index explains r^2 % of the data variation) = [" + String.format("%.3f", rSquare * 100) + "]%.");

        //Std.Error of Est
        double[] soErr = new double[dateNum]; //square of abs.error
        for (int i = 1; i < dateNum; i++) {
            soErr[i] = Math.pow(Math.abs(regressEst[i] - indexMarket[0][i]), 2);
        }
        double sosoErr = 0; // sum of Square of error
        for (int i = 0; i < dateNum; i++) {
            sosoErr += soErr[i];
        }
        double stdErrEst = 0;
        stdErrEst = Math.sqrt(sosoErr / (dateNum - 2));
        System.out.println("Std. Err. of Est. (average expected error for each index) = [" + String.format("%.3f", stdErrEst) + "].");

        //t value for significance
        //double t = beta / stdErrEst;
        //System.out.println("t score = [" + t + "].");
        //System.out.println("(Higher the t score, higher the predictive power the market index have to the index.)");
        //System.out.println("(t score also allows the significance of the model to be checked using t distribution.)");

        //check data size
        if (dateNum < 60) {
            System.out.println("Data samples are too small to reach a conclusive estimation");
        }

        //take input of the return of three month government treasury bill as the risk free discount rate. Can improve by adding t-bill of different time length
        System.out.println("The return of a three-month government treasury bill is ___% ");
        double riskFreeRate60 = 0; //Rf60
        int count0 = 0;
        do {
            try {
                Scanner in0 = new Scanner(System.in);
                riskFreeRate60 = in0.nextDouble();
                break;
            } catch (InputMismatchException e0) {
                System.out.println("Non-numeric values are not allowed, please re enter");
            } finally {
                count0++;
            }
        } while (count0 < 10);
        double riskFreeRate120 = 0; //Rf120
        int count1 = 0;
        if (dateNum > 120) {
            System.out.println("The return of a six-month government treasury bill is ___% ");
            do {
                try {
                    Scanner in0 = new Scanner(System.in);
                    riskFreeRate120 = in0.nextDouble();
                    break;
                } catch (InputMismatchException e0) {
                    System.out.println("Non-numeric values are not allowed, please re enter");
                } finally {
                    count1++;
                }
            } while (count1 < 10);
        }
        double riskFreeRate240 = 0; //Rf240
        int count2 = 0;
        if (dateNum > 120) {
            System.out.println("The return of a one year government treasury bill is ___% ");
            do {
                try {
                    Scanner in0 = new Scanner(System.in);
                    riskFreeRate240 = in0.nextDouble();
                    break;
                } catch (InputMismatchException e0) {
                    System.out.println("Non-numeric values are not allowed, please re enter");
                } finally {
                    count2++;
                }
            } while (count2 < 10);
        }

        //find market return for 60,120 and 240 days
        double marketD60; //market price 60 days ago
        double marketD0;
        double returnD60;
        marketD60 = indexMarket[1][60];
        marketD0 = indexMarket[1][0];
        returnD60 = (marketD0 - marketD60) / marketD60;
        System.out.println("Market 3 month return = [" + String.format("%.3f", returnD60 * 100) + "%].");
        double marketD120; //market price 120 days ago
        double returnD120 = 0;
        if (dateNum >= 120) {
            marketD120 = indexMarket[1][120];
            returnD120 = (marketD0 - marketD120) / marketD120;
            System.out.println("Market half year return = [" + String.format("%.3f", returnD120 * 100) + "%].");
        } else if (dateNum < 120) {
            System.out.println("Data samples are too small for half a year return");
        }
        double marketD240; //market price 240 days ago
        double returnD240 = 0;
        if (dateNum >= 240) {
            marketD240 = indexMarket[1][240];
            returnD240 = (marketD0 - marketD240) / marketD240;
            System.out.println("Market year return = [" + String.format("%.3f", returnD240 * 100) + "%].");
        } else if (dateNum < 240) {
            System.out.println("Data samples are too small for half a year return");
        }

        //return expectation calculation, E(ri) calculation, based on historical data
        double exp60;
        double exp120;
        double exp240;
        exp60 = riskFreeRate60 + beta * (returnD60 * 100 - riskFreeRate60);  //since rf is entered in % return should be in % as well. 
        exp120 = riskFreeRate120 + beta * (returnD120 * 100 - riskFreeRate120);
        exp240 = riskFreeRate240 + beta * (returnD240 * 100 - riskFreeRate240);
        if (dateNum >= 60) {
            System.out.println("In 3 month, given past market condition, the index should have a expected return = [" + String.format("%.3f", exp60 * 100) + "%].");
        }
        if (dateNum >= 60) {
            System.out.println("In 6 month, given past market condition, the index should have a expected return = [" + String.format("%.3f", exp120 * 100) + "%].");
        }
        if (dateNum >= 60) {
            System.out.println("In 1 year, given past market condition, the index should have a expected return = [" + String.format("%.3f", exp240 * 100) + "%].");
        }
    }
}
