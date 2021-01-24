package raytracing;

public abstract class HomogeneousShape implements RayShape {

  private double[] myColor;
  private double myReflectivity;
  private double myTransparency;

  public HomogeneousShape(double[] color, double reflectivity, double transparency) {
    myColor = checkColor(color);
    myReflectivity = reflectivity;
    myTransparency = transparency;
  }

  private double[] checkColor(double[] color) {
    return new double[] {
            Math.max(Math.min(color[0],1.0),0.0),
            Math.max(Math.min(color[1],1.0),0.0),
            Math.max(Math.min(color[2],1.0),0.0)
    };
  }

  @Override
  public double[] getColor(double[] surfacePoint) {
    return myColor;
  }

  @Override
  public double getReflectivity() {
    return myReflectivity;
  }

  @Override
  public void setReflectivity(double reflectivity) {
    myReflectivity = Math.max(Math.min(reflectivity,1.0),0.0);
  }

  @Override
  public void setTransparency(double transparency) {
    myTransparency = Math.max(Math.min(transparency,1.0),0.0);
  }

  @Override
  public double getTransparency() {
    return myTransparency;
  }
}
