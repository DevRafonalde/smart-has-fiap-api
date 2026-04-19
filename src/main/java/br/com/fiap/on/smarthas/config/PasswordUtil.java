package br.com.fiap.on.smarthas.config;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    public static String hashPassword(String senhaPura) {
        return BCrypt.hashpw(senhaPura, BCrypt.gensalt(12)); // custo 12
    }

    public static boolean verificarSenha(String senhaPura, String senhaHashed) {
        return BCrypt.checkpw(senhaPura, senhaHashed);
    }
}
