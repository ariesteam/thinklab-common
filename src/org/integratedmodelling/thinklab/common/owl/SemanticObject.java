package org.integratedmodelling.thinklab.common.owl;

import java.util.List;

import org.integratedmodelling.collections.Pair;
import org.integratedmodelling.exceptions.ThinklabCircularDependencyException;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.knowledge.IProperty;
import org.integratedmodelling.thinklab.api.knowledge.ISemanticObject;
import org.integratedmodelling.thinklab.api.lang.IList;
import org.integratedmodelling.thinklab.api.modelling.INamespace;

/**
 * This semantic object is a dummy - will just have is() working when the ontology functions are
 * integrated.
 *
 * @author Ferd
 *
 */
public class SemanticObject implements ISemanticObject<Object> {

	IList _list;
	
	public SemanticObject(IList list) {
		_list = list;
	}
	
	@Override
	public IList getSemantics() {
		return _list;
	}

	@Override
	public Object demote() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IConcept getDirectType() {
//		SemanticType st = new SemanticType(_list.first().toString());
//		return OWL.get().getConcept(st.toString());
		return null;
	}

	@Override
	public boolean is(Object other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getRelationshipsCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getRelationshipsCount(IProperty _subject) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ISemanticObject<?> get(IProperty property) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Pair<IProperty, ISemanticObject<?>>> getRelationships() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ISemanticObject<?>> getRelationships(IProperty property) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLiteral() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isConcept() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isObject() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCyclic() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public List<ISemanticObject<?>> getSortedRelationships(IProperty property)
			throws ThinklabCircularDependencyException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public INamespace getNamespace() {
		// TODO Auto-generated method stub
		return null;
	}

}
