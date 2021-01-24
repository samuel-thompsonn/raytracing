package raytracing.rayshape;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import raytracing.HomogeneousShape;
import raytracing.LinearUtil;

public class Sphere extends HomogeneousShape {

  private double[] myCenter;
  private double myRadius;
  private Image myTextureImage;

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
    myTextureImage = new Image("raytracing/rayshape/earthtexture.jpg");
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
  public double[] getColor(double[] surfacePoint) {
    PixelReader reader = myTextureImage.getPixelReader();
    double[] direction = LinearUtil.normalizedVector(LinearUtil.vectorSubtract(surfacePoint,myCenter));
    double[] angles = getThetaPhi(direction);

    double thetaProportion = angles[0] / (Math.PI*2);
//    System.out.println("Theta proportion: " + thetaProportion);
    if (thetaProportion >= 1.0) thetaProportion = 0.5;
    if (thetaProportion < 0.0) thetaProportion = 0.5;
    thetaProportion = 1 - thetaProportion;
    int u = (int)Math.floor(Math.min(myTextureImage.getWidth() * thetaProportion, myTextureImage.getWidth() - 1));

    double phiProportion = angles[1] / Math.PI;
//    System.out.println("Phi proportion: " + phiProportion);

    int v = (int)Math.floor(myTextureImage.getHeight() * (phiProportion));
//    System.out.println("About to find the color at u = " + u + ", v = " + v);
    if (u >= myTextureImage.getWidth() || v >= myTextureImage.getHeight()) {
      System.out.println("U or V is out of bounds!");
      System.out.println("u = " + u);
      System.out.println("v = " + v);
    }
    Color color = reader.getColor(u,v);
//    System.out.println("Found the color " + color.toString());
    return new double[]{
            color.getRed(),
            color.getGreen(),
            color.getBlue()
    };
  }

  private static double[] getThetaPhi(double[] surfacePoint) {
    double[] direction = LinearUtil.vectorSubtract(surfacePoint,new double[]{0,0,0});
    double theta = Math.atan(direction[1]/direction[0]);
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

  @Override
  public double[] getNormal(double[] surfacePoint) {
    return LinearUtil.normalizedVector(LinearUtil.vectorSubtract(surfacePoint,myCenter));
  }

  public void setTexture(String url) {
    myTextureImage = new Image(url);
  }
}
