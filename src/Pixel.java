public class Pixel implements IHasMetric<Pixel> {
  private double red = 0.0;
  private double green = 0.0;
  private double blue = 0.0;
  
  public Pixel() { }

  public Pixel(double r, double g, double b) {
    this.red = r;
    this.green = g;
    this.blue = b;
  }

  public Pixel(int r, int g, int b) {
    this.red = (double) r / 256;
    this.green = (double) g / 256;
    this.blue = (double) b / 256;
  }

  /**
   * Distance vs distance squared doesn't matter, so just use distance squared for performance.
   */
  @Override
  public double distanceTo(Pixel other) {
    return Math.pow(this.red - other.red(), 2)
        + Math.pow(this.green - other.green(), 2)
        + Math.pow(this.blue - other.blue(), 2);
  }

  public int toIntRgb() {
    int rgb = (int) (256 * red);
    rgb = (rgb << 8) + (int) (256 * green);
    rgb = (rgb << 8) + (int) (256 * blue);
    return rgb;
  }


  public double red() {
    return this.red;
  }

  public double green() {
    return this.green;
  }

  public double blue() {
    return this.blue;
  }
}
