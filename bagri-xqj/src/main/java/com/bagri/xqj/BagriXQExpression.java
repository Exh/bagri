package com.bagri.xqj;

import static com.bagri.xqj.BagriXQErrors.ex_expression_closed;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xquery.XQConstants;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQStaticContext;

import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.impl.ResultCursorBase;
import com.bagri.support.util.XMLUtils;

public class BagriXQExpression extends BagriXQDynamicContext implements XQExpression {
	
	private List<XQResultSequence> results = new ArrayList<>();

	BagriXQExpression(XQStaticContext context) {
		super(context);
	}

	BagriXQExpression(BagriXQConnection connection) {
		super(connection);
	}

	BagriXQExpression(BagriXQConnection connection, XQStaticContext context) {
		super(connection, context);
	}

	@Override
	public void close() throws XQException {
		super.close();
		closeResults();
	}
	
	private void closeResults() throws XQException {
		for (XQResultSequence sq: results) {
			if (!sq.isClosed()) {
				sq.close();
			}
		}
		results.clear();
	}
	
	@Override
	public void executeCommand(String cmd) throws XQException {
		
		checkState(ex_expression_closed);
		if (cmd == null) {
			throw new XQException("Provided command is null");
		}
		connection.executeCommand(cmd, getBindings());
	}

	@Override
	public void executeCommand(Reader cmd) throws XQException {
		
		String str;
		try {
			str = XMLUtils.textToString(cmd);
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
		executeCommand(str);
	}

	@Override
	public XQResultSequence executeQuery(String query) throws XQException {
		
		checkState(ex_expression_closed);
		if (query == null) {
			throw new XQException("Provided query is null");
		}
		closeResults();
		
		ResultCursor<XQItemAccessor> result = connection.executeQuery(query, context);
		XQResultSequence sequence;
		if (context.getScrollability() == XQConstants.SCROLLTYPE_SCROLLABLE) {
			sequence = new ScrollableXQResultSequence(this, ((ResultCursorBase<XQItemAccessor>) result).getList());
		} else {
			sequence = new IterableXQResultSequence(this, result); 
		}
		results.add(sequence);
		return sequence;
	}

	@Override
	public XQResultSequence executeQuery(Reader query) throws XQException {
		
		String str;
		try {
			str = XMLUtils.textToString(query);
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
		return executeQuery(str);
	}

	@Override
	public XQResultSequence executeQuery(InputStream query) throws XQException {
		
		String str;
		try {
			str = XMLUtils.textToString(query);
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
		return executeQuery(str);
	}
	
}
