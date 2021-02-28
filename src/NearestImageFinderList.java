import java.util.ArrayList;

public class NearestImageFinderList implements INearestNeighborFinder<Pixel, ImageWrapper> {
  private ArrayList<Pixel> pixels = new ArrayList<Pixel>();
  private ArrayList<ImageWrapper> images = new ArrayList<ImageWrapper>();

  @Override
  public void add(Pixel key, ImageWrapper value) {
    pixels.add(key);
    images.add(value);
  }

  @Override
  public ImageWrapper find(Pixel key) {
    if (this.pixels.size() == 0) {
      return null;
    }

    int nearestIndex = -1;
    double nearestDistance = Double.MAX_VALUE;

    for (int i = 0; i < pixels.size(); i++) {
      double distance = key.distanceTo(pixels.get(i));
      if (distance < nearestDistance) {
        nearestIndex = i;
        nearestDistance = distance;
      }
    }

    return this.images.get(nearestIndex);
  }
}
