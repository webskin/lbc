package controllers

import play.api._
import play.api.mvc._

import play.api.libs.json._
import play.api.libs.functional.syntax._

import models._
import scala.concurrent.Future
import play.api.libs.ws.WS
import play.api.libs.iteratee.{Iteratee, Enumeratee, Enumerator}
import play.api.templates.Html
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.Comet

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  // --------------------------------------------------------------------------
  // Action
  // --------------------------------------------------------------------------
  /*

  object Action extends ActionBuilder[Request] …

  ActionBuilder :
  --------------------------

  //
  // Constructs an `Action`.
  //
  // For example:
  // {{{
  // val echo = Action(parse.anyContent) { request =>
  //   Ok("Got request [" + request + "]")
  // }
  // }}}
  //
  // @tparam A the type of the request body
  // @param bodyParser the `BodyParser` to use to parse the request body
  // @param block the action code
  // @return an action
  //
  final def apply[A](bodyParser: BodyParser[A])(block: R[A] => Result): Action[A] = async(bodyParser) { req: R[A] =>
    block(req) match {
      case simple: SimpleResult => Future.successful(simple)
      case async: AsyncResult => async.unflatten
    }
  }

  //
  // Constructs an `Action` with default content.
  //
  // For example:
  // {{{
  // val echo = Action { request =>
  //   Ok("Got request [" + request + "]")
  // }
  // }}}
  //
  // @param block the action code
  // @return an action
  //
  final def apply(block: R[AnyContent] => Result): Action[AnyContent] = apply(BodyParsers.parse.anyContent)(block)

  //
  // Constructs an `Action` with default content, and no request parameter.
  //
  // For example:
  // {{{
  // val hello = Action {
  //   Ok("Hello!")
  // }
  // }}}
  //
  // @param block the action code
  // @return an action
  //
  final def apply(block: => Result): Action[AnyContent] = apply(_ => block)

  Todo:
  ------------------

  Provides an empty `Action` implementation: the result is a standard ‘Not implemented yet’ result page.

  For example:
  {{{
  def index(name:String) = TODO
  }}}

  val TODO = Action {
    // Status -|> SimpleResult
    // new Status[play.api.templates.Html](NOT_IMPLEMENTED)(views.html.defaultpages.todo())
    NotImplemented[play.api.templates.Html](views.html.defaultpages.todo())
  }

   */

  def test1 = TODO

  def test2 = Action {
    /*
    new Status(NOT_IMPLEMENTED) // SimpleResult(501, Map())

    // Set the result's content.
    //
    // @param content The content to send.
    //
    def apply[C](content: C)(implicit writeable: Writeable[C]): SimpleResult = {
      SimpleResult(
        ResponseHeader(status, writeable.contentType.map(ct => Map(CONTENT_TYPE -> ct)).getOrElse(Map.empty)),
        Enumerator(writeable.transform(content))
      )
    }
                              apply(htmlContent)(implicit writeable: Writeable[html content])
                               |
                               v
    new Status(NOT_IMPLEMENTED)(views.html.defaultpages.todo())

    */

    new Status(NOT_IMPLEMENTED)(views.html.defaultpages.todo())
  }

  def test3 = Action {
    // no content
    new Status(NOT_IMPLEMENTED)
  }

  def test4 = Action {
    // no content idem test3
    NotImplemented
  }

  // --------------------------------------------------------------------------
  // SimpleResult
  // --------------------------------------------------------------------------
  /**
   * Adds HTTP headers to this result.
   * For example:
   * {{{
   * Ok("Hello world").withHeaders(ETAG -> "0")
   * }}}
   * def withHeaders(headers: (String, String)*): A
   */

  def test5 = Action {
    Ok("ææææ") // UTF-8 => - octet
  }


  /*
  Content-Length:4
  Content-Type:text/plain; charset=utf-8
  X-LBC:bb
  X-LBC2:cc
   */
  def test6 = Action {
    Ok("test").withHeaders(("X-LBC", "bb"), ("X-LBC2" -> "cc"))
  }

  def test7 = Action { implicit request =>
    Ok(s"test : ${request.headers.get(USER_AGENT)}").withHeaders(("X-LBC", "bb"), ("X-LBC2" -> "cc"))
  }

  def test8 = Action(parse.anyContent) { implicit request =>
    Ok(s"test : ${request.headers.get(USER_AGENT)}").
      withHeaders(("X-LBC", "bb"), ("X-LBC2" -> "cc"))
  }

  // --------------------------------------------------------------------------
  // Async
  // --------------------------------------------------------------------------

  def test9 = Action.async { implicit request =>
    Future.successful(Ok(s"test : ${request.headers.get(USER_AGENT)}").
      withHeaders(("X-LBC", "bb"), ("X-LBC2" -> "cc")))
  }

  def test10 = Action.async { implicit request =>
    WS.url("https://www.google.fr/search?q=test9").get().map(response => {
      if (response.status == 200) Ok("The website is up") else NotFound("The website is down")
    })
  }

  def test11 = Action.async { implicit request =>
    WS.url("https://www.google.fr/search?q=test9").get().map(response => {
      if (response.status == 200) Ok(response.body).as(HTML) else NotFound("The website is down")
    })
  }

  def test12 = Action {
    Ok.sendFile(
      content = new java.io.File("/home/webskin/Downloads/Functional_Programmi_v13_MEAP.pdf"),
      inline = true
    )
  }

  def test13 = Action {
    Ok.sendFile(
      content = new java.io.File("/home/webskin/Downloads/Functional_Programmi_v13_MEAP.pdf"),
      inline = true
    ).withHeaders(ETAG -> "112233", CACHE_CONTROL -> "max-age=3600, s-maxage=600, must-revalidate")
  }


  def test14 = Action {
    Ok.chunked(
      Enumerator("kiki", "foo", "bar").andThen(Enumerator.eof)
    )
  }

  def test15 = Action {
    val events = Enumerator(
      """<script>console.log('kiki')</script>""",
      """<script>console.log('foo')</script>""",
      """<script>console.log('bar')</script>"""
    )
    Ok.chunked(events >>> Enumerator.eof).as(HTML)
  }

  val toCometMessage = Enumeratee.map[String] { data =>
    Html("""<script>console.log('""" + data + """')</script>""")
  }

  /*
  We can write this in a better way by using play.api.libs.iteratee.Enumeratee that is just an adapter to transform an
  Enumerator[A] into another Enumerator[B]. Let’s use it to wrap standard messages into the <script> tags:

  Writing events >>> Enumerator.eof &> toCometMessage is just another way of
  writing events.andThen(Enumerator.eof).through(toCometMessage)
   */
  def test16 = Action {
    val events = Enumerator("kiki", "foo", "bar")
    Ok.chunked((events &> toCometMessage) >>> Enumerator.eof)
  }

  /*
  We provide a Comet helper to handle these Comet chunked streams that do almost the same stuff that we just wrote.
  Actually it does more, like pushing an initial blank buffer data for browser compatibility, and it supports both
  String and JSON messages. It can also be extended via type classes to support more message types.
   */
  def test17 = Action {
    val events = Enumerator("kiki", "foo", "bar")
    Ok.chunked((events &> Comet(callback = "console.log")) >>> Enumerator.eof)
  }

  /*
  The forever iframe technique
  The standard technique to write a Comet socket is to load an infinite chunked comet response in an HTML iframe
  and to specify a callback calling the parent frame:

  def comet = Action {
    val events = Enumerator("kiki", "foo", "bar")
    Ok.stream((events &> Comet(callback = "parent.cometMessage")) >>> Enumerator.eof)
  }

  <script type="text/javascript">
    var cometMessage = function(event) {
      console.log('Received event: ' + event)
    }
  </script>

  <iframe src="/comet"></iframe>

   */

  def test18 = WebSocket.using[String] { request =>

    // Log events to the console
    val in = Iteratee.foreach[String](println).map { _ =>
      println("Disconnectedd")
    }

    // Send a single 'Hello!' message
    val out = Enumerator("Hello!", "kiki", "foo", "bar")

    (in, out)
  }
}

/*

sse


/**
https://github.com/remy/polyfills/blob/master/EventSource.js polyfill
modernizr


// Serves Server Sent Events over HTTP connection
def tweetFeed() = Action {
  implicit req => {
    /** Creates enumerator and channel for Strings through Concurrent factory object
     * for pushing data through the WebSocket */
    val (out, wsOutChannel) = Concurrent.broadcast[JsValue]

    [...]

    Ok.feed(out &> EventSource()).as("text/event-stream")
    }
  }


  client side
    var feed = new EventSource('/tweetFeed');
  feed.addEventListener('message', handler, false);
 */