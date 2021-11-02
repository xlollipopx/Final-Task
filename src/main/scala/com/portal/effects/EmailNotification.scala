package com.portal.effects

import cats.effect.Sync

import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.Properties

class EmailNotification {

  def send(userMail: String, code: String): Unit = {
    val properties         = new Properties
    val currentThread      = Thread.currentThread
    val contextClassLoader = currentThread.getContextClassLoader
    val propertiesStream   = contextClassLoader.getResourceAsStream("mail.properties")
    if (propertiesStream != null) {
      properties.load(propertiesStream)
      propertiesStream.close()
    } else {}
    val mailSession = Session.getDefaultInstance(properties)
    val message     = new MimeMessage(mailSession)
    message.setFrom(new InternetAddress("musictop932"))
    message.addRecipient(Message.RecipientType.TO, new InternetAddress(userMail))
    message.setSubject("hello")
    message.setText(generateMessage(code))
    val tr = mailSession.getTransport
    tr.connect(null, "qwertypoiu1055")
    tr.sendMessage(message, message.getAllRecipients)
    tr.close()
  }

  private def generateMessage(code: String) = "Hi! \nSupplier  " + code + " added some new products!"
}
