package serializaiton

import java.text.SimpleDateFormat
import java.util.Locale

import org.json4s.{DefaultFormats, Formats}

/**
  *
  */
trait JsonSupport {
  implicit val formats: Formats =  new DefaultFormats {
    override def dateFormatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZ yyyy", Locale.ENGLISH)
  }.preservingEmptyValues
}
