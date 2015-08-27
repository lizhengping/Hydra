package com.labatlas.atlas.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.msgpack.value.ImmutableValue;

/**
 *
 * @author Hwaipy
 */
public class MessageConvertorTest {

  public MessageConvertorTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testConvert() throws JsonProcessingException, IOException {
    HashMap map = new HashMap<>();
    map.put("key1", "value1");
    map.put("key2", 123);
    map.put("Key3", false);
    map.put("Key4", new byte[]{1, 2, 2, 2, 2, 1, 1, 2, 2, 1, 1, 4, 5, 4, 4});
    map.put("key5", new int[]{3, 526255, 1321, 4, -1});
    map.put("key6", null);
    map.put("key7", Long.MAX_VALUE);
    map.put("key8", Long.MIN_VALUE);
    map.put("key9", BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.TEN));
    HashMap mapIn = new HashMap();
    mapIn.putAll(map);
    map.put("KeyMap", mapIn);

    ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
    byte[] expResult = objectMapper.writeValueAsBytes(map);

    MessageUnpacker unpacker = new MessagePack().newUnpacker(expResult);
    ImmutableValue value = unpacker.unpackValue();

    MessageConvertor convertor = new MessageConvertor(value);
    Message message = convertor.convert();

    byte[] result = message.pack();
    Assert.assertArrayEquals(expResult, result);
  }

}
