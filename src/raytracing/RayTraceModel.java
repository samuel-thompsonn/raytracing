package raytracing;

import raytracing.linear_util.RayVector;
import raytracing.linear_util.SimpleRayVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class RayTraceModel {


  private List<RayShape> myShapes;
  private List<Light> myLights;
  private boolean dirtyBit;
  private double myFov;
  private int myNumBounces;
  private int myImageWidth;
  private int myImageHeight;
  private RayVector myCameraPos;
  private double[] myBackgroundColor;

  public RayTraceModel() {
    myShapes = new ArrayList<>();
    myLights = new ArrayList<>();
    myBackgroundColor = new double[]{1.0,0.7,0};
  }

  public void setFov(double fov) {
    myFov = fov;
  }

  public void setBackgroundColor(double[] color) {
    myBackgroundColor = color;
  }

  public void setNumBounces(int numBounces) {
    myNumBounces = numBounces;
  }

  public void setCameraPos(RayVector cameraPos) {
    myCameraPos = cameraPos;
  }

  public void setImageSize(int imageWidth, int imageHeight) {
    myImageWidth = imageWidth;
    myImageHeight = imageHeight;
  }

  public void addShape(RayShape shape) {
    myShapes.add(shape);
  }

  public void addLight(Light light) {
    myLights.add(light);
  }

  public List<Light> getLights() {
    return myLights;
  }

  public RayVector calculateShapeIntersection(RayVector start, RayVector direction) {
    RayVector closestIntersection = null;
    RayVector nearestIntersection = null;
    for (RayShape shape : myShapes) {
      RayVector intersection = shape.rayIntersection(start,direction);
      //Check if this intersection is closer to the camera than closestIntersection
      if (intersection == null) {
        continue;
      }
      if (closestIntersection == null) {
        closestIntersection = intersection;
      }
      else if (intersection.subtract(start).magnitude() <
              closestIntersection.subtract(start).magnitude()) {
        closestIntersection = intersection;
      }
    }
    return closestIntersection;
  }

  public RayShape findIntersectingShape(RayVector start, RayVector direction) {
    RayVector closestIntersection = null;
    RayShape intersectingShape = null;
    for (RayShape shape : myShapes) {
//      double[] intersection = LinearUtil.getSphereIntersection(start, direction, sphere.getCenter(), sphere.getRadius());
      RayVector intersection = shape.rayIntersection(start, direction);
      //Check if this intersection is closer to the camera than closestIntersection
      if (intersection == null) {
        continue;
      }
      if (closestIntersection == null) {
        closestIntersection = intersection;
        intersectingShape = shape;
      } else if (intersection.subtract(start).magnitude() <
                closestIntersection.subtract(start).magnitude()) {
        closestIntersection = intersection;
        intersectingShape = shape;
      }
    }
    return intersectingShape;
  }

  public double[] generatePixelRgb(int i, int j) {
    double horizontalAngle = (-myFov + ((i * (1.0/ myImageWidth)) * myFov * 2)) * ((2*Math.PI)/360);
    double verticalAngle = ((90-myFov) + ((j * (1.0/ myImageHeight)) * myFov * 2)) * ((2*Math.PI)/360);
    double x = Math.sin(verticalAngle) * Math.cos(horizontalAngle);
    double y = Math.sin(verticalAngle) * Math.sin(horizontalAngle);
    double z = Math.cos(verticalAngle);
    RayVector direction = new SimpleRayVector(x,y,z);
    return getRayColor(myCameraPos,direction,myNumBounces,1.0);
  }

  public double[] getRayColor(RayVector eyePos, RayVector direction, int numBounces, double refraction) {
    if (numBounces < 0) {;
      return myBackgroundColor.clone();
    }
    //1. Find closest intersection among all spheres
    RayVector closestIntersection = calculateShapeIntersection(eyePos,direction);
    RayShape intersectingShape = findIntersectingShape(eyePos,direction);
    if (closestIntersection == null) {
      return myBackgroundColor.clone();
    }

    //2. Draw a path from the intersection to light source
    double lightAmount = 0;
//    double[] normalDirection = LinearUtil.normalizedVector(LinearUtil.vectorSubtract(closestIntersection,intersectingShape.getPosition()));
    RayVector normalDirection = intersectingShape.getNormal(closestIntersection);
    for (Light light : getLights()) {
//      double[] lightDirection = LinearUtil.normalizedVector(LinearUtil.vectorSubtract(light.getPosition(),closestIntersection));
//      double lightAngle = Math.abs(LinearUtil.angleBetweenVectors(normalDirection,lightDirection));
//      double[] lightIntersection = calculateShapeIntersection(closestIntersection,lightDirection);
//      RayShape lightIntersectionSphere = findIntersectingShape(closestIntersection,lightDirection);
      RayVector lightDirection = light.getPosition().subtract(closestIntersection).normalized();
      double lightAngle = Math.abs(normalDirection.angleBetween(lightDirection));
      RayVector lightIntersection = calculateShapeIntersection(closestIntersection,lightDirection);
      RayShape lightIntersectionSphere = findIntersectingShape(closestIntersection,lightDirection);
      double lightThroughObjects = 1.0;
      if (lightIntersection != null && lightIntersectionSphere != intersectingShape) {
        lightThroughObjects = lightIntersectionSphere.getTransparency();
      }
      double albedoProportion = Math.abs((lightAngle / (Math.PI/2)) - 1);
      double transparencyFactor = (lightAngle > Math.PI/2)? intersectingShape.getTransparency() : 1.0;
//      double albedoProportion = 1;
      double lightProportion = albedoProportion * lightThroughObjects * transparencyFactor;
      lightAmount += lightProportion * light.getStrength();
    }

    //3. Use light strength and color
    lightAmount = Math.max(Math.min(lightAmount,1.0),0.0);
    double[] shapeColor = intersectingShape.getColor(closestIntersection);
    double[] localColor = new double[] {
            lightAmount * shapeColor[0],
            lightAmount * shapeColor[1],
            lightAmount * shapeColor[2],
    };
//    double[] reflectedRay = LinearUtil.vectorSubtract(direction,LinearUtil.vectorScale(2,LinearUtil.projectionOntoVector(normalDirection,direction)));
//    double[] orthogonalToNormal = LinearUtil.vectorSubtract(direction,LinearUtil.projectionOntoVector(normalDirection,direction));
    RayVector reflectedRay = direction.subtract(direction.projectionOnto(normalDirection).scale(2));
    RayVector orthogonalToNormal = direction.subtract(direction.projectionOnto(normalDirection));

    double[] reflectedColor = getRayColor(closestIntersection,reflectedRay,numBounces-1,refraction);

    //Problem: There's nothing here to tell me whether I am entering or exiting a refracting material.
    double newRefraction = (refraction == 1.0)? 1.5 : 1.0;
//    double newAngle = Math.asin((refraction/newRefraction)*Math.sin(Math.abs(LinearUtil.angleBetweenVectors(direction,normalDirection))));
//    double[] refractedRay = LinearUtil.normalizedVector(LinearUtil.vectorAdd(normalDirection,LinearUtil.vectorScale(Math.cos(newAngle),orthogonalToNormal)));

    double[] transparencyColor = getRayColor(closestIntersection,direction,numBounces-1,refraction);

    double[] postReflectionColor = interpolateColors(reflectedColor,localColor,intersectingShape.getReflectivity());
    double[] postOpacityColor = interpolateColors(transparencyColor,postReflectionColor,intersectingShape.getTransparency());

//    System.out.println("Closing stack frame with numBounces = " + numBounces);
    return postOpacityColor;
  }

  public void addShapes(RayShape... spheres) {
    for (RayShape shape : spheres) {
      addShape(shape);
    }
  }

  private double[] interpolateColors(double[] c1, double[] c2, double splitPoint) {
    splitPoint = Math.max(Math.min(splitPoint,1.0),0.0);
    return new double[]{
            c1[0]*splitPoint + c2[0]*(1-splitPoint),
            c1[1]*splitPoint + c2[1]*(1-splitPoint),
            c1[2]*splitPoint + c2[2]*(1-splitPoint),
    };
  }
}
