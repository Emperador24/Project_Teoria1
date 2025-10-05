// Programa para calcular el área de un círculo
// EspañolScript

// Declaración de variables
decimal radio = 5.0;
decimal pi = 3.14159265;

// Cálculo del área usando la fórmula: área = π * radio²
decimal area = pi * (radio ^ 2);

// Mostrar el resultado
imprimir("=== Cálculo del Área de un Círculo ===");
imprimir("Radio: " + radio);
imprimir("Pi: " + pi);
imprimir("Área = π * radio²");
imprimir("Área = " + pi + " * " + radio + "²");
imprimir("Área = " + area);

// Verificar si es un círculo grande o pequeño
si (area > 50.0) {
    imprimir("¡Es un círculo grande!");
} sino {
    imprimir("Es un círculo pequeño.");
}

// Calcular también el perímetro
decimal perimetro = 2.0 * pi * radio;
imprimir("Perímetro = 2π * radio = " + perimetro);
