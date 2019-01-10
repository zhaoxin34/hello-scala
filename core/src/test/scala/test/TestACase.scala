package test

import org.scalatest._

import collection.mutable.Stack
import org.scalatest.tagobjects.Slow
object DbTest extends Tag("com.mycompany.tags.DbTest")

class TestACase extends FlatSpec {
    "A Stack" should "pop values in last-in-first-out order" in {
        val stack = new Stack[Int]
        stack.push(1)
        stack.push(2)
        assert(stack.pop() === 2)
        assert(stack.pop() === 1)
    }


    it should "throw NoSuchElementException if an empty stack is popped" in {
        val emptyStack = new Stack[String]
        assertThrows[NoSuchElementException] {
            emptyStack.pop()
        }
    }

    "The Scala language" must "add correctly" taggedAs(Slow) in {
        val sum = 1 + 1
        assert(sum === 2)
    }

    it must "subtract correctly" taggedAs(Slow, DbTest) in {
        val diff = 4 - 1
        assert(diff === 3)
    }
}

