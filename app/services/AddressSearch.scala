package services

import javax.inject._
import scala.concurrent._
import scala.concurrent.duration._
import models.Address
import play.api.libs.ws._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import com.google.inject.ImplementedBy
import scala.util.{Try, Success, Failure}

@ImplementedBy(classOf[SolrAddressSearch])
trait AddressSearch {
  def search(q: String): Future[List[Address]]
  def bulkSearch(qs: List[String]): Future[List[Option[Address]]]
}

@Singleton
class SolrAddressSearch @Inject() (ws: WSClient)(implicit ec: ExecutionContext) extends AddressSearch {
  
  implicit val addressReads: Reads[Address] = (
        (JsPath \ "UPRN").read[Array[Long]] and
        (JsPath \ "FULL_ADDRESS").read[Array[String]] and
        (JsPath \ "score").read[Double]
      )(Address.apply _)
      
  def bulkSearch(qs: List[String]): Future[List[Option[Address]]] = {
    val listOfFutureResults = qs map searchTopResult
    val listOfFutureTryResults = listOfFutureResults map futureToFutureTry
    val futureListOfTryResults = Future.sequence(listOfFutureTryResults)
    val successfulFutureResults = futureListOfTryResults.map(_.filter(_.isSuccess).map(_.get))
    successfulFutureResults
  }
  
  def futureToFutureTry[T](f: Future[T]): Future[Try[T]] = f.map(Success(_)).recover({case x => Failure(x)})
  
  def searchTopResult(q: String): Future[Option[Address]] = search(q).map(_.headOption)
  
  def search(q: String): Future[List[Address]] = {
    val url = "http://localhost:8983/solr/address/select"
    val req  = ws.url(url).withRequestTimeout(1000.millis).withQueryString(
          "fl" -> "UPRN,FULL_ADDRESS,score",
          "df" -> "FULL_ADDRESS",
          "wt" -> "json",
          "indent" -> "true",
          "q.op" -> "AND",
          "q" -> q
        )
    
    req.get().map { response =>
      (response.json \ "response" \ "docs").as[List[Address]]
    }
  }
}