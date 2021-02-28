public interface INearestNeighborFinder<K extends IHasMetric<K>, V> {
  public void add(K key, V value);

  public V find(K key);
}
