package serializaiton

import java.util

import com.google.inject.{Inject, Singleton}
import org.apache.kafka.common.serialization.{LongSerializer, Serializer}

@Singleton
class ScalaLongSerializer @Inject()(longSerializer: LongSerializer) extends Serializer[Long] {
  def serialize(topic: String, data: Long): Array[Byte] = {
    longSerializer.serialize(topic, java.lang.Long.valueOf(data))
  }

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}

  override def close(): Unit = {}
}