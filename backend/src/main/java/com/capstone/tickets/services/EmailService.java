package com.capstone.tickets.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Send a password reset email with a custom HTML template
     * 
     * @param toEmail   Recipient email address
     * @param resetLink Password reset link
     * @param username  User's username
     */
    public void sendPasswordResetEmail(String toEmail, String resetLink, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Event Ticketing Platform");
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request");

            String htmlContent = buildPasswordResetEmailHtml(toEmail, resetLink, username);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Build HTML content for password reset email
     */
    private String buildPasswordResetEmailHtml(String email, String resetLink, String username) {
        return String.format(
                """
                        <!DOCTYPE html>
                        <html lang="en">
                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <title>Password Reset Request</title>
                        </head>
                        <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f3f4f6;">
                            <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f3f4f6; padding: 40px 20px;">
                                <tr>
                                    <td align="center">
                                        <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);">

                                            <!-- Header -->
                                            <tr>
                                                <td style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 30px; text-align: center;">
                                                    <div style="font-size: 32px; margin-bottom: 10px;">🎫</div>
                                                    <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 600;">Event Ticketing Platform</h1>
                                                    <p style="margin: 10px 0 0 0; color: #e0e7ff; font-size: 16px;">Password Reset Request</p>
                                                </td>
                                            </tr>

                                            <!-- Body -->
                                            <tr>
                                                <td style="padding: 40px 30px;">
                                                    <p style="margin: 0 0 20px 0; color: #374151; font-size: 16px; line-height: 1.5;">Hello <strong style="color: #667eea;">%s</strong>,</p>

                                                    <p style="margin: 0 0 20px 0; color: #374151; font-size: 16px; line-height: 1.5;">
                                                        We received a request to reset the password for your account (<strong style="color: #667eea;">%s</strong>).
                                                    </p>

                                                    <p style="margin: 0 0 30px 0; color: #374151; font-size: 16px; line-height: 1.5;">
                                                        Click the button below to create a new password:
                                                    </p>

                                                    <!-- Reset Button -->
                                                    <table width="100%%" cellpadding="0" cellspacing="0">
                                                        <tr>
                                                            <td align="center" style="padding: 20px 0;">
                                                                <a href="%s" style="display: inline-block; padding: 16px 40px; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: #ffffff; text-decoration: none; border-radius: 6px; font-weight: 600; font-size: 16px; box-shadow: 0 4px 6px rgba(102, 126, 234, 0.3);">Reset Password</a>
                                                            </td>
                                                        </tr>
                                                    </table>

                                                    <p style="margin: 30px 0 20px 0; color: #6b7280; font-size: 14px; line-height: 1.5;">
                                                        Or copy and paste this link into your browser:
                                                    </p>

                                                    <div style="background-color: #f9fafb; border: 1px solid #e5e7eb; border-radius: 6px; padding: 15px; word-break: break-all;">
                                                        <a href="%s" style="color: #667eea; text-decoration: none; font-size: 14px;">%s</a>
                                                    </div>

                                                    <!-- Warning Box -->
                                                    <div style="margin: 30px 0; padding: 20px; background-color: #fef3c7; border-left: 4px solid #f59e0b; border-radius: 6px;">
                                                        <p style="margin: 0 0 10px 0; color: #92400e; font-weight: 600; font-size: 14px;">⚠️ Important:</p>
                                                        <ul style="margin: 0; padding-left: 20px; color: #92400e; font-size: 14px; line-height: 1.6;">
                                                            <li>This link will expire in <strong>24 hours</strong></li>
                                                            <li>If you didn't request this, please ignore this email</li>
                                                            <li>Your password will remain unchanged until you create a new one</li>
                                                        </ul>
                                                    </div>

                                                    <p style="margin: 20px 0 0 0; color: #6b7280; font-size: 14px; line-height: 1.5;">
                                                        Need help? Contact our support team.
                                                    </p>
                                                </td>
                                            </tr>

                                            <!-- Footer -->
                                            <tr>
                                                <td style="background-color: #f9fafb; padding: 30px; text-align: center; border-top: 1px solid #e5e7eb;">
                                                    <p style="margin: 0 0 10px 0; color: #6b7280; font-size: 14px;">© 2025 Event Ticketing Platform. All rights reserved.</p>
                                                    <p style="margin: 0; color: #9ca3af; font-size: 12px;">This is an automated message, please do not reply to this email.</p>
                                                </td>
                                            </tr>

                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </body>
                        </html>
                        """,
                username, email, resetLink, resetLink, resetLink);
    }
}
