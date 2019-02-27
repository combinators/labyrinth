package org.combinators.labyrinth

import java.util.Properties

import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.auto._
import io.circe.parser._
import org.eclipse.paho.client.mqttv3.{IMqttMessageListener, MqttClient, MqttConnectOptions, MqttMessage}

import org.combinators.labyrinth.protocol.Task

object Agent extends App with LazyLogging {

  val connectionSettings = new Properties()
  connectionSettings.load(getClass.getResourceAsStream("connection.properties"))

  val broker = connectionSettings.getProperty("org.combinators.labyrinth.broker")
  val clientId = connectionSettings.getProperty("org.combinators.labyrinth.clientId")
  val taskTopic = connectionSettings.getProperty("org.combinators.labyrinth.taskTopic")

  logger.debug(s"""Connecting to broker "$broker" as "$clientId"""")
  val client: MqttClient = new MqttClient(broker, clientId)
  val options: MqttConnectOptions = new MqttConnectOptions()
  options.setAutomaticReconnect(true)
  options.setCleanSession(true)
  options.setConnectionTimeout(10)
  options.setMaxInflight(1000)
  client.connect(options)

  logger.debug(s"Receiving tasks from topic: $taskTopic")
  def onMessage: IMqttMessageListener = new IMqttMessageListener {
    override def messageArrived(topic: String, message: MqttMessage): Unit = {
      logger.debug(new String(message.getPayload))
      val decoded = decode[Task](new String(message.getPayload))
      if (decoded.isLeft) {
        logger.debug(s"Error: ${decoded.left.get}")
      } else {
        logger.debug(s"Received task: ${decoded.right.get}")
        new Labyrinth(decoded.right.get, client)
      }
    }
  }
  client.subscribe(taskTopic, 2, onMessage)

  println("Press <Enter> to exit.")
  scala.io.StdIn.readLine()

  logger.debug(s"Disconnecting $clientId from $broker")
  client.disconnect()
  client.close()
}
