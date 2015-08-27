package com.labatlas.atlas.message;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.msgpack.value.ArrayValue;
import org.msgpack.value.BinaryValue;
import org.msgpack.value.IntegerValue;
import org.msgpack.value.MapValue;
import org.msgpack.value.Value;

/**
 *
 * @author Hwaipy
 */
public class MessageConvertor {

  private final Value value;

  public MessageConvertor(Value value) {
    this.value = value;
  }

  public Message convert() {
    Object convert = convert(value, 0);
    if (convert instanceof Map) {
      Map map = (Map) convert;
      return Message.newMessage(map);
    } else {
      throw new IllegalArgumentException("Message should be a Map");
    }
  }
  private static final int MAX_DEEPTH = 100;

  private Object convert(Value value, int deepth) {
    if (deepth >= MAX_DEEPTH) {
      throw new IllegalStateException("Message over deepth.");
    }
    deepth++;
    switch (value.getValueType()) {
      case ARRAY:
        ArrayValue arrayValue = value.asArrayValue();
        ArrayList array = new ArrayList(arrayValue.size());
        for (Value valueItem : arrayValue) {
          array.add(convert(valueItem, deepth));
        }
        return array;
      case MAP:
        MapValue mapValue = value.asMapValue();
        HashMap map = new HashMap();
        for (Map.Entry<Value, Value> entry : mapValue.entrySet()) {
          map.put(convert(entry.getKey(), deepth), convert(entry.getValue(), deepth));
        }
        return map;
      case BINARY:
        BinaryValue binaryValue = value.asBinaryValue();
        ByteBuffer byteBuffer = ByteBuffer.wrap(binaryValue.asByteArray());
        return byteBuffer;
      case BOOLEAN:
        return "true".equals(value.toString());
      case FLOAT:
        return value.asFloatValue().toDouble();
      case INTEGER:
        IntegerValue integerValue = value.asIntegerValue();
        if (integerValue.isInLongRange()) {
          if (integerValue.isInIntRange()) {
            return integerValue.toInt();
          } else {
            return integerValue.toLong();
          }
        } else {
          return integerValue.asBigInteger();
        }
      case NIL:
        return null;
      case STRING:
        return value.asStringValue().toString();
      case EXTENSION:
      default:
        throw new IllegalStateException("Unknown ValueType: " + value.getValueType());
    }
  }
}
