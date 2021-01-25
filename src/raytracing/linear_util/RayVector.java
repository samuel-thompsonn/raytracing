package raytracing.linear_util;

public interface RayVector {

  public double x();

  public double y();

  public double z();

  public RayVector scale(double scalar);

  public double dotProduct(RayVector otherVector);

  public RayVector subtract(RayVector otherVector);

  public RayVector add(RayVector otherVector);

  public double magnitude();

  public RayVector normalized();

  public double angleBetween(RayVector otherVector);

  public RayVector projectionOnto(RayVector otherVector);

  public RayVector crossProduct(RayVector otherVector);

  public double elementAt(int index);
}
