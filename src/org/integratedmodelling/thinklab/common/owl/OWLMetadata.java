package org.integratedmodelling.thinklab.common.owl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.metadata.IMetadata;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * Implements metadata for ontologies, concepts and properties by providing an interface to
 * annotation properties of an OWL entity and exposing them with their fully qualified
 * property IDs as keys.
 * 
 * @author Ferd
 *
 */
public class OWLMetadata implements IMetadata {

	OWLEntity _owl;
	HashMap<String, Object> __data;
	
	public OWLMetadata(OWLEntity owl) {
		_owl = owl;
	}

	private Map<String, Object> getData() {
		if (__data == null) {
			__data = new HashMap<String, Object>();
			
			/*
			 * TODO visit annotations and store values here
			 */
		}
		return __data;
	}
	
	@Override
	public Object get(String string) {
		return getData().get(string);
	}

	@Override
	public Collection<String> getKeys() {
		return getData().keySet();
	}

	@Override
	public void merge(IMetadata md) {
	}

	@Override
	public String getString(String field) {
		Object o = get(field);		
		return o != null ? o.toString() : null;
	}

	@Override
	public Integer getInt(String field) {
		Object o = get(field);		
		return o != null && o instanceof Number ? ((Number)o).intValue() : null;
	}

	@Override
	public Long getLong(String field) {
		Object o = get(field);		
		return o != null && o instanceof Number ? ((Number)o).longValue() : null;
	}

	@Override
	public Double getDouble(String field) {
		Object o = get(field);		
		return o != null && o instanceof Number ? ((Number)o).doubleValue() : null;
	}

	@Override
	public Float getFloat(String field) {
		Object o = get(field);		
		return o != null && o instanceof Number ? ((Number)o).floatValue() : null;
	}

	@Override
	public Boolean getBoolean(String field) {
		Object o = get(field);		
		return o != null && o instanceof Boolean ? (Boolean)o : null;
	}

	@Override
	public IConcept getConcept(String field) {
		Object o = get(field);		
		return o != null && o instanceof IConcept ? (IConcept)o : null;
	}

	@Override
	public String getString(String field, String def) {
		Object o = get(field);		
		return o != null ? o.toString() : def;
	}

	@Override
	public int getInt(String field, int def) {
		Object o = get(field);		
		return o != null && o instanceof Number ? ((Number)o).intValue() : def;
	}

	@Override
	public long getLong(String field, long def) {
		Object o = get(field);		
		return o != null && o instanceof Number ? ((Number)o).longValue() : def;
	}

	@Override
	public double getDouble(String field, double def) {
		Object o = get(field);		
		return o != null && o instanceof Number ? ((Number)o).doubleValue() : def;
	}

	@Override
	public float getFloat(String field, float def) {
		Object o = get(field);		
		return o != null && o instanceof Number ? ((Number)o).floatValue() : def;
	}

	@Override
	public boolean getBoolean(String field, boolean def) {
		Object o = get(field);		
		return o != null && o instanceof Boolean ? (Boolean)o : def;
	}

	@Override
	public IConcept getConcept(String field, IConcept def) {
		Object o = get(field);		
		return o != null && o instanceof IConcept ? (IConcept)o : def;
	}
	
	@Override
	public Collection<Object> getValues() {
		return getData().values();
	}

}
