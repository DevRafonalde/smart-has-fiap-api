package br.com.fiap.on.smarthas.config;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    // Gera um hash para a senha
    public static String hashPassword(String senhaPura) {
        return BCrypt.hashpw(senhaPura, BCrypt.gensalt(12)); // custo 12
    }

    // Verifica se a senha corresponde ao hash
    public static boolean verificarSenha(String senhaPura, String senhaHashed) {
        return BCrypt.checkpw(senhaPura, senhaHashed);
    }
}
