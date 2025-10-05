import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.*;

public class InterpretePrincipal {
    
    // Listener personalizado para manejo de errores
    public static class MiErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                               int line, int charPositionInLine,
                               String msg, RecognitionException e) {
            System.err.println("Error de sintaxis en línea " + line + ", columna " + charPositionInLine + ": " + msg);
        }
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Uso: java InterpretePrincipal <archivo.es>");
            System.exit(1);
        }
        
        String nombreArchivo = args[0];
        
        try {
            // Leer el archivo de entrada
            String contenido = leerArchivo(nombreArchivo);
            
            // Ejecutar el programa
            ejecutarPrograma(contenido, nombreArchivo);
            
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error durante la interpretación: " + e.getMessage());
            System.exit(1);
        }
    }
    
    public static void ejecutarPrograma(String contenido, String nombreArchivo) {
        try {
            // Crear input stream
            ANTLRInputStream input = new ANTLRInputStream(contenido);
            
            // Crear lexer
            MilenguajeLexer lexer = new MilenguajeLexer(input);
            lexer.removeErrorListeners();
            lexer.addErrorListener(new MiErrorListener());
            
            // Crear token stream
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            
            // Crear parser
            MilenguajeParser parser = new MilenguajeParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new MiErrorListener());
            
            // Verificar si hay errores léxicos
            boolean hayErroresLexicos = false;
            for (Token token : tokens.getTokens()) {
                if (token.getType() == MilenguajeLexer.ERROR_CHAR) {
                    System.err.println("Error léxico en línea " + token.getLine() + 
                                     ", columna " + token.getCharPositionInLine() + 
                                     ": carácter no reconocido '" + token.getText() + "'");
                    hayErroresLexicos = true;
                }
            }
            
            if (hayErroresLexicos) {
                System.err.println("El programa contiene errores léxicos y no puede ser interpretado.");
                return;
            }
            
            // Parsear el programa
            ParseTree tree = parser.programa();
            
            // Verificar si hay errores de sintaxis
            if (parser.getNumberOfSyntaxErrors() > 0) {
                System.err.println("El programa contiene errores de sintaxis y no puede ser interpretado.");
                return;
            }
            
            // Crear y ejecutar el evaluador semántico
            EvaluadorSemantico evaluador = new EvaluadorSemantico();
            
            System.out.println("=== Ejecutando programa: " + nombreArchivo + " ===\n");
            
            evaluador.visit(tree);
            
            System.out.println("\n=== Fin de la ejecución ===");
            
        } catch (RuntimeException e) {
            System.err.println("Error de ejecución: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String leerArchivo(String nombreArchivo) throws IOException {
        StringBuilder contenido = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(nombreArchivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                contenido.append(linea).append("\n");
            }
        }
        
        return contenido.toString();
    }
    
    // Método para ejecutar código directamente desde String (útil para pruebas)
    public static void ejecutarCodigo(String codigo) {
        ejecutarPrograma(codigo, "programa_en_memoria");
    }
    
    // Método para modo interactivo
    public static void modoInteractivo() {
        System.out.println("=== Intérprete EspañolScript - Modo Interactivo ===");
        System.out.println("Escribe 'salir' para terminar\n");
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        while (true) {
            try {
                System.out.print(">>> ");
                String linea = reader.readLine();
                
                if (linea == null || linea.trim().equals("salir")) {
                    System.out.println("¡Hasta luego!");
                    break;
                }
                
                if (linea.trim().isEmpty()) {
                    continue;
                }
                
                // Ejecutar la línea como un programa completo
                ejecutarCodigo(linea);
                
            } catch (IOException e) {
                System.err.println("Error de entrada/salida: " + e.getMessage());
                break;
            }
        }
    }
}
