package raytracing.rayshape;

import raytracing.HomogeneousShape;
import raytracing.LinearUtil;

public class Sphere extends HomogeneousShape {

  private double[] myCenter;
  private double myRadius;

  public Sphere(double[] center, double radius) {
    this(center,radius,new double[]{1,1,1},0);
  }

  public Sphere(double[] center, double radius, double[] color) {
    this(center,radius,color,0);
  }

  public Sphere(double[] center, double radius, double[] color,double reflectivity) {
    super(color,reflectivity,0);
    myCenter = center;
    myRadius = radius;
    setReflectivity(reflectivity);
  }

  @Override
  public void changePosition(double[] positionChange) {
    myCenter = LinearUtil.vectorAdd(myCenter,positionChange);
  }

  @Override
  public void setPosition(double[] position) {
    myCenter = position;
  }

  @Override
  public double[] rayIntersection(double[] eye, double[] direction) {
    double d = LinearUtil.dotProduct(LinearUtil.vectorSubtract(eye,myCenter),direction);
    double b = Math.pow(LinearUtil.vectorMagnitude(LinearUtil.vectorSubtract(eye,myCenter)),2);
    double radicalValue = Math.pow(d,2) - (b - Math.pow(myRadius,2));
    if (radicalValue < 0) {
      return null;
    }
    else { //(radicalValue >= 0)
      //Objective: use the lower a value that is non negative
      double a1 = -d + Math.sqrt(radicalValue);
      double a2 = -d - Math.sqrt(radicalValue);
      double a = Math.min(a1,a2);
      if (a < TOLERANCE_MARGIN) {
        a = Math.max(a1,a2);
      }
      //To combat bounces resulting in an immediate collision
      if (a < TOLERANCE_MARGIN) {
        return null;
      }
      return LinearUtil.vectorAdd(eye,LinearUtil.vectorScale(a,direction));
    }
  }

  @Override
  public double[] getNormal(double[] surfacePoint) {
    return LinearUtil.normalizedVector(LinearUtil.vectorSubtract(surfacePoint,myCenter));
  }
}
