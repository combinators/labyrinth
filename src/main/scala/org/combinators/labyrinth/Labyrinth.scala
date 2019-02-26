package org.combinators.labyrinth

import com.typesafe.scalalogging.LazyLogging
import org.eclipse.paho.client.mqttv3.{IMqttMessageListener, MqttClient, MqttMessage}
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.combinators.cls.interpreter.{InhabitationResult, ReflectedRepository}
import org.combinators.labyrinth.protocol.{GetSolutions, Movement, Solution, Task}

class Labyrinth(task: Task, client: MqttClient) extends LazyLogging {
  logger.debug(s"Labyrinth: \n ${task.labyrinth.map(row => row.map(e => if (!e) "x" else " ").mkString("|", "|", "|")).mkString("\n")}")
  lazy val repo = new Repository(task)
  lazy val Gamma = repo.reflected
  logger.debug(Gamma.combinators.map { case (c, ty) => s"$c : $ty" }.mkString("{ ", ",\n", "}"))
  logger.debug(s"|- ? : ${ReflectedRepository.nativeTypeOf[repo.nativeTarget]} :&: ${repo.semanticTarget}")
  lazy val results: InhabitationResult[repo.nativeTarget] = Gamma.inhabit[repo.nativeTarget](repo.semanticTarget)

  logger.debug("Grammar:")
  logger.debug(
    results
      .grammar
      .map { case (ty, options) => s"$ty ::= ${options.mkString("", " | ", ";")}" }
      .mkString("{", "\n", "}"))

  lazy val resultIterator: Iterator[Seq[Movement]] = new Iterator[Seq[Movement]] {
    var pos = 0
    override def hasNext: Boolean = true
    override def next: Seq[Movement] = {
      val result = results.interpretedTerms.index(pos)
      pos += 1
      if (results.size.exists(s => pos >= s)) {
        pos = 0
      }
      result
    }
  }

  val onMessage: IMqttMessageListener = new IMqttMessageListener {
    override def messageArrived(topic: String, message: MqttMessage): Unit = {
      decode[GetSolutions](new String(message.getPayload)).foreach {
        case GetSolutions(maxCount) =>
          logger.debug(s"Received request for $maxCount solutions")
          (0 until maxCount).foreach { _ =>
            if (resultIterator.hasNext) {
              val next = Solution(resultIterator.next()).asJson.toString
              logger.debug(s"Sending: $next")
              client.publish(task.topicForSolutions, next.getBytes, 2, true)
            }
          }
      }
    }
  }

  client.subscribe(task.topicForRequests, 2, onMessage)
}
