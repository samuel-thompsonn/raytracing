package raytracing;

import raytracing.linear_util.RayVector;

public class Light {
  private RayVector myPosition;
  private double myStrength;

  public Light(RayVector position, double strength) {
    myPosition = position;
    myStrength = Math.max(Math.min(strength,1.0),0.0);
  }

  public RayVector getPosition() {
    return myPosition;
  }

  public double getStrength() {
    return myStrength;
  }
}
