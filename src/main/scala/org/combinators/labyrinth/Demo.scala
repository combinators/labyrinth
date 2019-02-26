package org.combinators.labyrinth

import java.util.Properties

import scala.util.Try
import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.auto._
//import protocol.GenericDerivation._
import io.circe.parser.decode
import io.circe.syntax._
import org.eclipse.paho.client.mqttv3.{IMqttMessageListener, MqttClient, MqttConnectOptions, MqttMessage}
import org.combinators.labyrinth.protocol._



object Demo extends App with LazyLogging {

  val connectionSettings = new Properties()
  connectionSettings.load(getClass.getResourceAsStream("connection.properties"))

  val broker = connectionSettings.getProperty("org.combinators.labyrinth.broker")
  val clientId = s"demoServer_${connectionSettings.getProperty("org.combinators.labyrinth.clientId")}"
  val taskTopic = connectionSettings.getProperty("org.combinators.labyrinth.taskTopic")

  logger.debug(s"""Connecting to broker "$broker" as "$clientId"""")
  val client: MqttClient = new MqttClient(broker, clientId)
  val options: MqttConnectOptions = new MqttConnectOptions()
  options.setAutomaticReconnect(true)
  options.setCleanSession(true)
  options.setConnectionTimeout(10)
  client.connect(options)



  val labyrinth: Seq[Seq[Boolean]] =
    Seq(
      Seq(false, true, false),
      Seq(true, true, true),
      Seq(true, false, true),
      Seq(true, true, true)
    )
  val start = (0, 2)
  val goal = (1, 0)

  val topicForSolutions = "demoLabSolutions"
  val topicForRequests = "demoLabRequests"

  logger.debug(s"Labyrinth: \n ${labyrinth.zipWithIndex.map {
    case (row, rowIdx) =>
      row.zipWithIndex.map {
        case (free, colIdx) =>
          if (colIdx == start._1 && rowIdx == start._2) "s"
          else if (colIdx == goal._1 && rowIdx == goal._2) "g"
          else if (!free) "x"
          else " "
      }.mkString("|", "|", "|")
    }.mkString("\n")}")

  val task: Task = Task(topicForSolutions, topicForRequests, labyrinth, start, goal)
  logger.debug(s"Sending Task ${task}")
  client.publish(taskTopic, task.asJson.toString.getBytes, 2, true)

  logger.debug(s"Receiving solutions from topic: $topicForSolutions")
  def onMessage: IMqttMessageListener = new IMqttMessageListener {
    override def messageArrived(topic: String, message: MqttMessage): Unit = {
      val solution = decode[Solution](new String(message.getPayload))
      if (solution.isLeft) {
        logger.debug(s"Error: ${solution.left.get}")
      } else {
        println(solution.right.get)
      }
    }
  }
  client.subscribe(topicForSolutions, 2, onMessage)

  var stop = false
  while (!stop) {
    println("Enter number of solutions to get or 0 to stop: ")
    val toGet = Try(scala.io.StdIn.readInt())
    if (toGet.isSuccess) {
      if (toGet.get > 0) {
        println(s"Requesting ${toGet.get} more solutions")
        client.publish(topicForRequests, GetSolutions(toGet.get).asJson.toString().getBytes, 2, true)
      } else {
        stop = true
      }
    }
  }

  logger.debug(s"Disconnecting $clientId from $broker")
  client.disconnect()
  client.close()
}
