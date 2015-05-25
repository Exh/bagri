package com.bagri.client.tpox.workload;

import java.util.Map;

import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;

import net.xqj.marklogic.MarkLogicXQInsertOptions;

import com.xqj2.XQConnection2;

public class MarkLogicXQJPlugin extends BagriXQJPlugin {

	private static int cnt = 0;

	@Override
	protected int execCommand(String query, Map<String, Object> params) throws XQException {
		
        if ("insertDocument".equals(query)) {
            XQConnection2 xqc = (XQConnection2) getConnection();
            String doc = (String) params.get("doc");
            XQItem item = xqc.createItemFromDocument(doc, null, null);
            MarkLogicXQInsertOptions insertOptions = null;
            String collect = (String) params.get("collect");
            if (collect != null) {
                insertOptions = new MarkLogicXQInsertOptions();
            	insertOptions.setCollections(new String[] {collect});
            }
            int idx = cnt++;
            xqc.insertItem("doc" + idx + ".xml", item, insertOptions);
            return 1;
        	
        }
        return super.execCommand(query, params);
	}
	
}
