package oss.rtjvm.inlines

// 2
object InlineConditions {

  inline def condition(b: Boolean): String =
    if (b) "Yes"
    else "No"

  val positive: String = condition(true) // 'condition' will be replaced by the compiler with the 'if else' statements
  /*
  val positive =  if (b) "Yes" else "No"
   */
  // This can be inlined even further by making the constants "Yes" and "No" known to the compiler at the compile time by
  // inlining the 'if else' expression as well:
  inline def condition2(b: Boolean): String =
    inline if (b) "Yes"
    else "No"

  val positive2: String = condition(true)
  /*
    val positive2 =  if (true) "Yes" else "No"
    => The result will be "Yes" and it's computed at compile time, because 'b' is known already to be 'true', since it's inlined
   */

  // The compiler can do basic computation, for instance with Boolean
  val positive3: String = condition(true && !false) // is also known to be "Yes" at compile time

  // The compiler cannot do other computations or other forms of inference, like:
//  val variable = true
//  val result: String = condition2(variable) // This won't compile because the value of 'variable' is not known in the general
  // case at the call site (variable not known statically)

  // We can also compute different types depending on the value of the condition so we can also return the appropriate
  // type of these 'if else' branches:
  transparent inline def conditionUnion(b: Boolean): String | Int =
    inline if (b) "Yes"
    else 0
  // => Since the 'if else' expression is inlined as well, it means the type is going to be known as well at compile time:
  val aString: String = conditionUnion(true)          // known at compile time to be String
  val anInt: Int      = conditionUnion(false)         // known at compile time to be Int
  val anotherInt: Int = conditionUnion(true && false) // known at compile time to be Int

  // inline match
  inline def matcher(x: Int): String = inline x match {
    case 1 => "one"
    case 2 => "two"
    case 3 => "three"
    case _ => "other" // necessary for compilation since the inline matcher doesn't have exhaustive checks
  }

  val theOne: String = matcher(1) // this is known to be "one" at compile time
  /*
  Compilation:
    val theOne: String = "one":String
   */
  // Unfortunately, the inline matcher does not have exhaustiveness checks, so if we don't provide the 'case _ => "other"' case
  // the code won't compile
  val other: String = matcher(99) // this won't compile unless 'case _ => "other"' is defined

  transparent inline def transparentMatcher(x: Int): String | Int = inline x match {
    case 1 => 1
    case 2 => "two"
    case 3 => "three"
    case _ => 0
  }

  val theOneInt: Int       = transparentMatcher(1)
  val theTwoString: String = transparentMatcher(2)

  inline def matchOption(x: Option[Any]): String =
    inline x match {
      case Some(value: String) => value
      case Some(value: Int)    => value.toString
      case None                => "nothing"
    }

  val something: String = matchOption(Some("something"))
//  val aBoolean = matchOption(Some(true)) // this won't compile, because we're not pattern matching 'Option[Boolean]' case
//    No exhaustiveness

  val anOption: Option[String] = Option("this is a string")
//  val matchedString = matchOption(anOption) // this won't compile, because we're not matching 'Option[String]', but matching 'Some[String]'
//  The pattern match in "matchOption" is general / not specific enough
//  This won't compile, but at runtime it would work and return "this is a string"

  // Recursion:
  inline def sum(n: Int): Int =
    inline if (n <= 0) 0
    else n + sum(n - 1)

  val ten: Int = sum(4) // 10
  /*
  Compilation:
     inline def sum(n: Int): Int =
       (if n.<=(0) then 0 else
         n.+(oss.rtjvm.inlines.InlineConditions.sum(n.-(1)))):Int
     val ten: Int = 10:Int
   */
  // The inline recursion run at compile time, that's why we see "ten" equal to "10"
  // The recursion has its limits, so sum(400000000) for example can result a stackoverflow error

  // Keeping the precise type with 'transparent inline':
  transparent inline def transparentSum(n: Int): Int =
    inline if (n <= 0) 0
    else n + transparentSum(n - 1)

  val transparentTen: 10 = transparentSum(4)
  // The compiler gives 'transparentTen' the type '10', precisely
  /*
  Compilation:
    val transparentTen: 10.type = 10
   */
}
