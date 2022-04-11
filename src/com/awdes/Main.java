package com.awdes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.function.UnaryOperator;

public class Main {

    public static void main(String[] args) throws IOException {
        digits();
        //test();
    }
    private static void test() throws IOException, NullPointerException { // Десериализация
        FileInputStream fileIS = new FileInputStream("Neuron.xml");
        XMLDecoder decoder = new XMLDecoder(fileIS);
        NeuralNetwork nn;
        nn = (NeuralNetwork) decoder.readObject();
        decoder.close();
        fileIS.close();

        TestNeuralNetwork(nn);
    }
    private static void digits() throws IOException {
        System.out.println("Обучение началось!");
        UnaryOperator<Double> sigmoid = x -> 1 / (1 + Math.exp(-x));
        UnaryOperator<Double> dsigmoid = y -> y * (1 - y);
        NeuralNetwork nn = new NeuralNetwork(0.001, sigmoid, dsigmoid, 784, 180, 120, 120, 10);

        int samples = 60000; // Количество изображений в дата сете
        BufferedImage[] images = new BufferedImage[samples];
        int[] digits = new int[samples];
        File[] imagesFiles = new File("C:\\Users\\Sasha\\train").listFiles(); // Тренировочный сет цифр
        for (int i = 0; i < samples; i++) {
            assert imagesFiles != null;{
                images[i] = ImageIO.read(imagesFiles[i]);}
            digits[i] = Integer.parseInt(imagesFiles[i].getName().charAt(10) + "");
        } // Добавление изображения числа в images (все изображения 28 на 28 пикселей)
          // Добавление числового значения изображения в digits(значение берётся с названия изображения)

        double[][] inputs = new double[samples][784];
        for (int i = 0; i < samples; i++) {
            for (int x = 0; x < 28; x++) {
                for (int y = 0; y < 28; y++) {
                    inputs[i][x + y * 28] = (images[i].getRGB(x, y) & 0xff) / 255.0;
                }
            }
        } // Смотрим значения градаций серого в диапозоне от 0 для чёрных пикселей
          // и 1 для белых (активация нейрона) Это будет первый слой для нейросети

        int epochs = 1400; // Количество эпох
        for (int i = 1; i < epochs; i++) { // Обучение нейросети
            int right = 0; // Количество верных предсказаний
            double errorSum = 0; // Функция ошибки
            int batchSize = 100; // Используется для оптимизации и ускорения обучения
            for (int j = 0; j < batchSize; j++) {
                int imgIndex = (int)(Math.random() * samples); // Используется для улучшения обучения
                double[] targets = new double[10];
                int digit = digits[imgIndex]; // Берём число для обучения
                targets[digit] = 1; // Цель для нейросети

                double[] outputs = nn.feedForward(inputs[imgIndex]);
                int maxDigit = 0;
                double maxDigitWeight = -1;
                for (int k = 0; k < 10; k++) {
                    if(outputs[k] > maxDigitWeight) {
                        maxDigitWeight = outputs[k];
                        maxDigit = k;
                    }
                }
                if(digit == maxDigit) right++;
                for (int k = 0; k < 10; k++) {
                    errorSum += (targets[k] - outputs[k]) * (targets[k] - outputs[k]); // ВЫсчитываем ошибку
                }
                nn.backpropagation(targets);
            }
            if(i % 100 == 0)
                System.out.println("epoch: " + i + ". correct: " + right + ". error: " + errorSum);
        }
        System.out.println("Обучение закончено!");

        // Сериализация
        FileOutputStream fileOS = new FileOutputStream("NeuronXML.xml");
        XMLEncoder encoder = new XMLEncoder(fileOS);
        encoder.writeObject(nn);
        encoder.close();


        TestNeuralNetwork(nn); // Тест обученной сети
    }

    private static void TestNeuralNetwork(NeuralNetwork nn) throws IOException {
        System.out.println("Начался тест с данными");
        int samples;
        double[][] inputs;
        File[] imagesFiles;
        int[] digits;
        BufferedImage[] images;
        samples = 10000;
        images = new BufferedImage[samples];
        digits = new int[samples];
        imagesFiles = new File("C:\\Users\\Sasha\\trainTest\\test").listFiles(); // Тренировочные данные
        for (int i = 0; i < samples; i++) {
            assert imagesFiles != null;
            images[i] = ImageIO.read(imagesFiles[i]);
            digits[i] = Integer.parseInt(imagesFiles[i].getName().charAt(10) + "");
        }

        inputs = new double[samples][784];
        for (int i = 0; i < samples; i++) {
            for (int x = 0; x < 28; x++) {
                for (int y = 0; y < 28; y++) {
                    inputs[i][x + y * 28] = (images[i].getRGB(x, y) & 0xff) / 255.0;
                }
            }
        }
        int sumRight = 0;

        for (int i = 0; i < samples; i++) {
            int digit = digits[i];
            double[] outputs = nn.feedForward(inputs[i]);
            int maxDigit = 0;
            double maxDigitWeight = -1;
            for (int k = 0; k < 10; k++) {
                if(outputs[k] > maxDigitWeight) {
                    maxDigitWeight = outputs[k];
                    maxDigit = k;
                }
            }
            if(digit == maxDigit) sumRight++;
        }
        System.out.println("correct: " + sumRight + ". Тест окончен");

    }
}
