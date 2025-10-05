// Programa para calcular los primeros 50 números de la sucesión de Fibonacci
// usando tipo largo para evitar desbordamiento en números grandes

funcion fibonacci(entero n) {
    si (n <= 1) {
        retornar n;
    } sino {
        largo a = 0;
        largo b = 1;
        largo temp = 0;
        
        para (entero i = 2; i <= n; i = i + 1) {
            temp = a + b;
            a = b;
            b = temp;
        }
        
        retornar b;
    }
}

// Programa principal
imprimir("Los primeros 50 números de la sucesión de Fibonacci:");

para (entero i = 0; i < 51; i = i + 1) {
    largo resultado = fibonacci(i);
    imprimir("F(" + i + ") = " + resultado);
}
