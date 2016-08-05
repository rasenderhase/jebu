package de.nikem.jebu.impl.websocket.server;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.sql.Date;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import javax.json.JsonObject;
import javax.json.JsonValue.ValueType;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtilsTest {
	private final Logger log = LoggerFactory.getLogger(getClass());
	private TestObject testObject;

	public static class TestObject {
		private String name;
		private Map<String, ?> childrenMap;
		private Collection<?> childrenCollection;
		private Object nullProperty;
		private Date date;
		private Object[] array;
		private short shorty;
		
		private int inty;
		private Integer integer;
		private boolean moody;
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Map<String, ?> getChildrenMap() {
			return childrenMap;
		}
		public void setChildrenMap(Map<String, ?> childrenMap) {
			this.childrenMap = childrenMap;
		}
		public Collection<?> getChildrenCollection() {
			return childrenCollection;
		}
		public void setChildrenCollection(Collection<?> childrenCollection) {
			this.childrenCollection = childrenCollection;
		}
		public Object getNullProperty() {
			return nullProperty;
		}
		public void setNullProperty(Object nullProperty) {
			this.nullProperty = nullProperty;
		}
		public Date getDate() {
			return date;
		}
		public void setDate(Date date) {
			this.date = date;
		}
		public Object[] getArray() {
			return array;
		}
		public void setArray(Object[] array) {
			this.array = array;
		}
		public short getShorty() {
			return shorty;
		}
		public void setShorty(short shorty) {
			this.shorty = shorty;
		}
		public int getInty() {
			return inty;
		}
		public void setInty(int inty) {
			this.inty = inty;
		}
		public Integer getInteger() {
			return integer;
		}
		public void setInteger(Integer integer) {
			this.integer = integer;
		}	
		public String gettyMethod() {
			return "gettyMethod";
		}
		public void getVoidThing() {
			//nothing to do
		}
		public String get() {
			return "get";
		}
		public boolean isMoody() {
			return moody;
		}
		public void setMoody(boolean moody) {
			this.moody = moody;
		}
	}
	
	
	@Before
	public void setUp() throws Exception {
		testObject = new TestObject();
		testObject.setArray(new Object[] { 1, "hi", new TestObject(), null});
		Collection<Object> childrenCollection = new LinkedList<Object>();
		childrenCollection.add(Integer.valueOf(2));
		childrenCollection.add("hello");
		childrenCollection.add(null);
		childrenCollection.add(testObject.getArray());
		testObject.setChildrenCollection(childrenCollection);
		testObject.setDate(new Date(1000000000));
	}
	
	@Test
	public void test() {
		JsonObject json = JsonUtils.populateMap(testObject);
		
		assertThat(json.get("childrenCollection").getValueType(), is(ValueType.ARRAY));
		log.debug(json.toString());		
	}
}
