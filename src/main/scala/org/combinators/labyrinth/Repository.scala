package org.combinators.labyrinth

import org.combinators.cls.interpreter.{ReflectedRepository, combinator}
import org.combinators.cls.types._
import org.combinators.cls.types.syntax._
import org.combinators.labyrinth.protocol._

class Repository(task: Task) {
  val width: Int = task.labyrinth.map(_.length).min
  val height: Int = task.labyrinth.length

  def intToType(x: Int): Type =
    (1 to x).foldLeft[Type]('Z)((n, _) => 'S(n))

  def anyPos(v: Variable, limit: Int): Kinding =
    (0 until limit).foldLeft(Kinding(v))((k, n) => k.addOption(intToType(n)))

  val positionRow = Variable("posRow")
  val positionColumn = Variable("posCol")

  val kinding = anyPos(positionRow, width).merge(anyPos(positionColumn, height))

  @combinator object start {
    def apply: Seq[Movement] = Seq.empty
    val semanticType: Type = 'Pos(intToType(task.start._1), intToType(task.start._2))
  }

  @combinator object up {
    def apply(moves: Seq[Movement], isFree: Unit): Seq[Movement] = Up() +: moves
    val semanticType: Type = 'Pos(positionRow, 'S(positionColumn)) =>: 'Free(positionRow, positionColumn) =>: 'Pos(positionRow, positionColumn)
  }

  @combinator object down {
    def apply(moves: Seq[Movement], isFree: Unit): Seq[Movement] = Down() +: moves
    val semanticType: Type = 'Pos(positionRow, positionColumn) =>: 'Free(positionRow, 'S(positionColumn)) =>: 'Pos(positionRow, 'S(positionColumn))
  }

  @combinator object left {
    def apply(moves: Seq[Movement], isFree: Unit): Seq[Movement] = Left() +: moves
    val semanticType: Type = 'Pos('S(positionRow), positionColumn) =>: 'Free(positionRow, positionColumn) =>: 'Pos(positionRow, positionColumn)
  }

  @combinator object right {
    def apply(moves: Seq[Movement], isFree: Unit): Seq[Movement] = Right() +: moves
    val semanticType: Type = 'Pos(positionRow, positionColumn) =>: 'Free('S(positionRow), positionColumn) =>: 'Pos('S(positionRow), positionColumn)
  }

  class FreeField(row: Int, col: Int) {
    def apply: Unit = ()
    val semanticType: Type = 'Free(intToType(row), intToType(col))
  }

  private def addFreeFields(toRepo: ReflectedRepository[Repository]): ReflectedRepository[Repository] = {
    val freeFields: Seq[FreeField] =
      (0 until height).flatMap(row => (0 until width).collect {
        case col if task.labyrinth(row)(col) => new FreeField(row, col)
      })
    freeFields.foldLeft(toRepo) {
      case (repo, freeField) => repo.addCombinator(freeField)
    }
  }

  val semanticTarget: Type = 'Pos (intToType(task.goal._1), intToType(task.goal._2))
  type nativeTarget = Seq[Movement]

  def reflected: ReflectedRepository[Repository] = {
    val repo = ReflectedRepository(
        this,
        classLoader = this.getClass.getClassLoader,
        substitutionSpace = this.kinding
      )
    addFreeFields(repo)
  }
}
