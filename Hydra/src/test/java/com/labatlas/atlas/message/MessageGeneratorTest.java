package com.labatlas.atlas.message;

import com.hydra.core.MessageGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.msgpack.jackson.dataformat.MessagePackFactory;

/**
 *
 * @author Hwaipy
 */
public class MessageGeneratorTest {

  private byte[] unitData;

  public MessageGeneratorTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() throws JsonProcessingException {
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
    unitData = objectMapper.writeValueAsBytes(map);
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testGenerate() throws JsonProcessingException, IOException {
    MessageGenerator generator = new MessageGenerator();
    generator.feed(ByteBuffer.wrap(unitData));
    Assert.assertArrayEquals(unitData, generator.next().pack());
    Assert.assertNull(generator.next());
  }

  @Test
  public void testPartialGenerate() throws JsonProcessingException, IOException {
    MessageGenerator generator = new MessageGenerator();
    for (int i = 0; i < unitData.length; i++) {
      generator.feed(ByteBuffer.wrap(new byte[]{unitData[i]}));
      if (i == unitData.length - 1) {
        Assert.assertArrayEquals(unitData, generator.next().pack());
      } else {
        Assert.assertNull(generator.next());
      }
    }
  }

  @Test
  public void testMultiGenerate() throws JsonProcessingException, IOException {
    MessageGenerator generator = new MessageGenerator();
    generator.feed(ByteBuffer.wrap(unitData));
    generator.feed(ByteBuffer.wrap(unitData));
    generator.feed(ByteBuffer.wrap(unitData));
    generator.feed(ByteBuffer.wrap(unitData));
    generator.feed(ByteBuffer.wrap(unitData));
    generator.feed(ByteBuffer.wrap(unitData, 0, 10));
    Assert.assertArrayEquals(unitData, generator.next().pack());
    Assert.assertArrayEquals(unitData, generator.next().pack());
    Assert.assertArrayEquals(unitData, generator.next().pack());
    Assert.assertArrayEquals(unitData, generator.next().pack());
    Assert.assertArrayEquals(unitData, generator.next().pack());
    Assert.assertNull(generator.next());
    generator.feed(ByteBuffer.wrap(unitData, 10, unitData.length - 11));
    generator.feed(ByteBuffer.wrap(unitData, unitData.length - 2, 1));
    Assert.assertArrayEquals(unitData, generator.next().pack());
  }

  @Test
  public void testOverSize() throws JsonProcessingException, IOException {
    int bufferSize = 100000;
    MessageGenerator generator = new MessageGenerator(bufferSize);
    int share = bufferSize / unitData.length;
    for (int i = 0; i < share; i++) {
      generator.feed(ByteBuffer.wrap(unitData));
    }
    ByteBuffer lastBuffer = ByteBuffer.wrap(unitData);
    generator.feed(lastBuffer);
    Assert.assertEquals((share + 1) * unitData.length - bufferSize, lastBuffer.remaining());
    for (int i = 0; i < share; i++) {
      Assert.assertArrayEquals(unitData, generator.next().pack());
    }
    Assert.assertNull(generator.next());
    generator.feed(lastBuffer);
    Assert.assertArrayEquals(unitData, generator.next().pack());
  }

  @Test
  public void testException() throws JsonProcessingException, IOException {
    MessageGenerator generator = new MessageGenerator();
    generator.feed(ByteBuffer.wrap(unitData));
    generator.feed(ByteBuffer.wrap(unitData, 1, unitData.length - 1));
    generator.feed(ByteBuffer.wrap(unitData));
    Assert.assertArrayEquals(unitData, generator.next().pack());
    try {
      generator.next();
      Assert.fail();
    } catch (Exception e) {
      Assert.assertEquals("java.lang.IllegalArgumentException: Message should be a Map", e.getMessage());
    }
  }
}
