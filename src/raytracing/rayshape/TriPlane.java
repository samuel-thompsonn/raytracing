package raytracing.rayshape;

import raytracing.HomogeneousShape;
import raytracing.LinearUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TriPlane extends HomogeneousShape {

  private double[] myNormal;
  private List<double[]> myPoints;

  public TriPlane(List<double[]> vertices) {
    super(new double[]{1,1,1},0,0);
    myPoints = new ArrayList<>(vertices);
    calculateNormal();
  }

  private void calculateNormal() {
    double[] v1 = LinearUtil.vectorSubtract(myPoints.get(1),myPoints.get(0));
    double[] v2 = LinearUtil.vectorSubtract(myPoints.get(2),myPoints.get(0));
    myNormal = LinearUtil.normalizedVector(LinearUtil.crossProduct(v1,v2));
    System.out.println("My normal: " + Arrays.toString(myNormal));
  }

  @Override
  public void changePosition(double[] positionChange) {
    //NOT YET IMPLEMENTED
    throw new UnsupportedOperationException();
  }

  @Override
  public void setPosition(double[] position) {

  }

  public double[] rayIntersection(double[] eye, double[] direction) {
    double denominator = LinearUtil.dotProduct(myNormal,direction);
    //Perhaps we aren't properly detecting when the direction and normal are orthogonal.
    if (Math.abs(denominator)<= TOLERANCE_MARGIN) {
      //Direction is orthogonal to normal, so it is either on the plane or never touches it,
      // and it's no big deal to say you can't see an infinitely thin plane from the side.
      return null;
    }
    double numerator = LinearUtil.dotProduct(myNormal,LinearUtil.vectorSubtract(myPoints.get(0),eye));
    double scalar = numerator/denominator;
    if (scalar <= TOLERANCE_MARGIN) { //Negative scalars should not count.
      return null;
    }
    double[] intersectionPoint = LinearUtil.vectorAdd(eye,LinearUtil.vectorScale(scalar,direction));
        double dotProduct = LinearUtil.dotProduct(myNormal,LinearUtil.vectorSubtract(intersectionPoint,myPoints.get(0)));
        if (Math.abs(dotProduct) >= TOLERANCE_MARGIN) {
          System.out.println("Intersection point is not orthogonal to normal..");
        }
    if (pointInPlaneBounds(intersectionPoint,0,1) && pointInPlaneBounds(intersectionPoint,1,2)) {
//      if(intersectionPoint[1] < 1.0) {
//        System.out.println("Too left intersection point: " + Arrays.toString(intersectionPoint));
//        double dotProduct = LinearUtil.dotProduct(myNormal,LinearUtil.vectorSubtract(intersectionPoint,myPoints.get(0)));
//        System.out.println("Dot product with my normal: " + dotProduct);
//      }
      return intersectionPoint;
    }
    return null;
  }

  @Override
  public double[] getNormal(double[] surfacePoint) {
    return myNormal;
  }

  //Checks if the point's projection is in the shadow cast by the tri plane on the firstIndex-secondIndex plane.
  //For the XY-plane, you would use firstIndex = 0, secondIndex = 1.
  private boolean pointInPlaneBounds(double[] point, int firstIndex, int secondIndex) {
    double minX = myPoints.get(0)[firstIndex];
    for (double[] triPoint : myPoints) {
      if (triPoint[firstIndex] < minX) {
        minX = triPoint[firstIndex];
      }
    }
    double maxX = myPoints.get(0)[firstIndex];
    for (double[] triPoint : myPoints) {
      if (triPoint[firstIndex] > maxX) {
        maxX = triPoint[firstIndex];
      }
    }
    double minY = myPoints.get(0)[secondIndex];
    for (double[] triPoint : myPoints) {
      if (triPoint[secondIndex] < minY) {
        minY = triPoint[1];
      }
    }
    double maxY = myPoints.get(0)[secondIndex];
    for (double[] triPoint : myPoints) {
      if (triPoint[secondIndex] > maxY) {
        maxY = triPoint[secondIndex];
      }
    }
    if (minY < -1 && secondIndex == 2) {
      System.out.println("minZ is too low!");
      System.out.println("minZ = " + minY);
      System.out.println("Vertex z-values:");
      for (double[] vert : myPoints) {
        System.out.println("vert[2] = " + vert[2]);
      }
    }
//    if ((point[0] > minX && point[0] < maxX && point[1] > minY && point[1] < maxY)
//    && point[2] > 1.0) {
//      System.out.println("minY = " + minY);
//      System.out.println("maxY = " + maxY);
//      System.out.println("point[1] = " + point[1]);
//      System.out.println("point[1]>maxY = " + (point[1] > maxY));
//    }
    if (point[firstIndex] > minX && point[firstIndex] < maxX && point[secondIndex] > minY && point[secondIndex] < maxY) {
      return pointInPlaneBoundsPrecise(point,firstIndex,secondIndex);
    }
    return false;
  }

  private boolean pointInPlaneBoundsPrecise(double[] point, int firstIndex, int secondIndex) {
    return (sameSideOfLine(myPoints.get(0),myPoints.get(1),point,myPoints.get(2),firstIndex,secondIndex) &&
            sameSideOfLine(myPoints.get(2),myPoints.get(0),point,myPoints.get(1),firstIndex,secondIndex) &&
            sameSideOfLine(myPoints.get(1),myPoints.get(2),point,myPoints.get(0),firstIndex,secondIndex));
  }

  private boolean sameSideOfLine(double[] lineStart, double[] lineEnd, double[] firstPoint, double[] secondPoint, int firstIndex, int secondIndex) {
    double slope = (lineEnd[secondIndex] - lineStart[secondIndex]) / (lineEnd[firstIndex] - lineStart[firstIndex]);
    double intercept = (slope * (-1) * lineStart[firstIndex]) + lineStart[secondIndex];
    return ((secondPoint[secondIndex] >= (slope * secondPoint[firstIndex]) + intercept - TOLERANCE_MARGIN)
            == firstPoint[secondIndex] >= (slope * firstPoint[firstIndex]) + intercept - TOLERANCE_MARGIN);
  }
}
