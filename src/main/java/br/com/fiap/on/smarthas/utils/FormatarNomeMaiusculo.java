package br.com.fiap.on.smarthas.utils;

import java.util.Set;

public final class FormatarNomeMaiusculo {
    private static final Set<String> PARTICULAS_MINUSCULAS = Set.of(
            "de", "da", "do", "dos", "das", "e"
    );

    private FormatarNomeMaiusculo() {
        // Evita instanciação (classe utilitária)
    }

    public static String formatar(String nome) {
        if (nome == null) return null;

        String normalizado = nome.trim().toLowerCase();
        if (normalizado.isEmpty()) return normalizado;

        String[] palavras = normalizado.split("\\s+");
        StringBuilder resultado = new StringBuilder(normalizado.length());

        for (int i = 0; i < palavras.length; i++) {
            if (i > 0) resultado.append(' ');

            String palavra = palavras[i];

            if (PARTICULAS_MINUSCULAS.contains(palavra)) {
                resultado.append(palavra);
            } else {
                resultado.append(capitalizarComSeparadores(palavra));
            }
        }

        return resultado.toString();
    }

    private static String capitalizarComSeparadores(String palavra) {
        StringBuilder resultado = new StringBuilder(palavra.length());

        int inicio = 0;

        for (int i = 0; i < palavra.length(); i++) {
            char c = palavra.charAt(i);

            if (c == '-' || c == '\'') {
                resultado.append(capitalizar(palavra, inicio, i));
                resultado.append(c);
                inicio = i + 1;
            }
        }

        // Último segmento
        resultado.append(capitalizar(palavra, inicio, palavra.length()));

        return resultado.toString();
    }

    private static String capitalizar(String texto, int inicio, int fim) {
        if (inicio >= fim) return "";

        StringBuilder sb = new StringBuilder(fim - inicio);

        sb.append(Character.toUpperCase(texto.charAt(inicio)));

        for (int i = inicio + 1; i < fim; i++) {
            sb.append(texto.charAt(i));
        }

        return sb.toString();
    }
}
