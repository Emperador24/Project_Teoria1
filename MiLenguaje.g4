grammar Milenguaje;

// REGLAS SINTÁCTICAS (Parser Rules)
programa: declaracion* EOF ;

declaracion: declaracionVariable
           | declaracionFuncion
           | sentencia
           ;

declaracionVariable: tipo IDENTIFICADOR ('=' expresion)? ';' ;

tipo: 'entero' | 'decimal' | 'booleano' | 'cadena' | 'largo' ;

declaracionFuncion: 'funcion' IDENTIFICADOR '(' parametros? ')' bloque ;

parametros: tipo IDENTIFICADOR (',' tipo IDENTIFICADOR)* ;

bloque: '{' declaracion* '}' ;

sentencia: bloquesentencia
         | sentenciaExpresion
         | sentenciaSi
         | sentenciaPara
         | sentenciaMientras
         | sentenciaRetornar
         | sentenciaImprimir
         | sentenciaRomper
         | sentenciaContinuar
         ;

bloquesentencia: bloque ;

sentenciaExpresion: expresion ';' ;

sentenciaSi: 'si' '(' expresion ')' sentencia ('sino' sentencia)? ;

sentenciaPara: 'para' '(' declaracionVariable expresion ';' expresion ')' sentencia ;

sentenciaMientras: 'mientras' '(' expresion ')' sentencia ;

sentenciaRetornar: 'retornar' expresion? ';' ;

sentenciaImprimir: 'imprimir' '(' expresion ')' ';' ;

sentenciaRomper: 'romper' ';' ;

sentenciaContinuar: 'continuar' ';' ;

expresion: asignacion ;

asignacion: IDENTIFICADOR '=' asignacion
          | expresionLogicaO
          ;

expresionLogicaO: expresionLogicaY ('o' expresionLogicaY)* ;

expresionLogicaY: expresionIgualdad ('y' expresionIgualdad)* ;

expresionIgualdad: expresionRelacional (('==' | '!=') expresionRelacional)* ;

expresionRelacional: expresionAritmetica (('<' | '<=' | '>' | '>=') expresionAritmetica)* ;

expresionAritmetica: expresionPotencia (('+' | '-') expresionPotencia)* ;

expresionPotencia: expresionMultiplicativa ('^' expresionMultiplicativa)? ;

expresionMultiplicativa: expresionUnaria (('*' | '/' | '%') expresionUnaria)* ;

expresionUnaria: ('no' | '-' | '+') expresionUnaria
               | expresionPrimaria
               ;

expresionPrimaria: NUMERO_ENTERO
                 | NUMERO_DECIMAL  
                 | CADENA
                 | 'verdadero'
                 | 'falso'
                 | IDENTIFICADOR
                 | llamadaFuncion
                 | expresionLeer
                 | '(' expresion ')'
                 ;

llamadaFuncion: IDENTIFICADOR '(' argumentos? ')' ;

argumentos: expresion (',' expresion)* ;

expresionLeer: 'leer' '(' ')' ;

// REGLAS LÉXICAS (Lexer Rules)

// Palabras reservadas
WS: [ \t\r\n]+ -> skip ;

COMENTARIO_LINEA: '//' ~[\r\n]* -> skip ;

COMENTARIO_BLOQUE: '/*' .*? '*/' -> skip ;

// Operadores y símbolos
ASIGNAR: '=' ;
IGUAL: '==' ;
DIFERENTE: '!=' ;
MENOR: '<' ;
MENOR_IGUAL: '<=' ;
MAYOR: '>' ;
MAYOR_IGUAL: '>=' ;
SUMA: '+' ;
RESTA: '-' ;
MULTIPLICAR: '*' ;
DIVIDIR: '/' ;
MODULO: '%' ;
POTENCIA: '^' ;
PARENTESIS_IZQ: '(' ;
PARENTESIS_DER: ')' ;
LLAVE_IZQ: '{' ;
LLAVE_DER: '}' ;
PUNTO_COMA: ';' ;
COMA: ',' ;

// Literales
NUMERO_ENTERO: [0-9]+ ;

NUMERO_DECIMAL: [0-9]+ '.' [0-9]+ ;

CADENA: '"' (~["\r\n] | '\\' .)* '"' ;

// Identificadores (máximo 10 caracteres)
IDENTIFICADOR: [a-zA-Z_][a-zA-Z0-9_]* {getText().length() <= 10}? ;

// Caracteres no reconocidos
ERROR_CHAR: . ;
