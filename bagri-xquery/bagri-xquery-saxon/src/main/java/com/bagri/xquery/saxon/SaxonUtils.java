package com.bagri.xquery.saxon;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import com.bagri.common.util.XMLUtils;
import com.bagri.xdm.system.XDMType;
import com.bagri.xqj.BagriXQUtils;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DOMObjectModel;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Sender;
import net.sf.saxon.evpull.PullEventSource;
import net.sf.saxon.evpull.StaxToEventBridge;
import net.sf.saxon.expr.EarlyEvaluationContext;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.*;
//import net.sf.saxon.xqj.SaxonDuration;
//import net.sf.saxon.xqj.SaxonXMLGregorianCalendar;
import static net.sf.saxon.om.StandardNames.*;
import static javax.xml.xquery.XQItemType.*;

public class SaxonUtils {

    public static Object itemToObject(Item item) throws XPathException {
        if (item instanceof AtomicValue) {
            AtomicValue p = ((AtomicValue)item);
            int t = p.getItemType().getPrimitiveType();
            switch (t) {
                case XS_ANY_URI:
                    return p.getStringValue();
                case XS_BASE64_BINARY:
                    return ((Base64BinaryValue)p).getBinaryValue();
                case XS_BOOLEAN:
                    return Boolean.valueOf(((BooleanValue)p).getBooleanValue());
                case XS_DATE:
                    return new SaxonXMLGregorianCalendar((CalendarValue)p);
                case XS_DATE_TIME:
                    return new SaxonXMLGregorianCalendar((CalendarValue)p);
                case XS_DECIMAL:
                    return ((DecimalValue)p).getDecimalValue();
                case XS_DOUBLE:
                    return new Double(((DoubleValue)p).getDoubleValue());
                case XS_DURATION:
                    return new SaxonDuration((DurationValue)p);
                case XS_FLOAT:
                    return new Float(((FloatValue)p).getFloatValue());
                case XS_G_DAY:
                case XS_G_MONTH:
                case XS_G_MONTH_DAY:
                case XS_G_YEAR:
                case XS_G_YEAR_MONTH:
                    return new SaxonXMLGregorianCalendar((CalendarValue)p);
                case XS_HEX_BINARY:
                    return ((HexBinaryValue)p).getBinaryValue();
                case XS_INTEGER:
                    if (p instanceof BigIntegerValue) {
                        return ((BigIntegerValue)p).asBigInteger();
                    } else {
                        int sub = ((AtomicType)p.getItemType()).getFingerprint();
                        switch (sub) {
                            case XS_INTEGER:
                            case XS_NEGATIVE_INTEGER:
                            case XS_NON_NEGATIVE_INTEGER:
                            case XS_NON_POSITIVE_INTEGER:
                            case XS_POSITIVE_INTEGER:
                            case XS_UNSIGNED_LONG:
                                return BigInteger.valueOf(((Int64Value)p).longValue());
                            case XS_BYTE:
                                return Byte.valueOf((byte)((Int64Value)p).longValue());
                            case XS_INT:
                            case XS_UNSIGNED_SHORT:
                                return Integer.valueOf((int)((Int64Value)p).longValue());
                            case XS_LONG:
                            case XS_UNSIGNED_INT:
                                return Long.valueOf(((Int64Value)p).longValue());
                            case XS_SHORT:
                            case XS_UNSIGNED_BYTE:
                                return Short.valueOf((short)((Int64Value)p).longValue());
                            default:
                                throw new XPathException("Unrecognized integer subtype " + sub);
                        }
                    }
                case XS_QNAME:
                    return ((QualifiedNameValue)p).toJaxpQName();
                case XS_STRING:
                case XS_UNTYPED_ATOMIC:
                    return p.getStringValue();
                case XS_TIME:
                    return new SaxonXMLGregorianCalendar((CalendarValue)p);
                case XS_DAY_TIME_DURATION:
                    return new SaxonDuration((DurationValue)p);
                case XS_YEAR_MONTH_DURATION:
                    return new SaxonDuration((DurationValue)p);
                default:
                    throw new XPathException("unsupported type");
            }
        } else if (item instanceof NodeInfo) {
            return NodeOverNodeInfo.wrap((NodeInfo)item);
            //try {
				//return QueryResult.serialize((NodeInfo)item);
			//} catch (XPathException ex) {
			//	throw new XQException(ex.getMessage());
			//}
        } else if (item instanceof ObjectValue) {
        	Object value = ((ObjectValue) item).getObject();
        	if (value instanceof XQItem) {
        		//
        		//return ((XQItem) value).getObject();
        		return value;
        	}
        }
        return item;
    }

	public static Item objectToItem(Object value, Configuration config) throws XPathException {
        if (value == null) {
            return null;
        }
        // convert to switch..
            if (value instanceof Boolean) {
                return BooleanValue.get(((Boolean)value).booleanValue());
            } else if (value instanceof byte[]) {
                return new HexBinaryValue((byte[])value);
            } else if (value instanceof Byte) {
                return new Int64Value(((Byte)value).byteValue()); //BuiltInAtomicType.BYTE, false);
            } else if (value instanceof Float) {
                return new FloatValue(((Float)value).floatValue());
            } else if (value instanceof Double) {
                return new DoubleValue(((Double)value).doubleValue());
            } else if (value instanceof Integer) {
                return new Int64Value(((Integer)value).intValue()); //BuiltInAtomicType.INT, false);
            } else if (value instanceof Long) {
                return new Int64Value(((Long)value).longValue()); //BuiltInAtomicType.LONG, false);
            } else if (value instanceof Short) {
                return new Int64Value(((Short)value).shortValue()); //BuiltInAtomicType.SHORT, false);
            } else if (value instanceof String) {
                return new StringValue((String)value);
            } else if (value instanceof BigDecimal) {
                return new DecimalValue((BigDecimal)value);
            } else if (value instanceof BigInteger) {
                return new BigIntegerValue((BigInteger)value);
            } else if (value instanceof SaxonDuration) {
                return ((SaxonDuration)value).getDurationValue();
            } else if (value instanceof Duration) {
                // this is simpler and safer (but perhaps slower) than extracting all the components
                //return DurationValue.makeDuration(value.toString()).asAtomic();
            	Duration dv = (Duration) value;
            	return new DurationValue(dv.getSign() >= 0, dv.getYears(), dv.getMonths(), dv.getDays(), 
            			dv.getHours(), dv.getMinutes(), dv.getSeconds(), 0); // take correct millis..
            } else if (value instanceof SaxonXMLGregorianCalendar) {
                return ((SaxonXMLGregorianCalendar)value).toCalendarValue();
            } else if (value instanceof XMLGregorianCalendar) {
                XMLGregorianCalendar g = (XMLGregorianCalendar)value;
                QName gtype = g.getXMLSchemaType();
                if (gtype.equals(DatatypeConstants.DATETIME)) {
                    return DateTimeValue.makeDateTimeValue(value.toString(), config.getConversionRules()).asAtomic();
                } else if (gtype.equals(DatatypeConstants.DATE)) {
                    return DateValue.makeDateValue(value.toString(), config.getConversionRules()).asAtomic();
                } else if (gtype.equals(DatatypeConstants.TIME)) {
                    return TimeValue.makeTimeValue(value.toString()).asAtomic();
                } else if (gtype.equals(DatatypeConstants.GYEAR)) {
                    return GYearValue.makeGYearValue(value.toString(), config.getConversionRules()).asAtomic();
                } else if (gtype.equals(DatatypeConstants.GYEARMONTH)) {
                    return GYearMonthValue.makeGYearMonthValue(value.toString(), config.getConversionRules()).asAtomic();
                } else if (gtype.equals(DatatypeConstants.GMONTH)) {
                	// a workaround for X3C schema bug
                	String val = value.toString();
                	if (val.endsWith("--")) {
                		val = val.substring(0, val.length() - 2);
                	}
                    return GMonthValue.makeGMonthValue(val, config.getConversionRules()).asAtomic();
                } else if (gtype.equals(DatatypeConstants.GMONTHDAY)) {
                    return GMonthDayValue.makeGMonthDayValue(value.toString(), config.getConversionRules()).asAtomic();
                } else if (gtype.equals(DatatypeConstants.GDAY)) {
                    return GDayValue.makeGDayValue(value.toString(), config.getConversionRules()).asAtomic();
                } else {
                    throw new AssertionError("Unknown Gregorian date type");
                }
            } else if (value instanceof QName) {
                QName q = (QName)value;
                return new QNameValue(q.getPrefix(), q.getNamespaceURI(), q.getLocalPart()); //BuiltInAtomicType.QNAME, null);
            } else {
            	return new ObjectValue(value);
            }
    }
    
    /**
     * Convert a Java object to a Saxon Item
     * @param value the Java object. If null is supplied, null is returned.
     * @return the corresponding Item
     * @throws XQException
     */
	public static Item convertToItem(Object value, Configuration config, BuiltInAtomicType type) throws XPathException {
        if (value == null) {
            return null;
        }
        // convert to switch..
        //try {
            if (value instanceof Boolean) {
                return BooleanValue.get(((Boolean)value).booleanValue());
            } else if (value instanceof byte[]) {
                return new HexBinaryValue((byte[])value);
            } else if (value instanceof Byte) {
                return new Int64Value(((Byte)value).byteValue(), type, false); //BuiltInAtomicType.BYTE, false);
            } else if (value instanceof Float) {
                return new FloatValue(((Float)value).floatValue(), type);
            } else if (value instanceof Double) {
                return new DoubleValue(((Double)value).doubleValue(), type);
            } else if (value instanceof Integer) {
                return new Int64Value(((Integer)value).intValue(), type, false); //BuiltInAtomicType.INT, false);
            } else if (value instanceof Long) {
                return new Int64Value(((Long)value).longValue(), type, false); //BuiltInAtomicType.LONG, false);
            } else if (value instanceof Short) {
                return new Int64Value(((Short)value).shortValue(), type, false); //BuiltInAtomicType.SHORT, false);
            } else if (value instanceof String) {
                return new StringValue((String)value, type);
            } else if (value instanceof BigDecimal) {
                return new DecimalValue((BigDecimal)value);
            } else if (value instanceof BigInteger) {
                return new BigIntegerValue((BigInteger)value, type);
            } else if (value instanceof SaxonDuration) {
                return ((SaxonDuration)value).getDurationValue();
            } else if (value instanceof Duration) {
                // this is simpler and safer (but perhaps slower) than extracting all the components
                //return DurationValue.makeDuration(value.toString()).asAtomic();
            	Duration dv = (Duration) value;
            	return new DurationValue(dv.getSign() >= 0, dv.getYears(), dv.getMonths(), dv.getDays(), 
            			dv.getHours(), dv.getMinutes(), dv.getSeconds(), 0, type); // take correct millis..
            } else if (value instanceof SaxonXMLGregorianCalendar) {
                return ((SaxonXMLGregorianCalendar)value).toCalendarValue();
            } else if (value instanceof XMLGregorianCalendar) {
                XMLGregorianCalendar g = (XMLGregorianCalendar)value;
                QName gtype = g.getXMLSchemaType();
                if (gtype.equals(DatatypeConstants.DATETIME)) {
                    return DateTimeValue.makeDateTimeValue(value.toString(), config.getConversionRules()).asAtomic();
                } else if (gtype.equals(DatatypeConstants.DATE)) {
                    return DateValue.makeDateValue(value.toString(), config.getConversionRules()).asAtomic();
                } else if (gtype.equals(DatatypeConstants.TIME)) {
                    return TimeValue.makeTimeValue(value.toString()).asAtomic();
                } else if (gtype.equals(DatatypeConstants.GYEAR)) {
                    return GYearValue.makeGYearValue(value.toString(), config.getConversionRules()).asAtomic();
                } else if (gtype.equals(DatatypeConstants.GYEARMONTH)) {
                    return GYearMonthValue.makeGYearMonthValue(value.toString(), config.getConversionRules()).asAtomic();
                } else if (gtype.equals(DatatypeConstants.GMONTH)) {
                	// a workaround for X3C schema bug
                	String val = value.toString();
                	if (val.endsWith("--")) {
                		val = val.substring(0, val.length() - 2);
                	}
                    return GMonthValue.makeGMonthValue(val, config.getConversionRules()).asAtomic();
                } else if (gtype.equals(DatatypeConstants.GMONTHDAY)) {
                    return GMonthDayValue.makeGMonthDayValue(value.toString(), config.getConversionRules()).asAtomic();
                } else if (gtype.equals(DatatypeConstants.GDAY)) {
                    return GDayValue.makeGDayValue(value.toString(), config.getConversionRules()).asAtomic();
                } else {
                    throw new AssertionError("Unknown Gregorian date type");
                }
            } else if (value instanceof QName) {
                QName q = (QName)value;
                return new QNameValue(q.getPrefix(), q.getNamespaceURI(), q.getLocalPart(), type, null);
                        //BuiltInAtomicType.QNAME, null);
            } else {
                throw new XPathException("Java object cannot be converted to an XQuery value");
            }
        //} catch (XPathException e) {
        //    XQException xqe = new XQException(e.getMessage());
        //    xqe.initCause(e);
        //    throw xqe;
        //}
    }

    public static Item convertToItem(Object value, Configuration config, int kind) throws XPathException {
    	if (value instanceof String) {
    		try {
				value = XMLUtils.textToDocument((String) value);
			} catch (IOException ex) {
				throw new XPathException(ex);
			}
    	}
    	
    	if (value instanceof Node) {
	    	JPConverter jp;
			switch (kind) {
				case XQItemType.XQITEMKIND_DOCUMENT:
			        jp = DOMObjectModel.getInstance().getJPConverter(Document.class, config);
			        break;
				case XQItemType.XQITEMKIND_ELEMENT:
			        jp = DOMObjectModel.getInstance().getJPConverter(Element.class, config);
			        break;
				case XQItemType.XQITEMKIND_ATTRIBUTE:
			        jp = DOMObjectModel.getInstance().getJPConverter(Attr.class, config);
			        break;
				case XQItemType.XQITEMKIND_COMMENT:
			        jp = DOMObjectModel.getInstance().getJPConverter(Comment.class, config);
			        break;
				case XQItemType.XQITEMKIND_PI:
			        jp = DOMObjectModel.getInstance().getJPConverter(ProcessingInstruction.class, config);
			        break;
				case XQItemType.XQITEMKIND_TEXT:
			        jp = DOMObjectModel.getInstance().getJPConverter(Text.class, config);
			        break;
				//case XQItemType.XQITEMKIND_NODE:
				default:
			        jp = DOMObjectModel.getInstance().getJPConverter(Node.class, config);
			}
	        //return Value.asItem(DOMObjectModel.getInstance().convertObjectToXPathValue(value, config));
	        return SequenceTool.asItem(jp.convert(value, new EarlyEvaluationContext(config, null)));
	    } else if (value instanceof Source) {
	        // Saxon extension to the XQJ specification
	        PipelineConfiguration pipe = config.makePipelineConfiguration();
	        Builder b = new TinyBuilder(pipe);
	        Sender.send((Source)value, b, null);
	        NodeInfo node = b.getCurrentRoot();
	        b.reset();
	        return node;
	    } else if (value instanceof XMLStreamReader) {
	        // Saxon extension to the XQJ specification
	        StaxToEventBridge bridge = new StaxToEventBridge();
	        bridge.setXMLStreamReader((XMLStreamReader)value);
	        PipelineConfiguration pipe = config.makePipelineConfiguration();
	        bridge.setPipelineConfiguration(pipe);
	        Builder b = new TinyBuilder(pipe);
	        Sender.send(new PullEventSource(bridge), b, null);
	        NodeInfo node = b.getCurrentRoot();
	        b.reset();
	        return node;
	    }
    	return null;
    }
    
	public static BuiltInAtomicType getAtomicType(XQItemType type) throws XQException {
		
		int kind = type.getItemKind();
		if (BagriXQUtils.isBaseTypeSupported(kind)) {
			switch (type.getBaseType()) {
				case XQBASETYPE_ANYATOMICTYPE: return BuiltInAtomicType.ANY_ATOMIC;
				case XQBASETYPE_ANYSIMPLETYPE: return null; 
				case XQBASETYPE_ANYTYPE: return null; 
				case XQBASETYPE_ANYURI: return BuiltInAtomicType.ANY_URI; 
				case XQBASETYPE_BASE64BINARY: return BuiltInAtomicType.BASE64_BINARY; 
				case XQBASETYPE_BOOLEAN: return BuiltInAtomicType.BOOLEAN;
				case XQBASETYPE_BYTE: return BuiltInAtomicType.BYTE;
				case XQBASETYPE_DATE: return BuiltInAtomicType.DATE;
				case XQBASETYPE_DATETIME: return BuiltInAtomicType.DATE_TIME;
				case XQBASETYPE_DAYTIMEDURATION: return BuiltInAtomicType.DAY_TIME_DURATION;
				case XQBASETYPE_DECIMAL: return BuiltInAtomicType.DECIMAL;
				case XQBASETYPE_DOUBLE: return BuiltInAtomicType.DOUBLE;
				case XQBASETYPE_DURATION: return BuiltInAtomicType.DURATION;
				case XQBASETYPE_ENTITIES: return null; 
				case XQBASETYPE_ENTITY: return BuiltInAtomicType.ENTITY;
				case XQBASETYPE_FLOAT: return BuiltInAtomicType.FLOAT;
				case XQBASETYPE_GDAY: return BuiltInAtomicType.G_DAY;
				case XQBASETYPE_GMONTH: return BuiltInAtomicType.G_MONTH;
				case XQBASETYPE_GMONTHDAY: return BuiltInAtomicType.G_MONTH_DAY;
				case XQBASETYPE_GYEAR: return BuiltInAtomicType.G_YEAR;
				case XQBASETYPE_GYEARMONTH: return BuiltInAtomicType.G_YEAR_MONTH;
				case XQBASETYPE_HEXBINARY: return BuiltInAtomicType.HEX_BINARY;
				case XQBASETYPE_ID: return BuiltInAtomicType.ID;
				case XQBASETYPE_IDREF: return BuiltInAtomicType.IDREF;
				case XQBASETYPE_IDREFS: return null;
				case XQBASETYPE_INT: return BuiltInAtomicType.INT;
				case XQBASETYPE_INTEGER: return BuiltInAtomicType.INTEGER;
				case XQBASETYPE_LANGUAGE: return BuiltInAtomicType.LANGUAGE;
				case XQBASETYPE_LONG: return BuiltInAtomicType.LONG;
				case XQBASETYPE_NAME: return BuiltInAtomicType.NAME;
				case XQBASETYPE_NCNAME: return BuiltInAtomicType.NCNAME;
				case XQBASETYPE_NEGATIVE_INTEGER: return BuiltInAtomicType.NEGATIVE_INTEGER;
				case XQBASETYPE_NMTOKEN: return BuiltInAtomicType.NMTOKEN;
				case XQBASETYPE_NMTOKENS: return null;
				case XQBASETYPE_NONNEGATIVE_INTEGER: return BuiltInAtomicType.NON_NEGATIVE_INTEGER;
				case XQBASETYPE_NONPOSITIVE_INTEGER: return BuiltInAtomicType.NON_POSITIVE_INTEGER;
				case XQBASETYPE_NORMALIZED_STRING: return BuiltInAtomicType.NORMALIZED_STRING;
				case XQBASETYPE_NOTATION: return BuiltInAtomicType.NOTATION;
				case XQBASETYPE_POSITIVE_INTEGER: return BuiltInAtomicType.POSITIVE_INTEGER;
				case XQBASETYPE_QNAME: return BuiltInAtomicType.QNAME;
				case XQBASETYPE_SHORT: return BuiltInAtomicType.SHORT;
				case XQBASETYPE_STRING: return BuiltInAtomicType.STRING;
				case XQBASETYPE_TIME: return BuiltInAtomicType.TIME;
				case XQBASETYPE_TOKEN: return BuiltInAtomicType.TOKEN;
				case XQBASETYPE_UNSIGNED_BYTE: return BuiltInAtomicType.UNSIGNED_BYTE;
				case XQBASETYPE_UNSIGNED_INT: return BuiltInAtomicType.UNSIGNED_INT;
				case XQBASETYPE_UNSIGNED_LONG: return BuiltInAtomicType.UNSIGNED_LONG;
				case XQBASETYPE_UNSIGNED_SHORT: return BuiltInAtomicType.UNSIGNED_SHORT;
				case XQBASETYPE_UNTYPED: return null;
				case XQBASETYPE_UNTYPEDATOMIC: return BuiltInAtomicType.UNTYPED_ATOMIC;
				case XQBASETYPE_YEARMONTHDURATION: return BuiltInAtomicType.YEAR_MONTH_DURATION;
			}
		}
		return null;
	}
	
	public static SequenceType type2Sequence(XDMType type) {
		ItemType it = type2Item(type.getType());
		int cardinality;
		switch (type.getCardinality()) {
			case one_or_more: cardinality = StaticProperty.ALLOWS_ONE_OR_MORE; break; 
			case zero_or_one: cardinality = StaticProperty.ALLOWS_ZERO_OR_ONE; break; 
			case zero_or_more: cardinality = StaticProperty.ALLOWS_ZERO_OR_MORE; break;
			default: cardinality = StaticProperty.ALLOWS_ONE;  
		}
		return SequenceType.makeSequenceType(it, cardinality);
	}
	
	public static ItemType type2Item(String type) {

		// provide long switch here..
		switch (type) {
			case "boolean": return BuiltInAtomicType.BOOLEAN; 
			case "byte": return BuiltInAtomicType.BYTE; 
			case "double": return BuiltInAtomicType.DOUBLE; 
			case "float": return BuiltInAtomicType.FLOAT; 
			case "int": return BuiltInAtomicType.INT; 
			case "integer": return BuiltInAtomicType.INTEGER; 
			case "long": return BuiltInAtomicType.LONG; 
			case "short": return BuiltInAtomicType.SHORT; 
			case "string": return BuiltInAtomicType.STRING; 
		}
		return BuiltInAtomicType.ANY_ATOMIC; 
	}


	/*    
    public static int getBaseType(AtomicValue value) {
        int type = value.getItemType().getPrimitiveType();
        switch (type) {
        	case StandardNames.XS_ANY_URI: return XQItemType.XQBASETYPE_ANYURI;
            case StandardNames.XS_BASE64_BINARY: return XQItemType.XQBASETYPE_BASE64BINARY;
            case StandardNames.XS_BOOLEAN: return XQItemType.XQBASETYPE_BOOLEAN;
            case StandardNames.XS_DATE: return XQItemType.XQBASETYPE_DATE;
            case StandardNames.XS_TIME: return XQItemType.XQBASETYPE_TIME;
            case StandardNames.XS_DATE_TIME: return XQItemType.XQBASETYPE_DATETIME;
            case StandardNames.XS_DECIMAL: return XQItemType.XQBASETYPE_DECIMAL;
            case StandardNames.XS_DOUBLE: return XQItemType.XQBASETYPE_DOUBLE;
            case StandardNames.XS_DURATION: return XQItemType.XQBASETYPE_DURATION;
            case StandardNames.XS_FLOAT: return XQItemType.XQBASETYPE_FLOAT;
            case StandardNames.XS_G_DAY: return XQItemType.XQBASETYPE_GDAY;
            case StandardNames.XS_G_MONTH: return XQItemType.XQBASETYPE_GMONTH;
            case StandardNames.XS_G_MONTH_DAY: return XQItemType.XQBASETYPE_GMONTHDAY;
            case StandardNames.XS_G_YEAR: return XQItemType.XQBASETYPE_GYEAR;
            case StandardNames.XS_G_YEAR_MONTH: return XQItemType.XQBASETYPE_GYEARMONTH;
            case StandardNames.XS_HEX_BINARY: return XQItemType.XQBASETYPE_HEXBINARY;
            case StandardNames.XS_INTEGER: {
                int sub = value.getItemType().getFingerprint();
                switch (sub) {
                	//case StandardNames.XS_INTEGER: return XQItemType.XQBASETYPE_INTEGER;
                    case StandardNames.XS_NEGATIVE_INTEGER: return XQItemType.XQBASETYPE_NEGATIVE_INTEGER;
                    case StandardNames.XS_NON_NEGATIVE_INTEGER: return XQItemType.XQBASETYPE_NONNEGATIVE_INTEGER;
                    case StandardNames.XS_NON_POSITIVE_INTEGER: return XQItemType.XQBASETYPE_NONPOSITIVE_INTEGER;
                    case StandardNames.XS_POSITIVE_INTEGER: return XQItemType.XQBASETYPE_POSITIVE_INTEGER;
                    case StandardNames.XS_UNSIGNED_LONG: return XQItemType.XQBASETYPE_UNSIGNED_LONG;
                    case StandardNames.XS_BYTE: return XQItemType.XQBASETYPE_BYTE;
                    case StandardNames.XS_INT: return XQItemType.XQBASETYPE_INT;
                    case StandardNames.XS_UNSIGNED_SHORT: return XQItemType.XQBASETYPE_UNSIGNED_SHORT;
                    case StandardNames.XS_LONG: return XQItemType.XQBASETYPE_LONG;
                    case StandardNames.XS_UNSIGNED_INT: return XQItemType.XQBASETYPE_UNSIGNED_INT;
                    case StandardNames.XS_SHORT: return XQItemType.XQBASETYPE_SHORT;
                    case StandardNames.XS_UNSIGNED_BYTE: return XQItemType.XQBASETYPE_UNSIGNED_BYTE;
                    default: return XQItemType.XQBASETYPE_INTEGER;
                }
            }
            case StandardNames.XS_STRING: {
                int sub = value.getItemType().getFingerprint();
                switch (sub) {
	                case StandardNames.XS_NAME: return XQItemType.XQBASETYPE_NAME;
	                case StandardNames.XS_NCNAME: return XQItemType.XQBASETYPE_NCNAME;
	                case StandardNames.XS_NMTOKEN: return XQItemType.XQBASETYPE_NMTOKEN;
                    default: return XQItemType.XQBASETYPE_STRING;
                }
            }
            case StandardNames.XS_QNAME: return XQItemType.XQBASETYPE_QNAME;
            case StandardNames.XS_UNTYPED_ATOMIC: return XQItemType.XQBASETYPE_UNTYPEDATOMIC;
            case StandardNames.XS_DAY_TIME_DURATION: return XQItemType.XQBASETYPE_DAYTIMEDURATION;
            case StandardNames.XS_YEAR_MONTH_DURATION: return XQItemType.XQBASETYPE_YEARMONTHDURATION;
            default:
                    //throw new XPathException("unsupported type");
        }
    	return XQItemType.XQBASETYPE_UNTYPEDATOMIC;
    }
*/    
}