package com.portal.effects

import java.util.Properties
import javax.mail.{Message, Session}
import javax.mail.internet.{InternetAddress, MimeMessage}

class EmailNotificationService {

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
    message.setFrom(new InternetAddress(properties.getProperty("address")))
    message.addRecipient(Message.RecipientType.TO, new InternetAddress(userMail))
    message.setSubject(properties.getProperty("subject"))
    message.setText(generateMessage(code))
    val tr = mailSession.getTransport
    tr.connect(null, properties.getProperty("password"))
    tr.sendMessage(message, message.getAllRecipients)
    tr.close()
  }

  private def generateMessage(code: String) = "Hi! \nSupplier  " + code + " added some new products!"
}
