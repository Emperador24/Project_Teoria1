# EspañolScript Interpreter

Interpreter for a Spanish-based programming language built with ANTLR4 and Java.

## Description

This project implements a full interpreter for a programming language designed with Spanish syntax. The language supports typed variables, functions, control structures, arithmetic and logical operations.

## Language Features

### Data Types
- `entero` (integer): integer numbers
- `largo` (long): long integer numbers
- `decimal` (decimal): floating-point numbers
- `booleano` (boolean): true/false values
- `cadena` (string): text in quotes

### Reserved Words
- Flow control: `si` (if), `sino` (else), `para` (for), `mientras` (while)
- Functions: `funcion` (function), `retornar` (return)
- Input/Output: `imprimir` (print), `leer` (read)
- Loop control: `romper` (break), `continuar` (continue)
- Boolean values: `verdadero` (true), `falso` (false)
- Logical operators: `y` (and), `o` (or), `no` (not)

### Operators
- Arithmetic: `+`, `-`, `*`, `/`, `%`, `^` (power)
- Relational: `<`, `<=`, `>`, `>=`, `==`, `!=`
- Logical: `y` (and), `o` (or), `no` (not)
- Assignment: `=`

### Example Code

```java
// Variable declaration
entero x = 10;
decimal pi = 3.1416;
cadena nombre = "Juan";
booleano activo = verdadero;

// Conditional structure
si (x > 5) {
    imprimir("x es mayor que 5");
} sino {
    imprimir("x es menor o igual a 5");
}

// For loop
para (entero i = 0; i < 10; i = i + 1) {
    imprimir(i);
}

// While loop
entero contador = 0;
mientras (contador < 5) {
    imprimir(contador);
    contador = contador + 1;
}

// Function
funcion factorial(entero n) {
    si (n <= 1) {
        retornar 1;
    }
    retornar n * factorial(n - 1);
}

// Function call
entero resultado = factorial(5);
imprimir(resultado);

// User input
cadena entrada = leer();
imprimir("Escribiste: " + entrada);
```

## Requirements

- Java JDK 8 or higher
- ANTLR 4.13.1 (or compatible version)
- Operating system: Linux, macOS, or Windows

## Installation

### 1. Download ANTLR

```bash
cd /usr/local/lib  # or any directory of your choice
wget https://www.antlr.org/download/antlr-4.13.1-complete.jar
```

### 2. Set Environment Variables (optional)

#### Linux/macOS
Add to your `~/.bashrc` or `~/.zshrc`:

```bash
export CLASSPATH=".:$HOME/antlr-4.13.1-complete.jar:$CLASSPATH"
alias antlr4='java -jar $HOME/antlr-4.13.1-complete.jar'
alias grun='java org.antlr.v4.gui.TestRig'
```

#### Windows
```cmd
set CLASSPATH=.;C:\antlr-4.13.1-complete.jar;%CLASSPATH%
```

## Compilation

### 1. Generate the parser from the grammar

```bash
java -jar antlr-4.13.1-complete.jar -visitor Milenguaje.g4
```

This generates:
- `MilenguajeLexer.java`
- `MilenguajeParser.java`
- `MilenguajeBaseVisitor.java`
- `MilenguajeVisitor.java`
- Auxiliary `.tokens` and `.interp` files

### 2. Compile all Java classes

#### Linux/macOS
```bash
javac -cp ".:antlr-4.13.1-complete.jar" *.java
```

#### Windows
```cmd
javac -cp ".;antlr-4.13.1-complete.jar" *.java
```

## Usage

### Run a program

#### Linux/macOS
```bash
java -cp ".:antlr-4.13.1-complete.jar" InterpretePrincipal archivo.es
```

#### Windows
```cmd
java -cp ".;antlr-4.13.1-complete.jar" InterpretePrincipal archivo.es
```

### View syntax tree

#### Text tree
```bash
java -cp ".:antlr-4.13.1-complete.jar" InterpretePrincipal archivo.es -tree
```

#### Graphical tree (requires GUI)
```bash
java -cp ".:antlr-4.13.1-complete.jar" InterpretePrincipal archivo.es -gui
```

#### View tokens
```bash
java -cp ".:antlr-4.13.1-complete.jar" InterpretePrincipal archivo.es -tokens
```

### Simple tree visualizer

```bash
java -cp ".:antlr-4.13.1-complete.jar" MostrarArbol archivo.es
```

## Automation Scripts

### compile.sh (Linux/macOS)

```bash
#!/bin/bash
echo "Generating parser..."
java -jar antlr-4.13.1-complete.jar -visitor Milenguaje.g4

echo "Compiling..."
javac -cp ".:antlr-4.13.1-complete.jar" *.java

echo "✓ Compilation successful"
```

### run.sh (Linux/macOS)

```bash
#!/bin/bash
if [ $# -eq 0 ]; then
    echo "Usage: ./run.sh archivo.es [options]"
    exit 1
fi

java -cp ".:antlr-4.13.1-complete.jar" InterpretePrincipal "$@"
```

### compile.bat (Windows)

```batch
@echo off
echo Generating parser...
java -jar antlr-4.13.1-complete.jar -visitor Milenguaje.g4

echo Compiling...
javac -cp ".;antlr-4.13.1-complete.jar" *.java

echo Compilation successful
pause
```

## Project Structure

```
.
├── Milenguaje.g4              # ANTLR grammar file
├── EvaluadorSemantico.java    # Visitor that executes code
├── InterpretePrincipal.java   # Interpreter entry point
├── MostrarArbol.java          # Utility to visualize the tree
├── README.md                  # This file
├── ejemplos/                  # Example programs
│   ├── hola_mundo.es
│   ├── factorial.es
│   ├── fibonacci.es
│   └── circulo.es
└── antlr-4.13.1-complete.jar  # ANTLR JAR file
```

## Examples

### Hello World

```
imprimir("Hola Mundo");
```

### Factorial

```
funcion factorial(entero n) {
    si (n <= 1) {
        retornar 1;
    }
    retornar n * factorial(n - 1);
}

entero resultado = factorial(5);
imprimir("5! = " + resultado);
```

### Fibonacci

```
funcion fibonacci(entero n) {
    si (n <= 1) {
        retornar n;
    }
    retornar fibonacci(n - 1) + fibonacci(n - 2);
}

para (entero i = 0; i < 10; i = i + 1) {
    imprimir("fib(" + i + ") = " + fibonacci(i));
}
```

## Technical Features

### Scope Management
- Supports local and global scopes
- Variable lookup in scope stack
- Local variables in functions

### Type System
- Runtime type checking
- Automatic conversion between compatible numeric types
- Detection of incompatible types

### Flow Control
- Lazy evaluation (short-circuit) for logical operators
- Handling of `romper` (break) and `continuar` (continue) in loops
- Return values in functions

### Error Handling
- Lexical errors with location
- Detailed syntax errors
- Semantic errors at runtime

## Limitations

- Identifiers have a maximum length of 10 characters
- No support for arrays or complex data structures
- No support for classes or objects
- Functions must be declared before use
- No explicit memory management

## Troubleshooting

### Error: Could not find or load main class

Ensure the classpath includes both the current directory and the ANTLR JAR:

```bash
java -cp ".:antlr-4.13.1-complete.jar" InterpretePrincipal archivo.es
```

### Error: package org.antlr.v4.runtime does not exist

Make sure to include the ANTLR JAR when compiling:

```bash
javac -cp ".:antlr-4.13.1-complete.jar" *.java
```

### .java files are not generated

Check the path to the ANTLR JAR and ensure the `.g4` grammar file has no syntax errors.

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a new branch for your feature (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Author

Samuel Medina Velasquez Loreto

## Additional Resources

- [ANTLR4 Documentation](https://github.com/antlr/antlr4/blob/master/doc/index.md)
- [The Definitive ANTLR 4 Reference](https://pragprog.com/titles/tpantlr2/the-definitive-antlr-4-reference/)
- [ANTLR4 Mega Tutorial](https://tomassetti.me/antlr-mega-tutorial/)

## Contact

For questions or suggestions, please open an issue in the repository.
