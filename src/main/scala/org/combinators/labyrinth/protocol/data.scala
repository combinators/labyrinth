package org.combinators.labyrinth.protocol

import io.circe.generic.JsonCodec

@JsonCodec
case class Task(
  topicForSolutions: String,
  topicForRequests: String,
  labyrinth: Seq[Seq[Boolean]],
  start: (Int, Int),
  goal: (Int, Int)
)


@JsonCodec case class GetSolutions(maxCount: Int)


@JsonCodec sealed trait Movement

@JsonCodec case class Up() extends Movement
@JsonCodec case class Down() extends Movement
@JsonCodec case class Left() extends Movement
@JsonCodec case class Right() extends Movement

@JsonCodec case class Solution(moves: Seq[Movement])
