
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.security.InvalidParameterException;

public class ImageWrapper {
  public final String name;

  public final int width;
  public final int height;
    
  private Pixel averagePixel;

  private BufferedImage baseImage;

  private BufferedImage compressedImage;
  private int compressedWidth = -1;
  private int compressedHeight = -1;
  
  public ImageWrapper(BufferedImage image, String name) {
    baseImage = image;
    this.name = name;

    width = baseImage.getWidth();
    height = baseImage.getHeight();
  }

  public BufferedImage getCompressedImage(int width, int height) {
    if (this.compressedImage != null && width == compressedWidth && height == compressedHeight) {
      return this.compressedImage;
    }
    this.compressedWidth = width;
    this.compressedHeight = height;
    this.compressedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    double sectionHeight = this.height / height;
    double sectionWidth = this.width / width;

    double upperBound = 0;
    double lowerBound = sectionHeight;

    for (int y = 0; y < height; y++) {
      double leftBound = 0;
      double rightBound = sectionWidth;
      for (int x = 0; x < width; x++) {
        Pixel averagePixel = this.getAveragePixelInBounds(leftBound, rightBound, upperBound, lowerBound);
        this.compressedImage.setRGB(x, y, averagePixel.toIntRgb());

        leftBound = rightBound;
        rightBound += sectionWidth;
      }

      upperBound = lowerBound;
      lowerBound += sectionHeight;
    }

    return this.compressedImage;
  }
  
  public Pixel getAveragePixel() {
    if (this.averagePixel == null) {
      this.averagePixel = this.getAveragePixelInBounds(0.0, this.width, 0.0, this.height);
    }
    
    return this.averagePixel;
  }

  /**
   * Note: Does not check if the bounds are within the image.
   * 
   * @param left
   * @param right
   * @param upper
   * @param lower
   */
  public Pixel getAveragePixelInBounds(double left, double right, double upper, double lower) {
    if (left < 0 || right > this.width || upper < 0 || lower > this.height) {
      System.out.println("Left: " + left + "\nRight: " + right + "\nUpper: " + upper + "\nLower: " + lower);
      throw new InvalidParameterException("Invalid image bound parameters.");
    }

    double red = 0.0;
    double green = 0.0;
    double blue = 0.0;

    if (upper == lower || left == right) {
      return new Pixel();
    }

    int upperFloor = (int) Math.floor(upper);
    int lowerCeil = (int) Math.ceil(lower);

    Raster raster = baseImage.getData();
    for (int y = upperFloor; y < lowerCeil; y++) {
      double[] totalColor = this.getRgbSumInRow(raster, y, left, right);
      red += totalColor[0];
      green += totalColor[1];
      blue += totalColor[2];
    }

    double upperUnusedHeight = upper - upperFloor;
    double[] unusedColor = this.getRgbSumInRow(raster, upperFloor, left, right);
    red -= upperUnusedHeight * unusedColor[0];
    green -= upperUnusedHeight * unusedColor[1];
    blue -= upperUnusedHeight * unusedColor[2];

    double lowerUnusedHeight = (double) lowerCeil - lower;
    unusedColor = this.getRgbSumInRow(raster, lowerCeil - 1, left, right);
    red -= lowerUnusedHeight * unusedColor[0];
    green -= lowerUnusedHeight * unusedColor[1];
    blue -= lowerUnusedHeight * unusedColor[2];

    double totalPixels = (lower - upper) * (right - left);
    red /= totalPixels;
    green /= totalPixels;
    blue /= totalPixels;

    return new Pixel(red, green, blue);
  }

  /**
   * Assumes left < right.
   * 
   * @param raster
   * @param row
   * @param left
   * @param right
   * @return
   */
  private double[] getRgbSumInRow(Raster raster, int row, double left, double right) {
    double[] rgbSum = new double[] { 0.0, 0.0, 0.0 };

    int leftFloor = (int) Math.floor(left);
    int rightCeil = (int) Math.ceil(right);

    for (int x = leftFloor; x < rightCeil; x++) {
      double[] values = new double[4];
      // Note that raster.getPixel returns doubles between 0 to 255
      raster.getPixel(x, row, values);
      rgbSum[0] += values[0] / 256;
      rgbSum[1] += values[1] / 256;
      rgbSum[2] += values[2] / 256;
    }

    double[] unusedColor = new double[] { 0.0, 0.0, 0.0, 0.0 };
    
    double leftUnusedWidth = left - leftFloor;
    raster.getPixel((int) leftFloor, row, unusedColor);
    rgbSum[0] -= leftUnusedWidth * unusedColor[0] / 256;
    rgbSum[1] -= leftUnusedWidth * unusedColor[1] / 256;
    rgbSum[2] -= leftUnusedWidth * unusedColor[2] / 256;

    double rightUnusedWidth = (double) rightCeil - right;
    raster.getPixel(rightCeil - 1, row, unusedColor);
    rgbSum[0] -= rightUnusedWidth * unusedColor[0] / 256;
    rgbSum[1] -= rightUnusedWidth * unusedColor[1] / 256;
    rgbSum[2] -= rightUnusedWidth * unusedColor[2] / 256;

    return rgbSum;
  } 
}
