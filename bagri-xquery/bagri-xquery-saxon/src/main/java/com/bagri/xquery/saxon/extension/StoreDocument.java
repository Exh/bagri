package com.bagri.xquery.saxon.extension;

import com.bagri.xdm.access.api.XDMDocumentManagement;
import com.bagri.xdm.domain.XDMDocument;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class StoreDocument extends ExtensionFunctionDefinition {
	
	private XDMDocumentManagement xdm;
	
	public StoreDocument(XDMDocumentManagement xdm) {
		this.xdm = xdm;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName("bgdm", "http://bagri.com/bagri-xdm", "store-document");
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {SequenceType.SINGLE_STRING};
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.SINGLE_STRING;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {

		return new ExtensionFunctionCall() {

			@Override
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				
				String xml = arguments[0].head().getStringValue();
				// validate document ?
				XDMDocument doc = xdm.storeDocument(xml);
				return new StringValue(doc.getUri());
			}
        };
	} 
}
