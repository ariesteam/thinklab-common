package org.integratedmodelling.thinklab.common.owl;

import java.util.Collection;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.knowledge.IKnowledge;
import org.integratedmodelling.thinklab.api.knowledge.IOntology;
import org.integratedmodelling.thinklab.api.knowledge.IProperty;

public class Property implements IProperty {

	String _id;
	String _cs;
	
	public Property(String cs, String id) {
		this._cs = cs;
		this._id = id;
	}
	
	@Override
	public String getConceptSpace() {
		return _cs;
	}

	@Override
	public String getLocalName() {
		return _id;
	}


	@Override
	public boolean is(IKnowledge concept) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean is(String semanticType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addAnnotation(String property, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getAnnotation(String property) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLabel(String languageCode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription(String languageCode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addDescription(String desc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDescription(String desc, String language) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addLabel(String desc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addLabel(String desc, String language) {
		// TODO Auto-generated method stub

	}

	@Override
	public IOntology getOntology() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isClassification() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLiteralProperty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isObjectProperty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAnnotation() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IProperty getInverseProperty() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IConcept> getRange() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IConcept> getDomain() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAbstract() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IProperty getParent() throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IProperty> getParents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IProperty> getAllParents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IProperty> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IProperty> getAllChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFunctional() {
		// TODO Auto-generated method stub
		return false;
	}

}
