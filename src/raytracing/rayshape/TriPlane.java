package raytracing.rayshape;

import raytracing.HomogeneousShape;
import raytracing.linear_util.RayVector;

import java.util.ArrayList;
import java.util.List;

public class TriPlane extends HomogeneousShape {

  private RayVector myNormal;
  private List<RayVector> myPoints;

  public TriPlane(List<RayVector> vertices) {
    super(new double[]{1,1,1},0,0);
    myPoints = new ArrayList<>(vertices);
    calculateNormal();
  }

  private void calculateNormal() {
//    double[] v1 = LinearUtil.vectorSubtract(myPoints.get(1),myPoints.get(0));
//    double[] v2 = LinearUtil.vectorSubtract(myPoints.get(2),myPoints.get(0));
//    myNormal = LinearUtil.normalizedVector(LinearUtil.crossProduct(v1,v2));

    RayVector v1 = myPoints.get(1).subtract(myPoints.get(0));
    RayVector v2 = myPoints.get(2).subtract(myPoints.get(0));
    myNormal = v1.crossProduct(v2);
    System.out.println("My normal: " + myNormal.toString());
  }

  @Override
  public void changePosition(RayVector positionChange) {
    //NOT YET IMPLEMENTED
    throw new UnsupportedOperationException();
  }

  @Override
  public void setPosition(RayVector position) {

  }

  public RayVector rayIntersection(RayVector eye, RayVector direction) {
//    double denominator = LinearUtil.dotProduct(myNormal,direction);
    double denominator = myNormal.dotProduct(direction);
    //Perhaps we aren't properly detecting when the direction and normal are orthogonal.
    if (Math.abs(denominator)<= TOLERANCE_MARGIN) {
      //Direction is orthogonal to normal, so it is either on the plane or never touches it,
      // and it's no big deal to say you can't see an infinitely thin plane from the side.
      return null;
    }
//    double numerator = LinearUtil.dotProduct(myNormal,LinearUtil.vectorSubtract(myPoints.get(0),eye));
    double numerator = myPoints.get(0).subtract(eye).dotProduct(myNormal);
    double scalar = numerator/denominator;
    if (scalar <= TOLERANCE_MARGIN) { //Negative scalars should not count.
      return null;
    }
//    double[] intersectionPoint = LinearUtil.vectorAdd(eye,LinearUtil.vectorScale(scalar,direction));
//    double dotProduct = LinearUtil.dotProduct(myNormal,LinearUtil.vectorSubtract(intersectionPoint,myPoints.get(0)));
    RayVector intersectionPoint = direction.scale(scalar).add(eye);
    double dotProduct = intersectionPoint.subtract(myPoints.get(0)).dotProduct(myNormal);
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
  public RayVector getNormal(RayVector surfacePoint) {
    return myNormal;
  }

  //Checks if the point's projection is in the shadow cast by the tri plane on the firstIndex-secondIndex plane.
  //For the XY-plane, you would use firstIndex = 0, secondIndex = 1.
  private boolean pointInPlaneBounds(RayVector point, int firstIndex, int secondIndex) {
    double minX = myPoints.get(0).x();
    for (RayVector triPoint : myPoints) {
      if (triPoint.elementAt(firstIndex) < minX) {
        minX = triPoint.elementAt(firstIndex);
      }
    }
    double maxX = myPoints.get(0).elementAt(firstIndex);
    for (RayVector triPoint : myPoints) {
      if (triPoint.elementAt(firstIndex) > maxX) {
        maxX = triPoint.elementAt(firstIndex);
      }
    }
    double minY = myPoints.get(0).elementAt(secondIndex);
    for (RayVector triPoint : myPoints) {
      if (triPoint.elementAt(secondIndex) < minY) {
        minY = triPoint.elementAt(secondIndex);
      }
    }
    double maxY = myPoints.get(0).elementAt(secondIndex);
    for (RayVector triPoint : myPoints) {
      if (triPoint.elementAt(secondIndex) > maxY) {
        maxY = triPoint.elementAt(secondIndex);
      }
    }
    if (minY < -1 && secondIndex == 2) {
      System.out.println("minZ is too low!");
      System.out.println("minZ = " + minY);
      System.out.println("Vertex z-values:");
      for (RayVector vert : myPoints) {
//        System.out.println("vert[2] = " + vert.[2]);
      }
    }
//    if ((point[0] > minX && point[0] < maxX && point[1] > minY && point[1] < maxY)
//    && point[2] > 1.0) {
//      System.out.println("minY = " + minY);
//      System.out.println("maxY = " + maxY);
//      System.out.println("point[1] = " + point[1]);
//      System.out.println("point[1]>maxY = " + (point[1] > maxY));
//    }
    if (point.elementAt(firstIndex) > minX && point.elementAt(firstIndex) < maxX && point.elementAt(secondIndex) > minY && point.elementAt(secondIndex) < maxY) {
      return pointInPlaneBoundsPrecise(point,firstIndex,secondIndex);
    }
    return false;
  }

  private boolean pointInPlaneBoundsPrecise(RayVector point, int firstIndex, int secondIndex) {
    return (sameSideOfLine(myPoints.get(0),myPoints.get(1),point,myPoints.get(2),firstIndex,secondIndex) &&
            sameSideOfLine(myPoints.get(2),myPoints.get(0),point,myPoints.get(1),firstIndex,secondIndex) &&
            sameSideOfLine(myPoints.get(1),myPoints.get(2),point,myPoints.get(0),firstIndex,secondIndex));
  }

  private boolean sameSideOfLine(RayVector lineStart, RayVector lineEnd, RayVector firstPoint, RayVector secondPoint, int firstIndex, int secondIndex) {
    double slope = (lineEnd.elementAt(secondIndex) - lineStart.elementAt(secondIndex)) / (lineEnd.elementAt(firstIndex)- lineStart.elementAt(firstIndex));
    double intercept = (slope * (-1) * lineStart.elementAt(firstIndex)) + lineStart.elementAt(secondIndex);
    return ((secondPoint.elementAt(secondIndex) >= (slope * secondPoint.elementAt(firstIndex)) + intercept - TOLERANCE_MARGIN)
            == firstPoint.elementAt(secondIndex) >= (slope * firstPoint.elementAt(firstIndex)) + intercept - TOLERANCE_MARGIN);
  }
}
