package oss.rtjvm.inlines

import scala.util.Random

// 1
object SimpleInlines {

  def increment(x: Int): Int = x + 1

  inline def inc(x: Int): Int = x + 1
  // The "inline" mark won't result to a method invocation, but instead the content of the method (x + 1) will be copied
  // at the place where you use this function

  val aNumber   = 3
  val four: Int = inc(aNumber) // Since 'inc' is 'inlined', the compiler will replace 'inc(3)' with its content 'aNumber + 1'
  // It's being done at compile time.

  // In order to see what the compiler gives as a result for the inline method, we need to start few terminals:

  // First terminal
  // run: `sbt clean cleanFiles compile` and since we have the sbt setting '-Xprint:postInlining' in the build.sbt file, we would see
  // all the scala code that the compiler would give us after the inlining step:
  /*
[info]     def increment(x: Int): Int = x.+(1)
[info]     inline def inc(x: Int): Int = x.+(1):Int
[info]     val aNumber: Int = 3
[info]     val four: Int = oss.rtjvm.inlines.SimpleInlines.aNumber.+(1):Int
   */

  // If we remove the 'inline' mark from 'inc' method, the result would be a method invocation:
  /*
[info]     def increment(x: Int): Int = x.+(1)
[info]     def inc(x: Int): Int = x.+(1)
[info]     val aNumber: Int = 3
[info]     val four: Int =
[info]       oss.rtjvm.inlines.SimpleInlines.inc(
[info]         oss.rtjvm.inlines.SimpleInlines.aNumber)
   */

  // We can as well inline the arguments of a function by passing them explicitly rather than evaluating them first before
  // function invocation/call:
  val eight: Int = inc(2 * aNumber + 1)
  /*
[info]     def increment(x: Int): Int = x.+(1)
[info]     inline def inc(x: Int): Int = x.+(1):Int
[info]     val aNumber: Int = 3
[info]     val four: Int = oss.rtjvm.inlines.SimpleInlines.aNumber.+(1):Int
[info]     val eight: Int =
[info]       {
[info]         val x$proxy1: Int = 2.*(oss.rtjvm.inlines.SimpleInlines.aNumber).+(1)
[info]         x$proxy1.+(1):Int
[info]       }
   */
  // interpretation: this reduces to:
  /*
    val proxy = 2 * aNumber + 1
    proxy + 1
   */

  // With inline arguments feature, we can define the following:
  inline def incia(inline x: Int): Int = x + 1

  val eightV2: Int = incia(2 * aNumber + 1)
  // In this case, it expands the variable directly in the method call without evaluating it first
  // This gives back: '2 * aNumber + 1 + 1', where '2 * aNumber + 1' is the argument and then we have the ' + 1' from 'incia'
  /*
[info]     inline def incia(inline x: Int): Int = x.+(1):Int
[info]     val eightV2: Int =
[info]       2.*(oss.rtjvm.inlines.SimpleInlines.aNumber).+(1).+(1):Int
   */
  // => Arguments can be expended to its expression in the method body (substitute the value in the method body) using
  //    inline without being computed first
  // => Conceptually this is similar to "by-name" invocation, but this is done at compile time directly
  // Only inline methods can have inline methods

  // Inline can be used for performance optimization, because instead of methods calls, you have the method body
  // directly injected into the code
  def testInline() = {
    inline def loop[A](inline start: A, inline condition: A => Boolean, inline advance: A => A)(inline action: A => Any) = {
      var a = start
      while (condition(a)) {
        action(a)
        a = advance(a)
      }
    }

    val start = System.currentTimeMillis()
    val r     = Random().nextInt(10000)
    val u     = Random().nextInt(10000)
    val arr   = Array.ofDim[Int](10000)

    loop(0, _ < 10000, _ + 1) { i =>
      loop(0, _ < 100000, _ + 1) { j =>
        arr(i) = arr(i) + u
      }
      arr(i) = arr(i) + r
    }
    println(s"Inline version: ${(System.currentTimeMillis() - start) / 1000.0} s")
  }

  def testNoInline() = {
    def loop[A](start: A, condition: A => Boolean, advance: A => A)(action: A => Any) = {
      var a = start
      while (condition(a)) {
        action(a)
        a = advance(a)
      }
    }

    val start = System.currentTimeMillis()
    val r     = Random().nextInt(10000)
    val u     = Random().nextInt(10000)
    val arr   = Array.ofDim[Int](10000)

    loop(0, _ < 10000, _ + 1) { i =>
      loop(0, _ < 100000, _ + 1) { j =>
        arr(i) = arr(i) + u
      }
      arr(i) = arr(i) + r
    }
    println(s"No inline version: ${(System.currentTimeMillis() - start) / 1000.0} s")
  }

  // Transparent inline: it means that the most concrete type definition of a function is going to be surfaced to the compiler
  // because the compiler has access to those concrete types at compile time
  inline def wrap(x: Int): Option[Int] = Some(x)
  val anOption: Option[Int]            = wrap(7) // compiles == type check is ok

  transparent inline def transparentWrap(x: Int): Option[Int] = Some(x)
  val anotherOption: Option[Int]                              = transparentWrap(7) // compiles == type check is ok
  // 'transparent': depending on the number you pass to 'transparentWrap' or depending on the compile-time information
  // that the body of the method gives to the compiler, I can use more concrete type than the one declared in the function
  // signature, which means I can do the following:
  val bOption: Some[Int] = transparentWrap(7) // compile == type check is ok .. this only works when we have set 'transparent'
  // We need to be careful with 'transparent' when working on methods that return big or recursive types, because it can
  // add an extra compiler burden and make the compilation slow

  def main(args: Array[String]): Unit = {
    testInline()
    testNoInline()
    // > sbt runMain oss.rtjvm.inlines.SimpleInlines

    // Result:
    /*
      Inline version: 0.107 s
      No inline version: 1.499 s
     */
    // => The inlined version is almost 15 times faster than the ordinary way, because in the inlined version the double
    // "loop" function, we don't have function calls, but we rather have 'while' loops, so no function calls and no indirection
    // that can cause the extra overhead
  }
}
