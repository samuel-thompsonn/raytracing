package raytracing;

public abstract class HomogeneousShape implements RayShape {

  private double[] myColor;
  private double myReflectivity;
  private double myTransparency;

  public HomogeneousShape(double[] color, double reflectivity, double transparency) {
    myColor = color;
    myReflectivity = reflectivity;
    myTransparency = transparency;
  }

  @Override
  public double[] getColor() {
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
