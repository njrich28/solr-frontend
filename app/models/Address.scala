package models

case class Address(uprn: Array[Long], fullAddress: Array[String], score: Double) {
  override def toString = s"[${uprn.mkString(" ")}, $score] ${fullAddress.mkString(" ")}"
}