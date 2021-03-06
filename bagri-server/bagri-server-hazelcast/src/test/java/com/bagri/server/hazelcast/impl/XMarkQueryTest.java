package com.bagri.server.hazelcast.impl;

import com.bagri.core.api.ResultCursor;
import com.bagri.core.system.Library;
import com.bagri.core.system.Module;
import com.bagri.core.system.Schema;
import com.bagri.core.test.BagriManagementTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.xquery.XQItemAccessor;

import static com.bagri.core.Constants.*;
import static com.bagri.core.test.TestUtils.*;
import static org.junit.Assert.*;

public class XMarkQueryTest extends BagriManagementTest {

    private static ClassPathXmlApplicationContext context;
	
    //private XQProcessor xqProc; 

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		sampleRoot = "../../etc/samples/xmark/";
		//System.setProperty(pn_log_level, "trace");
		System.setProperty(pn_node_instance, "0");
		System.setProperty("logback.configurationFile", "hz-logging.xml");
		System.setProperty(pn_config_properties_file, "xmark.properties");
		System.setProperty(pn_config_path, "src/test/resources");
		context = new ClassPathXmlApplicationContext("spring/cache-test-context.xml");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		context.close();
	}

	@Before
	public void setUp() throws Exception {
		xRepo = context.getBean(SchemaRepositoryImpl.class);
		SchemaRepositoryImpl xdmRepo = (SchemaRepositoryImpl) xRepo; 
		//xqProc = context.getBean("xqProcessor", XQProcessor.class);
		Schema schema = xdmRepo.getSchema();
		if (schema == null) {
			Properties props = loadProperties("src/test/resources/xmark.properties");
			schema = new Schema(1, new java.util.Date(), "test", "test", "test schema", true, props);
			xdmRepo.setSchema(schema);
			xdmRepo.setDataFormats(getBasicDataFormats());
			xdmRepo.setLibraries(new ArrayList<Library>());
			xdmRepo.setModules(new ArrayList<Module>());
			((ClientManagementImpl) xdmRepo.getClientManagement()).addClient(client_id, user_name);
			xdmRepo.setClientId(client_id);
			long txId = getTxManagement().beginTransaction();
			createDocumentTest(sampleRoot + getFileName("auction.xml"));
			getTxManagement().commitTransaction(txId);
		}
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void getPersonTest() throws Exception {
		// Q1.Return the name of the person with ID `person0'.

		String query = "declare variable $name external;\n" +
				"let $auction := fn:doc(\"auction.xml\") return\n" +
				"for $b in $auction/site/people/person[@id = $name] return $b/name/text()";
		
		Map<String, Object> params = new HashMap<>();
		params.put("name", "person0");
		checkCursorResult(query, params, null, "Huei Demke");
	}
	
	@Test
	public void getPersonLiteralTest() throws Exception {
		// Q1.Return the name of the person with ID `person0'.

		String query = //"declare base-uri \"file:/C:/Work/Bagri/git/bagri/etc/samples/xmark/\";\n" +
				"let $auction := fn:doc(\"auction.xml\") return\n" +
				"for $b in $auction/site/people/person[@id = 'person0'] return $b/name/text()";
		
		checkCursorResult(query, null, null, "Huei Demke");
	}
	
	@Test
	public void getIncreasesTest() throws Exception {
		// Q2. Return the initial increases of all open auctions.

		String query = "let $auction := doc(\"auction.xml\") return\n" +
				"for $b in $auction/site/open_auctions/open_auction\n" +
				"return <increase>{$b/bidder[1]/increase/text()}</increase>";
		
		try (ResultCursor<XQItemAccessor> results = query(query, null, null)) {
			int cnt = 0;
			for(XQItemAccessor item: results) {
				String text = item.getItemAsString(null);
				assertTrue("unexpected result: " + text, text.startsWith("<increase>") && text.endsWith("</increase>"));
				cnt++;
			}
			assertEquals(12, cnt);
		}
	}
	
	
	@Test
	public void getOpenAuctionsTest() throws Exception {
		// Q3. Return the IDs of all open auctions whose current
		//     increase is at least twice as high as the initial increase.

		String query = "let $auction := doc(\"auction.xml\") return\n" +
				"for $b in $auction/site/open_auctions/open_auction\n" +
				"where zero-or-one($b/bidder[1]/increase/text()) * 2 <= $b/bidder[last()]/increase/text()\n" +
				"return <increase first=\"{$b/bidder[1]/increase/text()}\" last=\"{$b/bidder[last()]/increase/text()}\"/>";
		
		try (ResultCursor<XQItemAccessor> results = query(query, null, null)) {
			//<increase first="4.50" last="12.00"/>
			//<increase first="6.00" last="30.00"/>
			int cnt = 0;
			for (XQItemAccessor item: results) {
				String text = item.getItemAsString(null);
				assertTrue("unexpected result: " + text, text.startsWith("<increase"));
				cnt++;
			}
			assertEquals(2, cnt);
		}
	}
	
	@Test
	public void getReservesTest() throws Exception {
		// Q4. List the reserves of those open auctions where a
		//     certain person issued a bid before another person.

		String query = "declare variable $name1 external;\n" +
				"declare variable $name2 external;\n" +
				"let $auction := doc(\"auction.xml\") return\n" +
				"for $b in $auction/site/open_auctions/open_auction\n" +
				"where\n" +
				"some $pr1 in $b/bidder/personref[@person = $name1],\n" +
				"	$pr2 in $b/bidder/personref[@person = $name2]\n" +
				"satisfies $pr1 << $pr2\n" +
				"return <history>{$b/reserve/text()}</history>";
		
		Map<String, Object> params = new HashMap<>();
		params.put("name1", "person8");
		params.put("name2", "person19");
		try (ResultCursor<XQItemAccessor> results = query(query, params, null)) {
			Iterator<XQItemAccessor> itr = results.iterator();
			assertTrue(itr.hasNext());
			assertNotNull(itr.next());
			assertFalse(itr.hasNext());
		}
	}

	@Test
	public void getSoldItemsTest() throws Exception {
		// Q5.  How many sold items cost more than 40?

		String query = "declare variable $pmin external;\n" +
				"let $auction := doc(\"auction.xml\") return\n" +
				"count(\n" +
				"  for $i in $auction/site/closed_auctions/closed_auction\n" +
				"  where $i/price/text() >= $pmin\n" +
				"  return $i/price\n" +
				")";
		
		Map<String, Object> params = new HashMap<>();
		params.put("pmin", new Integer(40));
		try (ResultCursor<XQItemAccessor> results = query(query, params, null)) {
			Iterator<XQItemAccessor> itr = results.iterator();
			assertTrue(itr.hasNext());
			assertEquals(7, itr.next().getInt());
			assertFalse(itr.hasNext());
		}
	}

	@Test
	public void getListedItemsTest() throws Exception {
		// Q6. How many items are listed on all continents?

		String query = "let $auction := doc(\"auction.xml\") return\n" +
				"for $b in $auction//site/regions return count($b//item)";

		try (ResultCursor<XQItemAccessor> results = query(query, null, null)) {
			Iterator<XQItemAccessor> itr = results.iterator();
			assertTrue(itr.hasNext());
			assertEquals(22, itr.next().getInt());
			assertFalse(itr.hasNext());
		}
	}

	@Test
	public void getDbPiecesTest() throws Exception {
		//Q7. How many pieces of prose are in our database?}

		String query = "let $auction := doc(\"auction.xml\") return\n" +
				"for $p in $auction/site return\n" +
				"  count($p//description) + count($p//annotation) + count($p//emailaddress)";

		try (ResultCursor<XQItemAccessor> results = query(query, null, null)) {
			Iterator<XQItemAccessor> itr = results.iterator();
			assertTrue(itr.hasNext());
			assertEquals(92, itr.next().getInt());
			assertFalse(itr.hasNext());
		}
	}

	@Test
	public void getPersonsWithItemsTest() throws Exception {
		// Q8. List the names of persons and the number of items they bought.
		//     (joins person, closed\_auction)}

		String query = "let $auction := doc(\"auction.xml\") return\n" +
				"for $p in $auction/site/people/person\n" +
				"let $a := \n" +
				"  for $t in $auction/site/closed_auctions/closed_auction\n" +
				"  where $t/buyer/@person = $p/@id\n" +
				"  return $t\n" +
				"return <item person=\"{$p/name/text()}\">{count($a)}</item>";

		try (ResultCursor<XQItemAccessor> results = query(query, null, null)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				String text = item.getItemAsString(null);
				assertTrue("unexpected result: " + text, text.startsWith("<item person") && text.endsWith("</item>"));
				cnt++;
			}
			assertEquals(25, cnt);
		}
	}
	
	@Test
	public void getPersonsWithItemsInEuropeTest() throws Exception {
		// Q9. List the names of persons and the names of the items they bought
		//     in Europe.  (joins person, closed\_auction, item)}

		String query = "let $auction := doc(\"auction.xml\") return\n" +
				"let $ca := $auction/site/closed_auctions/closed_auction return\n" +
				"let\n" +
				"	$ei := $auction/site/regions/europe/item\n" +
				"for $p in $auction/site/people/person\n" +
				"let $a := \n" +
				"	for $t in $ca\n" +
				"	where $p/@id = $t/buyer/@person\n" +
				"	return\n" +
				"		let $n := for $t2 in $ei where $t/itemref/@item = $t2/@id return $t2\n" +
				"		return <item>{$n/name/text()}</item>\n" +
				"return <person name=\"{$p/name/text()}\">{$a}</person>";

		try (ResultCursor<XQItemAccessor> results = query(query, null, null)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				String text = item.getItemAsString(null);
				assertTrue("unexpected result: " + text, text.startsWith("<person name"));
				cnt++;
			}
			assertEquals(25, cnt);
		}
	}

	@Test
	public void getPersonsWithInterestsTest() throws Exception {
		// Q10. List all persons according to their interest;
		//      use French markup in the result.

		String query = "let $auction := doc(\"auction.xml\") return\n" +
				"for $i in\n" +
				"	distinct-values($auction/site/people/person/profile/interest/@category)\n" +
				"let $p := \n" +
				"	for $t in $auction/site/people/person\n" +
				"	where $t/profile/interest/@category = $i\n" +
				"	return\n" +
				"		<personne>\n" +
				"			<statistiques>\n" +
		        "				<sexe>{$t/profile/gender/text()}</sexe>\n" +
		        "				<age>{$t/profile/age/text()}</age>\n" +
		        "				<education>{$t/profile/education/text()}</education>\n" +
		        "				<revenu>{fn:data($t/profile/@income)}</revenu>\n" +
		        "			</statistiques>\n" +
		        "			<coordonnees>\n" +
		        "				<nom>{$t/name/text()}</nom>\n" +
		        "				<rue>{$t/address/street/text()}</rue>\n" +
		        "				<ville>{$t/address/city/text()}</ville>\n" +
		        "				<pays>{$t/address/country/text()}</pays>\n" +
		        "				<reseau>\n" +
		        "					<courrier>{$t/emailaddress/text()}</courrier>\n" +
		        "					<pagePerso>{$t/homepage/text()}</pagePerso>\n" +
		        "				</reseau>\n" +
		        "			</coordonnees>\n" +
		        "			<cartePaiement>{$t/creditcard/text()}</cartePaiement>\n" +
		        "		</personne>\n" +
		        "return <categorie>{<id>{$i}</id>, $p}</categorie>";

		try (ResultCursor<XQItemAccessor> results = query(query, null, null)) {
			Iterator<XQItemAccessor> itr = results.iterator();
			assertTrue(itr.hasNext());
			String text = itr.next().getItemAsString(null);
			assertTrue("unexpected result: " + text, text.startsWith("<categorie>") && text.endsWith("</categorie>"));
			assertFalse(itr.hasNext());
		}
	}
	
	@Test
	public void getPersonsSalesTest() throws Exception {
		// Q11. For each person, list the number of items currently on sale whose
		//      price does not exceed 0.02% of the person's income.

		String query = "let $auction := doc(\"auction.xml\") return\n" +
				"for $p in $auction/site/people/person\n" +
				"let $l := \n" +
				"	for $i in $auction/site/open_auctions/open_auction/initial\n" +
				"	where $p/profile/@income > 5000 * exactly-one($i/text())\n" +
				"	return $i\n" +
				"return <items name=\"{$p/name/text()}\">{count($l)}</items>";

		try (ResultCursor<XQItemAccessor> results = query(query, null, null)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				String text = item.getItemAsString(null);
				assertTrue("unexpected result: " + text, text.startsWith("<items name") && text.endsWith("</items>"));
				cnt++;
			}
			assertEquals(25, cnt);
		}
	}
	
	@Test
	public void getReachPersonsTest() throws Exception {
		// Q12.  For each richer-than-average person, list the number of items 
		//       currently on sale whose price does not exceed 0.02% of the 
		//       person's income.

		String query = "let $auction := doc(\"auction.xml\") return\n" +
				"for $p in $auction/site/people/person\n" +
				"let $l := \n" +
				"	for $i in $auction/site/open_auctions/open_auction/initial\n" +
				"	where $p/profile/@income > 5000 * exactly-one($i/text())\n" +
				"	return $i\n" +
				"where $p/profile/@income > 50000\n" +
				"return <items person=\"{$p/profile/@income}\">{count($l)}</items>";

		try (ResultCursor<XQItemAccessor> results = query(query, null, null)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				String text = item.getItemAsString(null);
				assertTrue("unexpected result: " + text, text.startsWith("<items person") && text.endsWith("</items>"));
				cnt++;
			}
			assertEquals(3, cnt);
		}
	}
	
	@Test
	public void getAustralianItemsTest() throws Exception {
		// Q13. List the names of items registered in Australia along with 
		//      their descriptions.

		String query = "let $auction := doc(\"auction.xml\") return\n" +
				"for $i in $auction/site/regions/australia/item\n" +
				"return <item name=\"{$i/name/text()}\">{$i/description}</item>";

		try (ResultCursor<XQItemAccessor> results = query(query, null, null)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				String text = item.getItemAsString(null);
				assertTrue("unexpected result: " + text, text.startsWith("<item name") && text.endsWith("</item>"));
				cnt++;
			}
			assertEquals(2, cnt);
		}
	}

	@Test
	public void getGoldItemsTest() throws Exception {
		// Q14. Return the names of all items whose description contains the 
		//      word `gold'.

		String query = "declare variable $word external;\n" +
				"let $auction := doc(\"auction.xml\") return\n" +
				"for $i in $auction/site//item\n" +
				"where contains(string(exactly-one($i/description)), $word)\n" +
				"return $i/name/text()";

		Map<String, Object> params = new HashMap<>();
		params.put("word", "gold");
		try (ResultCursor<XQItemAccessor> results = query(query, params, null)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				cnt++;
			}
			assertEquals(2, cnt);
		}
	}

	@Test
	public void getKeywordsTest() throws Exception {
		// Q15. Print the keywords in emphasis in annotations of closed auctions.

		String query = "let $auction := doc(\"auction.xml\") return\n" +
				"for $a in \n" +
				"  $auction/site/closed_auctions/closed_auction/annotation/description/parlist/\n" +
				"   listitem/parlist/listitem/text/emph/keyword/text()\n" +
				"return <text>{$a}</text>";

		try (ResultCursor<XQItemAccessor> results = query(query, null, null)) {
			assertTrue(results.isEmpty());
		}
	}

	@Test
	public void getAuctionsWithKeywordsTest() throws Exception {
		// Q16. Return the IDs of those auctions
		//      that have one or more keywords in emphasis. (cf. Q15)

		String query = "let $auction := doc(\"auction.xml\") return\n" +
				"for $a in $auction/site/closed_auctions/closed_auction\n" +
				"where\n" +
				"	not(\n" +
				"		empty(\n" +
				"			$a/annotation/description/parlist/listitem/parlist/listitem/text/emph/\n" +
				"			 keyword/\n" +
				"			 text()\n" +
				"		)\n" +
				"	)\n" +
				"return <person id=\"{$a/seller/@person}\"/>";

		try (ResultCursor<XQItemAccessor> results = query(query, null, null)) {
			assertTrue(results.isEmpty());
		}
	}

	@Test
	public void getPersonsWithoutHomepageTest() throws Exception {
		// Q17. Which persons don't have a homepage?

		String query = "let $auction := doc(\"auction.xml\") return\n" +
				"for $p in $auction/site/people/person\n" +
				"where empty($p/homepage/text())\n" +
				"return <person name=\"{$p/name/text()}\"/>";

		try (ResultCursor<XQItemAccessor> results = query(query, null, null)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				String text = item.getItemAsString(null);
				assertTrue("unexpected result: " + text, text.startsWith("<person name"));
				cnt++;
			}
			assertEquals(15, cnt);
		}
	}
	
	@Test
	public void getConvertedCurrencyTest() throws Exception {
		// Q18.Convert the currency of the reserve of all open auctions to 
		//     another currency.

		String query = "declare namespace local = \"http://www.foobar.org\";\n" +
				"declare function local:convert($v as xs:decimal?) as xs:decimal?\n" +
				"{\n" +
				"	2.20371 * $v (: convert Dfl to Euro :)\n" +
				"};\n\n" +
				"let $auction := doc(\"auction.xml\") return\n" +
				"for $i in $auction/site/open_auctions/open_auction\n" +
				"return local:convert(zero-or-one($i/reserve))";

		try (ResultCursor<XQItemAccessor> results = query(query, null, null)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				BigDecimal bd = (BigDecimal) item.getObject();
				assertTrue("unexpected result: " + bd, bd.compareTo(BigDecimal.ZERO) > 0);
				cnt++;
			}
			assertEquals(6, cnt);
		}
	}
	
	@Test
	public void getItemsWithLocationsTest() throws Exception {
		// Q19. Give an alphabetically ordered list of all
		//      items along with their location.

		String query = "let $auction := doc(\"auction.xml\") return\n" +
				"for $b in $auction/site/regions//item\n" +
				"let $k := $b/name/text()\n" +
				"order by zero-or-one($b/location) ascending empty greatest\n" +
				"return <item name=\"{$k}\">{$b/location/text()}</item>";

		try (ResultCursor<XQItemAccessor> results = query(query, null, null)) {
			int cnt = 0;
			for (XQItemAccessor item: results) {
				String text = item.getItemAsString(null);
				assertTrue("unexpected result: " + text, text.startsWith("<item name") && text.endsWith("</item>"));
				cnt++;
			}
			assertEquals(22, cnt);
		}
	}
	
	@Test
	public void getGroupedCustomersTest() throws Exception {
		// Q20. Group customers by their
		//      income and output the cardinality of each group.

		String query = "let $auction := doc(\"auction.xml\") return\n" +
				"<result>\n" +
				"	<preferred>\n" +
				"		{count($auction/site/people/person/profile[@income >= 100000])}\n" +
				"	</preferred>\n" +
				"	<standard>\n" +
				"		{\n" +
				"			count(\n" +
		        "				$auction/site/people/person/\n" +
		        "				 profile[@income < 100000 and @income >= 30000]\n" +
		        "			)\n" +
		    	"		}\n" +
		    	"	</standard>\n" +
		    	"	<challenge>\n" +
		    	"		{count($auction/site/people/person/profile[@income < 30000])}\n" +
		    	"	</challenge>\n" +
		    	"	<na>\n" +
		    	"		{\n" +
		    	"			count(\n" +
		        "				for $p in $auction/site/people/person\n" +
		        "				where empty($p/profile/@income)\n" +
		        "				return $p\n" +
		        "			)\n" +
		    	"		}\n" +
		    	"	</na>\n" +
		    	"</result>";

		checkCursorResult(query, null, null, null);
			//<result>
			//  <preferred>
			//	0
			//  </preferred>
			//  <standard>
			//	9
			//  </standard>
			//  <challenge>
			//	2
			//  </challenge>
			//  <na>
			//	14
			//  </na>
			//</result>
	}
	
	
}
