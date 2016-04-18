package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import services.BusinessSearch
import scala.concurrent.ExecutionContext

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class BusinessSearchController @Inject()(addressSearch: BusinessSearch)(implicit ec: ExecutionContext) extends Controller {

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
      Ok(views.html.businessResults(q, results))
    }
  }
}
