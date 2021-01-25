package raytracing.linear_util;

public class SimpleRayVector implements RayVector {

  private final double myX;
  private final double myY;
  private final double myZ;

  public SimpleRayVector(double x, double y, double z) {
    myX = x;
    myY = y;
    myZ = z;
  }

  public SimpleRayVector(double[] vector) {
    myX = vector[0];
    myY = vector[1];
    myZ = vector[2];
  }

  @Override
  public double x() {
    return myX;
  }

  @Override
  public double y() {
    return myY;
    //I made a mistake here where I returned x instead of y.
  }

  @Override
  public double z() {
    return myZ;
  }

  @Override
  public RayVector scale(double scalar) {
    return new SimpleRayVector(scalar*myX,scalar*myY,scalar*myZ);
  }

  @Override
  public double dotProduct(RayVector otherVector) {
    return (otherVector.x() * myX) + (otherVector.y() * myY) + (otherVector.z() * myZ);
    //(Earlier, I had an implementation where I used the add operator in the last two clauses.)
  }

  @Override
  public RayVector subtract(RayVector otherVector) {
    return add(otherVector.scale(-1));
  }

  @Override
  public RayVector add(RayVector otherVector) {
    return new SimpleRayVector(myX + otherVector.x(), myY + otherVector.y(), myZ + otherVector.z());
  }

  @Override
  public double magnitude() {
    return Math.sqrt(Math.pow(myX,2)+Math.pow(myY,2)+Math.pow(myZ,2));
  }

  @Override
  public RayVector normalized() {
    return scale(1/magnitude());
  }

  @Override
  public double angleBetween(RayVector otherVector) {
    double cosTheta = dotProduct(otherVector) / (magnitude() * otherVector.magnitude());
    return Math.acos(Math.max(Math.min(cosTheta,1.0),-1.0));
  }

  @Override
  public RayVector projectionOnto(RayVector otherVector) {
    return otherVector.scale(dotProduct(otherVector)/Math.pow(otherVector.magnitude(),2));
  }

  @Override
  public RayVector crossProduct(RayVector b) {
    return new SimpleRayVector(
            y()*b.z() - z()*b.y(),
            z()*b.x() - x()*b.z(),
            x()*b.y() - y()*b.x()
    );
  }

  @Override
  public double elementAt(int index) {
    switch (index) {
      case 0:
        return myX;
      case 1:
        return myY;
      case 2:
        return myZ;
    }
    throw new IndexOutOfBoundsException(index);
  }

  @Override
  public String toString() {
    return String.format("[%f,%f,%f]",myX,myY,myZ);
  }
}
