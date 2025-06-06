package oss.rtjvm.warmup

// 2
object MatchTypes {

  // Pattern matching at type level

  def lastDigitOf(number: BigInt): Int = (number % 10).toInt
  def lastCharOf(string: String): Char =
    if (string.isEmpty) throw new NoSuchElementException
    else string.charAt(string.length - 1)

  def lastElemOf[A](list: List[A]): A =
    if (list.isEmpty) throw new NoSuchElementException
    else list.last

  type ConstituentPartOf[A] = A match {
    case BigInt  => Int
    case String  => Char
    case List[a] => a // lowercase 'a' is a special syntax to match variables
  }

  val number: ConstituentPartOf[BigInt]  = 2
  val char: ConstituentPartOf[String]    = 'a'
  val elem: ConstituentPartOf[List[Int]] = 123

  // generic solution:
  def lastPartOf[A](thing: A): ConstituentPartOf[A] = thing match {
    case number: BigInt => (number % 10).toInt
    case string: String =>
      if (string.isEmpty) throw new NoSuchElementException
      else string.charAt(string.length - 1)
    case list: List[_] =>
      if (list.isEmpty) throw new NoSuchElementException
      else list.last
  }

  val lastPartOfString = lastPartOf("Scala")

  // The match type is far more flexible than the regular generics, because depending on the type definition of 'ConstituentPart[A]'
  // and depending on the parameter, we get different types, so not necessarily the exact 'A' on the type param that the method was
  // typed with.

  // Recursion on matched types
  type LowestLevelPartOf[A] = A match {
    case List[a] =>
      LowestLevelPartOf[a] // 'a' acts like a variable and 'LowestLevelPartOf' acts like a function which returns a type
    case _ => A
  }

  val lastPartOfNestedList: LowestLevelPartOf[List[List[List[Int]]]] = 10 // recursively unwrap the list to the last type: Int
  // It's like a recursive function but at type level
  // Recursion at match type is so powerful especially when you're dealing with nested types, like tuples, nested lists ...etc
  // This is helpful when we create new types at compile time
  // So be caution with stackoverflow, when the compiler detects infinite cycles, which can cause it to crash, like this example:

//  type AnnoyingMatchType[A] = A match {
//    case _ => AnnoyingMatchType[A]
//  }
  // This example won't compile because of an "illegal cyclic type reference 'AnnoyingMatchType'".
  // Compiler can detect cycles in match types

  // You can crash the compiler with this example:
  type InfiniteRecursion[A] = A match {
    case Int => InfiniteRecursion[A]
  }

//  val crash: InfiniteRecursion[Int] = 12
  // This example will return this error: "Recursion limit exceeded."
  
  
  def main(args: Array[String]): Unit = {
    println(lastPartOfString)
  }
}
