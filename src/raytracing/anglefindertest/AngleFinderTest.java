package raytracing.anglefindertest;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import raytracing.LinearUtil;

import java.util.Arrays;
import java.util.Random;

public class AngleFinderTest {

  public static final int NUM_ITERATIONS = 10;

  public static void main(String[] args) {
    for (int i = 0; i < NUM_ITERATIONS; i ++) {
      double theta = (Math.PI*2)*((double)i/NUM_ITERATIONS);
      System.out.println("Theta = " + theta);
      double[] angles = getThetaPhi(LinearUtil.normalizedVector(new double[]{Math.cos(theta), Math.sin(theta),0}));
      System.out.println("Theta, Phi = " + Arrays.toString(angles));
      assert(Math.abs(theta - angles[0]) < 0.01);
      System.out.println("Theta - angles[0]: " + (theta-angles[0]));
      assert((Math.PI/2) - angles[1] < 0.01);
      System.out.println("=======================");
    }
  }

  private static double[] getThetaPhi(double[] surfacePoint) {
    double[] direction = LinearUtil.vectorSubtract(surfacePoint,new double[]{0,0,0});
    System.out.println("Direction: " + Arrays.toString(direction));
    double theta = Math.atan(direction[1]/direction[0]);
    System.out.println("Initial theta = " + theta);
    //if in first quadrant, add 2pi
    if (theta < 0 && direction[0] >= 0) {
      theta += 2*Math.PI;
    }
    //if in second or third quadrant, add pi to the angle to reflect it
    else if (direction[0] < 0){
      theta += Math.PI;
    }
    //If in fourth quadrant, do nothing

    double phi = Math.atan((Math.sqrt((direction[0]*direction[0])+(direction[1]*direction[1])))/direction[2]);
    if (phi < 0) {
      phi += Math.PI;
    }
    return new double[]{theta,phi};
  }
}
