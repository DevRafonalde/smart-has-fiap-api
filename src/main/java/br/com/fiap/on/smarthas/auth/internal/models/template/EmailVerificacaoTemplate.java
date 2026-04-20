package br.com.fiap.on.smarthas.auth.internal.models.template;

public class EmailVerificacaoTemplate {

    // Gera o HTML completo do e-mail de verificação
    // Recebe o nome amigável do usuário e o código de 6 dígitos
    public static String gerar(String nomeAmigavel, String codigo) {
        // Separa os dígitos do código para exibir em blocos individuais
        String[] digitos = codigo.split("");

        StringBuilder blocosDigitos = new StringBuilder();
        for (String digito : digitos) {
            blocosDigitos.append("""
                    <td style="
                        width: 48px;
                        height: 56px;
                        background-color: #F3F4F6;
                        border: 2px solid #E5E7EB;
                        border-radius: 8px;
                        text-align: center;
                        vertical-align: middle;
                        font-size: 28px;
                        font-weight: 700;
                        color: #111827;
                        font-family: 'Courier New', monospace;
                        padding: 0 4px;
                    ">%s</td>
                    <td style="width: 8px;"></td>
                    """.formatted(digito));
        }

        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Verificação de E-mail — SmartHAS Digital 360</title>
                </head>
                <body style="
                    margin: 0;
                    padding: 0;
                    background-color: #F9FAFB;
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                ">

                <!-- Wrapper -->
                <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color: #F9FAFB; padding: 40px 16px;">
                    <tr>
                        <td align="center">

                            <!-- Card principal -->
                            <table width="560" cellpadding="0" cellspacing="0" border="0" style="
                                background-color: #FFFFFF;
                                border-radius: 16px;
                                overflow: hidden;
                                box-shadow: 0 1px 3px rgba(0,0,0,0.08);
                                max-width: 560px;
                                width: 100%%;
                            ">

                                <!-- Header com gradiente -->
                                <tr>
                                    <td style="
                                        background: linear-gradient(135deg, #1A56DB 0%%, #1E429F 100%%);
                                        padding: 36px 40px;
                                        text-align: center;
                                    ">
                                        <!-- Ícone -->
                                        <div style="
                                            display: inline-block;
                                            width: 56px;
                                            height: 56px;
                                            background-color: rgba(255,255,255,0.15);
                                            border-radius: 50%%;
                                            line-height: 56px;
                                            text-align: center;
                                            font-size: 28px;
                                            margin-bottom: 16px;
                                        ">✉</div>
                                        <br>
                                        <span style="
                                            font-size: 22px;
                                            font-weight: 700;
                                            color: #FFFFFF;
                                            letter-spacing: -0.3px;
                                        ">SmartHAS · Digital 360</span>
                                    </td>
                                </tr>

                                <!-- Corpo -->
                                <tr>
                                    <td style="padding: 40px 40px 32px;">

                                        <!-- Saudação -->
                                        <p style="
                                            margin: 0 0 8px;
                                            font-size: 20px;
                                            font-weight: 600;
                                            color: #111827;
                                        ">Olá, %s 👋</p>

                                        <p style="
                                            margin: 0 0 32px;
                                            font-size: 15px;
                                            color: #6B7280;
                                            line-height: 1.6;
                                        ">
                                            Recebemos uma solicitação de verificação para o seu e-mail.
                                            Use o código abaixo para confirmar sua identidade:
                                        </p>

                                        <!-- Bloco do código -->
                                        <table cellpadding="0" cellspacing="0" border="0" width="100%%">
                                            <tr>
                                                <td style="
                                                    background-color: #EFF6FF;
                                                    border: 1px solid #BFDBFE;
                                                    border-radius: 12px;
                                                    padding: 24px;
                                                    text-align: center;
                                                ">
                                                    <p style="
                                                        margin: 0 0 16px;
                                                        font-size: 12px;
                                                        font-weight: 600;
                                                        color: #1D4ED8;
                                                        text-transform: uppercase;
                                                        letter-spacing: 0.8px;
                                                    ">Seu código de verificação</p>

                                                    <!-- Dígitos individuais -->
                                                    <table cellpadding="0" cellspacing="0" border="0" style="margin: 0 auto;">
                                                        <tr>
                                                            %s
                                                        </tr>
                                                    </table>

                                                    <p style="
                                                        margin: 20px 0 0;
                                                        font-size: 13px;
                                                        color: #6B7280;
                                                    ">
                                                        ⏱ Este código expira em <strong>15 minutos</strong>
                                                    </p>
                                                </td>
                                            </tr>
                                        </table>

                                        <!-- Aviso de segurança -->
                                        <table cellpadding="0" cellspacing="0" border="0" width="100%%" style="margin-top: 24px;">
                                            <tr>
                                                <td style="
                                                    background-color: #FFFBEB;
                                                    border: 1px solid #FDE68A;
                                                    border-radius: 8px;
                                                    padding: 14px 16px;
                                                ">
                                                    <p style="
                                                        margin: 0;
                                                        font-size: 13px;
                                                        color: #92400E;
                                                        line-height: 1.5;
                                                    ">
                                                        ⚠️ <strong>Não solicitou isso?</strong>
                                                        Se você não tentou verificar seu e-mail, ignore esta mensagem.
                                                        Sua conta permanece segura.
                                                    </p>
                                                </td>
                                            </tr>
                                        </table>

                                    </td>
                                </tr>

                                <!-- Divisor -->
                                <tr>
                                    <td style="padding: 0 40px;">
                                        <hr style="border: none; border-top: 1px solid #E5E7EB; margin: 0;">
                                    </td>
                                </tr>

                                <!-- Footer -->
                                <tr>
                                    <td style="padding: 24px 40px 32px; text-align: center;">
                                        <p style="
                                            margin: 0 0 6px;
                                            font-size: 13px;
                                            color: #9CA3AF;
                                        ">
                                            Este e-mail foi enviado automaticamente pela plataforma
                                            <strong style="color: #6B7280;">SmartHAS Digital 360</strong>.
                                        </p>
                                        <p style="
                                            margin: 0;
                                            font-size: 12px;
                                            color: #D1D5DB;
                                        ">
                                            Por favor, não responda a este e-mail.
                                        </p>
                                    </td>
                                </tr>

                            </table>
                            <!-- /Card principal -->

                        </td>
                    </tr>
                </table>
                <!-- /Wrapper -->

                </body>
                </html>
                """.formatted(nomeAmigavel, blocosDigitos.toString());
    }
}