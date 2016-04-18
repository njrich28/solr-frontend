package services

import javax.inject._
import scala.concurrent._
import scala.concurrent.duration._
import models.BusinessResult
import play.api.libs.ws._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import com.google.inject.ImplementedBy

@ImplementedBy(classOf[SolrBusinessSearch])
trait BusinessSearch {
  def search(q: String): Future[List[BusinessResult]]
}

@Singleton
class SolrBusinessSearch @Inject() (ws: WSClient)(implicit ec: ExecutionContext) extends BusinessSearch {
  
  implicit val addressReads: Reads[BusinessResult] = (
        (JsPath \ "CompanyName").read[String] and
        (JsPath \ "score").read[Double]
      )(BusinessResult.apply _)
  
  def search(q: String): Future[List[BusinessResult]] = {
    val url = "http://localhost:8983/solr/business/select"
    val req  = ws.url(url).withRequestTimeout(1000.millis).withQueryString(
          "fl" -> "CompanyName,score",
          "df" -> "CompanyName",
          "wt" -> "json",
          "indent" -> "true",
          "q.op" -> "AND",
          "q" -> q
        )
    
    req.get().map { response =>
      (response.json \ "response" \ "docs").as[List[BusinessResult]]
    }
  }
}