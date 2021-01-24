package raytracing;

import java.util.Arrays;

public class LinearUtil {

  public static double[] vectorScale(double scalar, double[] v) {
    double[] retVec = new double[v.length];
    for (int i = 0; i < v.length; i ++) {
      retVec[i] = scalar * v[i];
    }
    return retVec;
  }

  public static double dotProduct(double[] v1, double[] v2) {
    double sum = 0;
    for (int i = 0; i < v1.length; i ++) {
      sum += v1[i] * v2[i];
    }
    return sum;
  }

  public static double[] vectorAdd(double[] v1, double[] v2) {
    double[] retVec = new double[v1.length];
    for (int i = 0; i < v1.length; i ++) {
      retVec[i] = v1[i] + v2[i];
    }
    return retVec;
  }

  public static double[] vectorSubtract(double[] v1, double[] v2) {
    double[] retVec = new double[v1.length];
    for (int i = 0; i < v1.length; i ++) {
      retVec[i] = v1[i] - v2[i];
    }
    return retVec;
  }

  public static double vectorMagnitude(double[] v) {
    return Math.sqrt(dotProduct(v,v));
  }

  public static double[] normalizedVector(double[] v) {
    return vectorScale(1.0/vectorMagnitude(v),v);
  }

  public static double angleBetweenVectors(double[] v1, double[] v2) {
    double cosTheta = dotProduct(v1,v2) / (vectorMagnitude(v1)*vectorMagnitude(v2));
    cosTheta = Math.max(Math.min(cosTheta,1.0),-1.0);
    return Math.acos(cosTheta);
  }

  //Projection of a onto v
  public static double[] projectionOntoVector(double[] v, double[] a) {
    return vectorScale(Math.pow(vectorMagnitude(v),2)*dotProduct(v,a),v);
  }

  public static double[] crossProduct(double[] a, double[] b) {
    return new double[]{
            a[1]*b[2] - a[2]*b[1],
            a[2]*b[0] - a[0]*b[2],
            a[0]*b[1] - a[1]*b[0]
    };
  }
}
