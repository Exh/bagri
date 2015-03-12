package com.bagri.common.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryBuilder {
	
	private Map<Integer, ExpressionContainer> containers = new HashMap<>();
	
	public QueryBuilder() {
		//
	}
	
	public QueryBuilder(Collection<ExpressionContainer> containers) {
		setContainers(containers);
	}
	
	public void addContainer(ExpressionContainer container) {
		int docType = container.getExpression().getRoot().getDocType();
		addContainer(docType, container);
	}
	
	public void addContainer(int docType, ExpressionContainer container) {
		ExpressionContainer oldValue = containers.put(docType, container);
		//
	}

	public ExpressionContainer getContainer(int docType) {
		return containers.get(docType);
	}
	
	public Collection<ExpressionContainer> getContainers() {
		return containers.values();
	}
	
	public void setContainers(Collection<ExpressionContainer> containers) {
		this.containers.clear();
		if (containers != null) {
			for (ExpressionContainer ec: containers) {
				addContainer(ec);
			}
		}
	}
	
	public Collection<String> getEmptyParams() {
		List<String> result = new ArrayList<>();
		for (ExpressionContainer exCont: containers.values()) {
			for (Map.Entry<String, Object> param: exCont.getParams().entrySet()) {
				if (param.getValue() == null) {
					result.add(param.getKey());
				}
			}
		}
		return result;
	}
	
	public boolean hasEmptyParams() {
		for (ExpressionContainer exCont: containers.values()) {
			for (Object value: exCont.getParams().values()) {
				if (value == null) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void setEmptyParam(String pName, Object value) {
		for (ExpressionContainer exCont: containers.values()) {
			if (exCont.getParams().containsKey(pName) && exCont.getParam(pName) == null) {
				exCont.getParams().put(pName, value);
			}
		}		
	}
	
	public void resetParams(Map<String, Object> params) {
		for (ExpressionContainer exCont: containers.values()) {
			exCont.resetParams(params);
		}
	}

	@Override
	public String toString() {
		return "QueryBuilder [" + containers + "]";
	}
	
}