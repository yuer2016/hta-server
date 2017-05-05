package com.yicheng.statistics.common

import java.io.IOException
import java.text.SimpleDateFormat

import com.yicheng.statistics.repo.model.Annotation.DeserInnerMap
import com.fasterxml.jackson.annotation.{JsonRawValue, JsonUnwrapped}
import com.fasterxml.jackson.core._
import com.fasterxml.jackson.core.json.JsonWriteContext
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.`type`.MapLikeType
import com.fasterxml.jackson.databind.cfg.MapperConfig
import com.fasterxml.jackson.databind.deser._
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.introspect._
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.ser._
import com.fasterxml.jackson.databind.ser.impl.{UnwrappingBeanPropertyWriter, UnwrappingBeanSerializer}
import com.fasterxml.jackson.databind.ser.std.RawSerializer
import com.fasterxml.jackson.databind.util.NameTransformer
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.{DefaultScalaModule, JacksonModule}
import com.google.protobuf.ByteString
import com.google.protobuf.Descriptors.{EnumValueDescriptor, FieldDescriptor}
import com.trueaccord.scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}

/**
  * Created by yuer on 2016/12/16.
  */
object JsonMapper extends ObjectMapper with ScalaObjectMapper {
  registerModule(new DefaultScalaModule with JsonMapper.ExtensionModule)
  setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ"))

  //********************************************************************************
  // 自定义字段类型：
  //********************************************************************************
  // 自定义字段类型：输出原始JSON字符串的类型
  @JsonRawValue
  trait PropertyJsonRaw

  object PropertyJsonRaw {
    def apply(json: String) = new PropertyJsonRaw { override def toString: String = json }
  }

  // 自定义字段类型：自动提升成上级对象属性的类型
  @JsonUnwrapped
  trait PropertyUnwrapped

  // 自定义字段类型：上级对象中输出原始JSON字符串类型
  trait PropertyUnwrappedRaw extends PropertyUnwrapped with PropertyJsonRaw

  object PropertyUnwrappedRaw {
    def apply(json: String) = new PropertyUnwrappedRaw { override def toString: String = json }
  }

  /**
    * JSON序列化/反序列化的扩展类。实现以下扩展：
    * 1、Protobuf消息的序列化/反序列化
    * 2、InfoModel消息序列化时提升到上级对象
    */
  private trait ExtensionModule extends JacksonModule {
    // 注册Protobuf的消息序列化/反序列化扩展
    this += ProtobufSerializer
    this += ProtobufDeserializer
    // 注册自定义类型字段的序列化扩展。主要扩展如下：
    // 1、PropertyJsonRaw：对象序列化时相应部分输出原始JSON字符串，JSON字符串为对象toString的内容。
    // 2、PropertyUnwrapped：对象序列化时自动提升成上级对象的属性。
    // 3. 同时使用JsonRawValue/JsonUnwrapped：对象序列化时输出原始JSON字符串，并自动提升成上级对象的属性。
    // 4、自定义注释@DeserInnerXXX：支持容器内Map对象反序列化时指定Map的格式。
    //    this += UserDefinedSerializerModifier
    //    this += (_ addBeanDeserializerModifier UserDefinedDeserializerModifier)
    this += (_ insertAnnotationIntrospector UserDefinedAnnotationIntrospector)

    import com.fasterxml.jackson.databind.node.JsonNodeType
    import com.google.protobuf.Descriptors.FieldDescriptor.{JavaType => MessageType}

    import scala.collection.JavaConversions._

    // 自定义类型数据的序列化扩展：PropertyUnwrapped/PropertyJsonRaw/同时使用JsonRawValue和JsonUnwrapped
    private object UserDefinedAnnotationIntrospector extends JacksonAnnotationIntrospector {
      // 处理自定义注释@DeserInnerXXX
      override def refineDeserializationType(config: MapperConfig[_], a: Annotated, baseType: JavaType): JavaType = {
        val javaType = super.refineDeserializationType(config, a, baseType)
        Option(a.getAnnotation(classOf[DeserInnerMap])).map(ann => {
          Option(javaType.getContentType).filter(_.isMapLikeType).map(innerType => {
            if ((ann.keyAs() eq classOf[Void]) && (ann.contentAs() eq classOf[Void])) javaType
            else if (ann.keyAs() eq classOf[Void]) javaType.withContentType(innerType.withContentType(constructType(ann.contentAs())))
            else if (ann.contentAs() eq classOf[Void]) javaType.withContentType(innerType.asInstanceOf[MapLikeType].withKeyType(constructType(ann.keyAs())))
            else javaType.withContentType(innerType.asInstanceOf[MapLikeType].withKeyType(constructType(ann.keyAs())).withContentType(constructType(ann.contentAs())))
          }).getOrElse(javaType)
        }).getOrElse(javaType)
      }
      // 处理PropertyUnwrapped类型
      override def findUnwrappingNameTransformer(member: AnnotatedMember): NameTransformer = {
        super.findUnwrappingNameTransformer(member) match {
          case null =>
            Option({
              if (classOf[PropertyUnwrapped].isAssignableFrom(member.getType.getRawClass)) {
                findAnnotatedUnwrapped(AnnotatedClass.construct(member.getType, getDeserializationConfig))
              } else if (member.getType.isReferenceType && classOf[PropertyUnwrapped].isAssignableFrom(member.getType.getReferencedType.getRawClass)) {
                findAnnotatedUnwrapped(AnnotatedClass.construct(member.getType.getReferencedType, getDeserializationConfig))
              } else null
            }).map(ann => NameTransformer.simpleTransformer(ann.prefix, ann.suffix)).orNull
          case x => x
        }
      }
      // 处理同时使用JsonRawValue和JsonUnwrapped
      override def findSerializer(a: Annotated): AnyRef = {
        super.findSerializer(a) match {
          case ser: RawSerializer[_] =>
            Option(findAnnotatedUnwrapped(a)).map(x => RawUnwrappedWriter.unwrappedSerializer)
              .getOrElse(Option(findAnnotatedUnwrapped(AnnotatedClass.construct(a.getType, getDeserializationConfig)))
                .map(x => RawUnwrappedWriter.unwrappedSerializer).orNull)
          case x => x
        }
      }

      def findAnnotatedUnwrapped(a: Annotated): JsonUnwrapped = {
        Option(_findAnnotation(a, classOf[JsonUnwrapped])).map(ann => if (ann.enabled()) ann else null).orNull
      }

      private object RawUnwrappedWriter extends FakeUnwrappedBeanWriter(classOf[String]) {
        override def serializeAsField(bean: scala.Any, gen: JsonGenerator, prov: SerializerProvider): Unit = {
          Option(bean).map(_.toString).map(json => {
            val begin = json.indexOf('{')
            val end = json.lastIndexOf('}')
            val raw = if (begin >= 0 && end >= begin) json.substring(begin + 1, end).trim else json.trim
            if (!raw.isEmpty) {
              val writeContext = gen.getOutputContext.asInstanceOf[JsonWriteContext]
              if (writeContext.writeFieldName(null) == JsonWriteContext.STATUS_OK_AFTER_COMMA) gen.writeRaw(',')
              gen.writeRaw(raw)
              writeContext.writeValue()
            }
          })
        }
      }
    }

    // 注册Protobuf的消息序列化扩展
    private object ProtobufSerializer extends Serializers.Base {
      override def findSerializer(config: SerializationConfig, javaType: JavaType, beanDescription: BeanDescription): JsonSerializer[GeneratedMessage] = {
        if (classOf[GeneratedMessage].isAssignableFrom(javaType.getRawClass)) ProtobufWriter else null
      }
    }

    private object ProtobufWriter extends JsonSerializer[GeneratedMessage] with ContextualSerializer {
      override def createContextual(prov: SerializerProvider, property: BeanProperty): JsonSerializer[_] = {
        if (property.isInstanceOf[UnwrappingBeanPropertyWriter]) ProtobufUnwrappedWriter.unwrappedSerializer else this
      }

      override def serialize(value: GeneratedMessage, gen: JsonGenerator, provider: SerializerProvider): Unit = {
        gen.writeStartObject()
        value.getAllFields.foreach {
          case (fd, v) => gen.writeObjectField(fd.getJsonName, serializeField(fd, v))
        }
        gen.writeEndObject()
      }
    }

    private object ProtobufUnwrappedWriter extends FakeUnwrappedBeanWriter(classOf[GeneratedMessage]) {
      override def serializeAsField(bean: scala.Any, gen: JsonGenerator, prov: SerializerProvider): Unit = {
        bean.asInstanceOf[GeneratedMessage].getAllFields.foreach {
          case (fd, v) => gen.writeObjectField(fd.getJsonName, serializeField(fd, v))
        }
      }
    }

    // 注册Protobuf的消息反序列化扩展
    private object ProtobufDeserializer extends Deserializers.Base {
      override def findBeanDeserializer(javaType: JavaType, config: DeserializationConfig, beanDesc: BeanDescription): JsonDeserializer[GeneratedMessage] = {
        if (classOf[GeneratedMessage].isAssignableFrom(javaType.getRawClass)) new ProtobufParser(javaType) else null
      }
    }

    private class ProtobufParser(javaType: JavaType) extends StdDeserializer[GeneratedMessage](javaType) {
      val clazz = Class.forName(javaType.getRawClass.getName + "$")
      val companion = clazz.getField("MODULE$").get(clazz)
      override def deserialize(jp: JsonParser, ctxt: DeserializationContext): GeneratedMessage = {
        val jsonObj = jp.getCodec.readTree(jp).asInstanceOf[ObjectNode]
        parseObject(companion.asInstanceOf[GeneratedMessageCompanion[T] forSome {type T <: GeneratedMessage with Message[T]}], jsonObj).asInstanceOf[GeneratedMessage]
      }
    }

    // 输出Unwrapped类型JSON的工具类
    private class FakeUnwrappedBeanWriter(clazz: Class[_]) extends BeanPropertyWriter {
      lazy val unwrappedSerializer = new UnwrappingBeanSerializer(new BeanSerializer(getType, null, Array(this), null), NameTransformer.NOP)
      override def getType: JavaType = constructType(clazz)
      override def getName: String = ""
      override def rename(transformer: NameTransformer): BeanPropertyWriter = this
    }

    // 序列化Protobuf消息的工具函数
    private def serializeField(fd: FieldDescriptor, value: Any): Any = {
      if (fd.isMapField) value.asInstanceOf[Seq[GeneratedMessage]].map { v2 =>
        val key = v2.getField(v2.companion.descriptor.findFieldByNumber(1)).toString
        val fd2 = v2.companion.descriptor.findFieldByNumber(2)
        key -> serializeValue(fd2, v2.getField(fd2))
      } else if (fd.isRepeated) {
        value.asInstanceOf[Seq[Any]].map(serializeValue(fd, _))
      } else {
        serializeValue(fd, value)
      }
    }

    private def serializeValue(fd: FieldDescriptor, value: Any): Any = fd.getJavaType match {
      case MessageType.ENUM => value.asInstanceOf[EnumValueDescriptor].getNumber
      case MessageType.MESSAGE => value.asInstanceOf[GeneratedMessage]
      case MessageType.INT => value.asInstanceOf[Int]
      case MessageType.LONG => value.asInstanceOf[Long]
      case MessageType.DOUBLE => value.asInstanceOf[Double]
      case MessageType.FLOAT => value.asInstanceOf[Float]
      case MessageType.BOOLEAN => value.asInstanceOf[Boolean]
      case MessageType.STRING => value.asInstanceOf[String]
      case MessageType.BYTE_STRING => value.asInstanceOf[ByteString].toByteArray
    }

    // 反序列化Protobuf消息的工具函数
    private def parseObject[A <: GeneratedMessage with Message[A]](cmp: GeneratedMessageCompanion[A], value: ObjectNode): A = {
      val nodeMap: Map[String, JsonNode] = value.fields.map(k => k.getKey -> k.getValue).toMap
      val valueMap: Map[FieldDescriptor, Any] = (for {
        fd <- cmp.descriptor.getFields
        value <- nodeMap.get(fd.getJsonName)
      } yield (fd, parseField(cmp, fd, value))).toMap
      cmp.fromFieldsMap(valueMap)
    }

    private def parseField(cmp: GeneratedMessageCompanion[_], fd: FieldDescriptor, value: JsonNode): Any = {
      if (fd.isMapField) {
        value.getNodeType match {
          case JsonNodeType.OBJECT =>
            val mapEntryCmp = cmp.messageCompanionForField(fd)
            val keyDescriptor = fd.getMessageType.findFieldByNumber(1)
            val valueDescriptor = fd.getMessageType.findFieldByNumber(2)
            value.fields.map {
              field =>
                val name = keyDescriptor.getJavaType match {
                  case MessageType.BOOLEAN => java.lang.Boolean.valueOf(field.getKey)
                  case MessageType.DOUBLE => java.lang.Double.valueOf(field.getKey)
                  case MessageType.FLOAT => java.lang.Float.valueOf(field.getKey)
                  case MessageType.INT => java.lang.Integer.valueOf(field.getKey)
                  case MessageType.LONG => java.lang.Long.valueOf(field.getKey)
                  case MessageType.STRING => field.getKey
                  case _ => throw new RuntimeException(s"Unsupported type for key for ${fd.getName}")
                }
                mapEntryCmp.fromFieldsMap(Map(keyDescriptor -> name, valueDescriptor -> parseValue(mapEntryCmp, valueDescriptor, field.getValue)))
            }
          case _ => throw new IOException(s"Expected an object for map field ${fd.getJsonName} of ${fd.getContainingType.getName}")
        }
      } else if (fd.isRepeated) {
        value.getNodeType match {
          case JsonNodeType.ARRAY => value.elements.map(parseValue(cmp, fd, _)).toVector
          case _ => throw new IOException(s"Expected an array for repeated field ${fd.getJsonName} of ${fd.getContainingType.getName}")
        }
      } else {
        parseValue(cmp, fd, value)
      }
    }

    private def parseValue(cmp: GeneratedMessageCompanion[_], fd: FieldDescriptor, value: JsonNode): Any = (fd.getJavaType, value.getNodeType) match {
      case (MessageType.ENUM, JsonNodeType.NUMBER) => fd.getEnumType.findValueByNumber(value.asInt)
      case (MessageType.MESSAGE, JsonNodeType.OBJECT) =>
        // The asInstanceOf[] is a lie: we actually have a companion of some other message (not A),
        // but this doesn't matter after erasure.
        parseObject(cmp.messageCompanionForField(fd).asInstanceOf[GeneratedMessageCompanion[T] forSome {type T <: GeneratedMessage with Message[T]}], value.asInstanceOf[ObjectNode])
      case (MessageType.INT, JsonNodeType.NUMBER) => value.intValue
      case (MessageType.LONG, JsonNodeType.NUMBER) => value.longValue
      case (MessageType.DOUBLE, JsonNodeType.NUMBER) => value.decimalValue.doubleValue
      case (MessageType.FLOAT, JsonNodeType.NUMBER) => value.decimalValue.floatValue
      case (MessageType.BOOLEAN, JsonNodeType.BOOLEAN) => value.booleanValue
      case (MessageType.STRING, JsonNodeType.STRING) => value.textValue
      case (MessageType.BYTE_STRING, JsonNodeType.BINARY) => value.binaryValue
      case (MessageType.BYTE_STRING, JsonNodeType.STRING) => ByteString.copyFrom(Base64Variants.getDefaultVariant.decode(value.textValue))
      case (_, JsonNodeType.NULL) => null
      case _ => throw new IOException(s"Unexpected value ($value) for field ${fd.getJsonName} of ${fd.getContainingType.getName}")
    }
  }
}
