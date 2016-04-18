package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import services.AddressSearch
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(addressSearch: AddressSearch)(implicit ec: ExecutionContext) extends Controller {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def search = Action {
    Ok
  }
  
  def results(q: String) = Action.async {
    val futResults = addressSearch.search(q)
    futResults.map { results =>
      Ok(views.html.results(q, results))
    }
  }
  
  def bulkResults = Action.async { req =>
    req.body.asText.map { bodyText =>
      val lines = bodyText.split("\n").toList
      val futResults = addressSearch.bulkSearch(lines)
      futResults.map { r => Ok(r.mkString("\n"))}
    }.getOrElse(Future.successful(BadRequest))
  }
}
