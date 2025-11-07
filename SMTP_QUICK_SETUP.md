# Quick SMTP Setup Guide

## Run the Interactive Setup Script

The easiest way to configure SMTP:

```bash
./setup-smtp.sh
```

This script will:

1. Guide you through choosing an email provider
2. Collect your SMTP credentials securely
3. Configure Keycloak automatically
4. Send a test email to verify it works

---

## Manual Configuration Options

If you prefer to configure manually, here are the settings for common providers:

### 1. Gmail (Recommended for Testing)

**Prerequisites:**

1. Enable 2-Factor Authentication on your Google account
2. Create an App Password: https://myaccount.google.com/apppasswords
   - Select: Mail → Other (name it "Booking Site")
   - Copy the 16-character password tvxo sfox haqz ftph

**Configuration:**

```bash
docker exec backend-2-keycloak-1 /opt/keycloak/bin/kcadm.sh update realms/event-ticket-platform \
  -s 'smtpServer.host=smtp.gmail.com' \
  -s 'smtpServer.port=587' \
  -s 'smtpServer.from=your-email@gmail.com' \
  -s 'smtpServer.auth=true' \
  -s 'smtpServer.starttls=true' \
  -s 'smtpServer.ssl=false' \
  -s 'smtpServer.user=your-email@gmail.com' \
  -s 'smtpServer.password=your-16-char-app-password'
```

**Example:**

```bash
docker exec backend-2-keycloak-1 /opt/keycloak/bin/kcadm.sh update realms/event-ticket-platform \
  -s 'smtpServer.host=smtp.gmail.com' \
  -s 'smtpServer.port=587' \
  -s 'smtpServer.from=bookingsite@gmail.com' \
  -s 'smtpServer.auth=true' \
  -s 'smtpServer.starttls=true' \
  -s 'smtpServer.ssl=false' \
  -s 'smtpServer.user=bookingsite@gmail.com' \
  -s 'smtpServer.password=abcd efgh ijkl mnop'
```

### 2. Outlook/Hotmail

**Configuration:**

```bash
docker exec backend-2-keycloak-1 /opt/keycloak/bin/kcadm.sh update realms/event-ticket-platform \
  -s 'smtpServer.host=smtp-mail.outlook.com' \
  -s 'smtpServer.port=587' \
  -s 'smtpServer.from=your-email@outlook.com' \
  -s 'smtpServer.auth=true' \
  -s 'smtpServer.starttls=true' \
  -s 'smtpServer.ssl=false' \
  -s 'smtpServer.user=your-email@outlook.com' \
  -s 'smtpServer.password=your-password'
```

### 3. SendGrid (Professional Option)

**Prerequisites:**

1. Sign up at: https://sendgrid.com/ (free tier available)
2. Verify your sender email address
3. Create API Key: Settings → API Keys

**Configuration:**

```bash
docker exec backend-2-keycloak-1 /opt/keycloak/bin/kcadm.sh update realms/event-ticket-platform \
  -s 'smtpServer.host=smtp.sendgrid.net' \
  -s 'smtpServer.port=587' \
  -s 'smtpServer.from=noreply@yourdomain.com' \
  -s 'smtpServer.auth=true' \
  -s 'smtpServer.starttls=true' \
  -s 'smtpServer.ssl=false' \
  -s 'smtpServer.user=apikey' \
  -s 'smtpServer.password=YOUR_SENDGRID_API_KEY'
```

### 4. AWS SES

**Prerequisites:**

1. Set up AWS SES in your AWS account
2. Verify your sending email/domain
3. Create SMTP credentials

**Configuration:**

```bash
docker exec backend-2-keycloak-1 /opt/keycloak/bin/kcadm.sh update realms/event-ticket-platform \
  -s 'smtpServer.host=email-smtp.us-east-1.amazonaws.com' \
  -s 'smtpServer.port=587' \
  -s 'smtpServer.from=noreply@yourdomain.com' \
  -s 'smtpServer.auth=true' \
  -s 'smtpServer.starttls=true' \
  -s 'smtpServer.ssl=false' \
  -s 'smtpServer.user=YOUR_SES_SMTP_USERNAME' \
  -s 'smtpServer.password=YOUR_SES_SMTP_PASSWORD'
```

---

## Verify Configuration

### Check Current Settings

```bash
docker exec backend-2-keycloak-1 /opt/keycloak/bin/kcadm.sh get realms/event-ticket-platform | grep -A 10 "smtpServer"
```

### Send Test Email

```bash
# Test forgot password
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"your-test-email@example.com"}'

# Check the email in your inbox
```

---

## Troubleshooting

### Email Not Received?

1. **Check spam/junk folder** - First thing to check!

2. **Verify SMTP credentials:**

```bash
docker exec backend-2-keycloak-1 /opt/keycloak/bin/kcadm.sh get realms/event-ticket-platform | grep -A 10 "smtpServer"
```

3. **Check Keycloak logs for errors:**

```bash
docker logs backend-2-keycloak-1 2>&1 | grep -i "mail\|smtp\|email" | tail -20
```

4. **Test SMTP connection manually:**

```bash
# Using openssl (if available)
openssl s_client -starttls smtp -connect smtp.gmail.com:587
```

### Common Issues

**Gmail: "Username and Password not accepted"**

- Make sure you're using an App Password, not your regular Gmail password
- Enable 2-Factor Authentication first
- Remove any spaces from the App Password

**Outlook: Connection timeout**

- Outlook may block access from "less secure apps"
- Try using an app password if available
- Check if your account requires additional security settings

**SendGrid: Authentication failed**

- Make sure username is exactly `apikey` (lowercase)
- Verify API key has "Mail Send" permissions
- Check sender email is verified in SendGrid

**AWS SES: Email not delivered**

- Verify your sending email/domain in SES console
- Check you're not in sandbox mode (which limits recipients)
- Verify SMTP credentials are correct

---

## Testing Checklist

After configuration:

- [ ] SMTP settings saved in Keycloak
- [ ] Configuration verified with `get realms` command
- [ ] Test email sent via forgot password
- [ ] Email received in inbox (check spam!)
- [ ] Password reset link works
- [ ] Can set new password successfully

---

## Email Appearance

The default Keycloak email will look like:

**Subject:** Reset password

**Body:**

```
Someone just requested to change your event-ticket-platform account's credentials.
If this was you, click on the link below to reset them.

Reset password

This link will expire within 5 minutes.

If you didn't request this, just ignore this message.
```

To customize this:

- See `FORGOT_PASSWORD_GUIDE.md` for custom email template instructions
- You can add company branding, include username, etc.

---

## Security Notes

⚠️ **Important:**

- Never commit SMTP passwords to git
- Use environment variables for production
- Rotate credentials regularly
- Use dedicated email service accounts (not personal email)
- Consider using OAuth2 authentication for Gmail

---

## Need Help?

If you encounter issues:

1. Run `./setup-smtp.sh` - it has built-in diagnostics
2. Check `FORGOT_PASSWORD_GUIDE.md` for detailed troubleshooting
3. View Keycloak logs: `docker logs backend-2-keycloak-1`
4. Test with a simple email service like Gmail first
