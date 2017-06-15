package com.bagri.core.server.api.df.map;

import static com.bagri.support.util.XQUtils.getBaseTypeForObject;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.xquery.XQException;

import com.bagri.core.api.BagriException;
import com.bagri.core.model.Data;
import com.bagri.core.model.NodeKind;
import com.bagri.core.model.Occurrence;
import com.bagri.core.server.api.ContentParser;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ContentParserBase;

public class MapParser extends ContentParserBase implements ContentParser<Map<String, Object>> {

	MapParser(ModelManagement model) {
		super(model);
	}

	@Override
	public void init(Properties properties) {
		// think about possible props..
	}

	@Override
	public List<Data> parse(Map<String, Object> source) throws BagriException {
		ParserContext ctx = initContext();
		ctx.addDocument("/map");
		ctx.addData("map"); 
		ctx.addElement(); 
		// very simple map.
		// implement nested maps, arrays, etc..
		try {
			for (Map.Entry<String, Object> e: source.entrySet()) {
				int baseType = getBaseTypeForObject(e.getValue());
				ctx.addData("@" + e.getKey(), NodeKind.attribute, e.getValue(), baseType, Occurrence.zeroOrOne);
			}
		} catch (XQException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		ctx.endElement();
		return ctx.getDataList();
	}

	@Override
	public List<Data> parse(File file) throws BagriException {
		// not used for Maps
		return null;
	}

	@Override
	public List<Data> parse(InputStream stream) throws BagriException {
		// not used for Maps
		return null;
	}

	@Override
	public List<Data> parse(Reader reader) throws BagriException {
		// not used for Maps
		return null;
	}

}
