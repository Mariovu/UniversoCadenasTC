package com.example.universocadenas;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.FileReader;
public class CadenasBinarias {
    // Metodo para generar el universo de cadenas binarias de longitud k
    public static void generarUniversoAlArchivo(int k, String filename, int blockSize) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("{"); // Inicia el conjunto
            writer.write("\u03B5"); // Escribe la cadena vacía (ε)

            // Generar cadenas en bloques de tamaño blockSize
            for (int length = 1; length <= k; length++) {
                generarCadenasEnBloques(length, writer, blockSize);
            }
            writer.write("}"); // Cierra el conjunto
            System.out.println("Universo de cadenas generado y guardado en " + filename);
        } catch (IOException e) {
            System.err.println("Error al escribir en el archivo: " + e.getMessage());
        }
    }

    // Metodo para generar cadenas en bloques de memoria
    private static void generarCadenasEnBloques(int length, FileWriter writer, int TamBloque) throws IOException {
        int totalStrings = 1 << length; // 2^length
        int BloquesUsar = (totalStrings + TamBloque - 1) / TamBloque; // Calcular número óptimo de bloques de memoria

        for (int bloque = 0; bloque < BloquesUsar; bloque++) {
            int start = bloque * TamBloque;
            int end = Math.min(start + TamBloque, totalStrings);

            for (int i = start; i < end; i++) {
                String binaryString = String.format("%" + length + "s", Integer.toBinaryString(i)).replace(' ', '0');
                writer.write("," + binaryString); // Escribe la cadena separada por comas
            }
        }
    }

    // Clase para graficar
    public static class GraficaCadenasDeBits extends Application {

        private static final String FILE_PATH = "UniversoCadenas.txt"; // Ruta del archivo de cadenas
        private static boolean usarLog10 = false; // Indica si se debe usar log10
        private static int n = 0; // Longitud de las cadenas
        private static boolean graficaActiva = false; // Indica si la gráfica está activa

        @Override
        public void start(Stage primaryStage) {
            graficaActiva = true; // Marcar la grafica como activa

            // Configurar los ejes
            NumberAxis xAxis = new NumberAxis();
            xAxis.setLabel("Índice de la cadena");

            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel(usarLog10 ? "log10(Cantidad de 1s)" : "Cantidad de 1s");

            // Crear la grafica
            ScatterChart<Number, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);
            scatterChart.setTitle(usarLog10 ? "log10(Cantidad de 1s) en las cadenas binarias" : "Cantidad de 1s en las cadenas binarias");

            // Crear una serie de datos
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(usarLog10 ? "log10(Cantidad de 1s)" : "Cantidad de 1s");

            // Leer el archivo en bloques y procesar las cadenas
            try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
                StringBuilder buffer = new StringBuilder();
                char[] charBuffer = new char[8192]; // Buffer de 8 KB
                int charsRead;
                int index = 0;
                int totalStrings = 0;

                // Leer el archivo en bloques
                while ((charsRead = reader.read(charBuffer)) != -1) {
                    buffer.append(charBuffer, 0, charsRead);

                    // Procesar las cadenas en el buffer
                    int commaIndex;
                    while ((commaIndex = buffer.indexOf(",")) != -1) {
                        String cadena = buffer.substring(0, commaIndex);
                        buffer.delete(0, commaIndex + 1);

                        // Ignorar la cadena vacía (ε)
                        if (cadena.equals("\u03B5")) continue;

                        // Contar la cantidad de 1s en la cadena
                        int countOnes = contarUnos(cadena);

                        // Solo graficar si la cadena tiene al menos un "1"
                        if (countOnes > 0) {
                            double yValue = usarLog10 ? Math.log10(countOnes) : countOnes;

                            // Aplicar muestreo solo si n es grande
                            if (n > 20) {
                                // Muestrear cada 1024 cadenas
                                if (totalStrings % (1 << 10) == 0) {
                                    series.getData().add(new XYChart.Data<>(index, yValue));
                                    index++;
                                }
                            } else {
                                // No aplicar muestreo para n pequeño
                                series.getData().add(new XYChart.Data<>(index, yValue));
                                index++;
                            }
                        }

                        totalStrings++;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Añadir la serie de datos a la gráfica
            scatterChart.getData().add(series);

            // Configurar la ventana
            Scene scene = new Scene(scatterChart, 800, 600);
            primaryStage.setTitle(usarLog10 ? "Gráfica de log10(Cantidad de 1s)" : "Gráfica de cadenas binarias");
            primaryStage.setScene(scene);
            primaryStage.show();

            // Manejar el cierre de la ventana
            primaryStage.setOnCloseRequest(event -> {
                graficaActiva = false; // Marcar la gráfica como inactiva
            });
        }

        // Método para contar la cantidad de 1s en una cadena
        private int contarUnos(String binaryString) {
            int count = 0;
            for (char c : binaryString.toCharArray()) {
                if (c == '1') count++;
            }
            return count;
        }

        // Método para lanzar la gráfica desde el programa principal
        public static void launchGrafica(boolean useLog10, int n) {
            if (graficaActiva) {
                System.out.println("La gráfica ya está activa. Cierre la ventana actual antes de abrir una nueva.");
                return;
            }

            GraficaCadenasDeBits.usarLog10 = useLog10;
            GraficaCadenasDeBits.n = n; // Pasar el valor de n a la clase de graficación
            new Thread(() -> Application.launch(GraficaCadenasDeBits.class)).start();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();
        boolean continuar = true;

        while (continuar) {
            // Declarar la variable n fuera del bloque if
            int n = 0;

            // Menú principal
            System.out.println("Seleccione el modo de operación:");
            System.out.println("1. Manual");
            System.out.println("2. Automático");
            System.out.println("3. Graficar Cantidad de \"1\"");
            System.out.println("4. Graficar Cantidad de \"1\" (log10)");
            System.out.print("Ingrese su opción (1, 2, 3 o 4): ");
            int opcion = scanner.nextInt();

            if (opcion == 1 || opcion == 2) {
                if (opcion == 1) {
                    // Modo manual: el usuario ingresa n
                    System.out.print("Ingrese el valor de n (0-1000): ");
                    n = scanner.nextInt();

                    if (n < 0 || n > 1000) {
                        System.out.println("Valor de n fuera del rango permitido.");
                        continue;
                    }
                } else if (opcion == 2) {
                    // Modo automático: generar n aleatorio
                    n = random.nextInt(1001); // Número aleatorio entre 0 y 1000
                    System.out.println("Generando cadenas para n = " + n);
                }

                String filename = "UniversoCadenas.txt";
                int blockSize = 10000; // Tamaño del bloque (ajusta según sea necesario)

                generarUniversoAlArchivo(n, filename, blockSize);
            } else if (opcion == 3) {
                // Graficar la cantidad de "1"
                System.out.println("Iniciando la gráfica...");
                GraficaCadenasDeBits.launchGrafica(false, n); // Pasar n como parámetro
            } else if (opcion == 4) {
                // Graficar la cantidad de "1" con log10
                System.out.println("Iniciando la gráfica con log10...");
                GraficaCadenasDeBits.launchGrafica(true, n); // Pasar n como parámetro
            } else {
                System.out.println("Opción no válida. Intente de nuevo.");
                continue;
            }

            // Preguntar si desea continuar
            System.out.print("¿Desea realizar otra operación? (s/n): ");
            String respuesta = scanner.next();
            if (!respuesta.equalsIgnoreCase("s")) {
                continuar = false;
            }
        }

        scanner.close();
    }
}