package com.ctfp.service.helper

import at.quickme.kotlinmailer.delivery.send
import at.quickme.kotlinmailer.email.emailBuilder
import at.quickme.kotlinmailer.smtp.SmtpSecurity
import at.quickme.kotlinmailer.smtp.smtpMailer
import com.ctfp.config.MailConfig
import org.slf4j.LoggerFactory

/** Sends a plain-text email; throws if it can't be delivered. */
fun interface MailSender {
    suspend fun send(
        recipient: String,
        subject: String,
        text: String,
    )
}

private val log = LoggerFactory.getLogger(MailSender::class.java)

/** SMTP when [MailConfig.smtp] has a host, otherwise a sender that writes each email to the log. */
fun mailSender(config: MailConfig): MailSender {
    val smtp = config.smtp
    if (smtp.host.isBlank()) {
        // The log then holds whatever the emails carry, password reset links included.
        log.warn("SMTP is not configured (SMTP_HOST); emails are written to the log instead of sent")
        return MailSender { recipient, subject, text -> log.info("Email to {}: {}\n{}", recipient, subject, text) }
    }

    val mailer =
        smtpMailer(smtp.host) {
            security = SmtpSecurity.valueOf(smtp.security.uppercase())
            if (smtp.port > 0) port = smtp.port
            if (smtp.username.isNotBlank()) credentials(smtp.username, smtp.password)
        }
    return MailSender { recipient, subject, text ->
        emailBuilder {
            from(config.from)
            to(recipient)
            this.subject = subject
            this.text = text
        }.send(mailer)
    }
}
