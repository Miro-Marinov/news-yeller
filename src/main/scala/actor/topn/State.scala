package finrax.actor.topn

import finrax.util.GenericMessage

import scala.reflect.runtime.universe.TypeTag

case class State[A: TypeTag](values: Vector[A]) extends GenericMessage[State[A]]