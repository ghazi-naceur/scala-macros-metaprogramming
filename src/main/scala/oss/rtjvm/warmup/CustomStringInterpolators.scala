package oss.rtjvm.warmup

// 1
object CustomStringInterpolators {

  // s-interpolation
  val pi = 3.14159
  val sInterpolator = s"The value is ${pi + 0.000002}"
  // side note: The floating point standard can lose precision on some numbers and some calculations, especially for 'Mod'
  // or multiplication or division or even an addition with the Double type.
  // This is why you should never use 'Double' type to represent money, otherwise you risk to lose precision

  // f-interpolator, similar to 'printf'
  val fIntepolator = f"The value of pi up to 3 significant digits is $pi%3.2f"

  // raw-interpolator: escape sequences
  val rawInterpolator = raw"The value of ppi is $pi\n this is not a new line, because it's escaped with 'raw'"

  // custom interpolator; sql"select * from table" ... etc
  case class Person(name: String, age: Int)

  // name,age => Person
  def string2Person(line: String): Person = {
    val tokens = line.split(",")
    Person(tokens(0), tokens(1).toInt)
  }
  // The goal is to provide 'person"Netero,125"' and get 'Person(Netero, 125)' as a result
  // To achieve that, you need: StringContext + extension method
  // The extension method should be the name of the interpolator you want to add
  extension(sc: StringContext) {
    def person(args: Any*): Person = {
      val concat = sc.s(args*) // The '.s' is the s-interpolator. It will concatenate the whole input. Respreading the args using '*' is essential here
      string2Person(concat)
    }
  }

  val person = person"Netero,125"
  val name = "Isaac"
  val age = 125
  val person2 = person"$name,$age"


  def main(args: Array[String]): Unit = {

    println(sInterpolator)
    println(fIntepolator)
    println(rawInterpolator)
    println(person)
    println(person2)
  }
}
