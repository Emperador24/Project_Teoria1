import java.util.*;
import java.io.*;

public class EvaluadorSemantico extends MilenguajeBaseVisitor<Object> {
    
    // Tabla de símbolos para variables
    private Map<String, Variable> tablaSimbolos = new HashMap<>();
    
    // Tabla de símbolos para funciones
    private Map<String, Funcion> tablaFunciones = new HashMap<>();
    
    // Pila para el manejo de ámbitos (scopes)
    private Stack<Map<String, Variable>> pilaAmbitos = new Stack<>();
    
    // Control de flujo
    private boolean debeRetornar = false;
    private Object valorRetorno = null;
    private boolean debeRomper = false;
    private boolean debeContinuar = false;
    
    // Scanner global para entrada
    private static Scanner scanner = new Scanner(System.in);
    
    // Clase para representar variables
    public static class Variable {
        String tipo;
        Object valor;
        boolean inicializada;
        
        public Variable(String tipo, Object valor, boolean inicializada) {
            this.tipo = tipo;
            this.valor = valor;
            this.inicializada = inicializada;
        }
    }
    
    // Clase para representar funciones
    public static class Funcion {
        String nombre;
        List<String> tiposParametros;
        List<String> nombresParametros;
        MilenguajeParser.BloqueContext cuerpo;
        
        public Funcion(String nombre, List<String> tipos, List<String> nombres, MilenguajeParser.BloqueContext cuerpo) {
            this.nombre = nombre;
            this.tiposParametros = tipos;
            this.nombresParametros = nombres;
            this.cuerpo = cuerpo;
        }
    }
    
    // Inicializar nuevo ámbito
    private void iniciarAmbito() {
        pilaAmbitos.push(new HashMap<>());
    }
    
    // Terminar ámbito actual
    private void terminarAmbito() {
        if (!pilaAmbitos.isEmpty()) {
            pilaAmbitos.pop();
        }
    }
    
    // Buscar variable en todos los ámbitos
    private Variable buscarVariable(String nombre) {
        // Buscar en ámbitos locales (pila)
        for (int i = pilaAmbitos.size() - 1; i >= 0; i--) {
            if (pilaAmbitos.get(i).containsKey(nombre)) {
                return pilaAmbitos.get(i).get(nombre);
            }
        }
        // Buscar en ámbito global
        return tablaSimbolos.get(nombre);
    }
    
    // Declarar variable en el ámbito actual
    private void declararVariable(String nombre, Variable variable) {
        if (!pilaAmbitos.isEmpty()) {
            pilaAmbitos.peek().put(nombre, variable);
        } else {
            tablaSimbolos.put(nombre, variable);
        }
    }
    
    @Override
    public Object visitPrograma(MilenguajeParser.ProgramaContext ctx) {
        try {
            for (MilenguajeParser.DeclaracionContext decl : ctx.declaracion()) {
                if (debeRetornar || debeRomper || debeContinuar) break;
                visit(decl);
            }
            return null;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error en el programa: " + e.getMessage());
        }
    }
    
    @Override
    public Object visitDeclaracionVariable(MilenguajeParser.DeclaracionVariableContext ctx) {
        String tipo = ctx.tipo().getText();
        String nombre = ctx.IDENTIFICADOR().getText();
        
        // Verificar si ya existe en el ámbito actual
        if ((!pilaAmbitos.isEmpty() && pilaAmbitos.peek().containsKey(nombre)) || 
            (pilaAmbitos.isEmpty() && tablaSimbolos.containsKey(nombre))) {
            throw new RuntimeException("Variable '" + nombre + "' ya está declarada");
        }
        
        Object valor = null;
        boolean inicializada = false;
        
        if (ctx.expresion() != null) {
            valor = visit(ctx.expresion());
            inicializada = true;
            
            // Convertir el valor al tipo correcto si es necesario
            valor = convertirATipo(tipo, valor);
            
            // Verificar compatibilidad de tipos
            if (!esCompatibleTipo(tipo, valor)) {
                throw new RuntimeException("Tipo incompatible para variable '" + nombre + "'. Esperado: " + tipo + ", recibido: " + obtenerTipo(valor));
            }
        }
        
        declararVariable(nombre, new Variable(tipo, valor, inicializada));
        return null;
    }
    
    @Override
    public Object visitDeclaracionFuncion(MilenguajeParser.DeclaracionFuncionContext ctx) {
        String nombre = ctx.IDENTIFICADOR().getText();
        
        if (tablaFunciones.containsKey(nombre)) {
            throw new RuntimeException("Función '" + nombre + "' ya está declarada");
        }
        
        List<String> tiposParametros = new ArrayList<>();
        List<String> nombresParametros = new ArrayList<>();
        
        if (ctx.parametros() != null) {
            for (int i = 0; i < ctx.parametros().tipo().size(); i++) {
                tiposParametros.add(ctx.parametros().tipo(i).getText());
                nombresParametros.add(ctx.parametros().IDENTIFICADOR(i).getText());
            }
        }
        
        tablaFunciones.put(nombre, new Funcion(nombre, tiposParametros, nombresParametros, ctx.bloque()));
        return null;
    }
    
    @Override
    public Object visitSentenciaSi(MilenguajeParser.SentenciaSiContext ctx) {
        Object condicion = visit(ctx.expresion());
        
        if (!(condicion instanceof Boolean)) {
            throw new RuntimeException("La condición del 'si' debe ser booleana");
        }
        
        if ((Boolean) condicion) {
            visit(ctx.sentencia(0));
        } else if (ctx.sentencia().size() > 1) {
            visit(ctx.sentencia(1));
        }
        
        return null;
    }
    
    @Override
    public Object visitSentenciaPara(MilenguajeParser.SentenciaParaContext ctx) {
        iniciarAmbito();
        
        try {
            // Inicialización
            visit(ctx.declaracionVariable());
            
            while (true) {
                // Condición
                Object condicion = visit(ctx.expresion(0));
                if (!(condicion instanceof Boolean) || !(Boolean) condicion) {
                    break;
                }
                
                // Cuerpo del bucle
                visit(ctx.sentencia());
                
                if (debeRomper) {
                    debeRomper = false;
                    break;
                }
                
                if (debeContinuar) {
                    debeContinuar = false;
                }
                
                if (debeRetornar) {
                    break;
                }
                
                // Incremento
                visit(ctx.expresion(1));
            }
        } finally {
            terminarAmbito();
        }
        
        return null;
    }
    
    @Override
    public Object visitSentenciaMientras(MilenguajeParser.SentenciaMientrasContext ctx) {
        while (true) {
            Object condicion = visit(ctx.expresion());
            
            if (!(condicion instanceof Boolean) || !(Boolean) condicion) {
                break;
            }
            
            visit(ctx.sentencia());
            
            if (debeRomper) {
                debeRomper = false;
                break;
            }
            
            if (debeContinuar) {
                debeContinuar = false;
                continue;
            }
            
            if (debeRetornar) {
                break;
            }
        }
        
        return null;
    }
    
    @Override
    public Object visitSentenciaRetornar(MilenguajeParser.SentenciaRetornarContext ctx) {
        if (ctx.expresion() != null) {
            valorRetorno = visit(ctx.expresion());
        } else {
            valorRetorno = null;
        }
        debeRetornar = true;
        return null;
    }
    
    @Override
    public Object visitSentenciaImprimir(MilenguajeParser.SentenciaImprimirContext ctx) {
        Object valor = visit(ctx.expresion());
        System.out.println(convertirAString(valor));
        return null;
    }
    
    @Override
    public Object visitSentenciaRomper(MilenguajeParser.SentenciaRomperContext ctx) {
        debeRomper = true;
        return null;
    }
    
    @Override
    public Object visitSentenciaContinuar(MilenguajeParser.SentenciaContinuarContext ctx) {
        debeContinuar = true;
        return null;
    }
    
    @Override
    public Object visitAsignacion(MilenguajeParser.AsignacionContext ctx) {
        if (ctx.IDENTIFICADOR() != null) {
            // Es una asignación
            String nombre = ctx.IDENTIFICADOR().getText();
            Object valor = visit(ctx.asignacion());
            
            Variable variable = buscarVariable(nombre);
            if (variable == null) {
                throw new RuntimeException("Variable '" + nombre + "' no está declarada");
            }
            
            // Convertir el valor al tipo correcto
            valor = convertirATipo(variable.tipo, valor);
            
            if (!esCompatibleTipo(variable.tipo, valor)) {
                throw new RuntimeException("Tipo incompatible para asignación a '" + nombre + "'. Esperado: " + variable.tipo + ", recibido: " + obtenerTipo(valor));
            }
            
            variable.valor = valor;
            variable.inicializada = true;
            return valor;
        } else {
            // No es asignación, continuar con expresionLogicaO
            return visit(ctx.expresionLogicaO());
        }
    }
    
    @Override
    public Object visitExpresionLogicaO(MilenguajeParser.ExpresionLogicaOContext ctx) {
        Object izquierda = visit(ctx.expresionLogicaY(0));
        
        for (int i = 1; i < ctx.expresionLogicaY().size(); i++) {
            if (!(izquierda instanceof Boolean)) {
                throw new RuntimeException("Operador 'o' requiere operandos booleanos");
            }
            
            if ((Boolean) izquierda) {
                return true; // Short-circuit evaluation
            }
            
            Object derecha = visit(ctx.expresionLogicaY(i));
            if (!(derecha instanceof Boolean)) {
                throw new RuntimeException("Operador 'o' requiere operandos booleanos");
            }
            
            izquierda = (Boolean) izquierda || (Boolean) derecha;
        }
        
        return izquierda;
    }
    
    @Override
    public Object visitExpresionLogicaY(MilenguajeParser.ExpresionLogicaYContext ctx) {
        Object izquierda = visit(ctx.expresionIgualdad(0));
        
        for (int i = 1; i < ctx.expresionIgualdad().size(); i++) {
            if (!(izquierda instanceof Boolean)) {
                throw new RuntimeException("Operador 'y' requiere operandos booleanos");
            }
            
            if (!(Boolean) izquierda) {
                return false; // Short-circuit evaluation
            }
            
            Object derecha = visit(ctx.expresionIgualdad(i));
            if (!(derecha instanceof Boolean)) {
                throw new RuntimeException("Operador 'y' requiere operandos booleanos");
            }
            
            izquierda = (Boolean) izquierda && (Boolean) derecha;
        }
        
        return izquierda;
    }
    
    @Override
    public Object visitExpresionIgualdad(MilenguajeParser.ExpresionIgualdadContext ctx) {
        Object izquierda = visit(ctx.expresionRelacional(0));
        
        for (int i = 0; i < ctx.getChildCount(); i += 2) {
            if (i + 1 < ctx.getChildCount() && i + 2 < ctx.getChildCount()) {
                String operador = ctx.getChild(i + 1).getText();
                Object derecha = visit(ctx.expresionRelacional((i / 2) + 1));
                
                switch (operador) {
                    case "==":
                        izquierda = esIgual(izquierda, derecha);
                        break;
                    case "!=":
                        izquierda = !esIgual(izquierda, derecha);
                        break;
                }
            }
        }
        
        return izquierda;
    }
    
    @Override
    public Object visitExpresionRelacional(MilenguajeParser.ExpresionRelacionalContext ctx) {
        Object izquierda = visit(ctx.expresionAritmetica(0));
        
        for (int i = 0; i < ctx.getChildCount(); i += 2) {
            if (i + 1 < ctx.getChildCount() && i + 2 < ctx.getChildCount()) {
                String operador = ctx.getChild(i + 1).getText();
                Object derecha = visit(ctx.expresionAritmetica((i / 2) + 1));
                
                izquierda = aplicarOperadorRelacional(operador, izquierda, derecha);
            }
        }
        
        return izquierda;
    }
    
    @Override
    public Object visitExpresionAritmetica(MilenguajeParser.ExpresionAritmeticaContext ctx) {
        Object izquierda = visit(ctx.expresionPotencia(0));
        
        for (int i = 0; i < ctx.getChildCount(); i += 2) {
            if (i + 1 < ctx.getChildCount() && i + 2 < ctx.getChildCount()) {
                String operador = ctx.getChild(i + 1).getText();
                Object derecha = visit(ctx.expresionPotencia((i / 2) + 1));
                
                izquierda = aplicarOperadorAritmetico(operador, izquierda, derecha);
            }
        }
        
        return izquierda;
    }
    
    @Override
    public Object visitExpresionPotencia(MilenguajeParser.ExpresionPotenciaContext ctx) {
        Object izquierda = visit(ctx.expresionMultiplicativa(0));
        
        if (ctx.expresionMultiplicativa().size() > 1) {
            Object derecha = visit(ctx.expresionMultiplicativa(1));
            return aplicarOperadorAritmetico("^", izquierda, derecha);
        }
        
        return izquierda;
    }
    
    @Override
    public Object visitExpresionMultiplicativa(MilenguajeParser.ExpresionMultiplicativaContext ctx) {
        Object izquierda = visit(ctx.expresionUnaria(0));
        
        for (int i = 0; i < ctx.getChildCount(); i += 2) {
            if (i + 1 < ctx.getChildCount() && i + 2 < ctx.getChildCount()) {
                String operador = ctx.getChild(i + 1).getText();
                Object derecha = visit(ctx.expresionUnaria((i / 2) + 1));
                
                izquierda = aplicarOperadorAritmetico(operador, izquierda, derecha);
            }
        }
        
        return izquierda;
    }
    
    @Override
    public Object visitExpresionUnaria(MilenguajeParser.ExpresionUnariaContext ctx) {
        if (ctx.getChildCount() == 2) {
            String operador = ctx.getChild(0).getText();
            Object operando = visit(ctx.expresionUnaria());
            
            switch (operador) {
                case "no":
                    if (!(operando instanceof Boolean)) {
                        throw new RuntimeException("Operador 'no' requiere operando booleano");
                    }
                    return !(Boolean) operando;
                case "-":
                    if (operando instanceof Integer) {
                        return -(Integer) operando;
                    } else if (operando instanceof Double) {
                        return -(Double) operando;
                    } else if (operando instanceof Long) {
                        return -(Long) operando;
                    } else {
                        throw new RuntimeException("Operador '-' requiere operando numérico");
                    }
                case "+":
                    if (operando instanceof Integer || operando instanceof Double || operando instanceof Long) {
                        return operando;
                    } else {
                        throw new RuntimeException("Operador '+' requiere operando numérico");
                    }
                default:
                    throw new RuntimeException("Operador unario desconocido: " + operador);
            }
        } else {
            return visit(ctx.expresionPrimaria());
        }
    }
    
    @Override
    public Object visitExpresionPrimaria(MilenguajeParser.ExpresionPrimariaContext ctx) {
        if (ctx.NUMERO_ENTERO() != null) {
            String numeroTexto = ctx.NUMERO_ENTERO().getText();
            try {
                // Intentar parsear como Integer primero
                return Integer.parseInt(numeroTexto);
            } catch (NumberFormatException e) {
                // Si es muy grande para Integer, usar Long
                return Long.parseLong(numeroTexto);
            }
        } else if (ctx.NUMERO_DECIMAL() != null) {
            return Double.parseDouble(ctx.NUMERO_DECIMAL().getText());
        } else if (ctx.CADENA() != null) {
            String texto = ctx.CADENA().getText();
            return texto.substring(1, texto.length() - 1); // Remover comillas
        } else if (ctx.getText().equals("verdadero")) {
            return true;
        } else if (ctx.getText().equals("falso")) {
            return false;
        } else if (ctx.IDENTIFICADOR() != null) {
            String nombre = ctx.IDENTIFICADOR().getText();
            Variable variable = buscarVariable(nombre);
            
            if (variable == null) {
                throw new RuntimeException("Variable '" + nombre + "' no está declarada");
            }
            
            if (!variable.inicializada) {
                throw new RuntimeException("Variable '" + nombre + "' no está inicializada");
            }
            
            return variable.valor;
        } else if (ctx.llamadaFuncion() != null) {
            return visit(ctx.llamadaFuncion());
        } else if (ctx.expresionLeer() != null) {
            return visit(ctx.expresionLeer());
        } else if (ctx.expresion() != null) {
            return visit(ctx.expresion());
        }
        
        throw new RuntimeException("Expresión primaria no reconocida");
    }
    
    @Override
    public Object visitLlamadaFuncion(MilenguajeParser.LlamadaFuncionContext ctx) {
        String nombre = ctx.IDENTIFICADOR().getText();
        Funcion funcion = tablaFunciones.get(nombre);
        
        if (funcion == null) {
            throw new RuntimeException("Función '" + nombre + "' no está declarada");
        }
        
        List<Object> argumentos = new ArrayList<>();
        if (ctx.argumentos() != null) {
            for (MilenguajeParser.ExpresionContext expr : ctx.argumentos().expresion()) {
                argumentos.add(visit(expr));
            }
        }
        
        if (argumentos.size() != funcion.tiposParametros.size()) {
            throw new RuntimeException("Número incorrecto de argumentos para función '" + nombre + "'");
        }
        
        // Verificar tipos de argumentos
        for (int i = 0; i < argumentos.size(); i++) {
            Object argumento = convertirATipo(funcion.tiposParametros.get(i), argumentos.get(i));
            if (!esCompatibleTipo(funcion.tiposParametros.get(i), argumento)) {
                throw new RuntimeException("Argumento " + (i + 1) + " de función '" + nombre + "' tiene tipo incorrecto");
            }
            argumentos.set(i, argumento);
        }
        
        // Ejecutar función
        return ejecutarFuncion(funcion, argumentos);
    }
    
    @Override
    public Object visitExpresionLeer(MilenguajeParser.ExpresionLeerContext ctx) {
        return scanner.nextLine();
    }
    
    @Override
    public Object visitBloque(MilenguajeParser.BloqueContext ctx) {
        iniciarAmbito();
        
        try {
            for (MilenguajeParser.DeclaracionContext decl : ctx.declaracion()) {
                if (debeRetornar || debeRomper || debeContinuar) break;
                visit(decl);
            }
        } finally {
            terminarAmbito();
        }
        
        return null;
    }
    
    // Métodos auxiliares
    
    private Object ejecutarFuncion(Funcion funcion, List<Object> argumentos) {
        // Guardar estado actual
        boolean retornoAnterior = debeRetornar;
        Object valorRetornoAnterior = valorRetorno;
        
        debeRetornar = false;
        valorRetorno = null;
        
        iniciarAmbito();
        
        try {
            // Declarar parámetros en nuevo ámbito
            for (int i = 0; i < funcion.nombresParametros.size(); i++) {
                String nombreParam = funcion.nombresParametros.get(i);
                String tipoParam = funcion.tiposParametros.get(i);
                Object valorParam = argumentos.get(i);
                
                declararVariable(nombreParam, new Variable(tipoParam, valorParam, true));
            }
            
            // Ejecutar cuerpo de la función
            visit(funcion.cuerpo);
            
            Object resultado = valorRetorno;
            
            // Restaurar estado
            debeRetornar = retornoAnterior;
            valorRetorno = valorRetornoAnterior;
            
            return resultado;
            
        } finally {
            terminarAmbito();
        }
    }
    
    private Object convertirATipo(String tipoEsperado, Object valor) {
        if (valor == null) return null;
        
        switch (tipoEsperado) {
            case "entero":
                if (valor instanceof Integer) return valor;
                if (valor instanceof Long) {
                    Long longVal = (Long) valor;
                    if (longVal >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE) {
                        return longVal.intValue();
                    } else {
                        throw new RuntimeException("Valor long fuera del rango de entero: " + longVal);
                    }
                }
                if (valor instanceof Double) {
                    return ((Double) valor).intValue();
                }
                break;
                
            case "largo":
                if (valor instanceof Long) return valor;
                if (valor instanceof Integer) return ((Integer) valor).longValue();
                if (valor instanceof Double) return ((Double) valor).longValue();
                break;
                
            case "decimal":
                if (valor instanceof Double) return valor;
                if (valor instanceof Integer) return ((Integer) valor).doubleValue();
                if (valor instanceof Long) return ((Long) valor).doubleValue();
                break;
                
            case "booleano":
                if (valor instanceof Boolean) return valor;
                break;
                
            case "cadena":
                if (valor instanceof String) return valor;
                break;
        }
        
        return valor;
    }
    
    private boolean esCompatibleTipo(String tipoEsperado, Object valor) {
        if (valor == null) return true;
        
        switch (tipoEsperado) {
            case "entero":
                return valor instanceof Integer;
            case "decimal":
                return valor instanceof Double;
            case "booleano":
                return valor instanceof Boolean;
            case "cadena":
                return valor instanceof String;
            case "largo":
                return valor instanceof Long;
            default:
                return false;
        }
    }
    
    private String obtenerTipo(Object valor) {
        if (valor == null) return "null";
        if (valor instanceof Integer) return "entero";
        if (valor instanceof Long) return "largo";
        if (valor instanceof Double) return "decimal";
        if (valor instanceof Boolean) return "booleano";
        if (valor instanceof String) return "cadena";
        return valor.getClass().getSimpleName();
    }
    
    private boolean esIgual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        
        // Comparación especial para números
        if (esNumerico(a) && esNumerico(b)) {
            double aDouble = convertirADouble(a);
            double bDouble = convertirADouble(b);
            return aDouble == bDouble;
        }
        
        return a.equals(b);
    }
    
    private boolean esNumerico(Object valor) {
        return valor instanceof Integer || valor instanceof Long || valor instanceof Double;
    }
    
    private Object aplicarOperadorRelacional(String operador, Object izquierda, Object derecha) {
        if (esNumerico(izquierda) && esNumerico(derecha)) {
            double a = convertirADouble(izquierda);
            double b = convertirADouble(derecha);
            
            switch (operador) {
                case "<": return a < b;
                case "<=": return a <= b;
                case ">": return a > b;
                case ">=": return a >= b;
            }
        }
        
        throw new RuntimeException("Operador '" + operador + "' no aplicable a estos tipos");
    }
    
    private Object aplicarOperadorAritmetico(String operador, Object izquierda, Object derecha) {
        // Concatenación de cadenas
        if (operador.equals("+") && (izquierda instanceof String || derecha instanceof String)) {
            return convertirAString(izquierda) + convertirAString(derecha);
        }
        
        // Operaciones numéricas
        if (esNumerico(izquierda) && esNumerico(derecha)) {
            // Determinar el tipo de resultado basado en los operandos
            boolean hayDouble = (izquierda instanceof Double || derecha instanceof Double);
            boolean hayLong = (izquierda instanceof Long || derecha instanceof Long);
            
            if (hayDouble) {
                // Si hay algún double, el resultado es double
                double a = convertirADouble(izquierda);
                double b = convertirADouble(derecha);
                
                switch (operador) {
                    case "+": return a + b;
                    case "-": return a - b;
                    case "*": return a * b;
                    case "/": 
                        if (b == 0) throw new RuntimeException("División por cero");
                        return a / b;
                    case "%": 
                        if (b == 0) throw new RuntimeException("División por cero en módulo");
                        return a % b;
                    case "^": return Math.pow(a, b);
                }
            } else if (hayLong) {
                // Si hay algún long, el resultado es long
                long a = convertirALong(izquierda);
                long b = convertirALong(derecha);
                
                switch (operador) {
                    case "+": return a + b;
                    case "-": return a - b;
                    case "*": return a * b;
                    case "/": 
                        if (b == 0) throw new RuntimeException("División por cero");
                        return a / b;
                    case "%": 
                        if (b == 0) throw new RuntimeException("División por cero en módulo");
                        return a % b;
                    case "^": return (long) Math.pow(a, b);
                }
            } else {
                // Solo integers, el resultado es integer
                int a = (Integer) izquierda;
                int b = (Integer) derecha;
                
                switch (operador) {
                    case "+": return a + b;
                    case "-": return a - b;
                    case "*": return a * b;
                    case "/": 
                        if (b == 0) throw new RuntimeException("División por cero");
                        return a / b;
                    case "%": 
                        if (b == 0) throw new RuntimeException("División por cero en módulo");
                        return a % b;
                    case "^": return (int) Math.pow(a, b);
                }
            }
        }
        
        throw new RuntimeException("Operador '" + operador + "' no aplicable a estos tipos");
    }
    
    private double convertirADouble(Object valor) {
        if (valor instanceof Integer) {
            return ((Integer) valor).doubleValue();
        } else if (valor instanceof Double) {
            return (Double) valor;
        } else if (valor instanceof Long) {
            return ((Long) valor).doubleValue();
        }
        throw new RuntimeException("No se puede convertir a double: " + valor.getClass().getSimpleName());
    }
    
    private long convertirALong(Object valor) {
        if (valor instanceof Integer) {
            return ((Integer) valor).longValue();
        } else if (valor instanceof Long) {
            return (Long) valor;
        } else if (valor instanceof Double) {
            return ((Double) valor).longValue();
        }
        throw new RuntimeException("No se puede convertir a long: " + valor.getClass().getSimpleName());
    }
    
    private String convertirAString(Object valor) {
        if (valor == null) return "null";
        if (valor instanceof Boolean) return (Boolean) valor ? "verdadero" : "falso";
        return valor.toString();
    }
    
    // Método para cerrar el scanner al finalizar
    public static void cerrarScanner() {
        if (scanner != null) {
            scanner.close();
        }
    }
}
