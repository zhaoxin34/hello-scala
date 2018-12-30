package joky.test

import java.io.File

case class Config(foo: Int = -1, out: File = new File("."), xyz: Boolean = false,
                  libName: String = "", maxCount: Int = -1, verbose: Boolean = false, debug: Boolean = false,
                  mode: String = "", files: Seq[File] = Seq(), keepalive: Boolean = false,
                  jars: Seq[File] = Seq(), kwargs: Map[String,String] = Map())

/**
  * @Auther: zhaoxin
  * @Date: 2018/12/14 10:09
  * @Description:
  *              如下示例可在sbt shell中执行，或者sbt "runMain joky.ScoptTest --help"
  *              runMain joky.ScoptTest --help
  *              runMain joky.ScoptTest "update" --nk 1 --out . --foo=1 --jars=a,b,c
  */
object ScoptTest extends App {
    val parser = new scopt.OptionParser[Config]("scopt") {
        head("scopt", "3.x")

        opt[Int]('f', "foo").action( (x, c) =>
            c.copy(foo = x) ).text("foo is an integer property")

        opt[File]('o', "out").required().valueName("<file>").
            action( (x, c) => c.copy(out = x) ).
            text("out is a required file property")

        opt[(String, Int)]("max").action({
            case ((k, v), c) => c.copy(libName = k, maxCount = v) }).
            validate( x =>
                if (x._2 > 0) success
                else failure("Value <max> must be >0") ).
            keyValueName("<libname>", "<max>").
            text("maximum count for <libname>")

        opt[Seq[File]]('j', "jars").valueName("<jar1>,<jar2>...").action( (x,c) =>
            c.copy(jars = x) ).text("jars to include")

        opt[Map[String,String]]("kwargs").valueName("k1=v1,k2=v2...").action( (x, c) =>
            c.copy(kwargs = x) ).text("other arguments")

        opt[Unit]("verbose").action( (_, c) =>
            c.copy(verbose = true) ).text("verbose is a flag")

        opt[Unit]("debug").hidden().action( (_, c) =>
            c.copy(debug = true) ).text("this option is hidden in the usage text")

        help("help").text("prints this usage text")

        arg[File]("<file>...").unbounded().optional().action( (x, c) =>
            c.copy(files = c.files :+ x) ).text("optional unbounded args")

        note("some notes.")

        cmd("update").action( (_, c) => c.copy(mode = "update") ).
            text("update is a command.").
            children(
                opt[Unit]("not-keepalive").abbr("nk").action( (_, c) =>
                    c.copy(keepalive = false) ).text("disable keepalive"),
                opt[Boolean]("xyz").action( (x, c) =>
                    c.copy(xyz = x) ).text("xyz is a boolean property"),
                opt[Unit]("debug-update").hidden().action( (_, c) =>
                    c.copy(debug = true) ).text("this option is hidden in the usage text"),
                checkConfig( c =>
                    if (c.keepalive && c.xyz) failure("xyz cannot keep alive")
                    else success )
            )
    }

    // parser.parse returns Option[C]
    parser.parse(args, Config()) match {
        case Some(config) => execute(config)

        case None => println("config error")
    }

    def execute(config: Config): Unit = {
        println(config)
    }
}
