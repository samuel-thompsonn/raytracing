package raytracing;

public class Light {
  private double[] myPosition;
  private double myStrength;

  public Light(double[] position, double strength) {
    myPosition = position;
    myStrength = Math.max(Math.min(strength,1.0),0.0);
  }

  public double[] getPosition() {
    return myPosition;
  }

  public double getStrength() {
    return myStrength;
  }
}
