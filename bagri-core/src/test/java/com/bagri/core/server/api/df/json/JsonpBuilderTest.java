package com.bagri.core.server.api.df.json;

import com.bagri.core.DataKey;
import com.bagri.core.model.Data;
import com.bagri.core.model.Element;
import com.bagri.core.model.Elements;
import com.bagri.core.model.NodeKind;
import com.bagri.core.model.Null;
import com.bagri.core.model.Occurrence;
import com.bagri.core.model.ParseResults;
import com.bagri.core.model.Path;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ModelManagementImpl;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.json.stream.JsonGenerator;
import javax.xml.xquery.XQItemType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.*;

public class JsonpBuilderTest {

	private static String json = 
			"{\n" +
			"  \"firstName\": \"John\",\n" +
			"  \"lastName\": \"Smith\",\n" +
			"  \"age\": 25,\n" +
			"  \"address\": {\n" +
			"    \"streetAddress\": \"21 2nd Street\",\n" +
			"    \"city\": \"New York\",\n" +
			"    \"state\": \"NY\",\n" +
			"    \"postalCode\": \"10021\"\n" +
			"  },\n" +
			"  \"phoneNumber\": [\n" +
			"    {\n" +
			"      \"type\": \"home\",\n" +
			"      \"number\": \"212 555-1234\"\n" +
			"    },\n" +
			"    {\n" +
			"      \"type\": \"fax\",\n" +
			"      \"number\": \"646 555-4567\",\n" +
			"      \"comment\": null\n" +
			"    }\n" +
			"  ],\n" +
			"  \"gender\": {\n" +
			"    \"type\": \"male\"\n" +
			"  }\n" +
			"}";
				
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("logback.configurationFile", "test_logging.xml");
		//System.setProperty(pn_log_level, "trace");
	}

	//@AfterClass
	//public static void tearDownAfterClass() throws Exception {
	//}

	//@Before
	//public void setUp() throws Exception {
	//}

	//@After
	//public void tearDown() throws Exception {
	//}

	@Test
	public void testBuildManual() throws Exception {
		ModelManagement model = new ModelManagementImpl();
		// to prepare model:
		//XmlStaxParser parser = new XmlStaxParser(model);
		//List<Data> data = parser.parse(json);
		//data.clear();
		List<Data> data = new ArrayList<>();
		data.add(new Data(new Path("", "/", NodeKind.document, 1, 0, 25, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {}, null)));
		data.add(new Data(new Path("/firstName", "/", NodeKind.attribute, 2, 1, 2, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {1}, "John")));
		data.add(new Data(new Path("/lastName", "/", NodeKind.attribute, 3, 1, 3, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {2}, "Smith")));
		data.add(new Data(new Path("/age", "/", NodeKind.attribute, 4, 1, 4, XQItemType.XQBASETYPE_LONG, Occurrence.onlyOne), new Element(new int[] {3}, new Long(25))));
		data.add(new Data(new Path("/address", "/", NodeKind.element, 5, 1, 9, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {4}, null)));
		data.add(new Data(new Path("/address/streetAddress", "/", NodeKind.attribute, 6, 5, 6, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {4, 1}, "21 2nd Street")));
		data.add(new Data(new Path("/address/city", "/", NodeKind.attribute, 7, 5, 7, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {4, 2}, "New York")));
		data.add(new Data(new Path("/address/state", "/", NodeKind.attribute, 8, 5, 8, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {4, 3}, "NY")));
		data.add(new Data(new Path("/address/postalCode", "/", NodeKind.attribute, 9, 5, 9, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {4, 4}, "10021")));
		data.add(new Data(new Path("/phoneNumber", "/", NodeKind.array, 10, 1, 14, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.zeroOrOne), new Element(new int[] {5}, null)));
		data.add(new Data(new Path("/phoneNumber/", "/", NodeKind.element, 11, 10, 14, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.oneOrMany), new Element(new int[] {5, 1}, null)));
		data.add(new Data(new Path("/phoneNumber/type", "/", NodeKind.attribute, 12, 11, 12, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {5, 1, 1}, "home")));
		data.add(new Data(new Path("/phoneNumber/number", "/", NodeKind.attribute, 13, 11, 13, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {5, 1, 2}, "212 555-1234")));
		data.add(new Data(new Path("/phoneNumber/", "/", NodeKind.element, 11, 10, 14, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.oneOrMany), new Element(new int[] {5, 2}, null)));
		data.add(new Data(new Path("/phoneNumber/type", "/", NodeKind.attribute, 12, 11, 12, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {5, 2, 1}, "fax")));
		data.add(new Data(new Path("/phoneNumber/number", "/", NodeKind.attribute, 13, 11, 13, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {5, 2, 2}, "646 555-4567")));
		data.add(new Data(new Path("/phoneNumber/comment", "/", NodeKind.attribute, 14, 11, 14, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {5, 2, 3}, Null._null)));
		data.add(new Data(new Path("/gender", "/", NodeKind.element, 15, 1, 16, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {6}, null)));
		data.add(new Data(new Path("/gender/type", "/", NodeKind.attribute, 16, 15, 16, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {6, 1}, "male")));
		//System.out.println(data);
		JsonpBuilder builder = new JsonpBuilder(model);
		Properties props = new Properties();
		props.setProperty(JsonGenerator.PRETTY_PRINTING, "true");
		builder.init(props);
		String content = builder.buildContent(data);
		System.out.println(content);
		assertNotNull(content);
		// now compare content vs xml..
		//assertEquals(xml, content);
	}

	@Test
	public void testBuildPerson() throws Exception {
		ModelManagement model = new ModelManagementImpl();
		JsonpParser parser = new JsonpParser(model);
		ParseResults results = parser.parse(json); 
		List<Data> data = results.getResults();
		System.out.println(data);
		assertNotNull(data);
		assertEquals(19, data.size()); 
		JsonpBuilder builder = new JsonpBuilder(model);
		String content = builder.buildContent(data);
		System.out.println("content: " + content);
		assertNotNull(content);
	}
		
	@Test
	public void testBuildManualSparse() throws Exception {
		ModelManagement model = new ModelManagementImpl();
		// to prepare model:
		JsonpParser parser = new JsonpParser(model);
		ParseResults results = parser.parse(json); 
		List<Data> data = results.getResults();
		data.clear();
		data = new ArrayList<>();
		//data.add(new Data(new Path("", "/", NodeKind.document, 1, 0, 25, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {}, null)));
		data.add(new Data(new Path("/firstName", "/", NodeKind.attribute, 2, 1, 2, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {1}, "John")));
		data.add(new Data(new Path("/lastName", "/", NodeKind.attribute, 3, 1, 3, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {2}, "Smith")));
		data.add(new Data(new Path("/age", "/", NodeKind.attribute, 4, 1, 4, XQItemType.XQBASETYPE_LONG, Occurrence.onlyOne), new Element(new int[] {3}, new Long(25))));
		data.add(new Data(new Path("/address/streetAddress", "/", NodeKind.attribute, 6, 5, 6, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {4, 1}, "21 2nd Street")));
		data.add(new Data(new Path("/address/city", "/", NodeKind.attribute, 7, 5, 7, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {4, 2}, "New York")));
		data.add(new Data(new Path("/address/state", "/", NodeKind.attribute, 8, 5, 8, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {4, 3}, "NY")));
		data.add(new Data(new Path("/address/postalCode", "/", NodeKind.attribute, 9, 5, 9, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {4, 4}, "10021")));
		data.add(new Data(new Path("/phoneNumber/type", "/", NodeKind.attribute, 12, 11, 12, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {5, 1, 1}, "home")));
		data.add(new Data(new Path("/phoneNumber/number", "/", NodeKind.attribute, 13, 11, 13, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {5, 1, 2}, "212 555-1234")));
		data.add(new Data(new Path("/phoneNumber/type", "/", NodeKind.attribute, 12, 11, 12, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {5, 2, 1}, "fax")));
		data.add(new Data(new Path("/phoneNumber/number", "/", NodeKind.attribute, 13, 11, 13, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {5, 2, 2}, "646 555-4567")));
		data.add(new Data(new Path("/phoneNumber/comment", "/", NodeKind.attribute, 14, 11, 14, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {5, 2, 3}, Null._null)));
		data.add(new Data(new Path("/gender/type", "/", NodeKind.attribute, 16, 15, 16, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {6, 1}, "male")));
		//System.out.println(data);
		JsonpBuilder builder = new JsonpBuilder(model);
		Properties props = new Properties();
		props.setProperty(JsonGenerator.PRETTY_PRINTING, "true");
		builder.init(props);
		Map<DataKey, Elements> dataMap = JsonpBuilder.dataToElements(data);
		String content = builder.buildContent(dataMap);
		System.out.println(content);
		assertNotNull(content);
		// now compare content vs xml..
		//assertEquals(xml, content);
	}
	
}

