package com.awdes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.*;

public class Main {

    static ConsoleFileHandler LOGGER = new ConsoleFileHandler();
    private final static String PATH = "NeuronXML.xml";


    public static void main(String[] args) throws IOException {
        menu();
    }
    private static void menu() throws IOException {
        LOGGER.publish(new LogRecord(Level.INFO,"Программа начала свою работу"));
        Scanner scanner = new Scanner(System.in);
        System.out.println("Выберите вариант:\n" +
                            "1. Обычное обучение;\n" +
                            "2. Десириализация;");
        int choice = scanner.nextInt();
        switch (choice){
            case 1:
                LOGGER.publish(new LogRecord(Level.INFO, "Было выбрано обычное обучение"));
                training();
                break;
            case 2:
                LOGGER.publish(new LogRecord(Level.INFO, "Была выбрана десериализация"));
                deserialization();
                break;
            default:
                System.out.println("Выберите 1 или 2");
                menu();
                break;
        }
    }

    private static void deserialization() throws IOException, NullPointerException { // Десериализация
        if (Files.exists(Paths.get(PATH))) {
            FileInputStream fileIS = new FileInputStream(PATH);
            XMLDecoder decoder = new XMLDecoder(fileIS);
            NeuralNetwork nn;
            nn = (NeuralNetwork) decoder.readObject();
            decoder.close();
            fileIS.close();
            FormDigits f = new FormDigits(nn);
            new Thread(f).start();
        }
        else {
            LOGGER.publish(new LogRecord(Level.WARNING,"Десериализация не удалась. Необходимого файла не существует."));
            menu();
        }
    }
    private static void training() throws IOException {
        boolean isWrite = true;
        LOGGER.publish(new LogRecord(Level.INFO,"Заполняем данные..."));
        NeuralNetwork nn = new NeuralNetwork(0.01, 784, 70, 70, 10);

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
        double[][] inputs = getInputs(samples, images);

        trainNeuralNetwork(nn, samples, digits, inputs);
        TestNeuralNetwork(nn); // Тест обученной сети
        // Сериализация
        serialization(isWrite, nn);
    }

    private static void serialization(boolean isWrite, NeuralNetwork nn) throws FileNotFoundException {
        if (!Files.exists(Paths.get(PATH)) || isWrite){
            LOGGER.publish(new LogRecord(Level.INFO,"Началась сериализация"));
            FileOutputStream fileOS = new FileOutputStream(PATH);
            XMLEncoder encoder = new XMLEncoder(fileOS);
            encoder.writeObject(nn);
            encoder.close();
            LOGGER.publish(new LogRecord(Level.INFO,"Сериализация закончилась"));
        }
    }

    private static void trainNeuralNetwork(NeuralNetwork nn, int samples, int[] digits, double[][] inputs) {
        LOGGER.publish(new LogRecord(Level.INFO,"Обучение началось!"));
        int epochs = 7000; // Количество эпох
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
            if(i % 1000 == 0){
                LOGGER.publish(new LogRecord(Level.INFO,"epoch: " + i + ". correct: " + right + ". error: " + errorSum));
            }
        }
        LOGGER.publish(new LogRecord(Level.INFO,"Обучение закончено!"));
    }

    private static double[][] getInputs(int samples, BufferedImage[] images) {
        double[][] inputs = new double[samples][784];
        for (int i = 0; i < samples; i++) {
            for (int x = 0; x < 28; x++) {
                for (int y = 0; y < 28; y++) {
                    inputs[i][x + y * 28] = (images[i].getRGB(x, y) & 0xff) / 255.0;
                }
            }
        } // Смотрим значения градаций серого в диапозоне от 0 для чёрных пикселей
        // и 1 для белых (активация нейрона) Это будет первый слой для нейросети
        return inputs;
    }

    private static void TestNeuralNetwork(NeuralNetwork nn) throws IOException {
        LOGGER.publish(new LogRecord(Level.INFO,"Начался тест с данными"));
        int samples = 10000;
        double[][] inputs;
        File[] imagesFiles;
        int[] digits;
        BufferedImage[] images;
        images = new BufferedImage[samples];
        digits = new int[samples];
        imagesFiles = new File("C:\\Users\\Sasha\\trainTest\\test").listFiles(); // Тренировочные данные
        for (int i = 0; i < samples; i++) {
            assert imagesFiles != null;
            images[i] = ImageIO.read(imagesFiles[i]);
            digits[i] = Integer.parseInt(imagesFiles[i].getName().charAt(10) + "");
        }

        inputs = getInputs(samples, images);
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
        LOGGER.publish(new LogRecord(Level.INFO,"correct: " + sumRight + ". uncorrected: " + (samples - sumRight) + ". Тест окончен"));
    }
}
