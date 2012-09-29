package org.integratedmodelling.thinklab.common.owl;

import java.io.File;
import java.util.Arrays;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.exceptions.ThinklabUnsupportedOperationException;
import org.integratedmodelling.thinklab.api.factories.IKnowledgeManager;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.knowledge.IOntology;
import org.integratedmodelling.thinklab.api.knowledge.IProperty;
import org.integratedmodelling.thinklab.api.knowledge.ISemanticObject;
import org.integratedmodelling.thinklab.api.knowledge.kbox.IKbox;
import org.integratedmodelling.thinklab.api.lang.IList;
import org.integratedmodelling.thinklab.api.lang.IModelParser;

/**
 * Knowledge manager for client library, which will not allow any operation but will create
 * blindly any concept and ontology that it's asked to produce. Used to parse models where all concepts
 * defined are expected to be created. Don't use improperly.
 * 
 * TODO implement all the concept, property, ontology and reasoning functions using OWLAPI 2.0 and 
 * use as delegate for the KM in thinklab. We need reasoning in the client if we want any meaningful
 * way to organize concepts.
 * 
 * @author Ferd
 *
 */
public class KnowledgeManager implements IKnowledgeManager {

	private static KnowledgeManager _this;
	
	private OWL _manager = new OWL();
	
	public static IKnowledgeManager get() {
		if (_this == null) {
			_this = new KnowledgeManager();
		}
		return _this;
	}

	public KnowledgeManager()  {
		
	}	
	
	@Override
	public IConcept getConcept(String concept) {
		return _manager.getConcept(concept);
	}

	@Override
	public IConcept getLeastGeneralCommonConcept(IConcept... cc) {
		return _manager.getLeastGeneralCommonConcept(Arrays.asList(cc));
	}
	
	@Override
	public IProperty getProperty(String prop) {
		return _manager.getProperty(prop);
	}

	@Override
	public void dropKbox(String uri) throws ThinklabException {
		throw new ThinklabUnsupportedOperationException("drop kbox");
	}

	@Override
	public ISemanticObject<?> parse(String literal, IConcept c)
			throws ThinklabException {
		throw new ThinklabUnsupportedOperationException("parse literal");
	}

	@Override
	public ISemanticObject<?> annotate(Object object) throws ThinklabException {
		throw new ThinklabUnsupportedOperationException("annotate object");
	}

	@Override
	public IKbox createKbox(String uri) throws ThinklabException {
		throw new ThinklabUnsupportedOperationException("create kbox");
	}

	@Override
	public IKbox requireKbox(String uri) throws ThinklabException {
		throw new ThinklabUnsupportedOperationException("require kbox");
	}

	@Override
	public Object instantiate(IList a) throws ThinklabException {
		throw new ThinklabUnsupportedOperationException("instantiate semantics");
	}

	@Override
	public void registerAnnotatedClass(Class<?> cls, IConcept concept) {
	}

	@Override
	public ISemanticObject<?> entify(IList semantics) throws ThinklabException {
		return new SemanticObject(semantics);
	}

	@Override
	public IConcept getXSDMapping(String string) {
		return _manager.getDatatypeMapping(string);
	}

	public void loadKnowledge(File directory) throws ThinklabException {
		_manager.load(directory);
	}

	public IOntology requireOntology(String id, String ontologyNamespacePrefix) {
		return _manager.requireOntology(id, ontologyNamespacePrefix);
	}

	public void releaseOntology(IOntology ontology) {
		_manager.releaseOntology(ontology);
	}
	
	public IModelParser getOWLParser() {
		return _manager;
	}

	@Override
	public IConcept getRootConcept() {
		return _manager.getRootConcept();
	}
}
