/*
* Написать программу которая на вход принимает путь до файлика формата data.csv и число максимального потребления
* Программа должна в новый файлик рядом с входным вывести информацию о всех экологичных пользователях
* Экологичным считается тот, кто каждый природный ресурс потребляет меньше изначально заданного числа
* */

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

public class EcoPeople {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String pathToFile = scanner.nextLine();
        String ecoNumberScanner = scanner.nextLine();   //критерий оценки экологичности

        File newDir = new File(pathToFile);
        String pathToFolder = newDir.getParent();
        int ecoNumber = Integer.parseInt(ecoNumberScanner);

        EcoPeople ecoPeople = new EcoPeople();
        ecoPeople.createAFile(pathToFile, pathToFolder, ecoNumber);
    }

    private StringBuilder dataFromFile(String pathToFile) {
        StringBuilder dataFromFile = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                new FileInputStream(pathToFile), StandardCharsets.UTF_8))) {
            String row;
            while ((row = bufferedReader.readLine()) != null) {
                dataFromFile.append(row);
                dataFromFile.append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return dataFromFile;
    }

    private String[][] createMatrixOfInputFileData(String pathToFile) {
        StringBuilder dataFromFile = dataFromFile(pathToFile);
        return createMatrix(dataFromFile);
    }

    private String[][] createMatrix(StringBuilder data) {
        String[] dataFromFileToStringArray = data.toString().split("\n");
        String[][] dataFromFileToStringMatrix = new String[dataFromFileToStringArray.length][];
        for (int i = 0; i < dataFromFileToStringArray.length; i++) {
            dataFromFileToStringMatrix[i] = dataFromFileToStringArray[i].split("\\|");
        }
        return dataFromFileToStringMatrix;
    }

    private int[][] identicalAccounts(String[][] dataMatrix) {
        StringBuilder doubleCount = new StringBuilder();
        for (int i = 0; i < dataMatrix[0].length; i++) {
            for (int j = i + 1; j < dataMatrix[0].length; j++) {
                if (dataMatrix[0][i].substring(0, dataMatrix[0][i].length() - 1).equals(
                        dataMatrix[0][j].substring(0, dataMatrix[0][j].length() - 1))) {
                    doubleCount.append(i).append("|").append(j).append("\n");
                }
            }
        }
        String[][] doubleCountMatrix = createMatrix(doubleCount);
        int[][] numericalSum = new int[doubleCountMatrix.length][doubleCountMatrix[0].length];
        for (int i = 0; i < doubleCountMatrix.length; i++) {
            for (int j = 0; j < doubleCountMatrix[0].length; j++) {
                numericalSum[i][j] = Integer.parseInt(doubleCountMatrix[i][j]);
            }
        }
        return numericalSum;
    }

    private int[] numericalColumns(String[][] dataMatrix) {  //этот метод нужен, чтоб выбрать
        //только числа из исходной матрицы
        StringBuilder countOfColumns = new StringBuilder();
        int lenOfNumMatrix = 0;
        for (int i = 0; i < dataMatrix[0].length; i++) {
            if (dataMatrix[0][i].contains("Count")) {
                countOfColumns.append(i);
                lenOfNumMatrix++;
                if (i != dataMatrix[0].length - 1) {    //не добавляем в самый конец пробел
                    countOfColumns.append(" ");
                }
            }
        }
        int[] columnsArray = new int[lenOfNumMatrix];
        for (int i = 0; i < lenOfNumMatrix; i++) {
            columnsArray[i] = Integer.parseInt(countOfColumns.toString().split("\\s")[i]);
        }
        return columnsArray;
    }

    private int[][] numericalMatrix(String[][] dataMatrix) {
        int[][] numericalData = new int[dataMatrix.length - 1][numericalColumns(dataMatrix).length];
        for (int i = 1; i < dataMatrix.length; i++) {   //пропускаем шапку
            for (int j = 0; j < numericalColumns(dataMatrix).length; j++) {
                numericalData[i - 1][j] = Integer.parseInt(dataMatrix[i][numericalColumns(dataMatrix)[0] + j]);
            }
        }
        return numericalData;
    }

    private int[][] totalScore(String[][] dataMatrix) {
        int[][] numericalSum = new int[numericalMatrix(dataMatrix).length]
                        [numericalMatrix(dataMatrix)[0].length];    //создаём форматированную матрицу, где общие Count
                                                                    //суммируются и перемещаюся влевый столбец, если
                                                                    //до этого есть нулевые столбцы.
        int add = 0;
        for (int i = 0; i < numericalSum.length; i++) {
            for (int j = 0; j < numericalSum[0].length; j++) {
                for (int k = 0; k < identicalAccounts(dataMatrix).length; k++) {
                    if (j == identicalAccounts(dataMatrix)[k][identicalAccounts(dataMatrix)[k].length - 1] - 2) {
                        numericalSum[i][j - 1] += numericalMatrix(dataMatrix)[i][j];
                        if (add > 0) {
                            numericalSum[i][j - 2] = numericalSum[i][j - 1];
                            numericalSum[i][j - 1] = 0;
                        }
                        numericalSum[i][j] = 0;
                        if (j != numericalSum[0].length - 1) {
                            add++;
                        } else {
                            add = 0;
                        }
                    } else {
                        numericalSum[i][j] = numericalMatrix(dataMatrix)[i][j];
                    }
                }
            }
        }
        int[][] newNumericalSum = new int[numericalSum.length][];   //Обрезаем нулевые столбцы
        for (int i = 0; i < numericalSum.length; i++) {
            newNumericalSum[i] = Arrays.copyOfRange(numericalSum[i], 0,
                    numericalMatrix(dataMatrix)[0].length - identicalAccounts(dataMatrix).length);
        }
        return newNumericalSum;
    }

    private boolean[] ecoPeople(String[][] dataMatrix, int ecoNumber) {
        boolean[] ecoPeople = new boolean[totalScore(dataMatrix).length];
        for (int i = 0; i < totalScore(dataMatrix).length; i++) {
            for (int j = 0; j < totalScore(dataMatrix)[0].length; j++) {
                if (totalScore(dataMatrix)[i][j] > ecoNumber) {
                    ecoPeople[i] = false;
                    break;
                } else {
                    ecoPeople[i] = true;
                }
            }
        }
        return ecoPeople;
    }

    private File writingToFile(File file, String pathToFile, int ecoNumber) {
        try (BufferedWriter bufferedWriter =
                     Files.newBufferedWriter(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8)) {
            String[] dataFromFileToStringArray = dataFromFile(pathToFile).toString().split("\n");
            bufferedWriter.write(dataFromFileToStringArray[0]);
            bufferedWriter.newLine();
            for (int i = 0; i < dataFromFileToStringArray.length - 1; i++) {
                if (ecoPeople(createMatrixOfInputFileData(pathToFile), ecoNumber)[i]) {
                    bufferedWriter.write(dataFromFileToStringArray[i + 1]);
                    bufferedWriter.newLine();
                }
            }
        } catch (Exception e) {
            e.getCause();
        }
        return file;
    }

    private void createAFile(String pathToFile, String directory, int ecoNumber) {
        File file = new File(directory, "EcoPeople.csv");
        try {
            boolean createdFile =
                    writingToFile(file, pathToFile, ecoNumber).createNewFile();  //записываем в файл код и создаём
                                                                                //этот файл в директории
            if (createdFile) {
                System.out.println("File has been created");
            }
        } catch (IOException e) {
            e.getCause();
        }
    }
}
