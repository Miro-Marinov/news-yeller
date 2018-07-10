package finrax.util

import scala.reflect.runtime.universe.{TypeTag, typeOf}

/** Handy class for working around erased generics in actor receive blocks.
  *
  * Typical usage: {{{
  *   val myMessage = GenericMessage.matcher[MyMessage[Int]]
  *   def receive: Receive = {
  *     case myMessage(msg) => // do something with msg: MyMessage[Int]
  *   }
  * }}}
  *
  * ==Detail==
  *
  * The `GenericMessage` class is intended to be subclassed by message classes that will be sent between Akka actors,
  * and specifically those with generic type parameters. In Akka, actors are required to be able to handle messages with
  * no compile time type information (i.e. messages of type `Any`). Each actor class defines a receive block that
  * matches against types of message, and defines an action for each type. As this check takes place at runtime, only
  * the runtime type information is available. That means it is not possible to distinguish between two different
  * specialisations of a generic type, e.g. this will not work as you expect:
  * {{{
  * case class MyMessage[A](values: List[A])
  *
  * class MyActor extends Actor {
  *   def receive = {
  *     case a: MyMessage[Int] => ...
  *     case b: MyMessage[String] => ...
  *   }
  * }
  * }}}
  *
  * Although you might expect `MyMessage[Int]` and `MyMessage[String]` to be handled separately, they actually won't be
  * - at runtime the generic type information is erased, and so they become indistinguishable. What will actually happen
  * is that both specialisations of the message type will be handled by case a. This could ultimately lead to a
  * `ClassCastException` if you try to get the values out of the message and find they're of an unexpected type.
  *
  * `GenericMessage` can resolve this problem. It adds a method called `conformsTo` that is intended to be used as an
  * if-guard in a pattern match. It's not necessary to understand how it works, but the end result would look like this:
  * {{{
  * case class MyMessage[A](values: List[A]) extends GenericMessage[MyMessage[A]]
  *
  * class MyActor extends Actor {
  *   def receive = {
  *     case a: MyMessage[Int] if a.conformsTo[MyMessage[Int]] => ...
  *     case b: MyMessage[String] if b.conformsTo[MyMessage[String]] => ...
  *   }
  * }
  * }}}
  *
  * Using this approach, `MyMessage[Int]` and `MyMessage[String]` will actually be handled separately.
  *
  * Although this approach works, the pattern match is quite awkward to write and read - in particular it's unfortunate
  * that the full type of the message has to be written out twice. `GenericMessage.matcher[T]` can be used to avoid this
  * boilerplate. It returns a pattern matcher that will match a particular specialisation of the generic message type
  * (internally using the same approach as above). Here's the example updated to use it:
  * {{{
  * case class MyMessage[A](values: List[A]) extends GenericMessage[A]
  *
  * class MyActor extends Actor {
  *   private val myMessageInt = GenericMessage.matcher[MyMessage[Int]]
  *   private val myMessageString = GenericMessage.matcher[MyMessage[String]]
  *
  *   def receive = {
  *     case myMessageInt(a) => ...
  *     case myMessageString(b) => ...
  *   }
  * }
  * }}}
  *
  * This works the same as the code in the last example, but allows for a much simpler-looking receive block. Note that
  * you cannot call `GenericMessage.matcher[T]` directly in the match due to it not being a stable identifier - you must
  * assign it to a variable.
  */
object GenericMessage {
  sealed trait Matcher[T] { def unapply(msg: GenericMessage[_]): Option[T] }

  def matcher[U <: GenericMessage[U] : TypeTag]: Matcher[U] = new Matcher[U] {
    def unapply(msg: GenericMessage[_]): Option[U] = msg match {
      case t: U @unchecked if t.conformsTo[U] => Some(t)
      case _ => None
    }
  }
}

abstract class GenericMessage[+T : TypeTag] extends Serializable {
  // TODO: Why?
  this: T =>
  def conformsTo[U >: T : TypeTag]: Boolean = typeOf[T] <:< typeOf[U]
}
