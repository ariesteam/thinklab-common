package org.integratedmodelling.thinklab.common.owl;

import java.util.Collection;

import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.metadata.IMetadata;

/**
 * Implements metadata for ontologies, concepts and properties by providing an interface to
 * annotation properties.
 * 
 * @author Ferd
 *
 */
public class OWLMetadata implements IMetadata {

	@Override
	public Object get(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void merge(IMetadata md) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
		return null;
	}

}
