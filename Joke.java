import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

class Joke {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String path = scanner.nextLine();           //путь до папки
        String nameOfFile = scanner.nextLine();     //joke
        String fileExtension = scanner.nextLine();  //java

        File newDir = new File(path);
        Joke jokeDemo = new Joke();
        jokeDemo.processFilesFromFolder(newDir, nameOfFile, fileExtension);
    }

    private void processFilesFromFolder(File folder, String nameOfFile, String fileExtension) {
        createAFile(folder, nameOfFile, fileExtension);
        File[] folders = folder.listFiles();
        for (File directory : folders) {
            if (directory.isDirectory()) {
                createAFile(directory, nameOfFile, fileExtension);
                processFilesFromFolder(directory, nameOfFile, fileExtension);
            }
        }
    }

    private File writingToFile(File file, String nameOfFile) {
        try (BufferedWriter bufferedWriter =
                     Files.newBufferedWriter(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8)) {
            bufferedWriter.write("class " + nameOfFile + " {");
            bufferedWriter.newLine();
            bufferedWriter.write("\tpublic static void main(String[] args) {");
            bufferedWriter.newLine();
            bufferedWriter.write("\t\tSystem.out.println(\"hello world\");");
            bufferedWriter.newLine();
            bufferedWriter.write("\t{");
            bufferedWriter.newLine();
            bufferedWriter.write("{");
        } catch (Exception e) {
            e.getCause();
        }
        return file;
    }

    private void createAFile(File directory, String nameOfFile, String fileExtension) {
        File file = new File(directory.getAbsolutePath(), nameOfFile + "." + fileExtension);
        try {//записываем в файл код и создаём этот файл в директории
            boolean createdFile = writingToFile(file, nameOfFile).createNewFile();
        } catch (IOException e) {
            e.getCause();
        }
    }
}
