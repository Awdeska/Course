package com.awdes;

public class Layer {

    public int size;
    public double[] neurons;
    public double[] biases;
    public double[][] weights;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public double[] getBiases() {
        return biases;
    }

    public void setBiases(double[] biases) {
        this.biases = biases;
    }

    public double[] getNeurons() {
        return neurons;
    }

    public void setNeurons(double[] neurons) {
        this.neurons = neurons;
    }

    public double[][] getWeights() {
        return weights;
    }

    public void setWeights(double[][] weights) {
        this.weights = weights;
    }

    public Layer(int size, int nextSize) {
        this.size = size;
        neurons = new double[size];
        biases = new double[size];
        weights = new double[size][nextSize];
    }

    public Layer(){};
}
