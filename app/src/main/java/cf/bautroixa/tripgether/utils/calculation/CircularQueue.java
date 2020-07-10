package cf.bautroixa.tripgether.utils.calculation;

public class CircularQueue {
    int size, pos, currentSize;
    float[] data;

    public CircularQueue(int size) {
        this.size = size;
        this.pos = 0;
        this.data = new float[size];
        this.currentSize = 0;
    }

    public void push(float a) {
        this.data[pos] = a;
        this.pos = (pos + 1) % size;
        if (this.currentSize < this.size) this.currentSize++;
    }

    public int getSize() {
        return this.currentSize;
    }

    public boolean isFull() {
        return this.size == this.currentSize;
    }

    public void clear() {
        this.currentSize = 0;
        this.pos = 0;
    }

    public float getAverage() {
        float sum = 0;
        for (int i = 0; i < currentSize; i++) {
            sum += data[i];
        }
        return sum / currentSize;
    }

    public float getMin() {
        float min = 100000000;
        for (int i = 0; i < currentSize; i++) {
            if (data[i] < min) {
                min = data[i];
            }
        }
        return min;
    }
}
